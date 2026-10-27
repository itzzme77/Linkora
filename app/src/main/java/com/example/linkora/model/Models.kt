package com.example.linkora.model

/**
 * Message delivery status.
 */
enum class DeliveryState {
    QUEUED,    // Waiting to be sent to mesh
    RELAYED,   // Picked up by a relay node (1 tick)
    DELIVERED, // Acknowledged by intended recipient (2 ticks)
    READ,      // Read by recipient (2 glowing cyan/emerald ticks)
    FAILED,    // Transmission error
    EXPIRED    // TTL elapsed before delivery
}

/**
 * Contact cryptographic identity verification state.
 */
enum class VerificationState {
    UNVERIFIED,
    VERIFIED,
    CHANGED // Identity key changed without verification - security warning!
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT
}

/**
 * Represents a decrypted chat message for local conversation display.
 */
data class ChatMessage(
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val encryptedPayload: String,
    val createdAt: Long,
    var deliveryState: DeliveryState,
    val isOutgoing: Boolean,
    var hopCount: Int = 0,
    val messageType: MessageType = MessageType.TEXT,
    val mediaPath: String? = null,
    val mediaSize: Long = 0L,
    val mimeType: String? = null,
    var isStarred: Boolean = false,
    var isPinned: Boolean = false,
    var reaction: String? = null,
    var isEdited: Boolean = false
)

/**
 * Call direction for persistent call logging.
 */
enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

/**
 * Represents a persistent call record for call history.
 */
data class CallLogEntry(
    val callId: String,
    val peerId: String,
    val peerName: String,
    val direction: CallDirection,
    val durationSeconds: Int,
    val timestamp: Long,
    val endReason: String? = null
)

/**
 * Represents a known or verified contact.
 */
data class Contact(
    val identityId: String,
    val username: String,
    val publicKeyBase64: String,
    var verificationState: VerificationState,
    val safetyFingerprint: String,
    var lastSeen: Long = System.currentTimeMillis()
)

/**
 * Represents a nearby peer discovered via wireless transport.
 */
data class PeerDevice(
    val endpointId: String,
    val deviceId: String,
    val displayName: String,
    var identityId: String? = null,
    var isConnected: Boolean = false,
    var lastSeen: Long = System.currentTimeMillis(),
    var isSimulated: Boolean = false
)

/**
 * Envelope stored in the store-and-forward relay queue.
 */
data class RelayQueueItem(
    val messageId: String,
    var hopLimit: Int,
    val nextAttemptAt: Long,
    var attemptCount: Int,
    val expiryAt: Long,
    val envelopeJson: String
)

/**
 * Represents an encrypted group chat.
 */
data class GroupChat(
    val groupId: String,
    val name: String,
    val creatorId: String,
    val groupKeyBase64: String,
    val memberIds: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Payload sent inside pairwise-encrypted GROUP_INVITE envelopes.
 */
data class GroupInvite(
    val groupId: String,
    val groupName: String,
    val creatorId: String,
    val groupKeyBase64: String,
    val memberIds: List<String>
)
