package com.example.hiddenarmy.data

data class UserStatistics(
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val bestScore: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalBattles: Int = 0,
    val totalBasesCaptured: Int = 0
) {
    val winRate: Float
        get() = if (gamesPlayed > 0) (wins.toFloat() / gamesPlayed * 100f) else 0f
}
