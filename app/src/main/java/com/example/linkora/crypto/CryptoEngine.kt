package com.example.linkora.crypto

import android.util.Base64
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object Base64Compat {
    fun encode(bytes: ByteArray): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(bytes)
        } catch (e: Throwable) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    }

    fun decode(str: String): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(str.trim())
        } catch (e: Throwable) {
            android.util.Base64.decode(str.trim(), android.util.Base64.DEFAULT)
        }
    }
}

/**
 * Robust cryptographic engine for Linkora Offline Mesh Chat.
 * Implements:
 * 1. EC P-256 (secp256r1) key generation for long-term identity & ephemeral sessions.
 * 2. ECDH (Elliptic-curve Diffie-Hellman) shared secret agreement.
 * 3. HKDF (RFC 5869) key derivation to derive 256-bit AES-GCM keys.
 * 4. AES-256-GCM authenticated encryption (AEAD) with 96-bit random nonce & 128-bit auth tag.
 * 5. ECDSA (SHA256withECDSA) digital signatures for message authenticity and delivery receipts.
 * 6. Cryptographic user IDs and formatted safety fingerprints for out-of-band verification.
 */
object CryptoEngine {

    private const val EC_CURVE = "secp256r1"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val AES_KEY_LENGTH = 32

    private val secureRandom = SecureRandom()

    /**
     * Generates a new EC P-256 key pair.
     */
    fun generateEcKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        val ecSpec = ECGenParameterSpec(EC_CURVE)
        kpg.initialize(ecSpec, secureRandom)
        return kpg.generateKeyPair()
    }

    /**
     * Converts a PublicKey to a standard Base64 string (X.509 format).
     */
    fun publicKeyToBase64(publicKey: PublicKey): String {
        return Base64Compat.encode(publicKey.encoded)
    }

    /**
     * Parses a Base64 string back into an EC PublicKey.
     */
    fun base64ToPublicKey(base64: String): PublicKey {
        val keyBytes = Base64Compat.decode(base64)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * Derives a stable Identity ID from a public key using SHA-256.
     * Format: "usr_" + first 16 hex chars of the digest.
     */
    fun deriveIdentityId(publicKey: PublicKey): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(publicKey.encoded)
        val hex = digest.joinToString("") { "%02x".format(it) }
        return "usr_${hex.substring(0, 16)}"
    }

    /**
     * Formats a human-readable safety fingerprint for out-of-band QR or visual comparison.
     * Groups SHA-256 into 6 blocks of 4 hex digits: "XXXX XXXX XXXX XXXX XXXX XXXX".
     */
    fun formatSafetyFingerprint(publicKey: PublicKey): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(publicKey.encoded)
        val hex = digest.joinToString("") { "%02X".format(it) }
        return hex.substring(0, 24).chunked(4).joinToString(" ")
    }

    /**
     * Hybrid E2EE: Encrypts plaintext message for recipient's public key.
     *
     * Steps:
     * 1. Sender generates an ephemeral EC key pair.
     * 2. Performs ECDH between ephemeral private key and recipient's public key -> shared secret.
     * 3. Derives 256-bit AES key using HKDF-SHA256 with salt.
     * 4. Encrypts plaintext using AES-256-GCM.
     * 5. Returns packaged ciphertext containing ephemeral public key + IV + ciphertext.
     */
    fun encryptForRecipient(recipientPublicKey: PublicKey, plaintext: String): EncryptedPayload {
        val ephemeralKeyPair = generateEcKeyPair()

        // 1. ECDH key agreement
        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(ephemeralKeyPair.private)
        ka.doPhase(recipientPublicKey, true)
        val sharedSecret = ka.generateSecret()

        // 2. Generate random 96-bit IV and 128-bit salt
        val iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }
        val salt = ByteArray(16).also { secureRandom.nextBytes(it) }

        // 3. HKDF key derivation
        val aesKey = deriveAesKey(sharedSecret, salt, "Linkora-E2EE-v1".toByteArray(Charsets.UTF_8))

        // 4. AES-256-GCM encryption
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"), gcmSpec)
        val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return EncryptedPayload(
            ephemeralPublicKeyBase64 = publicKeyToBase64(ephemeralKeyPair.public),
            saltBase64 = Base64Compat.encode(salt),
            ivBase64 = Base64Compat.encode(iv),
            ciphertextBase64 = Base64Compat.encode(cipherBytes)
        )
    }

    /**
     * Decrypts an EncryptedPayload using recipient's private key.
     * Validates authentication tag; throws GeneralSecurityException if modified or unauthorized.
     */
    fun decryptFromSender(recipientPrivateKey: PrivateKey, payload: EncryptedPayload): String {
        val ephemeralPublicKey = base64ToPublicKey(payload.ephemeralPublicKeyBase64)
        val salt = Base64Compat.decode(payload.saltBase64)
        val iv = Base64Compat.decode(payload.ivBase64)
        val cipherBytes = Base64Compat.decode(payload.ciphertextBase64)

        // 1. ECDH key agreement
        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(recipientPrivateKey)
        ka.doPhase(ephemeralPublicKey, true)
        val sharedSecret = ka.generateSecret()

        // 2. HKDF key derivation
        val aesKey = deriveAesKey(sharedSecret, salt, "Linkora-E2EE-v1".toByteArray(Charsets.UTF_8))

        // 3. AES-256-GCM decryption
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(aesKey, "AES"), gcmSpec)
        val plainBytes = cipher.doFinal(cipherBytes)

        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Signs data using private identity key (SHA256withECDSA).
     */
    fun signData(privateKey: PrivateKey, data: ByteArray): String {
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(privateKey)
        signer.update(data)
        return Base64Compat.encode(signer.sign())
    }

    /**
     * Verifies ECDSA signature using sender's public key.
     */
    fun verifySignature(publicKey: PublicKey, data: ByteArray, signatureBase64: String): Boolean {
        return try {
            val signatureBytes = Base64Compat.decode(signatureBase64)
            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(data)
            verifier.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Simple HKDF-Expand-Extract implementation (RFC 5869) using HmacSHA256.
     */
    private fun deriveAesKey(ikm: ByteArray, salt: ByteArray, info: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")

        // HKDF-Extract: PRK = HMAC-Hash(salt, IKM)
        val saltKey = if (salt.isNotEmpty()) SecretKeySpec(salt, "HmacSHA256") else SecretKeySpec(ByteArray(32), "HmacSHA256")
        mac.init(saltKey)
        val prk = mac.doFinal(ikm)

        // HKDF-Expand: OKM = HMAC-Hash(PRK, info || 0x01)
        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        mac.update(info)
        mac.update(0x01.toByte())
        val okm = mac.doFinal()

        // Return 32 bytes (256-bit AES key)
        return okm.copyOf(AES_KEY_LENGTH)
    }

    /**
     * Generates a random 256-bit symmetric key for a group chat.
     */
    fun generateGroupKey(): String {
        val key = ByteArray(32)
        secureRandom.nextBytes(key)
        return Base64Compat.encode(key)
    }

    /**
     * Encrypts a group message using AES-256-GCM with the shared group key.
     */
    fun encryptGroupMessage(groupKeyBase64: String, plaintext: String): String {
        val keyBytes = Base64Compat.decode(groupKeyBase64)
        val iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), gcmSpec)
        val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return "${Base64Compat.encode(iv)}|${Base64Compat.encode(cipherBytes)}"
    }

    /**
     * Decrypts a group message using AES-256-GCM with the shared group key.
     */
    fun decryptGroupMessage(groupKeyBase64: String, encryptedPayload: String): String {
        val parts = encryptedPayload.split("|")
        if (parts.size != 2) throw IllegalArgumentException("Invalid group payload format")
        val iv = Base64Compat.decode(parts[0])
        val cipherBytes = Base64Compat.decode(parts[1])
        val keyBytes = Base64Compat.decode(groupKeyBase64)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), gcmSpec)
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Container for encrypted payload data sent over the wire.
     */
    data class EncryptedPayload(
        val ephemeralPublicKeyBase64: String,
        val saltBase64: String,
        val ivBase64: String,
        val ciphertextBase64: String
    ) {
        fun serialize(): String {
            return "$ephemeralPublicKeyBase64|$saltBase64|$ivBase64|$ciphertextBase64"
        }

        companion object {
            fun deserialize(raw: String): EncryptedPayload {
                val parts = raw.split("|")
                if (parts.size != 4) {
                    throw IllegalArgumentException("Invalid encrypted payload format")
                }
                return EncryptedPayload(
                    ephemeralPublicKeyBase64 = parts[0],
                    saltBase64 = parts[1],
                    ivBase64 = parts[2],
                    ciphertextBase64 = parts[3]
                )
            }
        }
    }
}
