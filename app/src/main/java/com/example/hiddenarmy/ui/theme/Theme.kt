package com.example.hiddenarmy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.hiddenarmy.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = TacticalCyanPrimaryDark,
    onPrimary = TacticalCyanOnPrimaryDark,
    primaryContainer = TacticalCyanContainerDark,
    onPrimaryContainer = TacticalCyanOnContainerDark,
    secondary = TacticalCpuPrimaryDark,
    onSecondary = TacticalCpuOnPrimaryDark,
    secondaryContainer = TacticalCpuContainerDark,
    onSecondaryContainer = TacticalCpuOnContainerDark,
    tertiary = NeutralGoldDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = TacticalCyanPrimaryLight,
    onPrimary = TacticalCyanOnPrimaryLight,
    primaryContainer = TacticalCyanContainerLight,
    onPrimaryContainer = TacticalCyanOnContainerLight,
    secondary = TacticalCpuPrimaryLight,
    onSecondary = TacticalCpuOnPrimaryLight,
    secondaryContainer = TacticalCpuContainerLight,
    onSecondaryContainer = TacticalCpuOnContainerLight,
    tertiary = NeutralGoldLight,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    outline = LightOutline
)

@Composable
fun HiddenArmyTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}