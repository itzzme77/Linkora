package com.example.linkora

import com.example.linkora.model.EnvelopeType
import com.example.linkora.model.MeshEnvelope
import org.junit.Assert.*
import org.junit.Test

class MeshEnvelopeTest {

    @Test
    fun testValidEnvelopeSerialization() {
        val now = System.currentTimeMillis()
        val envelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = "msg_12345",
            senderDeviceId = "dev_sender",
            senderIdentityId = "usr_alice",
            recipientId = "usr_bob",
            createdAt = now,
            expiresAt = now + 60000,
            hopLimit = 8,
            type = EnvelopeType.TEXT,
            encryptedPayload = "mock_ciphertext"
        )

        assertTrue(envelope.isValid(now))

        val json = envelope.toJson()
        val parsed = MeshEnvelope.fromJson(json)

        assertNotNull(parsed)
        assertEquals(envelope.messageId, parsed?.messageId)
        assertEquals(envelope.senderIdentityId, parsed?.senderIdentityId)
        assertEquals(envelope.recipientId, parsed?.recipientId)
        assertEquals(envelope.hopLimit, parsed?.hopLimit)
        assertEquals(envelope.type, parsed?.type)
        assertEquals(envelope.encryptedPayload, parsed?.encryptedPayload)
    }

    @Test
    fun testExpiredEnvelopeIsInvalid() {
        val now = System.currentTimeMillis()
        val expiredEnvelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = "msg_expired",
            senderDeviceId = "dev_sender",
            senderIdentityId = "usr_alice",
            recipientId = "usr_bob",
            createdAt = now - 100000,
            expiresAt = now - 50000, // Expired in the past
            hopLimit = 5,
            type = EnvelopeType.TEXT,
            encryptedPayload = "payload"
        )

        assertFalse(expiredEnvelope.isValid(now))
    }

    @Test
    fun testInvalidHopLimitIsRejected() {
        val now = System.currentTimeMillis()
        val badHopEnvelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = "msg_bad_hop",
            senderDeviceId = "dev_sender",
            senderIdentityId = "usr_alice",
            recipientId = "usr_bob",
            createdAt = now,
            expiresAt = now + 60000,
            hopLimit = -1, // Negative hop limit
            type = EnvelopeType.TEXT,
            encryptedPayload = "payload"
        )

        assertFalse(badHopEnvelope.isValid(now))
    }

    @Test
    fun testOversizedPayloadIsRejected() {
        val now = System.currentTimeMillis()
        val oversizedString = "A".repeat(70000) // Exceeds 64KB max payload safety limit
        val oversizedEnvelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = "msg_large",
            senderDeviceId = "dev_sender",
            senderIdentityId = "usr_alice",
            recipientId = "usr_bob",
            createdAt = now,
            expiresAt = now + 60000,
            hopLimit = 8,
            type = EnvelopeType.TEXT,
            encryptedPayload = oversizedString
        )

        assertFalse(oversizedEnvelope.isValid(now))
    }
}
