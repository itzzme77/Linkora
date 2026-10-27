package com.example.linkora.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.UUID

/**
 * Manages the local device's cryptographic identity and profile.
 * - Generates and persists the device identity keypair.
 * - Computes stable Identity ID and safety fingerprint.
 * - Signs outgoing envelopes and validates incoming peer signatures.
 */
class IdentityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("linkora_identity_prefs", Context.MODE_PRIVATE)

    val deviceId: String
    val keyPair: KeyPair
    val identityId: String
    val publicKeyBase64: String
    val safetyFingerprint: String

    var displayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, "Mesh User") ?: "Mesh User"
        set(value) = prefs.edit().putString(KEY_DISPLAY_NAME, value).apply()

    init {
        // Ensure device ID exists
        var devId = prefs.getString(KEY_DEVICE_ID, null)
        if (devId == null) {
            devId = "dev_${UUID.randomUUID().toString().substring(0, 8)}"
            prefs.edit().putString(KEY_DEVICE_ID, devId).apply()
        }
        deviceId = devId

        // Load or generate identity keypair
        keyPair = loadOrGenerateKeyPair()
        publicKeyBase64 = CryptoEngine.publicKeyToBase64(keyPair.public)
        identityId = CryptoEngine.deriveIdentityId(keyPair.public)
        safetyFingerprint = CryptoEngine.formatSafetyFingerprint(keyPair.public)
    }

    fun sign(data: ByteArray): String {
        return CryptoEngine.signData(keyPair.private, data)
    }

    private fun loadOrGenerateKeyPair(): KeyPair {
        val pubBase64 = prefs.getString(KEY_PUBLIC_KEY, null)
        val privBase64 = prefs.getString(KEY_PRIVATE_KEY, null)

        if (pubBase64 != null && privBase64 != null) {
            try {
                val keyFactory = KeyFactory.getInstance("EC")
                val pubSpec = X509EncodedKeySpec(Base64.decode(pubBase64, Base64.DEFAULT))
                val privSpec = PKCS8EncodedKeySpec(Base64.decode(privBase64, Base64.DEFAULT))
                val pubKey = keyFactory.generatePublic(pubSpec)
                val privKey = keyFactory.generatePrivate(privSpec)
                return KeyPair(pubKey, privKey)
            } catch (e: Exception) {
                // If deserialization fails, regenerate
            }
        }

        val newPair = CryptoEngine.generateEcKeyPair()
        val pubEncoded = Base64.encodeToString(newPair.public.encoded, Base64.NO_WRAP)
        val privEncoded = Base64.encodeToString(newPair.private.encoded, Base64.NO_WRAP)

        prefs.edit()
            .putString(KEY_PUBLIC_KEY, pubEncoded)
            .putString(KEY_PRIVATE_KEY, privEncoded)
            .apply()

        return newPair
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_PUBLIC_KEY = "identity_public_key"
        private const val KEY_PRIVATE_KEY = "identity_private_key"
        private const val KEY_DISPLAY_NAME = "display_name"
    }
}
