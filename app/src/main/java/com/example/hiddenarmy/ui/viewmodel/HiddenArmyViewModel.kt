package com.example.hiddenarmy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiddenarmy.ai.CpuAction
import com.example.hiddenarmy.ai.HiddenArmyCpuStrategy
import com.example.hiddenarmy.data.GamePreferences
import com.example.hiddenarmy.logic.HiddenArmyGameEngine
import com.example.hiddenarmy.model.ActiveBattle
import com.example.hiddenarmy.model.BaseOwner
import com.example.hiddenarmy.model.BattleResult
import com.example.hiddenarmy.model.GameState
import com.example.hiddenarmy.model.GameStatus
import com.example.hiddenarmy.model.Position
import com.example.hiddenarmy.model.Side
import com.example.hiddenarmy.model.Unit
import com.example.hiddenarmy.util.SoundAndHaptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HiddenArmyViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = GamePreferences(application)
    val soundAndHaptics = SoundAndHaptics(application)

    private val _gameState = MutableStateFlow(HiddenArmyGameEngine.createInitialGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _bannerEvent = MutableSharedFlow<String>()
    val bannerEvent: SharedFlow<String> = _bannerEvent.asSharedFlow()

    private var cpuJob: Job? = null
    private var isProcessingAction = false

    init {
        // Start match fresh
        startNewGame()
    }

    fun startNewGame() {
        cpuJob?.cancel()
        isProcessingAction = false
        val initial = HiddenArmyGameEngine.createInitialGameState()
        _gameState.value = initial
    }

    fun onCellClicked(position: Position) {
        val state = _gameState.value

        // Rapid tap protection & turn validation
        if (isProcessingAction || state.status != GameStatus.PLAYER_TURN) {
            return
        }

        val clickedUnit = state.units.find { it.position == position }

        // Case 1: Tapped a player unit
        if (clickedUnit != null && clickedUnit.side == Side.PLAYER) {
            if (state.selectedUnitId == clickedUnit.id) {
                // Deselect if already selected
                _gameState.value = state.copy(
                    selectedUnitId = null,
                    legalMoves = emptyList(),
                    attackTargets = emptyList()
                )
            } else {
                // Select player unit and compute valid actions
                val legalMoves = HiddenArmyGameEngine.getLegalMoves(clickedUnit, state.units, state.bases)
                val attacks = HiddenArmyGameEngine.getAttackTargets(clickedUnit, state.units)
                _gameState.value = state.copy(
                    selectedUnitId = clickedUnit.id,
                    legalMoves = legalMoves,
                    attackTargets = attacks
                )
                soundAndHaptics.playSelection(
                    soundEnabled = preferences.soundEnabled.value,
                    hapticsEnabled = preferences.hapticsEnabled.value
                )
            }
            return
        }

        // Case 2: An existing player unit is selected
        val selectedId = state.selectedUnitId ?: return
        val selectedUnit = state.units.find { it.id == selectedId } ?: return

        // Check if clicked cell is an attack target (contains adjacent enemy unit)
        if (position in state.attackTargets) {
            val defender = state.units.find { it.position == position && it.side == Side.CPU }
            if (defender != null) {
                executeAttack(selectedUnit, defender)
                return
            }
        }

        // Check if clicked cell is a valid move
        if (position in state.legalMoves) {
            executePlayerMove(selectedUnit.id, position)
            return
        }

        // Tapped outside valid moves / attacks -> clear selection
        _gameState.value = state.copy(
            selectedUnitId = null,
            legalMoves = emptyList(),
            attackTargets = emptyList()
        )
    }

    private fun executePlayerMove(unitId: String, targetPosition: Position) {
        isProcessingAction = true
        soundAndHaptics.playMove(
            soundEnabled = preferences.soundEnabled.value,
            hapticsEnabled = preferences.hapticsEnabled.value
        )

        val beforeState = _gameState.value
        val afterMove = HiddenArmyGameEngine.executeMove(beforeState, unitId, targetPosition)

        // Check if a base was captured
        val baseAtTarget = afterMove.bases.find { it.position == targetPosition }
        if (baseAtTarget != null && baseAtTarget.owner == BaseOwner.PLAYER) {
            soundAndHaptics.playBaseCapture(
                soundEnabled = preferences.soundEnabled.value,
                hapticsEnabled = preferences.hapticsEnabled.value
            )
            emitBanner("Base captured!")
        }

        _gameState.value = afterMove

        // Pass turn to CPU
        advanceToCpuTurn()
    }

    private fun executeAttack(attacker: Unit, defender: Unit) {
        isProcessingAction = true
        soundAndHaptics.playAttack(
            soundEnabled = preferences.soundEnabled.value,
            hapticsEnabled = preferences.hapticsEnabled.value
        )

        val result = HiddenArmyGameEngine.resolveBattle(attacker, defender)

        if (preferences.battleAnimationEnabled.value) {
            _gameState.value = _gameState.value.copy(
                status = GameStatus.BATTLE,
                activeBattle = ActiveBattle(
                    attacker = attacker,
                    defender = defender,
                    result = result,
                    battlePosition = defender.position
                ),
                selectedUnitId = null,
                legalMoves = emptyList(),
                attackTargets = emptyList()
            )
            viewModelScope.launch {
                delay(650)
                finishBattle(attacker.id, defender.id, result, isPlayerAttacking = true)
            }
        } else {
            finishBattle(attacker.id, defender.id, result, isPlayerAttacking = true)
        }
    }

    fun skipBattleAnimation() {
        val battle = _gameState.value.activeBattle ?: return
        finishBattle(battle.attacker.id, battle.defender.id, battle.result, battle.attacker.side == Side.PLAYER)
    }

    private fun finishBattle(
        attackerId: String,
        defenderId: String,
        result: BattleResult,
        isPlayerAttacking: Boolean
    ) {
        soundAndHaptics.playReveal(
            soundEnabled = preferences.soundEnabled.value,
            hapticsEnabled = preferences.hapticsEnabled.value
        )

        val updated = HiddenArmyGameEngine.applyBattleOutcome(
            _gameState.value,
            attackerId,
            defenderId,
            result
        )

        val banner = when {
            result.attackerSurvives && isPlayerAttacking -> "Enemy defeated! Identity revealed."
            result.defenderSurvives && isPlayerAttacking -> "Your attack failed! Enemy revealed."
            result.attackerSurvives && !isPlayerAttacking -> "CPU unit won the battle!"
            result.defenderSurvives && !isPlayerAttacking -> "You defended successfully!"
            else -> "Both units eliminated!"
        }
        emitBanner(banner)

        _gameState.value = updated.copy(activeBattle = null)

        if (updated.status.isGameOver) {
            handleGameOver(updated)
            return
        }

        if (isPlayerAttacking) {
            advanceToCpuTurn()
        } else {
            advanceToPlayerTurn()
        }
    }

    private fun advanceToCpuTurn() {
        val next = HiddenArmyGameEngine.startTurn(_gameState.value, Side.CPU)
        _gameState.value = next

        if (next.status.isGameOver) {
            handleGameOver(next)
            return
        }

        // Trigger CPU AI
        cpuJob?.cancel()
        cpuJob = viewModelScope.launch {
            // CPU thinking delay (400-600ms)
            delay(500)
            executeCpuAction()
        }
    }

    private fun executeCpuAction() {
        val state = _gameState.value
        if (state.status != GameStatus.CPU_TURN) {
            isProcessingAction = false
            return
        }

        val difficulty = preferences.cpuDifficulty.value
        val action = HiddenArmyCpuStrategy.selectAction(state, difficulty)

        if (action == null) {
            // No moves available, pass turn back to player
            advanceToPlayerTurn()
            return
        }

        when (action) {
            is CpuAction.Move -> {
                val afterMove = HiddenArmyGameEngine.executeMove(state, action.unitId, action.targetPosition)
                _gameState.value = afterMove

                val base = afterMove.bases.find { it.position == action.targetPosition }
                if (base != null && base.owner == BaseOwner.CPU) {
                    emitBanner("CPU captured a base!")
                }

                soundAndHaptics.playMove(
                    soundEnabled = preferences.soundEnabled.value,
                    hapticsEnabled = preferences.hapticsEnabled.value
                )

                advanceToPlayerTurn()
            }
            is CpuAction.Attack -> {
                val attacker = state.units.find { it.id == action.attackerId }
                val defender = state.units.find { it.id == action.defenderId }
                if (attacker == null || defender == null) {
                    advanceToPlayerTurn()
                    return
                }

                soundAndHaptics.playAttack(
                    soundEnabled = preferences.soundEnabled.value,
                    hapticsEnabled = preferences.hapticsEnabled.value
                )

                val result = HiddenArmyGameEngine.resolveBattle(attacker, defender)

                if (preferences.battleAnimationEnabled.value) {
                    _gameState.value = state.copy(
                        status = GameStatus.BATTLE,
                        activeBattle = ActiveBattle(
                            attacker = attacker,
                            defender = defender,
                            result = result,
                            battlePosition = defender.position
                        )
                    )
                    viewModelScope.launch {
                        delay(650)
                        finishBattle(attacker.id, defender.id, result, isPlayerAttacking = false)
                    }
                } else {
                    finishBattle(attacker.id, defender.id, result, isPlayerAttacking = false)
                }
            }
        }
    }

    private fun advanceToPlayerTurn() {
        val next = HiddenArmyGameEngine.startTurn(_gameState.value, Side.PLAYER)
        _gameState.value = next
        isProcessingAction = false

        if (next.status.isGameOver) {
            handleGameOver(next)
        }
    }

    private fun handleGameOver(state: GameState) {
        isProcessingAction = false
        val score = state.calculateScore()
        val isWin = state.status == GameStatus.PLAYER_WON
        val isLoss = state.status == GameStatus.CPU_WON
        val isDraw = state.status == GameStatus.DRAW

        if (isWin) {
            soundAndHaptics.playVictory(
                soundEnabled = preferences.soundEnabled.value,
                hapticsEnabled = preferences.hapticsEnabled.value
            )
            emitBanner("Victory! You eliminated the enemy or held all bases!")
        } else if (isLoss) {
            soundAndHaptics.playDefeat(
                soundEnabled = preferences.soundEnabled.value,
                hapticsEnabled = preferences.hapticsEnabled.value
            )
            emitBanner("Defeat! CPU conquered the battlefield.")
        } else if (isDraw) {
            emitBanner("Draw! Both armies fell simultaneously.")
        }

        preferences.recordMatchResult(
            isWin = isWin,
            isLoss = isLoss,
            isDraw = isDraw,
            score = score,
            battles = state.battles,
            basesCaptured = state.basesCapturedCount
        )
    }

    private fun emitBanner(message: String) {
        viewModelScope.launch {
            _bannerEvent.emit(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cpuJob?.cancel()
        soundAndHaptics.release()
    }
}
