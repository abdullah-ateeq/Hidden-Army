package com.example.hiddenarmy.model

enum class UnitType(val displayName: String, val power: Int, val maxMove: Int) {
    SCOUT("Scout", 1, 2),
    SOLDIER("Soldier", 2, 1),
    CAPTAIN("Captain", 3, 1),
    COMMANDER("Commander", 4, 1);

    companion object {
        fun powerOf(type: UnitType): Int = type.power
    }
}
