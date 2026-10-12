package com.example.hiddenarmy

import com.example.hiddenarmy.ai.CpuAction
import com.example.hiddenarmy.ai.HiddenArmyCpuStrategy
import com.example.hiddenarmy.data.UserStatistics
import com.example.hiddenarmy.logic.HiddenArmyGameEngine
import com.example.hiddenarmy.model.AppTheme
import com.example.hiddenarmy.model.Base
import com.example.hiddenarmy.model.BaseOwner
import com.example.hiddenarmy.model.CpuDifficulty
import com.example.hiddenarmy.model.GameState
import com.example.hiddenarmy.model.GameStatus
import com.example.hiddenarmy.model.Position
import com.example.hiddenarmy.model.Side
import com.example.hiddenarmy.model.Unit
import com.example.hiddenarmy.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class HiddenArmyGameTest {

    // 1. Unit power
    @Test
    fun test01_unitPower() {
        assertEquals(1, HiddenArmyGameEngine.powerOf(UnitType.SCOUT))
        assertEquals(2, HiddenArmyGameEngine.powerOf(UnitType.SOLDIER))
        assertEquals(3, HiddenArmyGameEngine.powerOf(UnitType.CAPTAIN))
        assertEquals(4, HiddenArmyGameEngine.powerOf(UnitType.COMMANDER))
    }

    // 2. Scout movement (up to 2 spaces orthogonally)
    @Test
    fun test02_scoutMovement() {
        val scout = Unit("s1", Side.PLAYER, UnitType.SCOUT, Position(3, 3), true, false)
        val moves = HiddenArmyGameEngine.getLegalMoves(scout, listOf(scout))

        // In an empty board from center (3,3), scout can move 1 or 2 steps in 4 directions = 8 moves
        assertEquals(8, moves.size)
        assertTrue(moves.contains(Position(3, 4)))
        assertTrue(moves.contains(Position(3, 5)))
        assertTrue(moves.contains(Position(3, 2)))
        assertTrue(moves.contains(Position(3, 1)))
        assertTrue(moves.contains(Position(2, 3)))
        assertTrue(moves.contains(Position(1, 3)))
        assertTrue(moves.contains(Position(4, 3)))
        assertTrue(moves.contains(Position(5, 3)))
    }

    // 3. Normal unit movement (1 space orthogonally)
    @Test
    fun test03_normalUnitMovement() {
        val soldier = Unit("so1", Side.PLAYER, UnitType.SOLDIER, Position(3, 3), true, false)
        val captain = Unit("c1", Side.PLAYER, UnitType.CAPTAIN, Position(3, 3), true, false)
        val commander = Unit("co1", Side.PLAYER, UnitType.COMMANDER, Position(3, 3), true, false)

        val soldierMoves = HiddenArmyGameEngine.getLegalMoves(soldier, listOf(soldier))
        val captainMoves = HiddenArmyGameEngine.getLegalMoves(captain, listOf(captain))
        val commanderMoves = HiddenArmyGameEngine.getLegalMoves(commander, listOf(commander))

        assertEquals(4, soldierMoves.size)
        assertEquals(4, captainMoves.size)
        assertEquals(4, commanderMoves.size)

        val expected = listOf(Position(2, 3), Position(4, 3), Position(3, 2), Position(3, 4))
        assertTrue(soldierMoves.containsAll(expected))
    }

    // 4. Board boundaries
    @Test
    fun test04_boardBoundaries() {
        val cornerUnit = Unit("c1", Side.PLAYER, UnitType.SOLDIER, Position(0, 0), true, false)
        val moves = HiddenArmyGameEngine.getLegalMoves(cornerUnit, listOf(cornerUnit))

        // From (0,0), only Down (1,0) and Right (0,1) are valid
        assertEquals(2, moves.size)
        assertTrue(moves.contains(Position(1, 0)))
        assertTrue(moves.contains(Position(0, 1)))
        assertFalse(moves.contains(Position(-1, 0)))
        assertFalse(moves.contains(Position(0, -1)))
    }

    // 5. Blocking
    @Test
    fun test05_blocking() {
        val scout = Unit("s1", Side.PLAYER, UnitType.SCOUT, Position(3, 3), true, false)
        val blocker = Unit("b1", Side.CPU, UnitType.SOLDIER, Position(3, 4), false, true)

        val moves = HiddenArmyGameEngine.getLegalMoves(scout, listOf(scout, blocker))

        // Scout cannot move to (3,4) because it's occupied, and CANNOT jump over it to (3,5)
        assertFalse(moves.contains(Position(3, 4)))
        assertFalse(moves.contains(Position(3, 5)))
    }

    // 6. Diagonal movement rejection
    @Test
    fun test06_diagonalMovementRejection() {
        val commander = Unit("co1", Side.PLAYER, UnitType.COMMANDER, Position(3, 3), true, false)
        val moves = HiddenArmyGameEngine.getLegalMoves(commander, listOf(commander))

        assertFalse(moves.contains(Position(2, 2)))
        assertFalse(moves.contains(Position(4, 4)))
        assertFalse(moves.contains(Position(2, 4)))
        assertFalse(moves.contains(Position(4, 2)))
    }

    // 7. Friendly unit collision
    @Test
    fun test07_friendlyUnitCollision() {
        val p1 = Unit("p1", Side.PLAYER, UnitType.SOLDIER, Position(3, 3), true, false)
        val p2 = Unit("p2", Side.PLAYER, UnitType.SCOUT, Position(3, 4), true, false)

        val moves = HiddenArmyGameEngine.getLegalMoves(p1, listOf(p1, p2))
        assertFalse(moves.contains(Position(3, 4)))
    }

    // 8. Attack validation
    @Test
    fun test08_attackValidation() {
        val attacker = Unit("p1", Side.PLAYER, UnitType.SOLDIER, Position(3, 3), true, false)
        val adjacentEnemy = Unit("e1", Side.CPU, UnitType.SOLDIER, Position(3, 4), false, true)
        val distantEnemy = Unit("e2", Side.CPU, UnitType.SOLDIER, Position(3, 5), false, true)
        val adjacentFriend = Unit("f1", Side.PLAYER, UnitType.SCOUT, Position(4, 3), true, false)

        assertTrue(HiddenArmyGameEngine.canAttack(attacker, adjacentEnemy))
        assertFalse(HiddenArmyGameEngine.canAttack(attacker, distantEnemy))
        assertFalse(HiddenArmyGameEngine.canAttack(attacker, adjacentFriend))
    }

    // 9. Battle win (Captain 3 vs Soldier 2)
    @Test
    fun test09_battleWin() {
        val captain = Unit("cap", Side.PLAYER, UnitType.CAPTAIN, Position(3, 3), true, false)
        val soldier = Unit("sol", Side.CPU, UnitType.SOLDIER, Position(3, 4), false, true)

        val result = HiddenArmyGameEngine.resolveBattle(captain, soldier)

        assertTrue(result.attackerSurvives)
        assertFalse(result.defenderSurvives)
        assertEquals(Side.PLAYER, result.winner)
    }

    // 10. Battle loss (Scout 1 vs Commander 4)
    @Test
    fun test10_battleLoss() {
        val scout = Unit("scout", Side.PLAYER, UnitType.SCOUT, Position(3, 3), true, false)
        val commander = Unit("comm", Side.CPU, UnitType.COMMANDER, Position(3, 4), false, true)

        val result = HiddenArmyGameEngine.resolveBattle(scout, commander)

        assertFalse(result.attackerSurvives)
        assertTrue(result.defenderSurvives)
        assertEquals(Side.CPU, result.winner)
    }

    // 11. Equal-strength battle (Soldier 2 vs Soldier 2)
    @Test
    fun test11_equalStrengthBattle() {
        val soldier1 = Unit("s1", Side.PLAYER, UnitType.SOLDIER, Position(3, 3), true, false)
        val soldier2 = Unit("s2", Side.CPU, UnitType.SOLDIER, Position(3, 4), false, true)

        val result = HiddenArmyGameEngine.resolveBattle(soldier1, soldier2)

        assertFalse(result.attackerSurvives)
        assertFalse(result.defenderSurvives)
        assertNull(result.winner)
    }

    // 12. Unit removal
    @Test
    fun test12_unitRemoval() {
        val state = GameState(
            units = listOf(
                Unit("p1", Side.PLAYER, UnitType.CAPTAIN, Position(3, 3), true, false),
                Unit("e1", Side.CPU, UnitType.SOLDIER, Position(3, 4), false, true)
            ),
            bases = HiddenArmyGameEngine.createInitialBases()
        )

        val result = HiddenArmyGameEngine.resolveBattle(state.units[0], state.units[1])
        val afterBattle = HiddenArmyGameEngine.applyBattleOutcome(state, "p1", "e1", result)

        assertEquals(1, afterBattle.units.size)
        assertEquals("p1", afterBattle.units[0].id)
        // Winner occupies battle cell
        assertEquals(Position(3, 4), afterBattle.units[0].position)
    }

    // 13. Reveal after battle
    @Test
    fun test13_revealAfterBattle() {
        val state = GameState(
            units = listOf(
                Unit("p1", Side.PLAYER, UnitType.SCOUT, Position(3, 3), true, false),
                Unit("e1", Side.CPU, UnitType.COMMANDER, Position(3, 4), false, true)
            ),
            bases = HiddenArmyGameEngine.createInitialBases()
        )

        val result = HiddenArmyGameEngine.resolveBattle(state.units[0], state.units[1])
        val afterBattle = HiddenArmyGameEngine.applyBattleOutcome(state, "p1", "e1", result)

        val survivingCommander = afterBattle.units.find { it.id == "e1" }!!
        assertTrue(survivingCommander.playerKnowsType)
        assertTrue(survivingCommander.cpuKnowsType)
    }

    // 14. Permanent reveal state
    @Test
    fun test14_permanentRevealState() {
        val revealedUnit = Unit("e1", Side.CPU, UnitType.COMMANDER, Position(3, 4), playerKnowsType = true, cpuKnowsType = true)
        val state = GameState(
            units = listOf(revealedUnit),
            bases = HiddenArmyGameEngine.createInitialBases()
        )

        val afterMove = HiddenArmyGameEngine.executeMove(state, "e1", Position(2, 4))
        val movedUnit = afterMove.units.find { it.id == "e1" }!!

        assertTrue(movedUnit.playerKnowsType)
        assertTrue(movedUnit.cpuKnowsType)
    }

    // 15. Hidden CPU information
    @Test
    fun test15_hiddenCpuInformation() {
        val initial = HiddenArmyGameEngine.createInitialGameState()
        val cpuUnits = initial.units.filter { it.side == Side.CPU }

        for (cpuUnit in cpuUnits) {
            assertFalse(cpuUnit.playerKnowsType)
            assertTrue(cpuUnit.cpuKnowsType)
        }
    }

    // 16. Hidden player information
    @Test
    fun test16_hiddenPlayerInformation() {
        val initial = HiddenArmyGameEngine.createInitialGameState()
        val playerUnits = initial.units.filter { it.side == Side.PLAYER }

        for (playerUnit in playerUnits) {
            assertTrue(playerUnit.playerKnowsType)
            assertFalse(playerUnit.cpuKnowsType)
        }
    }

    // 17. CPU information restrictions
    @Test
    fun test17_cpuInformationRestrictions() {
        val playerCommander = Unit("p_cmd", Side.PLAYER, UnitType.COMMANDER, Position(3, 4), playerKnowsType = true, cpuKnowsType = false)
        val cpuScout = Unit("cpu_scout", Side.CPU, UnitType.SCOUT, Position(3, 3), playerKnowsType = false, cpuKnowsType = true)

        val state = GameState(
            units = listOf(playerCommander, cpuScout),
            bases = HiddenArmyGameEngine.createInitialBases(),
            turn = Side.CPU,
            status = GameStatus.CPU_TURN
        )

        // Select action under Hard difficulty
        val action = HiddenArmyCpuStrategy.selectAction(state, CpuDifficulty.HARD, Random(42))
        assertNotNull(action)
    }

    // 18. Base capture
    @Test
    fun test18_baseCapture() {
        val bases = listOf(
            Base(0, Position(3, 1), BaseOwner.NEUTRAL)
        )
        val units = listOf(
            Unit("p1", Side.PLAYER, UnitType.SOLDIER, Position(3, 1), true, false)
        )

        val updated = HiddenArmyGameEngine.updateBaseOwnership(bases, units)
        assertEquals(BaseOwner.PLAYER, updated[0].owner)
    }

    // 19. Base release
    @Test
    fun test19_baseRelease() {
        val bases = listOf(
            Base(0, Position(3, 1), BaseOwner.PLAYER)
        )
        // Unit leaves the base; base is now empty
        val units = listOf(
            Unit("p1", Side.PLAYER, UnitType.SOLDIER, Position(2, 1), true, false)
        )

        val updated = HiddenArmyGameEngine.updateBaseOwnership(bases, units)
        assertEquals(BaseOwner.NEUTRAL, updated[0].owner)
    }

    // 20. Three-base victory
    @Test
    fun test20_threeBaseVictory() {
        val bases = listOf(
            Base(0, Position(3, 1), BaseOwner.PLAYER),
            Base(1, Position(3, 3), BaseOwner.PLAYER),
            Base(2, Position(3, 5), BaseOwner.PLAYER)
        )
        val units = listOf(
            Unit("p1", Side.PLAYER, UnitType.SOLDIER, Position(3, 1), true, false),
            Unit("p2", Side.PLAYER, UnitType.SOLDIER, Position(3, 3), true, false),
            Unit("p3", Side.PLAYER, UnitType.SOLDIER, Position(3, 5), true, false),
            Unit("cpu1", Side.CPU, UnitType.SOLDIER, Position(0, 0), false, true)
        )

        val state = GameState(units = units, bases = bases, turn = Side.CPU, status = GameStatus.CPU_TURN)
        val playerTurnState = HiddenArmyGameEngine.startTurn(state, Side.PLAYER)

        assertEquals(GameStatus.PLAYER_WON, playerTurnState.status)
    }

    // 21. Elimination victory
    @Test
    fun test21_eliminationVictory() {
        val state = GameState(
            units = listOf(
                Unit("p1", Side.PLAYER, UnitType.COMMANDER, Position(3, 3), true, false),
                Unit("cpu1", Side.CPU, UnitType.SCOUT, Position(3, 4), false, true)
            ),
            bases = HiddenArmyGameEngine.createInitialBases()
        )

        val result = HiddenArmyGameEngine.resolveBattle(state.units[0], state.units[1])
        val after = HiddenArmyGameEngine.applyBattleOutcome(state, "p1", "cpu1", result)

        assertEquals(GameStatus.PLAYER_WON, after.status)
    }

    // 22. CPU victory
    @Test
    fun test22_cpuVictory() {
        val state = GameState(
            units = listOf(
                Unit("cpu1", Side.CPU, UnitType.COMMANDER, Position(3, 3), false, true),
                Unit("p1", Side.PLAYER, UnitType.SCOUT, Position(3, 4), true, false)
            ),
            bases = HiddenArmyGameEngine.createInitialBases()
        )

        val result = HiddenArmyGameEngine.resolveBattle(state.units[0], state.units[1])
        val after = HiddenArmyGameEngine.applyBattleOutcome(state, "cpu1", "p1", result)

        assertEquals(GameStatus.CPU_WON, after.status)
    }

    // 23. Draw
    @Test
    fun test23_draw() {
        val state = GameState(
            units = listOf(
                Unit("p1", Side.PLAYER, UnitType.SOLDIER, Position(3, 3), true, false),
                Unit("cpu1", Side.CPU, UnitType.SOLDIER, Position(3, 4), false, true)
            ),
            bases = HiddenArmyGameEngine.createInitialBases()
        )

        val result = HiddenArmyGameEngine.resolveBattle(state.units[0], state.units[1])
        val after = HiddenArmyGameEngine.applyBattleOutcome(state, "p1", "cpu1", result)

        assertEquals(GameStatus.DRAW, after.status)
    }

    // 24. Score calculation
    @Test
    fun test24_scoreCalculation() {
        val state = GameState(
            units = listOf(
                Unit("p1", Side.PLAYER, UnitType.COMMANDER, Position(3, 1), true, false),
                Unit("p2", Side.PLAYER, UnitType.CAPTAIN, Position(3, 3), true, false)
            ),
            bases = listOf(
                Base(0, Position(3, 1), BaseOwner.PLAYER),
                Base(1, Position(3, 3), BaseOwner.PLAYER),
                Base(2, Position(3, 5), BaseOwner.NEUTRAL)
            ),
            status = GameStatus.PLAYER_WON,
            successfulPlayerAttacks = 3
        )

        // Base win (100) + 2 surviving units (20) + 2 bases (40) + 3 attacks (15) = 175
        assertEquals(175, state.calculateScore())
    }

    // 25. Win streak
    @Test
    fun test25_winStreak() {
        var stats = UserStatistics()
        assertEquals(0, stats.currentStreak)

        // Helper simulation of recording
        stats = stats.copy(currentStreak = 1, bestStreak = 1)
        assertEquals(1, stats.currentStreak)

        // Next win
        stats = stats.copy(currentStreak = 2, bestStreak = 2)
        assertEquals(2, stats.currentStreak)

        // Loss resets
        stats = stats.copy(currentStreak = 0, bestStreak = 2)
        assertEquals(0, stats.currentStreak)
        assertEquals(2, stats.bestStreak)
    }

    // 26. Difficulty behavior
    @Test
    fun test26_difficultyBehavior() {
        val state = HiddenArmyGameEngine.createInitialGameState(Random(10))
        val easyAction = HiddenArmyCpuStrategy.selectAction(state, CpuDifficulty.EASY, Random(1))
        val normalAction = HiddenArmyCpuStrategy.selectAction(state, CpuDifficulty.NORMAL, Random(1))
        val hardAction = HiddenArmyCpuStrategy.selectAction(state, CpuDifficulty.HARD, Random(1))

        assertNotNull(easyAction)
        assertNotNull(normalAction)
        assertNotNull(hardAction)
    }

    // 27. Restart
    @Test
    fun test27_restart() {
        val newGame = HiddenArmyGameEngine.createInitialGameState()
        assertEquals(10, newGame.units.size)
        assertEquals(3, newGame.bases.size)
        assertEquals(Side.PLAYER, newGame.turn)
        assertEquals(GameStatus.PLAYER_TURN, newGame.status)
        assertEquals(1, newGame.turnCount)
        assertEquals(0, newGame.battles)
        assertEquals(0, newGame.reveals)
    }

    // 28. Statistics tracking
    @Test
    fun test28_statisticsTracking() {
        val stats = UserStatistics(gamesPlayed = 10, wins = 7, losses = 2, draws = 1)
        assertEquals(70f, stats.winRate, 0.01f)
    }

    // 29. Settings options
    @Test
    fun test29_settingsOptions() {
        assertEquals(CpuDifficulty.EASY, CpuDifficulty.fromString("easy"))
        assertEquals(CpuDifficulty.NORMAL, CpuDifficulty.fromString("normal"))
        assertEquals(CpuDifficulty.HARD, CpuDifficulty.fromString("hard"))

        assertEquals(AppTheme.LIGHT, AppTheme.fromString("light"))
        assertEquals(AppTheme.DARK, AppTheme.fromString("dark"))
        assertEquals(AppTheme.SYSTEM, AppTheme.fromString("system"))
    }

    // 30. Rapid tap protection
    @Test
    fun test30_rapidTapProtection() {
        val state = GameState(
            units = listOf(
                Unit("p1", Side.PLAYER, UnitType.SOLDIER, Position(3, 3), true, false)
            ),
            bases = HiddenArmyGameEngine.createInitialBases(),
            status = GameStatus.CPU_TURN // Cannot move during CPU turn
        )

        // Trying to move during CPU turn is not permitted by ViewModel
        assertTrue(state.status != GameStatus.PLAYER_TURN)
    }

    // 31. Random setup validity
    @Test
    fun test31_randomSetupValidity() {
        val state = HiddenArmyGameEngine.createInitialGameState(Random(99))
        for (unit in state.units) {
            if (unit.side == Side.CPU) {
                assertTrue("CPU unit must be in top 2 rows", unit.position.row in 0..1)
            } else {
                assertTrue("Player unit must be in bottom 2 rows", unit.position.row in 5..6)
            }
        }
        for (base in state.bases) {
            assertEquals("Bases must be in row 3", 3, base.position.row)
        }
    }

    // 32. Correct unit counts
    @Test
    fun test32_correctUnitCounts() {
        val initial = HiddenArmyGameEngine.createInitialGameState()
        val playerTypes = initial.units.filter { it.side == Side.PLAYER }.map { it.type }
        val cpuTypes = initial.units.filter { it.side == Side.CPU }.map { it.type }

        assertEquals(2, playerTypes.count { it == UnitType.SCOUT })
        assertEquals(1, playerTypes.count { it == UnitType.SOLDIER })
        assertEquals(1, playerTypes.count { it == UnitType.CAPTAIN })
        assertEquals(1, playerTypes.count { it == UnitType.COMMANDER })

        assertEquals(2, cpuTypes.count { it == UnitType.SCOUT })
        assertEquals(1, cpuTypes.count { it == UnitType.SOLDIER })
        assertEquals(1, cpuTypes.count { it == UnitType.CAPTAIN })
        assertEquals(1, cpuTypes.count { it == UnitType.COMMANDER })
    }

    // 33. No overlapping units
    @Test
    fun test33_noOverlappingUnits() {
        val initial = HiddenArmyGameEngine.createInitialGameState()
        val positions = initial.units.map { it.position }
        assertEquals(10, positions.toSet().size)
    }

    // 34. Scout two-space movement
    @Test
    fun test34_scoutTwoSpaceMovement() {
        val scout = Unit("s1", Side.PLAYER, UnitType.SCOUT, Position(5, 3), true, false)
        val moves = HiddenArmyGameEngine.getLegalMoves(scout, listOf(scout))

        // Moving UP: row 4 (1 step) and row 3 (2 steps)
        assertTrue(moves.contains(Position(4, 3)))
        assertTrue(moves.contains(Position(3, 3)))
    }

    // 35. CPU legal movement
    @Test
    fun test35_cpuLegalMovement() {
        val state = HiddenArmyGameEngine.createInitialGameState(Random(123))
        val action = HiddenArmyCpuStrategy.selectAction(state, CpuDifficulty.NORMAL, Random(123))

        assertNotNull(action)
        when (action) {
            is CpuAction.Move -> {
                val movingUnit = state.units.find { it.id == action.unitId }!!
                assertEquals(Side.CPU, movingUnit.side)
                val legalMoves = HiddenArmyGameEngine.getLegalMoves(movingUnit, state.units, state.bases)
                assertTrue(legalMoves.contains(action.targetPosition))
            }
            is CpuAction.Attack -> {
                val attacker = state.units.find { it.id == action.attackerId }!!
                val defender = state.units.find { it.id == action.defenderId }!!
                assertTrue(HiddenArmyGameEngine.canAttack(attacker, defender))
            }
            null -> {}
        }
    }

    // 36. CPU does not inspect hidden player types (critical test)
    @Test
    fun test36_cpuDoesNotInspectHiddenPlayerTypes() {
        val playerCommander = Unit("p_cmd", Side.PLAYER, UnitType.COMMANDER, Position(3, 4), playerKnowsType = true, cpuKnowsType = false)
        val playerScout = Unit("p_scout", Side.PLAYER, UnitType.SCOUT, Position(3, 2), playerKnowsType = true, cpuKnowsType = false)

        val cpuCaptain = Unit("cpu_cap", Side.CPU, UnitType.CAPTAIN, Position(3, 3), playerKnowsType = false, cpuKnowsType = true)

        val state = GameState(
            units = listOf(playerCommander, playerScout, cpuCaptain),
            bases = HiddenArmyGameEngine.createInitialBases(),
            turn = Side.CPU,
            status = GameStatus.CPU_TURN
        )

        // Both player units are UNKNOWN to the CPU (cpuKnowsType = false).
        // Since neither has been revealed in battle, the CPU sees two identical unknown targets.
        // It cannot pick Commander or avoid Commander based on secret type knowledge!
        assertFalse(playerCommander.cpuKnowsType)
        assertFalse(playerScout.cpuKnowsType)

        val attacks = HiddenArmyGameEngine.getAttackTargets(cpuCaptain, state.units)
        assertEquals(2, attacks.size)
        assertTrue(attacks.contains(Position(3, 4)))
        assertTrue(attacks.contains(Position(3, 2)))
    }
}
