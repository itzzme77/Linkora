package com.example.linkora.model

import com.google.gson.Gson

/**
 * Supported message types in Linkora Mesh protocol.
 */
enum class EnvelopeType {
    TEXT,
    DELIVERY_RECEIPT,
    IDENTITY_ANNOUNCE,
    PING,
    GROUP_TEXT,
    GROUP_INVITE,
    FILE_CHUNK,
    GROUP_FILE_CHUNK,
    CALL_SIGNAL,
    CALL_AUDIO_FRAME
}

/**
 * Signaling payload for live P2P offline voice calls.
 */
data class CallSignalPayload(
    val callId: String,
    val senderIdentityId: String,
    val senderName: String,
    val action: String // "OFFER", "RINGING", "ANSWER", "REJECT", "END"
)

/**
 * Payload carried inside encryptedPayload for FILE_CHUNK envelopes.
 */
data class FileChunkPayload(
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val messageType: String, // "IMAGE", "VIDEO", "AUDIO", "DOCUMENT"
    val caption: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val chunkData: String, // Base64 encoded chunk data
    val checksum: String = ""
)

/**
 * Versioned wire envelope passed between mesh peers.
 * Intermediate relays inspect only the routing header and hop limit,
 * never accessing the encryptedPayload.
 */
data class MeshEnvelope(
    val protocolVersion: Int = 1,
    val messageId: String,
    val senderDeviceId: String,
    val senderIdentityId: String,
    val recipientId: String,
    val createdAt: Long,
    val expiresAt: Long,
    var hopLimit: Int,
    val type: EnvelopeType,
    val encryptedPayload: String,
    val signature: String? = null
) {
    /**
     * Verifies that the envelope complies with schema, size limits, and TTL.
     */
    fun isValid(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        if (protocolVersion != 1) return false
        if (messageId.isBlank() || senderDeviceId.isBlank() || recipientId.isBlank()) return false
        if (hopLimit < 0) return false
        if (currentTimeMs > expiresAt) return false
        // Payload size limit guard (max 64KB for safety in mesh network)
        if (encryptedPayload.length > 65536) return false
        return true
    }

    /**
     * Serializes to JSON for wireless transport.
     */
    fun toJson(): String {
        return gson.toJson(this)
    }

    /**
     * Canonical byte representation used for digital signature verification.
     */
    fun getSignableData(): ByteArray {
        val canonical = "$protocolVersion|$messageId|$senderDeviceId|$senderIdentityId|$recipientId|$createdAt|$expiresAt|$type|$encryptedPayload"
        return canonical.toByteArray(Charsets.UTF_8)
    }

    companion object {
        private val gson = Gson()

        fun fromJson(json: String): MeshEnvelope? {
            return try {
                gson.fromJson(json, MeshEnvelope::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
