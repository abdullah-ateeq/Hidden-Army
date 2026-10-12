package com.example.hiddenarmy.data

import android.content.Context
import android.content.SharedPreferences
import com.example.hiddenarmy.model.AppTheme
import com.example.hiddenarmy.model.CpuDifficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(getSavedTheme())
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _dynamicColor = MutableStateFlow(getSavedDynamicColor())
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _soundEnabled = MutableStateFlow(getSavedSound())
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(getSavedHaptics())
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _cpuDifficulty = MutableStateFlow(getSavedDifficulty())
    val cpuDifficulty: StateFlow<CpuDifficulty> = _cpuDifficulty.asStateFlow()

    private val _battleAnimationEnabled = MutableStateFlow(getSavedBattleAnimation())
    val battleAnimationEnabled: StateFlow<Boolean> = _battleAnimationEnabled.asStateFlow()

    private val _confirmRestart = MutableStateFlow(getSavedConfirmRestart())
    val confirmRestart: StateFlow<Boolean> = _confirmRestart.asStateFlow()

    private val _showRules = MutableStateFlow(getSavedShowRules())
    val showRules: StateFlow<Boolean> = _showRules.asStateFlow()

    private val _statistics = MutableStateFlow(getSavedStatistics())
    val statistics: StateFlow<UserStatistics> = _statistics.asStateFlow()

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _theme.value = theme
    }

    private fun getSavedTheme(): AppTheme {
        val name = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return AppTheme.fromString(name)
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }

    private fun getSavedDynamicColor(): Boolean =
        prefs.getBoolean(KEY_DYNAMIC_COLOR, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundEnabled.value = enabled
    }

    private fun getSavedSound(): Boolean =
        prefs.getBoolean(KEY_SOUND, true)

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _hapticsEnabled.value = enabled
    }

    private fun getSavedHaptics(): Boolean =
        prefs.getBoolean(KEY_HAPTICS, true)

    fun setCpuDifficulty(difficulty: CpuDifficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
        _cpuDifficulty.value = difficulty
    }

    private fun getSavedDifficulty(): CpuDifficulty {
        val name = prefs.getString(KEY_DIFFICULTY, CpuDifficulty.NORMAL.name) ?: CpuDifficulty.NORMAL.name
        return CpuDifficulty.fromString(name)
    }

    fun setBattleAnimationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BATTLE_ANIMATION, enabled).apply()
        _battleAnimationEnabled.value = enabled
    }

    private fun getSavedBattleAnimation(): Boolean =
        prefs.getBoolean(KEY_BATTLE_ANIMATION, true)

    fun setConfirmRestart(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_RESTART, enabled).apply()
        _confirmRestart.value = enabled
    }

    private fun getSavedConfirmRestart(): Boolean =
        prefs.getBoolean(KEY_CONFIRM_RESTART, true)

    fun setShowRules(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_RULES, enabled).apply()
        _showRules.value = enabled
    }

    private fun getSavedShowRules(): Boolean =
        prefs.getBoolean(KEY_SHOW_RULES, true)

    fun getSavedStatistics(): UserStatistics {
        return UserStatistics(
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            wins = prefs.getInt(KEY_WINS, 0),
            losses = prefs.getInt(KEY_LOSSES, 0),
            draws = prefs.getInt(KEY_DRAWS, 0),
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
            bestStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            totalBattles = prefs.getInt(KEY_TOTAL_BATTLES, 0),
            totalBasesCaptured = prefs.getInt(KEY_TOTAL_BASES_CAPTURED, 0)
        )
    }

    fun recordMatchResult(
        isWin: Boolean,
        isLoss: Boolean,
        isDraw: Boolean,
        score: Int,
        battles: Int,
        basesCaptured: Int
    ) {
        val current = getSavedStatistics()
        val newGamesPlayed = current.gamesPlayed + 1
        val newWins = if (isWin) current.wins + 1 else current.wins
        val newLosses = if (isLoss) current.losses + 1 else current.losses
        val newDraws = if (isDraw) current.draws + 1 else current.draws

        val newStreak = when {
            isWin -> current.currentStreak + 1
            isLoss -> 0
            else -> current.currentStreak // draw leaves streak unchanged
        }
        val newBestStreak = maxOf(newStreak, current.bestStreak)
        val newBestScore = maxOf(score, current.bestScore)
        val newTotalBattles = current.totalBattles + battles
        val newTotalBases = current.totalBasesCaptured + basesCaptured

        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, newGamesPlayed)
            .putInt(KEY_WINS, newWins)
            .putInt(KEY_LOSSES, newLosses)
            .putInt(KEY_DRAWS, newDraws)
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .putInt(KEY_BEST_STREAK, newBestStreak)
            .putInt(KEY_BEST_SCORE, newBestScore)
            .putInt(KEY_TOTAL_BATTLES, newTotalBattles)
            .putInt(KEY_TOTAL_BASES_CAPTURED, newTotalBases)
            .apply()

        _statistics.value = UserStatistics(
            gamesPlayed = newGamesPlayed,
            wins = newWins,
            losses = newLosses,
            draws = newDraws,
            bestScore = newBestScore,
            currentStreak = newStreak,
            bestStreak = newBestStreak,
            totalBattles = newTotalBattles,
            totalBasesCaptured = newTotalBases
        )
    }

    fun resetStatistics() {
        prefs.edit()
            .remove(KEY_GAMES_PLAYED)
            .remove(KEY_WINS)
            .remove(KEY_LOSSES)
            .remove(KEY_DRAWS)
            .remove(KEY_CURRENT_STREAK)
            .remove(KEY_BEST_STREAK)
            .remove(KEY_BEST_SCORE)
            .remove(KEY_TOTAL_BATTLES)
            .remove(KEY_TOTAL_BASES_CAPTURED)
            .apply()
        _statistics.value = UserStatistics()
    }

    fun resetSettings() {
        prefs.edit()
            .remove(KEY_THEME)
            .remove(KEY_DYNAMIC_COLOR)
            .remove(KEY_SOUND)
            .remove(KEY_HAPTICS)
            .remove(KEY_DIFFICULTY)
            .remove(KEY_BATTLE_ANIMATION)
            .remove(KEY_CONFIRM_RESTART)
            .remove(KEY_SHOW_RULES)
            .apply()

        _theme.value = AppTheme.SYSTEM
        _dynamicColor.value = true
        _soundEnabled.value = true
        _hapticsEnabled.value = true
        _cpuDifficulty.value = CpuDifficulty.NORMAL
        _battleAnimationEnabled.value = true
        _confirmRestart.value = true
        _showRules.value = true
    }

    companion object {
        private const val PREFS_NAME = "hidden_army_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_SOUND = "sound"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_DIFFICULTY = "cpu_difficulty"
        private const val KEY_BATTLE_ANIMATION = "battle_animation"
        private const val KEY_CONFIRM_RESTART = "confirm_restart"
        private const val KEY_SHOW_RULES = "show_rules"

        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_WINS = "wins"
        private const val KEY_LOSSES = "losses"
        private const val KEY_DRAWS = "draws"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_TOTAL_BATTLES = "total_battles"
        private const val KEY_TOTAL_BASES_CAPTURED = "total_bases_captured"
    }
}
