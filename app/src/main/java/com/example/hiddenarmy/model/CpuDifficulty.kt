package com.example.hiddenarmy.model

enum class CpuDifficulty(val label: String, val description: String) {
    EASY("Easy", "Casual AI with more random exploration"),
    NORMAL("Normal", "Balanced tactical AI respecting bases"),
    HARD("Hard", "Aggressive tactical AI protecting commander");

    companion object {
        fun fromString(value: String): CpuDifficulty =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: NORMAL
    }
}
