package com.example.linkora.call

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * CallSoundEngine:
 * Handles all acoustic feedback for Linkora encrypted voice calls:
 * - Outgoing call ringback tone (repeating standard telephone ringback cadence)
 * - Incoming call ringtone (system ringtone looping + synchronized vibration)
 * - Busy on other line tone (fast repeated 480Hz+620Hz busy signal)
 * - Call declined / ended disconnect tones
 */
class CallSoundEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var outgoingJob: Job? = null
    private var incomingRingtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    init {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrator initialization error: ${e.message}")
        }
    }

    /**
     * Plays the outgoing call ringback tone (US/ITU standard ring cadence: 2s tone, 3s pause).
     */
    @Synchronized
    fun startOutgoingRingback() {
        stopAll()
        outgoingJob = scope.launch {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 75)
                while (isActive) {
                    toneGen.startTone(ToneGenerator.TONE_SUP_RINGTONE, 2000)
                    delay(4500)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Outgoing ringback error: ${e.message}")
            } finally {
                try {
                    toneGen?.stopTone()
                    toneGen?.release()
                } catch (_: Exception) {}
            }
        }
    }

    private var incomingJob: Job? = null

    /**
     * Plays the incoming call ringtone (classic telephone ring cadence + synchronized phone vibration).
     */
    @Synchronized
    fun startIncomingRinging() {
        stopAll()
        incomingJob = scope.launch {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_RING, 85)
                while (isActive) {
                    // Classic telephone ring: 2000ms ring tone
                    toneGen.startTone(ToneGenerator.TONE_SUP_RINGTONE, 2000)

                    // Synchronized phone vibration pattern (vibrate 800ms, pause 400ms, vibrate 800ms)
                    val pattern = longArrayOf(0, 800, 400, 800)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(pattern, -1)
                    }

                    delay(4000)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Incoming ringing error: ${e.message}")
            } finally {
                try {
                    toneGen?.stopTone()
                    toneGen?.release()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Plays the busy line tone (repeated fast beeps) when peer is on another call or declines.
     */
    fun playBusyTone(onFinish: (() -> Unit)? = null) {
        stopAll()
        scope.launch {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 85)
                toneGen.startTone(ToneGenerator.TONE_SUP_BUSY, 2400)
                delay(2500)
            } catch (e: Exception) {
                Log.w(TAG, "Busy tone error: ${e.message}")
            } finally {
                try {
                    toneGen?.stopTone()
                    toneGen?.release()
                } catch (_: Exception) {}
                onFinish?.invoke()
            }
        }
    }

    /**
     * Plays the short disconnect chime when a call is concluded or declined.
     */
    fun playCallEndedTone(onFinish: (() -> Unit)? = null) {
        stopAll()
        scope.launch {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 500)
                delay(600)
            } catch (e: Exception) {
                Log.w(TAG, "Call ended tone error: ${e.message}")
            } finally {
                try {
                    toneGen?.stopTone()
                    toneGen?.release()
                } catch (_: Exception) {}
                onFinish?.invoke()
            }
        }
    }

    /**
     * Stops all active tones, ringtones, and vibrations immediately.
     */
    @Synchronized
    fun stopAll() {
        outgoingJob?.cancel()
        outgoingJob = null

        incomingJob?.cancel()
        incomingJob = null

        try {
            incomingRingtone?.stop()
        } catch (_: Exception) {}
        incomingRingtone = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
    }

    companion object {
        private const val TAG = "CallSoundEngine"
    }
}
