package com.example.hiddenarmy.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class SoundAndHaptics(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "Vibrator unavailable", e)
            null
        }
    }

    private val toneGenerator: ToneGenerator? by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "ToneGenerator unavailable", e)
            null
        }
    }

    fun playSelection(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_BEEP, 30)
    }

    fun playMove(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_ACK, 40)
    }

    fun playAttack(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateMedium()
        if (soundEnabled) playTone(ToneGenerator.TONE_CDMA_PIP, 80)
    }

    fun playReveal(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateMedium()
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_PROMPT, 60)
    }

    fun playBaseCapture(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateMedium()
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_PROMPT, 120)
    }

    fun playVictory(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateSuccess()
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_ACK, 300)
    }

    fun playDefeat(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateWarning()
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_NACK, 250)
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "Failed playing tone", e)
        }
    }

    private fun vibrateLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "Vibration failed", e)
        }
    }

    private fun vibrateMedium() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "Vibration failed", e)
        }
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "Vibration failed", e)
        }
    }

    private fun vibrateWarning() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 40, 50), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "Vibration failed", e)
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            Log.w("SoundAndHaptics", "ToneGenerator release error", e)
        }
    }
}
