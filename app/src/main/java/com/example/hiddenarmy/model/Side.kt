package com.example.hiddenarmy.model

enum class Side {
    PLAYER,
    CPU;

    fun opponent(): Side = if (this == PLAYER) CPU else PLAYER
}
