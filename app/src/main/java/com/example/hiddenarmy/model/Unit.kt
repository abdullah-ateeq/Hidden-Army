package com.example.hiddenarmy.model

data class Unit(
    val id: String,
    val side: Side,
    val type: UnitType,
    val position: Position,
    val playerKnowsType: Boolean,
    val cpuKnowsType: Boolean
)

typealias GameUnit = Unit
