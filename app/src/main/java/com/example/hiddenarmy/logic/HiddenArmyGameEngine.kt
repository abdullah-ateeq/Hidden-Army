package com.example.hiddenarmy.logic

import com.example.hiddenarmy.model.Base
import com.example.hiddenarmy.model.BaseOwner
import com.example.hiddenarmy.model.BattleResult
import com.example.hiddenarmy.model.GameState
import com.example.hiddenarmy.model.GameStatus
import com.example.hiddenarmy.model.Position
import com.example.hiddenarmy.model.Side
import com.example.hiddenarmy.model.Unit
import com.example.hiddenarmy.model.UnitType
import kotlin.random.Random

object HiddenArmyGameEngine {

    const val BOARD_SIZE = 7

    val ORTHOGONAL_DIRECTIONS = listOf(
        Position(-1, 0), // UP
        Position(1, 0),  // DOWN
        Position(0, -1), // LEFT
        Position(0, 1)   // RIGHT
    )

    fun createInitialBases(): List<Base> {
        return listOf(
            Base(id = 0, position = Position(row = 3, column = 1), owner = BaseOwner.NEUTRAL),
            Base(id = 1, position = Position(row = 3, column = 3), owner = BaseOwner.NEUTRAL),
            Base(id = 2, position = Position(row = 3, column = 5), owner = BaseOwner.NEUTRAL)
        )
    }

    fun standardArmyTypes(): List<UnitType> {
        return listOf(
            UnitType.SCOUT,
            UnitType.SCOUT,
            UnitType.SOLDIER,
            UnitType.CAPTAIN,
            UnitType.COMMANDER
        )
    }

    fun powerOf(type: UnitType): Int = type.power

    fun createInitialUnits(random: Random = Random): List<Unit> {
        // CPU units in top 2 rows: rows 0 and 1 (14 cells total)
        val cpuCells = (0..1).flatMap { r -> (0 until BOARD_SIZE).map { c -> Position(r, c) } }
            .shuffled(random)
            .take(5)

        val cpuTypes = standardArmyTypes().shuffled(random)

        val cpuUnits = cpuCells.mapIndexed { index, pos ->
            Unit(
                id = "cpu_$index",
                side = Side.CPU,
                type = cpuTypes[index],
                position = pos,
                playerKnowsType = false,
                cpuKnowsType = true
            )
        }

        // Player units in bottom 2 rows: rows 5 and 6 (14 cells total)
        val playerCells = (5..6).flatMap { r -> (0 until BOARD_SIZE).map { c -> Position(r, c) } }
            .shuffled(random)
            .take(5)

        val playerTypes = standardArmyTypes().shuffled(random)

        val playerUnits = playerCells.mapIndexed { index, pos ->
            Unit(
                id = "player_$index",
                side = Side.PLAYER,
                type = playerTypes[index],
                position = pos,
                playerKnowsType = true,
                cpuKnowsType = false
            )
        }

        return cpuUnits + playerUnits
    }

    fun createInitialGameState(random: Random = Random): GameState {
        val bases = createInitialBases()
        val units = createInitialUnits(random)
        val updatedBases = updateBaseOwnership(bases, units)

        return GameState(
            units = units,
            bases = updatedBases,
            turn = Side.PLAYER,
            status = GameStatus.PLAYER_TURN,
            turnCount = 1,
            battles = 0,
            reveals = 0,
            successfulPlayerAttacks = 0,
            basesCapturedCount = 0
        )
    }

    fun getLegalMoves(
        unit: Unit,
        units: List<Unit>,
        bases: List<Base> = emptyList()
    ): List<Position> {
        val occupiedPositions = units.associateBy { it.position }
        val moves = mutableListOf<Position>()

        for (dir in ORTHOGONAL_DIRECTIONS) {
            val maxSteps = if (unit.type == UnitType.SCOUT) 2 else 1

            for (step in 1..maxSteps) {
                val target = Position(
                    row = unit.position.row + dir.row * step,
                    column = unit.position.column + dir.column * step
                )

                // Must stay inside board
                if (!target.isValid(BOARD_SIZE)) {
                    break
                }

                // If occupied by any unit, movement stops immediately (cannot move into it, cannot jump over it)
                if (occupiedPositions.containsKey(target)) {
                    break
                }

                moves.add(target)
            }
        }

        return moves
    }

    fun getAttackTargets(
        unit: Unit,
        units: List<Unit>
    ): List<Position> {
        val enemyOccupiedPositions = units
            .filter { it.side != unit.side }
            .map { it.position }
            .toSet()

        val attacks = mutableListOf<Position>()

        // Attack range is strictly 1 orthogonal step for ALL units, including Scout
        for (dir in ORTHOGONAL_DIRECTIONS) {
            val target = Position(
                row = unit.position.row + dir.row,
                column = unit.position.column + dir.column
            )
            if (target.isValid(BOARD_SIZE) && target in enemyOccupiedPositions) {
                attacks.add(target)
            }
        }

        return attacks
    }

    fun canAttack(attacker: Unit, defender: Unit): Boolean {
        if (attacker.side == defender.side) return false
        return attacker.position.isOrthogonallyAdjacent(defender.position)
    }

    fun resolveBattle(attacker: Unit, defender: Unit): BattleResult {
        require(canAttack(attacker, defender)) { "Attacker cannot attack defender" }

        val attackerPower = powerOf(attacker.type)
        val defenderPower = powerOf(defender.type)

        val attackerSurvives = attackerPower > defenderPower
        val defenderSurvives = defenderPower > attackerPower

        val winner = when {
            attackerSurvives -> attacker.side
            defenderSurvives -> defender.side
            else -> null // Equal strength: both destroyed
        }

        return BattleResult(
            attackerSurvives = attackerSurvives,
            defenderSurvives = defenderSurvives,
            winner = winner,
            revealedAttacker = attacker.type,
            revealedDefender = defender.type,
            attackerId = attacker.id,
            defenderId = defender.id,
            battlePosition = defender.position
        )
    }

    fun updateBaseOwnership(
        bases: List<Base>,
        units: List<Unit>
    ): List<Base> {
        val unitsByPos = units.associateBy { it.position }
        return bases.map { base ->
            val occupant = unitsByPos[base.position]
            val newOwner = when (occupant?.side) {
                Side.PLAYER -> BaseOwner.PLAYER
                Side.CPU -> BaseOwner.CPU
                null -> BaseOwner.NEUTRAL
            }
            base.copy(owner = newOwner)
        }
    }

    fun executeMove(
        currentState: GameState,
        unitId: String,
        targetPosition: Position
    ): GameState {
        val movingUnit = currentState.units.find { it.id == unitId }
            ?: return currentState

        val legalMoves = getLegalMoves(movingUnit, currentState.units, currentState.bases)
        if (targetPosition !in legalMoves) {
            return currentState
        }

        val updatedUnits = currentState.units.map { unit ->
            if (unit.id == unitId) {
                unit.copy(position = targetPosition)
            } else {
                unit
            }
        }

        val updatedBases = updateBaseOwnership(currentState.bases, updatedUnits)

        // Check if this move captured a base
        val previousBase = currentState.bases.find { it.position == targetPosition }
        val newBase = updatedBases.find { it.position == targetPosition }
        val newlyCaptured = newBase != null && previousBase?.owner != newBase.owner &&
                ((movingUnit.side == Side.PLAYER && newBase.owner == BaseOwner.PLAYER) ||
                 (movingUnit.side == Side.CPU && newBase.owner == BaseOwner.CPU))

        val newCapturedCount = if (newlyCaptured && movingUnit.side == Side.PLAYER) {
            currentState.basesCapturedCount + 1
        } else {
            currentState.basesCapturedCount
        }

        return currentState.copy(
            units = updatedUnits,
            bases = updatedBases,
            selectedUnitId = null,
            legalMoves = emptyList(),
            attackTargets = emptyList(),
            basesCapturedCount = newCapturedCount
        )
    }

    fun applyBattleOutcome(
        currentState: GameState,
        attackerId: String,
        defenderId: String,
        result: BattleResult
    ): GameState {
        val attacker = currentState.units.find { it.id == attackerId } ?: return currentState
        val defender = currentState.units.find { it.id == defenderId } ?: return currentState

        // Count new reveals
        var newReveals = 0
        if (!attacker.playerKnowsType || !attacker.cpuKnowsType) newReveals++
        if (!defender.playerKnowsType || !defender.cpuKnowsType) newReveals++

        val remainingUnits = mutableListOf<Unit>()

        for (unit in currentState.units) {
            when (unit.id) {
                attackerId -> {
                    if (result.attackerSurvives) {
                        // Attacker survives and occupies defender's cell
                        remainingUnits.add(
                            unit.copy(
                                position = defender.position,
                                playerKnowsType = true,
                                cpuKnowsType = true
                            )
                        )
                    }
                }
                defenderId -> {
                    if (result.defenderSurvives) {
                        // Defender survives in place
                        remainingUnits.add(
                            unit.copy(
                                playerKnowsType = true,
                                cpuKnowsType = true
                            )
                        )
                    }
                }
                else -> {
                    remainingUnits.add(unit)
                }
            }
        }

        val updatedBases = updateBaseOwnership(currentState.bases, remainingUnits)

        val playerWonAttack = attacker.side == Side.PLAYER && result.attackerSurvives
        val newSuccessfulPlayerAttacks = if (playerWonAttack) {
            currentState.successfulPlayerAttacks + 1
        } else {
            currentState.successfulPlayerAttacks
        }

        // Check if elimination occurred
        val playerAlive = remainingUnits.any { it.side == Side.PLAYER }
        val cpuAlive = remainingUnits.any { it.side == Side.CPU }

        val newStatus = when {
            !playerAlive && !cpuAlive -> GameStatus.DRAW
            !cpuAlive -> GameStatus.PLAYER_WON
            !playerAlive -> GameStatus.CPU_WON
            else -> currentState.status // Remains in progress until turn switch check
        }

        return currentState.copy(
            units = remainingUnits,
            bases = updatedBases,
            selectedUnitId = null,
            legalMoves = emptyList(),
            attackTargets = emptyList(),
            status = newStatus,
            battles = currentState.battles + 1,
            reveals = currentState.reveals + newReveals,
            successfulPlayerAttacks = newSuccessfulPlayerAttacks
        )
    }

    /**
     * Called when transitioning turn to next side.
     * Evaluates territory win condition (controlling all 3 bases at the beginning of turn).
     */
    fun startTurn(currentState: GameState, nextTurnSide: Side): GameState {
        if (currentState.status.isGameOver) {
            return currentState
        }

        val playerAlive = currentState.units.any { it.side == Side.PLAYER }
        val cpuAlive = currentState.units.any { it.side == Side.CPU }

        if (!playerAlive && !cpuAlive) {
            return currentState.copy(status = GameStatus.DRAW)
        }
        if (!cpuAlive) {
            return currentState.copy(status = GameStatus.PLAYER_WON)
        }
        if (!playerAlive) {
            return currentState.copy(status = GameStatus.CPU_WON)
        }

        // Territory win condition:
        // A side wins if they control all 3 bases at the beginning of their turn!
        val allBasesPlayerControlled = currentState.bases.size == 3 &&
                currentState.bases.all { it.owner == BaseOwner.PLAYER }
        val allBasesCpuControlled = currentState.bases.size == 3 &&
                currentState.bases.all { it.owner == BaseOwner.CPU }

        if (nextTurnSide == Side.PLAYER && allBasesPlayerControlled) {
            return currentState.copy(
                turn = Side.PLAYER,
                status = GameStatus.PLAYER_WON
            )
        }

        if (nextTurnSide == Side.CPU && allBasesCpuControlled) {
            return currentState.copy(
                turn = Side.CPU,
                status = GameStatus.CPU_WON
            )
        }

        val newTurnCount = if (nextTurnSide == Side.PLAYER) {
            currentState.turnCount + 1
        } else {
            currentState.turnCount
        }

        val nextStatus = if (nextTurnSide == Side.PLAYER) {
            GameStatus.PLAYER_TURN
        } else {
            GameStatus.CPU_TURN
        }

        return currentState.copy(
            turn = nextTurnSide,
            status = nextStatus,
            turnCount = newTurnCount,
            selectedUnitId = null,
            legalMoves = emptyList(),
            attackTargets = emptyList(),
            activeBattle = null
        )
    }
}
