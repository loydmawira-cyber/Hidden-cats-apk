package com.example.hiddencats.core

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class SoundHapticsManager(context: Context) {
    private val appContext = context.applicationContext

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 75)
    } catch (e: Exception) {
        Log.w("SoundHaptics", "ToneGenerator init error: ${e.message}")
        null
    }

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    fun playCellSelect(sound: Boolean = true, haptics: Boolean = true) {
        if (sound) {
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30) } catch (_: Exception) {}
        }
        if (haptics) vibrate(12)
    }

    fun playCorrect(sound: Boolean = true, haptics: Boolean = true) {
        if (sound) {
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100) } catch (_: Exception) {}
        }
        if (haptics) vibrate(30)
    }

    fun playWrong(sound: Boolean = true, haptics: Boolean = true) {
        if (sound) {
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 140) } catch (_: Exception) {}
        }
        if (haptics) vibrate(75)
    }

    fun playHint(sound: Boolean = true, haptics: Boolean = true) {
        if (sound) {
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 120) } catch (_: Exception) {}
        }
        if (haptics) vibrate(25)
    }

    fun playWin(sound: Boolean = true, haptics: Boolean = true) {
        if (sound) {
            try { toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300) } catch (_: Exception) {}
        }
        if (haptics) vibratePattern(longArrayOf(0, 50, 60, 100))
    }

    private fun vibrate(ms: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(ms)
                }
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {}
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
