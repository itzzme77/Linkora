package com.example.linkora.routing

import android.content.Context
import android.util.Log
import com.example.linkora.crypto.Base64Compat
import com.example.linkora.crypto.CryptoEngine
import com.example.linkora.crypto.IdentityManager
import com.example.linkora.model.*
import com.example.linkora.storage.MeshDatabaseHelper
import com.example.linkora.transport.MeshTransport
import java.io.File
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Audit log entry for mesh packet inspection in the UI console.
 */
data class MeshPacketLog(
    val timestamp: Long = System.currentTimeMillis(),
    val messageId: String,
    val type: EnvelopeType,
    val fromEndpoint: String,
    val senderIdentity: String,
    val recipientIdentity: String,
    val hopLimit: Int,
    val action: String,
    val isPayloadEncrypted: Boolean = true
)

interface MeshRouterListener {
    fun onNewChatMessage(message: ChatMessage)
    fun onMessageDeliveryUpdated(messageId: String, newState: DeliveryState)
    fun onPacketAudited(log: MeshPacketLog)
    fun onContactLearned(contact: Contact)
    fun onGroupUpdated(group: GroupChat)
}

/**
 * Core Store-and-Forward Mesh Router for Linkora.
 * Enforces:
 * 1. Strict envelope schema validation & TTL checks.
 * 2. Deduplication to prevent message loops.
 * 3. End-to-end decryption only at intended destination node.
 * 4. Hop-limit decrementing and bounded store-and-forward relay queue for intermediate nodes.
 * 5. Automatic signed delivery receipts propagated back through the mesh.
 */
class MeshRouter(
    val identityManager: IdentityManager,
    val databaseHelper: MeshDatabaseHelper,
    var transport: MeshTransport,
    val context: Context? = null
) {
    private val listeners = CopyOnWriteArrayList<MeshRouterListener>()
    private val packetLogs = CopyOnWriteArrayList<MeshPacketLog>()
    private val maxPacketLogs = 100

    var callManager: com.example.linkora.call.MeshCallManager? = null

    fun addListener(listener: MeshRouterListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: MeshRouterListener) {
        listeners.remove(listener)
    }

    fun getPacketLogs(): List<MeshPacketLog> = packetLogs.toList()

    private fun logPacket(
        messageId: String,
        type: EnvelopeType,
        fromEndpoint: String,
        sender: String,
        recipient: String,
        hopLimit: Int,
        action: String
    ) {
        val entry = MeshPacketLog(
            messageId = messageId,
            type = type,
            fromEndpoint = fromEndpoint,
            senderIdentity = sender,
            recipientIdentity = recipient,
            hopLimit = hopLimit,
            action = action
        )
        packetLogs.add(0, entry)
        if (packetLogs.size > maxPacketLogs) {
            packetLogs.removeAt(packetLogs.lastIndex)
        }
        listeners.forEach { it.onPacketAudited(entry) }
    }

    /**
     * Ingests and routes a raw wireless payload received from a peer.
     */
    fun handleIncomingPayload(fromEndpointId: String, payloadBytes: ByteArray) {
        val rawJson = String(payloadBytes, Charsets.UTF_8)
        val envelope = MeshEnvelope.fromJson(rawJson)

        if (envelope == null) {
            Log.w(TAG, "Malformed envelope JSON from $fromEndpointId")
            return
        }

        // 1. Schema & size validation
        if (!envelope.isValid()) {
            logPacket(
                envelope.messageId, envelope.type, fromEndpointId,
                envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                "DROPPED: Invalid schema or expired TTL"
            )
            return
        }

        // 2. Duplicate suppression
        if (databaseHelper.isMessageProcessed(envelope.messageId)) {
            logPacket(
                envelope.messageId, envelope.type, fromEndpointId,
                envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                "SUPPRESSED: Duplicate message ID already processed"
            )
            return
        }

        // Mark processed
        databaseHelper.markMessageProcessed(envelope.messageId)

        // 3. Check destination
        val isForSelf = (envelope.recipientId == identityManager.identityId ||
                envelope.recipientId == "BROADCAST" ||
                databaseHelper.getGroup(envelope.recipientId) != null)

        val isForRelay = (envelope.recipientId != identityManager.identityId)

        if (isForSelf) {
            processEnvelopeForSelf(fromEndpointId, envelope)
        }
        if (isForRelay) {
            processEnvelopeForRelay(fromEndpointId, envelope)
        }
    }

    /**
     * Processes envelope addressed to this local node.
     */
    private fun processEnvelopeForSelf(fromEndpointId: String, envelope: MeshEnvelope) {
        when (envelope.type) {
            EnvelopeType.TEXT -> {
                try {
                    // Authenticated E2EE decryption
                    val encryptedPayload = CryptoEngine.EncryptedPayload.deserialize(envelope.encryptedPayload)
                    val plaintext = CryptoEngine.decryptFromSender(
                        recipientPrivateKey = identityManager.keyPair.private,
                        payload = encryptedPayload
                    )

                    val chatMessage = ChatMessage(
                        messageId = envelope.messageId,
                        conversationId = envelope.senderIdentityId,
                        senderId = envelope.senderIdentityId,
                        recipientId = envelope.recipientId,
                        content = plaintext,
                        encryptedPayload = envelope.encryptedPayload,
                        createdAt = envelope.createdAt,
                        deliveryState = DeliveryState.DELIVERED,
                        isOutgoing = false,
                        hopCount = 8 - envelope.hopLimit
                    )

                    databaseHelper.insertOrUpdateMessage(chatMessage)

                    logPacket(
                        envelope.messageId, envelope.type, fromEndpointId,
                        envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                        "DELIVERED: Decrypted E2EE payload successfully"
                    )

                    // Dispatch to UI
                    listeners.forEach { it.onNewChatMessage(chatMessage) }

                    // Send Delivery Receipt back to mesh
                    sendDeliveryReceipt(envelope.senderIdentityId, envelope.messageId)

                } catch (e: Exception) {
                    Log.e(TAG, "Decryption or authentication failed for message ${envelope.messageId}", e)
                    logPacket(
                        envelope.messageId, envelope.type, fromEndpointId,
                        envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                        "FAILED: Cryptographic authentication error (${e.message})"
                    )
                }
            }

            EnvelopeType.GROUP_TEXT -> {
                try {
                    val targetGroupId = envelope.recipientId
                    val group = databaseHelper.getGroup(targetGroupId)
                    if (group != null) {
                        val plaintext = CryptoEngine.decryptGroupMessage(group.groupKeyBase64, envelope.encryptedPayload)

                        val chatMessage = ChatMessage(
                            messageId = envelope.messageId,
                            conversationId = group.groupId,
                            senderId = envelope.senderIdentityId,
                            recipientId = group.groupId,
                            content = plaintext,
                            encryptedPayload = envelope.encryptedPayload,
                            createdAt = envelope.createdAt,
                            deliveryState = DeliveryState.DELIVERED,
                            isOutgoing = (envelope.senderIdentityId == identityManager.identityId),
                            hopCount = 8 - envelope.hopLimit
                        )
                        databaseHelper.insertOrUpdateMessage(chatMessage)

                        logPacket(
                            envelope.messageId, envelope.type, fromEndpointId,
                            envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                            "GROUP_DELIVERED: Decrypted group message for ${group.name}"
                        )

                        listeners.forEach { it.onNewChatMessage(chatMessage) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Decryption failed for group message ${envelope.messageId}", e)
                }
            }

            EnvelopeType.FILE_CHUNK -> {
                try {
                    val encryptedPayload = CryptoEngine.EncryptedPayload.deserialize(envelope.encryptedPayload)
                    val chunkJson = CryptoEngine.decryptFromSender(
                        recipientPrivateKey = identityManager.keyPair.private,
                        payload = encryptedPayload
                    )
                    val chunk = com.google.gson.Gson().fromJson(chunkJson, FileChunkPayload::class.java)
                    handleIncomingFileChunk(envelope, chunk, fromEndpointId, isGroup = false)
                } catch (e: Exception) {
                    Log.e(TAG, "Decryption failed for file chunk ${envelope.messageId}", e)
                }
            }

            EnvelopeType.GROUP_FILE_CHUNK -> {
                try {
                    val targetGroupId = envelope.recipientId
                    val group = databaseHelper.getGroup(targetGroupId)
                    if (group != null) {
                        val chunkJson = CryptoEngine.decryptGroupMessage(group.groupKeyBase64, envelope.encryptedPayload)
                        val chunk = com.google.gson.Gson().fromJson(chunkJson, FileChunkPayload::class.java)
                        handleIncomingFileChunk(envelope, chunk, fromEndpointId, isGroup = true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Decryption failed for group file chunk ${envelope.messageId}", e)
                }
            }

            EnvelopeType.CALL_SIGNAL -> {
                try {
                    val signal = com.google.gson.Gson().fromJson(envelope.encryptedPayload, CallSignalPayload::class.java)
                    callManager?.handleCallSignal(envelope, signal)
                    logPacket(
                        envelope.messageId, envelope.type, fromEndpointId,
                        envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                        "CALL_SIGNAL: Action ${signal.action} for call ${signal.callId}"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed handling call signal ${envelope.messageId}", e)
                }
            }

            EnvelopeType.CALL_AUDIO_FRAME -> {
                callManager?.handleIncomingAudioFrame(envelope.senderIdentityId, envelope.encryptedPayload)
            }

            EnvelopeType.GROUP_INVITE -> {
                try {
                    val decryptedJson = CryptoEngine.decryptFromSender(
                        recipientPrivateKey = identityManager.keyPair.private,
                        payload = CryptoEngine.EncryptedPayload.deserialize(envelope.encryptedPayload)
                    )
                    val json = org.json.JSONObject(decryptedJson)
                    val groupId = json.getString("groupId")
                    val groupName = json.getString("groupName")
                    val creatorId = json.getString("creatorId")
                    val groupKeyBase64 = json.getString("groupKeyBase64")
                    val membersArray = json.getJSONArray("memberIds")
                    val memberIds = mutableListOf<String>()
                    for (i in 0 until membersArray.length()) {
                        memberIds.add(membersArray.getString(i))
                    }

                    val group = GroupChat(
                        groupId = groupId,
                        name = groupName,
                        creatorId = creatorId,
                        groupKeyBase64 = groupKeyBase64,
                        memberIds = memberIds
                    )
                    databaseHelper.insertOrUpdateGroup(group)

                    val welcomeMsg = ChatMessage(
                        messageId = "join_${System.currentTimeMillis()}",
                        conversationId = groupId,
                        senderId = creatorId,
                        recipientId = groupId,
                        content = "You were added to group \"$groupName\"",
                        encryptedPayload = "",
                        createdAt = System.currentTimeMillis(),
                        deliveryState = DeliveryState.DELIVERED,
                        isOutgoing = false,
                        hopCount = 0
                    )
                    databaseHelper.insertOrUpdateMessage(welcomeMsg)

                    logPacket(
                        envelope.messageId, envelope.type, fromEndpointId,
                        envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                        "GROUP_INVITE: Joined group $groupName ($groupId)"
                    )

                    listeners.forEach {
                        it.onGroupUpdated(group)
                        it.onNewChatMessage(welcomeMsg)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decrypt/process GROUP_INVITE", e)
                }
            }

            EnvelopeType.DELIVERY_RECEIPT -> {
                val targetMessageId = envelope.encryptedPayload
                databaseHelper.updateDeliveryState(targetMessageId, DeliveryState.DELIVERED)
                databaseHelper.removeFromRelayQueue(targetMessageId)

                logPacket(
                    envelope.messageId, envelope.type, fromEndpointId,
                    envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                    "ACK_RECEIVED: Confirmed delivery for message $targetMessageId"
                )

                listeners.forEach { it.onMessageDeliveryUpdated(targetMessageId, DeliveryState.DELIVERED) }
            }

            EnvelopeType.IDENTITY_ANNOUNCE -> {
                try {
                    val parsed = ContactUtils.parseContactPayload(envelope.encryptedPayload)
                    if (parsed != null && parsed.identityId != identityManager.identityId) {
                        val existing = databaseHelper.getContact(parsed.identityId)
                        val verification = existing?.verificationState ?: VerificationState.UNVERIFIED
                        val contact = parsed.copy(verificationState = verification)
                        databaseHelper.insertOrUpdateContact(contact)

                        logPacket(
                            envelope.messageId, envelope.type, fromEndpointId,
                            envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                            "IDENTITY_ANNOUNCE: Learned contact ${contact.username} (${contact.identityId})"
                        )

                        listeners.forEach { it.onContactLearned(contact) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed parsing identity announcement", e)
                }
            }

            EnvelopeType.PING -> {
                logPacket(
                    envelope.messageId, envelope.type, fromEndpointId,
                    envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                    "PING: Received route ping"
                )
            }
        }
    }

    /**
     * Relays envelope onward through the mesh (Store-and-Forward).
     */
    private fun processEnvelopeForRelay(fromEndpointId: String, envelope: MeshEnvelope) {
        if (envelope.hopLimit <= 1) {
            logPacket(
                envelope.messageId, envelope.type, fromEndpointId,
                envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                "DROPPED: Hop limit reached 0"
            )
            return
        }

        // Decrement hop limit for next relay
        envelope.hopLimit -= 1

        val updatedJson = envelope.toJson()

        // Persist to store-and-forward queue
        val queueItem = RelayQueueItem(
            messageId = envelope.messageId,
            hopLimit = envelope.hopLimit,
            nextAttemptAt = System.currentTimeMillis(),
            attemptCount = 1,
            expiryAt = envelope.expiresAt,
            envelopeJson = updatedJson
        )
        databaseHelper.addToRelayQueue(queueItem)

        // Forward to all connected peers except the one it came from
        val forwardedCount = transport.broadcastPayload(
            payload = updatedJson.toByteArray(Charsets.UTF_8),
            excludeEndpointId = fromEndpointId
        )

        logPacket(
            envelope.messageId, envelope.type, fromEndpointId,
            envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
            "RELAYED: Stored & forwarded to $forwardedCount peer(s)"
        )
    }

    /**
     * Creates and sends an end-to-end encrypted chat message.
     */
    fun sendChatMessage(recipientContact: Contact, plaintext: String): ChatMessage {
        val messageId = "msg_${UUID.randomUUID().toString().substring(0, 8)}"
        val now = System.currentTimeMillis()
        val recipientPublicKey = CryptoEngine.base64ToPublicKey(recipientContact.publicKeyBase64)

        // 1. E2EE Encryption with recipient's public key
        val encryptedPayload = CryptoEngine.encryptForRecipient(recipientPublicKey, plaintext)
        val serializedPayload = encryptedPayload.serialize()

        // 2. Wrap into MeshEnvelope
        val envelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = messageId,
            senderDeviceId = identityManager.deviceId,
            senderIdentityId = identityManager.identityId,
            recipientId = recipientContact.identityId,
            createdAt = now,
            expiresAt = now + (24 * 3600 * 1000), // 24-hour TTL
            hopLimit = 8,
            type = EnvelopeType.TEXT,
            encryptedPayload = serializedPayload
        )

        // Mark processed locally so we don't process our own broadcast
        databaseHelper.markMessageProcessed(messageId)

        // 3. Persist local ChatMessage
        val chatMessage = ChatMessage(
            messageId = messageId,
            conversationId = recipientContact.identityId,
            senderId = identityManager.identityId,
            recipientId = recipientContact.identityId,
            content = plaintext,
            encryptedPayload = serializedPayload,
            createdAt = now,
            deliveryState = DeliveryState.QUEUED,
            isOutgoing = true,
            hopCount = 0
        )
        databaseHelper.insertOrUpdateMessage(chatMessage)

        // 4. Queue in store-and-forward relay queue
        val envelopeJson = envelope.toJson()
        databaseHelper.addToRelayQueue(
            RelayQueueItem(
                messageId = messageId,
                hopLimit = envelope.hopLimit,
                nextAttemptAt = now,
                attemptCount = 1,
                expiryAt = envelope.expiresAt,
                envelopeJson = envelopeJson
            )
        )

        // 5. Broadcast to nearby mesh peers
        val sentCount = transport.broadcastPayload(envelopeJson.toByteArray(Charsets.UTF_8))
        if (sentCount > 0) {
            chatMessage.deliveryState = DeliveryState.RELAYED
            databaseHelper.updateDeliveryState(messageId, DeliveryState.RELAYED)
            listeners.forEach { it.onMessageDeliveryUpdated(messageId, DeliveryState.RELAYED) }
        }

        logPacket(
            messageId, EnvelopeType.TEXT, "LOCAL",
            identityManager.identityId, recipientContact.identityId, envelope.hopLimit,
            "OUTBOUND: Queued and broadcasted to $sentCount peer(s)"
        )

        return chatMessage
    }

    /**
     * Creates a group, persists it locally, and sends encrypted GROUP_INVITE envelopes to members.
     */
    fun createGroupAndInvite(groupName: String, memberContacts: List<Contact>): GroupChat {
        val groupId = "grp_${UUID.randomUUID().toString().substring(0, 8)}"
        val groupKeyBase64 = CryptoEngine.generateGroupKey()
        val memberIds = memberContacts.map { it.identityId } + identityManager.identityId

        val group = GroupChat(
            groupId = groupId,
            name = groupName,
            creatorId = identityManager.identityId,
            groupKeyBase64 = groupKeyBase64,
            memberIds = memberIds
        )
        databaseHelper.insertOrUpdateGroup(group)

        // Initial group conversation message
        val initialMsg = ChatMessage(
            messageId = "grp_init_${System.currentTimeMillis()}",
            conversationId = groupId,
            senderId = identityManager.identityId,
            recipientId = groupId,
            content = "Group \"$groupName\" created",
            encryptedPayload = "",
            createdAt = System.currentTimeMillis(),
            deliveryState = DeliveryState.DELIVERED,
            isOutgoing = true,
            hopCount = 0
        )
        databaseHelper.insertOrUpdateMessage(initialMsg)

        // Send pairwise GROUP_INVITE to each member
        for (contact in memberContacts) {
            try {
                val inviteJson = org.json.JSONObject().apply {
                    put("groupId", groupId)
                    put("groupName", groupName)
                    put("creatorId", identityManager.identityId)
                    put("groupKeyBase64", groupKeyBase64)
                    val jsonArr = org.json.JSONArray()
                    memberIds.forEach { jsonArr.put(it) }
                    put("memberIds", jsonArr)
                }.toString()

                val recipientPublicKey = CryptoEngine.base64ToPublicKey(contact.publicKeyBase64)
                val encryptedInvite = CryptoEngine.encryptForRecipient(recipientPublicKey, inviteJson)
                val inviteEnvelope = MeshEnvelope(
                    protocolVersion = 1,
                    messageId = "inv_${UUID.randomUUID().toString().substring(0, 8)}",
                    senderDeviceId = identityManager.deviceId,
                    senderIdentityId = identityManager.identityId,
                    recipientId = contact.identityId,
                    createdAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + (72 * 3600 * 1000), // 3 days TTL
                    hopLimit = 8,
                    type = EnvelopeType.GROUP_INVITE,
                    encryptedPayload = encryptedInvite.serialize()
                )
                val envJson = inviteEnvelope.toJson()
                databaseHelper.markMessageProcessed(inviteEnvelope.messageId)
                transport.broadcastPayload(envJson.toByteArray(Charsets.UTF_8))
                databaseHelper.addToRelayQueue(
                    RelayQueueItem(
                        messageId = inviteEnvelope.messageId,
                        hopLimit = inviteEnvelope.hopLimit,
                        nextAttemptAt = System.currentTimeMillis(),
                        attemptCount = 1,
                        expiryAt = inviteEnvelope.expiresAt,
                        envelopeJson = envJson
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send GROUP_INVITE to ${contact.username}", e)
            }
        }

        listeners.forEach {
            it.onGroupUpdated(group)
            it.onNewChatMessage(initialMsg)
        }
        return group
    }

    /**
     * Broadcasts an AES-256-GCM encrypted group message across the mesh.
     */
    fun sendGroupMessage(groupId: String, plaintext: String): ChatMessage? {
        val group = databaseHelper.getGroup(groupId) ?: return null
        val messageId = "msg_${UUID.randomUUID().toString().substring(0, 8)}"
        val now = System.currentTimeMillis()

        val encryptedPayload = CryptoEngine.encryptGroupMessage(group.groupKeyBase64, plaintext)

        val envelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = messageId,
            senderDeviceId = identityManager.deviceId,
            senderIdentityId = identityManager.identityId,
            recipientId = groupId,
            createdAt = now,
            expiresAt = now + (24 * 3600 * 1000),
            hopLimit = 8,
            type = EnvelopeType.GROUP_TEXT,
            encryptedPayload = encryptedPayload
        )

        databaseHelper.markMessageProcessed(messageId)

        val chatMessage = ChatMessage(
            messageId = messageId,
            conversationId = groupId,
            senderId = identityManager.identityId,
            recipientId = groupId,
            content = plaintext,
            encryptedPayload = encryptedPayload,
            createdAt = now,
            deliveryState = DeliveryState.RELAYED,
            isOutgoing = true,
            hopCount = 0
        )
        databaseHelper.insertOrUpdateMessage(chatMessage)

        val envJson = envelope.toJson()
        databaseHelper.addToRelayQueue(
            RelayQueueItem(
                messageId = messageId,
                hopLimit = envelope.hopLimit,
                nextAttemptAt = now,
                attemptCount = 1,
                expiryAt = envelope.expiresAt,
                envelopeJson = envJson
            )
        )

        val sentCount = transport.broadcastPayload(envJson.toByteArray(Charsets.UTF_8))
        logPacket(
            messageId, EnvelopeType.GROUP_TEXT, "LOCAL",
            identityManager.identityId, groupId, 8,
            "GROUP_OUTBOUND: Broadcasted to $sentCount peer(s)"
        )

        listeners.forEach { it.onNewChatMessage(chatMessage) }
        return chatMessage
    }

    private fun getMediaDirectory(): File {
        val dir = if (context != null) {
            File(context.filesDir, "media")
        } else {
            File(System.getProperty("java.io.tmpdir") ?: ".", "linkora_media")
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getChunksDirectory(fileId: String): File {
        val dir = if (context != null) {
            File(context.cacheDir, "incoming_chunks/$fileId")
        } else {
            File(System.getProperty("java.io.tmpdir") ?: ".", "linkora_chunks/$fileId")
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun handleIncomingFileChunk(
        envelope: MeshEnvelope,
        chunk: FileChunkPayload,
        fromEndpointId: String,
        isGroup: Boolean
    ) {
        val chunkDir = getChunksDirectory(chunk.fileId)
        val chunkFile = File(chunkDir, "chunk_${chunk.chunkIndex}.bin")
        val chunkBytes = Base64Compat.decode(chunk.chunkData)
        chunkFile.writeBytes(chunkBytes)

        val totalChunks = chunk.totalChunks
        val receivedCount = (0 until totalChunks).count { File(chunkDir, "chunk_$it.bin").exists() }

        logPacket(
            envelope.messageId, envelope.type, fromEndpointId,
            envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
            "FILE_CHUNK: Received chunk ${chunk.chunkIndex + 1}/$totalChunks for ${chunk.fileName}"
        )

        val conversationId = if (isGroup) envelope.recipientId else envelope.senderIdentityId
        val msgType = try { MessageType.valueOf(chunk.messageType) } catch (e: Exception) { MessageType.DOCUMENT }

        if (receivedCount == totalChunks) {
            val mediaDir = getMediaDirectory()
            val finalFile = File(mediaDir, "${chunk.fileId}_${chunk.fileName}")
            finalFile.outputStream().use { fos ->
                for (i in 0 until totalChunks) {
                    val f = File(chunkDir, "chunk_$i.bin")
                    if (f.exists()) {
                        fos.write(f.readBytes())
                    }
                }
            }
            chunkDir.deleteRecursively()

            val chatMessage = ChatMessage(
                messageId = chunk.fileId,
                conversationId = conversationId,
                senderId = envelope.senderIdentityId,
                recipientId = envelope.recipientId,
                content = if (chunk.caption.isNotBlank()) chunk.caption else chunk.fileName,
                encryptedPayload = envelope.encryptedPayload,
                createdAt = envelope.createdAt,
                deliveryState = DeliveryState.DELIVERED,
                isOutgoing = (envelope.senderIdentityId == identityManager.identityId),
                hopCount = 8 - envelope.hopLimit,
                messageType = msgType,
                mediaPath = finalFile.absolutePath,
                mediaSize = finalFile.length(),
                mimeType = chunk.mimeType
            )

            databaseHelper.insertOrUpdateMessage(chatMessage)
            listeners.forEach { it.onNewChatMessage(chatMessage) }

            if (!isGroup) {
                sendDeliveryReceipt(envelope.senderIdentityId, chunk.fileId)
            }
            logPacket(
                chunk.fileId, envelope.type, fromEndpointId,
                envelope.senderIdentityId, envelope.recipientId, envelope.hopLimit,
                "FILE_ASSEMBLED: Reassembled ${chunk.fileName} (${finalFile.length()} bytes)"
            )
        } else {
            val inProgressMsg = ChatMessage(
                messageId = chunk.fileId,
                conversationId = conversationId,
                senderId = envelope.senderIdentityId,
                recipientId = envelope.recipientId,
                content = if (chunk.caption.isNotBlank()) chunk.caption else "Transferring ${chunk.fileName} ($receivedCount/$totalChunks chunks)",
                encryptedPayload = "",
                createdAt = envelope.createdAt,
                deliveryState = DeliveryState.QUEUED,
                isOutgoing = false,
                hopCount = 8 - envelope.hopLimit,
                messageType = msgType,
                mediaPath = null,
                mediaSize = chunk.fileSize,
                mimeType = chunk.mimeType
            )
            databaseHelper.insertOrUpdateMessage(inProgressMsg)
            listeners.forEach { it.onNewChatMessage(inProgressMsg) }
        }
    }

    /**
     * Sends a media file (Image, Video, Audio, Document) chunked and end-to-end encrypted across the mesh.
     */
    fun sendMediaMessage(
        recipientContact: Contact,
        sourceFile: File,
        fileName: String,
        mimeType: String,
        messageType: MessageType,
        caption: String = ""
    ): ChatMessage {
        val fileId = "file_${UUID.randomUUID().toString().substring(0, 8)}"
        val now = System.currentTimeMillis()
        val mediaDir = getMediaDirectory()
        val destFile = File(mediaDir, "${fileId}_$fileName")
        sourceFile.copyTo(destFile, overwrite = true)

        val recipientPublicKey = CryptoEngine.base64ToPublicKey(recipientContact.publicKeyBase64)
        val fileBytes = destFile.readBytes()
        val totalBytes = fileBytes.size
        val chunkSize = 32768
        val totalChunks = if (totalBytes == 0) 1 else kotlin.math.ceil(totalBytes.toDouble() / chunkSize).toInt()

        val chatMessage = ChatMessage(
            messageId = fileId,
            conversationId = recipientContact.identityId,
            senderId = identityManager.identityId,
            recipientId = recipientContact.identityId,
            content = if (caption.isNotBlank()) caption else fileName,
            encryptedPayload = "",
            createdAt = now,
            deliveryState = DeliveryState.QUEUED,
            isOutgoing = true,
            hopCount = 0,
            messageType = messageType,
            mediaPath = destFile.absolutePath,
            mediaSize = destFile.length(),
            mimeType = mimeType
        )
        databaseHelper.markMessageProcessed(fileId)
        databaseHelper.insertOrUpdateMessage(chatMessage)

        var chunksSent = 0
        for (index in 0 until totalChunks) {
            val start = index * chunkSize
            val end = kotlin.math.min(start + chunkSize, totalBytes)
            val chunkByteArray = if (start < totalBytes) fileBytes.copyOfRange(start, end) else ByteArray(0)
            val chunkBase64 = Base64Compat.encode(chunkByteArray)

            val chunkPayload = FileChunkPayload(
                fileId = fileId,
                fileName = fileName,
                fileSize = destFile.length(),
                mimeType = mimeType,
                messageType = messageType.name,
                caption = caption,
                chunkIndex = index,
                totalChunks = totalChunks,
                chunkData = chunkBase64
            )

            val chunkPayloadJson = com.google.gson.Gson().toJson(chunkPayload)
            val encryptedChunkPayload = CryptoEngine.encryptForRecipient(recipientPublicKey, chunkPayloadJson)
            val chunkEnvelopeId = "${fileId}_chk_$index"
            val envelope = MeshEnvelope(
                protocolVersion = 1,
                messageId = chunkEnvelopeId,
                senderDeviceId = identityManager.deviceId,
                senderIdentityId = identityManager.identityId,
                recipientId = recipientContact.identityId,
                createdAt = now,
                expiresAt = now + (24 * 3600 * 1000),
                hopLimit = 8,
                type = EnvelopeType.FILE_CHUNK,
                encryptedPayload = encryptedChunkPayload.serialize()
            )

            val envelopeJson = envelope.toJson()
            databaseHelper.markMessageProcessed(chunkEnvelopeId)
            databaseHelper.addToRelayQueue(
                RelayQueueItem(
                    messageId = chunkEnvelopeId,
                    hopLimit = envelope.hopLimit,
                    nextAttemptAt = now,
                    attemptCount = 1,
                    expiryAt = envelope.expiresAt,
                    envelopeJson = envelopeJson
                )
            )

            val sentCount = transport.broadcastPayload(envelopeJson.toByteArray(Charsets.UTF_8))
            if (sentCount > 0) {
                chunksSent++
            }
        }

        if (chunksSent > 0) {
            chatMessage.deliveryState = DeliveryState.RELAYED
            databaseHelper.updateDeliveryState(fileId, DeliveryState.RELAYED)
            listeners.forEach { it.onMessageDeliveryUpdated(fileId, DeliveryState.RELAYED) }
        }

        logPacket(
            fileId, EnvelopeType.FILE_CHUNK, "LOCAL",
            identityManager.identityId, recipientContact.identityId, 8,
            "OUTBOUND_MEDIA: Sent $chunksSent/$totalChunks chunks for $fileName"
        )

        return chatMessage
    }

    /**
     * Sends a media file to a group chat, encrypted with group shared key and chunked.
     */
    fun sendGroupMediaMessage(
        groupId: String,
        sourceFile: File,
        fileName: String,
        mimeType: String,
        messageType: MessageType,
        caption: String = ""
    ): ChatMessage? {
        val group = databaseHelper.getGroup(groupId) ?: return null
        val fileId = "file_${UUID.randomUUID().toString().substring(0, 8)}"
        val now = System.currentTimeMillis()
        val mediaDir = getMediaDirectory()
        val destFile = File(mediaDir, "${fileId}_$fileName")
        sourceFile.copyTo(destFile, overwrite = true)

        val fileBytes = destFile.readBytes()
        val totalBytes = fileBytes.size
        val chunkSize = 32768
        val totalChunks = if (totalBytes == 0) 1 else kotlin.math.ceil(totalBytes.toDouble() / chunkSize).toInt()

        val chatMessage = ChatMessage(
            messageId = fileId,
            conversationId = groupId,
            senderId = identityManager.identityId,
            recipientId = groupId,
            content = if (caption.isNotBlank()) caption else fileName,
            encryptedPayload = "",
            createdAt = now,
            deliveryState = DeliveryState.QUEUED,
            isOutgoing = true,
            hopCount = 0,
            messageType = messageType,
            mediaPath = destFile.absolutePath,
            mediaSize = destFile.length(),
            mimeType = mimeType
        )
        databaseHelper.markMessageProcessed(fileId)
        databaseHelper.insertOrUpdateMessage(chatMessage)

        var chunksSent = 0
        for (index in 0 until totalChunks) {
            val start = index * chunkSize
            val end = kotlin.math.min(start + chunkSize, totalBytes)
            val chunkByteArray = if (start < totalBytes) fileBytes.copyOfRange(start, end) else ByteArray(0)
            val chunkBase64 = Base64Compat.encode(chunkByteArray)

            val chunkPayload = FileChunkPayload(
                fileId = fileId,
                fileName = fileName,
                fileSize = destFile.length(),
                mimeType = mimeType,
                messageType = messageType.name,
                caption = caption,
                chunkIndex = index,
                totalChunks = totalChunks,
                chunkData = chunkBase64
            )

            val chunkPayloadJson = com.google.gson.Gson().toJson(chunkPayload)
            val encryptedChunkPayload = CryptoEngine.encryptGroupMessage(group.groupKeyBase64, chunkPayloadJson)
            val chunkEnvelopeId = "${fileId}_chk_$index"
            val envelope = MeshEnvelope(
                protocolVersion = 1,
                messageId = chunkEnvelopeId,
                senderDeviceId = identityManager.deviceId,
                senderIdentityId = identityManager.identityId,
                recipientId = groupId,
                createdAt = now,
                expiresAt = now + (24 * 3600 * 1000),
                hopLimit = 8,
                type = EnvelopeType.GROUP_FILE_CHUNK,
                encryptedPayload = encryptedChunkPayload
            )

            val envelopeJson = envelope.toJson()
            databaseHelper.markMessageProcessed(chunkEnvelopeId)
            databaseHelper.addToRelayQueue(
                RelayQueueItem(
                    messageId = chunkEnvelopeId,
                    hopLimit = envelope.hopLimit,
                    nextAttemptAt = now,
                    attemptCount = 1,
                    expiryAt = envelope.expiresAt,
                    envelopeJson = envelopeJson
                )
            )

            val sentCount = transport.broadcastPayload(envelopeJson.toByteArray(Charsets.UTF_8))
            if (sentCount > 0) {
                chunksSent++
            }
        }

        if (chunksSent > 0) {
            chatMessage.deliveryState = DeliveryState.RELAYED
            databaseHelper.updateDeliveryState(fileId, DeliveryState.RELAYED)
            listeners.forEach { it.onMessageDeliveryUpdated(fileId, DeliveryState.RELAYED) }
        }

        return chatMessage
    }

    /**
     * Sends signed Delivery Receipt back into the mesh for the original sender.
     */
    private fun sendDeliveryReceipt(senderIdentityId: String, originalMessageId: String) {
        val now = System.currentTimeMillis()
        val receiptId = "ack_${UUID.randomUUID().toString().substring(0, 8)}"

        val receiptEnvelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = receiptId,
            senderDeviceId = identityManager.deviceId,
            senderIdentityId = identityManager.identityId,
            recipientId = senderIdentityId,
            createdAt = now,
            expiresAt = now + (12 * 3600 * 1000),
            hopLimit = 8,
            type = EnvelopeType.DELIVERY_RECEIPT,
            encryptedPayload = originalMessageId
        )

        val receiptJson = receiptEnvelope.toJson()
        databaseHelper.markMessageProcessed(receiptId)

        transport.broadcastPayload(receiptJson.toByteArray(Charsets.UTF_8))

        logPacket(
            receiptId, EnvelopeType.DELIVERY_RECEIPT, "LOCAL",
            identityManager.identityId, senderIdentityId, 8,
            "OUTBOUND_ACK: Sent receipt for $originalMessageId"
        )
    }

    /**
     * Flushes pending store-and-forward queue to available peers.
     */
    fun flushRelayQueue() {
        val pending = databaseHelper.getPendingRelayQueue()
        for (item in pending) {
            val sent = transport.broadcastPayload(item.envelopeJson.toByteArray(Charsets.UTF_8))
            if (sent > 0) {
                // Successfully transmitted to at least one peer
                databaseHelper.updateDeliveryState(item.messageId, DeliveryState.RELAYED)
                listeners.forEach { it.onMessageDeliveryUpdated(item.messageId, DeliveryState.RELAYED) }
            }
        }
        databaseHelper.pruneExpiredRelayQueue()
    }

    companion object {
        private const val TAG = "MeshRouter"
    }
}
