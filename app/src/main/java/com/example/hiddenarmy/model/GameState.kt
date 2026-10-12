package com.example.hiddenarmy.model

data class ActiveBattle(
    val attacker: Unit,
    val defender: Unit,
    val result: BattleResult,
    val battlePosition: Position
)

data class GameState(
    val units: List<Unit>,
    val bases: List<Base>,
    val selectedUnitId: String? = null,
    val legalMoves: List<Position> = emptyList(),
    val attackTargets: List<Position> = emptyList(),
    val turn: Side = Side.PLAYER,
    val status: GameStatus = GameStatus.PLAYER_TURN,
    val turnCount: Int = 1,
    val battles: Int = 0,
    val reveals: Int = 0,
    val successfulPlayerAttacks: Int = 0,
    val basesCapturedCount: Int = 0,
    val activeBattle: ActiveBattle? = null,
    val lastBannerMessage: String? = null
) {
    val playerUnits: List<Unit> get() = units.filter { it.side == Side.PLAYER }
    val cpuUnits: List<Unit> get() = units.filter { it.side == Side.CPU }
    val playerControlledBases: List<Base> get() = bases.filter { it.owner == BaseOwner.PLAYER }
    val cpuControlledBases: List<Base> get() = bases.filter { it.owner == BaseOwner.CPU }
    val selectedUnit: Unit? get() = units.find { it.id == selectedUnitId }

    fun calculateScore(): Int {
        val baseScore = when (status) {
            GameStatus.PLAYER_WON -> 100
            GameStatus.DRAW -> 50
            else -> 0
        }
        val survivingUnitsBonus = playerUnits.size * 10
        val controlledBasesBonus = playerControlledBases.size * 20
        val attacksBonus = successfulPlayerAttacks * 5
        return baseScore + survivingUnitsBonus + controlledBasesBonus + attacksBonus
    }
}
