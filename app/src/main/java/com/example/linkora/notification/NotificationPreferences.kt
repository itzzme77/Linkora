package com.example.linkora.notification

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user preferences for SMS / chat arrival alerts, heads-up top banners, and vibration.
 */
object NotificationPreferences {
    private const val PREFS_NAME = "linkora_notifications_prefs"
    private const val KEY_MESSAGE_NOTIFICATIONS = "key_message_notifications"
    private const val KEY_HEADS_UP_BANNER = "key_heads_up_banner"
    private const val KEY_VIBRATION_SOUND = "key_vibration_sound"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isMessageNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_MESSAGE_NOTIFICATIONS, true)
    }

    fun setMessageNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_MESSAGE_NOTIFICATIONS, enabled).apply()
    }

    fun isHeadsUpBannerEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HEADS_UP_BANNER, true)
    }

    fun setHeadsUpBannerEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HEADS_UP_BANNER, enabled).apply()
    }

    fun isVibrationSoundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIBRATION_SOUND, true)
    }

    fun setVibrationSoundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VIBRATION_SOUND, enabled).apply()
    }
}
