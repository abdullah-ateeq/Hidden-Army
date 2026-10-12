package com.example.hiddenarmy.model

enum class AppTheme(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromString(value: String): AppTheme =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
    }
}
