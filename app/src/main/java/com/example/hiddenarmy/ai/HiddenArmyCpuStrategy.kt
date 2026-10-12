package com.example.hiddenarmy.ai

import com.example.hiddenarmy.logic.HiddenArmyGameEngine
import com.example.hiddenarmy.model.BaseOwner
import com.example.hiddenarmy.model.CpuDifficulty
import com.example.hiddenarmy.model.GameState
import com.example.hiddenarmy.model.Position
import com.example.hiddenarmy.model.Side
import com.example.hiddenarmy.model.Unit
import com.example.hiddenarmy.model.UnitType
import kotlin.random.Random

sealed class CpuAction {
    data class Move(val unitId: String, val targetPosition: Position) : CpuAction()
    data class Attack(val attackerId: String, val defenderId: String) : CpuAction()
}

object HiddenArmyCpuStrategy {

    private const val ESTIMATED_UNKNOWN_POWER = 2.2f

    fun selectAction(
        state: GameState,
        difficulty: CpuDifficulty,
        random: Random = Random
    ): CpuAction? {
        val cpuUnits = state.units.filter { it.side == Side.CPU }
        if (cpuUnits.isEmpty()) return null

        val allActions = mutableListOf<CpuAction>()
        for (cpuUnit in cpuUnits) {
            val moves = HiddenArmyGameEngine.getLegalMoves(cpuUnit, state.units, state.bases)
            for (move in moves) {
                allActions.add(CpuAction.Move(cpuUnit.id, move))
            }
            val attacks = HiddenArmyGameEngine.getAttackTargets(cpuUnit, state.units)
            for (attackPos in attacks) {
                val targetUnit = state.units.find { it.position == attackPos && it.side == Side.PLAYER }
                if (targetUnit != null) {
                    allActions.add(CpuAction.Attack(cpuUnit.id, targetUnit.id))
                }
            }
        }

        if (allActions.isEmpty()) return null

        // On Easy difficulty, 45% chance to make a random legal move
        if (difficulty == CpuDifficulty.EASY && random.nextFloat() < 0.45f) {
            return allActions.random(random)
        }

        // Score each action based on CPU tactical priorities
        var bestScore = Float.NEGATIVE_INFINITY
        var bestActions = mutableListOf<CpuAction>()

        for (action in allActions) {
            val score = evaluateAction(action, state, difficulty, random)
            if (score > bestScore) {
                bestScore = score
                bestActions = mutableListOf(action)
            } else if (score == bestScore) {
                bestActions.add(action)
            }
        }

        return bestActions.randomOrNull(random) ?: allActions.random(random)
    }

    private fun evaluateAction(
        action: CpuAction,
        state: GameState,
        difficulty: CpuDifficulty,
        random: Random
    ): Float {
        return when (action) {
            is CpuAction.Attack -> evaluateAttack(action, state, difficulty)
            is CpuAction.Move -> evaluateMove(action, state, difficulty)
        }
    }

    private fun evaluateAttack(
        action: CpuAction.Attack,
        state: GameState,
        difficulty: CpuDifficulty
    ): Float {
        val attacker = state.units.find { it.id == action.attackerId } ?: return -100f
        val defender = state.units.find { it.id == action.defenderId } ?: return -100f

        val attackerPower = attacker.type.power.toFloat()
        // IMPORTANT: The CPU must NEVER inspect defender.type if cpuKnowsType is false!
        val defenderEstimatedPower: Float = if (defender.cpuKnowsType) {
            defender.type.power.toFloat()
        } else {
            ESTIMATED_UNKNOWN_POWER
        }

        val playerUnitsCount = state.units.count { it.side == Side.PLAYER }

        // 1. Immediate game-winning attack: eliminating the last player unit
        if (playerUnitsCount == 1 && defender.cpuKnowsType && attackerPower > defenderEstimatedPower) {
            return 1000f
        }

        // Known combat calculation
        if (defender.cpuKnowsType) {
            val powerDiff = attackerPower - defenderEstimatedPower
            return when {
                powerDiff > 0f -> {
                    // Win guaranteed! Reward attacking weaker unit
                    // Priority 3: Attack a known weaker unit
                    500f + (defenderEstimatedPower * 30f) - (attackerPower * 5f)
                }
                powerDiff == 0f -> {
                    // Equal strength: both destroyed
                    if (attacker.type == UnitType.COMMANDER) {
                        // Hard difficulty: never willingly trade Commander
                        if (difficulty == CpuDifficulty.HARD) -200f else -50f
                    } else if (attacker.type == UnitType.SCOUT || attacker.type == UnitType.SOLDIER) {
                        // Trading scout or soldier is acceptable
                        80f
                    } else {
                        10f
                    }
                }
                else -> {
                    // Guaranteed loss: avoid attacking stronger unit!
                    -800f + powerDiff * 50f
                }
            }
        } else {
            // UNKNOWN Defender Evaluation
            // Priority 5: Attack unknown unit if favorable
            val baseUnknownScore = when (attacker.type) {
                UnitType.COMMANDER -> {
                    // Commander power 4: beats Scout(1), Soldier(2), Captain(3); ties Commander(4).
                    // On Hard difficulty, slightly favor probing with scouts/soldiers first unless securing territory
                    if (difficulty == CpuDifficulty.HARD) 220f else 300f
                }
                UnitType.CAPTAIN -> {
                    // Captain power 3: beats Scout(1, 1), Soldier(2); loses to Commander(4).
                    240f
                }
                UnitType.SOLDIER -> {
                    // Soldier power 2: beats Scout(1, 1), ties Soldier(2).
                    150f
                }
                UnitType.SCOUT -> {
                    // Scout power 1: exploration / reveal attempt
                    if (difficulty == CpuDifficulty.HARD) 110f else 90f
                }
            }

            // If defender occupies a neutral or player base, extra incentive to clear it
            val isDefenderOnBase = state.bases.any { it.position == defender.position }
            val baseBonus = if (isDefenderOnBase) 100f else 0f

            return baseUnknownScore + baseBonus
        }
    }

    private fun evaluateMove(
        action: CpuAction.Move,
        state: GameState,
        difficulty: CpuDifficulty
    ): Float {
        val unit = state.units.find { it.id == action.unitId } ?: return -100f
        val target = action.targetPosition
        var score = 0f

        val baseAtTarget = state.bases.find { it.position == target }
        val baseAtCurrent = state.bases.find { it.position == unit.position }

        // 2. Capture an unprotected base
        if (baseAtTarget != null && baseAtTarget.owner != BaseOwner.CPU) {
            score += 400f
            // If CPU already controls 2 bases, capturing the 3rd base sets up the 3-base win condition!
            val cpuControlledCount = state.bases.count { it.owner == BaseOwner.CPU }
            if (cpuControlledCount == 2) {
                score += 350f
            }
        }

        // 4. Defend a controlled base: leaving a controlled base leaves it neutral unless another unit stays
        if (baseAtCurrent != null && baseAtCurrent.owner == BaseOwner.CPU) {
            // Unit is currently holding a base. Leaving it loses control!
            score -= 250f
        }

        // 6. Move toward neutral / player bases
        val targetBases = state.bases.filter { it.owner != BaseOwner.CPU }
        if (targetBases.isNotEmpty()) {
            val minDistance = targetBases.minOf { it.position.orthogonalDistance(target) }
            val currentMinDist = targetBases.minOf { it.position.orthogonalDistance(unit.position) }
            if (minDistance < currentMinDist) {
                score += (6 - minDistance) * 15f
            }
        }

        // 7. Move toward player units (exploration & pressure)
        val playerUnits = state.units.filter { it.side == Side.PLAYER }
        if (playerUnits.isNotEmpty()) {
            val minPlayerDist = playerUnits.minOf { it.position.orthogonalDistance(target) }
            val currentMinPlayerDist = playerUnits.minOf { it.position.orthogonalDistance(unit.position) }
            if (minPlayerDist < currentMinPlayerDist) {
                score += (6 - minPlayerDist) * 8f
            }

            // Hard difficulty: Scout exploration forward
            if (unit.type == UnitType.SCOUT) {
                score += (target.row - unit.position.row) * 12f // moving down towards player territory
            }
        }

        // Danger check on Hard: avoid moving an unrevealed Commander adjacent to known stronger enemies
        if (difficulty == CpuDifficulty.HARD && unit.type == UnitType.COMMANDER) {
            val adjacentEnemies = state.units.filter {
                it.side == Side.PLAYER && it.position.isOrthogonallyAdjacent(target)
            }
            for (enemy in adjacentEnemies) {
                if (enemy.cpuKnowsType && enemy.type.power == 4) {
                    score -= 50f
                }
            }
        }

        return score
    }
}
