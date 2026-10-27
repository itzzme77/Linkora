package com.example.linkora

import com.example.linkora.crypto.CryptoEngine
import org.junit.Assert.*
import org.junit.Test
import java.security.GeneralSecurityException

class CryptoEngineTest {

    @Test
    fun testEcKeyPairGenerationAndSerialization() {
        val keyPair = CryptoEngine.generateEcKeyPair()
        assertNotNull(keyPair.public)
        assertNotNull(keyPair.private)

        val pubBase64 = CryptoEngine.publicKeyToBase64(keyPair.public)
        assertFalse(pubBase64.isBlank())

        val restoredPublicKey = CryptoEngine.base64ToPublicKey(pubBase64)
        assertArrayEquals(keyPair.public.encoded, restoredPublicKey.encoded)
    }

    @Test
    fun testIdentityDerivationAndFingerprint() {
        val keyPair = CryptoEngine.generateEcKeyPair()
        val identityId = CryptoEngine.deriveIdentityId(keyPair.public)
        assertTrue(identityId.startsWith("usr_"))
        assertEquals(20, identityId.length) // "usr_" (4) + 16 hex chars = 20

        val fingerprint = CryptoEngine.formatSafetyFingerprint(keyPair.public)
        // Expected format: "XXXX XXXX XXXX XXXX XXXX XXXX" (6 blocks of 4)
        val blocks = fingerprint.split(" ")
        assertEquals(6, blocks.size)
        blocks.forEach { assertEquals(4, it.length) }
    }

    @Test
    fun testE2EEEncryptionAndDecryption() {
        val aliceKeyPair = CryptoEngine.generateEcKeyPair()
        val bobKeyPair = CryptoEngine.generateEcKeyPair()

        val originalPlaintext = "Hello Bob! This is an offline mesh encrypted secret."

        // Alice encrypts for Bob
        val payload = CryptoEngine.encryptForRecipient(bobKeyPair.public, originalPlaintext)
        assertNotNull(payload)
        assertNotEquals(originalPlaintext, payload.ciphertextBase64)

        // Bob decrypts with his private key
        val decryptedText = CryptoEngine.decryptFromSender(bobKeyPair.private, payload)
        assertEquals(originalPlaintext, decryptedText)
    }

    @Test
    fun testE2EETamperDetectionFailsDecryption() {
        val aliceKeyPair = CryptoEngine.generateEcKeyPair()
        val bobKeyPair = CryptoEngine.generateEcKeyPair()

        val originalPlaintext = "Confidential financial or location metadata."
        val payload = CryptoEngine.encryptForRecipient(bobKeyPair.public, originalPlaintext)

        // Tamper with serialized payload
        val serialized = payload.serialize()
        val tamperedSerialized = serialized.substring(0, serialized.length - 2) + "AA"
        val tamperedPayload = CryptoEngine.EncryptedPayload.deserialize(tamperedSerialized)

        // Decryption should throw an AEAD authentication error
        try {
            CryptoEngine.decryptFromSender(bobKeyPair.private, tamperedPayload)
            fail("Expected decryption of tampered payload to throw GeneralSecurityException")
        } catch (e: GeneralSecurityException) {
            // Expected - authentication tag failed verification
            assertTrue(true)
        }
    }

    @Test
    fun testDigitalSignatures() {
        val keyPair = CryptoEngine.generateEcKeyPair()
        val data = "Important Linkora Mesh Packet".toByteArray(Charsets.UTF_8)

        val signature = CryptoEngine.signData(keyPair.private, data)
        assertFalse(signature.isBlank())

        // Verify valid signature
        val isValid = CryptoEngine.verifySignature(keyPair.public, data, signature)
        assertTrue(isValid)

        // Verify invalid signature against tampered data
        val tamperedData = "Tampered Linkora Mesh Packet".toByteArray(Charsets.UTF_8)
        val isInvalid = CryptoEngine.verifySignature(keyPair.public, tamperedData, signature)
        assertFalse(isInvalid)
    }
}
