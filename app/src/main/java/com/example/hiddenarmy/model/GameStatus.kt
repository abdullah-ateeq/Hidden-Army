package com.example.hiddenarmy.model

enum class GameStatus {
    PLAYER_TURN,
    CPU_TURN,
    BATTLE,
    PLAYER_WON,
    CPU_WON,
    DRAW;

    val isGameOver: Boolean
        get() = this == PLAYER_WON || this == CPU_WON || this == DRAW
}
