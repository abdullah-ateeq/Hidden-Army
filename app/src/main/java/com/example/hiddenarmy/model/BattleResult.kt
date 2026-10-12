package com.example.hiddenarmy.model

data class BattleResult(
    val attackerSurvives: Boolean,
    val defenderSurvives: Boolean,
    val winner: Side?,
    val revealedAttacker: UnitType,
    val revealedDefender: UnitType,
    val attackerId: String = "",
    val defenderId: String = "",
    val battlePosition: Position = Position(0, 0)
)
