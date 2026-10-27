package com.example.linkora.crypto

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity

/**
 * BiometricAuthManager: Handles biometric (fingerprint/face) and device PIN
 * authentication for Linkora App Lock & Private Space.
 */
class BiometricAuthManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

    fun isAppLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun isDeviceSecure(): Boolean {
        return keyguardManager?.isDeviceSecure == true
    }

    fun createConfirmDeviceCredentialIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            keyguardManager?.createConfirmDeviceCredentialIntent(
                "Unlock Linkora",
                "Confirm your fingerprint, face, or passcode to access your offline conversations."
            )
        } else {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "linkora_security_prefs"
        private const val KEY_APP_LOCK_ENABLED = "key_app_lock_enabled"
        const val REQUEST_CODE_BIOMETRIC_UNLOCK = 10099
    }
}
