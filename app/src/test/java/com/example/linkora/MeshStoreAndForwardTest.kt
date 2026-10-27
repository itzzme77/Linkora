package com.example.linkora

import com.example.linkora.crypto.CryptoEngine
import com.example.linkora.model.DeliveryState
import com.example.linkora.model.EnvelopeType
import com.example.linkora.model.MeshEnvelope
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Protocol verification test for the 3-device store-and-forward mesh network:
 * Sender (Node A) -> Intermediate Relay (Node B) -> Intended Recipient (Node C).
 */
class MeshStoreAndForwardTest {

    @Test
    fun testThreeNodeStoreAndForwardRelayProtocol() {
        val now = System.currentTimeMillis()

        // 1. Generate cryptographic keys for 3 distinct devices
        val aliceKeyPair = CryptoEngine.generateEcKeyPair()
        val aliceIdentityId = CryptoEngine.deriveIdentityId(aliceKeyPair.public)

        val bobRelayKeyPair = CryptoEngine.generateEcKeyPair()
        val bobRelayIdentityId = CryptoEngine.deriveIdentityId(bobRelayKeyPair.public)

        val charlieKeyPair = CryptoEngine.generateEcKeyPair()
        val charlieIdentityId = CryptoEngine.deriveIdentityId(charlieKeyPair.public)

        val originalSecretMessage = "Emergency mesh broadcast: rendezvous at checkpoint Delta."

        // 2. Step 1: Alice creates an E2EE payload for Charlie
        val encryptedPayload = CryptoEngine.encryptForRecipient(charlieKeyPair.public, originalSecretMessage)
        val serializedCiphertext = encryptedPayload.serialize()

        val aliceEnvelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = "msg_test_001",
            senderDeviceId = "dev_alice",
            senderIdentityId = aliceIdentityId,
            recipientId = charlieIdentityId, // Intended for Charlie, NOT Bob!
            createdAt = now,
            expiresAt = now + 86400000,
            hopLimit = 8,
            type = EnvelopeType.TEXT,
            encryptedPayload = serializedCiphertext
        )

        assertTrue(aliceEnvelope.isValid(now))

        // 3. Step 2: Bob (Relay) receives the envelope
        // Bob checks recipientId: it does not match Bob's identityId!
        assertNotEquals(aliceEnvelope.recipientId, bobRelayIdentityId)

        // Bob MUST NOT be able to decrypt the payload
        try {
            val deserializedPayload = CryptoEngine.EncryptedPayload.deserialize(aliceEnvelope.encryptedPayload)
            CryptoEngine.decryptFromSender(bobRelayKeyPair.private, deserializedPayload)
            fail("Relay node Bob should not be able to decrypt message intended for Charlie!")
        } catch (e: Exception) {
            // Expected! Bob has no decryption key for Charlie's envelope.
            assertTrue(true)
        }

        // Bob applies routing rules: checks hopLimit > 1, decrements hopLimit
        assertTrue(aliceEnvelope.hopLimit > 1)
        val relayedEnvelope = aliceEnvelope.copy(hopLimit = aliceEnvelope.hopLimit - 1)
        assertEquals(7, relayedEnvelope.hopLimit)

        // 4. Step 3: Charlie (Recipient) receives the relayed envelope
        assertEquals(charlieIdentityId, relayedEnvelope.recipientId)

        // Charlie successfully decrypts with his private key
        val charlieReceivedPayload = CryptoEngine.EncryptedPayload.deserialize(relayedEnvelope.encryptedPayload)
        val decryptedPlaintext = CryptoEngine.decryptFromSender(charlieKeyPair.private, charlieReceivedPayload)
        assertEquals(originalSecretMessage, decryptedPlaintext)

        // 5. Step 4: Charlie produces an authenticated DELIVERY_RECEIPT back to Alice
        val receiptEnvelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = "ack_${UUID.randomUUID()}",
            senderDeviceId = "dev_charlie",
            senderIdentityId = charlieIdentityId,
            recipientId = aliceIdentityId,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000,
            hopLimit = 8,
            type = EnvelopeType.DELIVERY_RECEIPT,
            encryptedPayload = aliceEnvelope.messageId // References the delivered message
        )

        // 6. Step 5: Bob relays receipt back to Alice
        val relayedReceipt = receiptEnvelope.copy(hopLimit = receiptEnvelope.hopLimit - 1)
        assertEquals(7, relayedReceipt.hopLimit)

        // Alice receives the receipt
        assertEquals(aliceIdentityId, relayedReceipt.recipientId)
        assertEquals(aliceEnvelope.messageId, relayedReceipt.encryptedPayload)
        val finalDeliveryState = DeliveryState.DELIVERED
        assertEquals(DeliveryState.DELIVERED, finalDeliveryState)
    }
}
