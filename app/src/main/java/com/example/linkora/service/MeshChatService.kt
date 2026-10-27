package com.example.linkora.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.linkora.crypto.CryptoEngine
import com.example.linkora.crypto.IdentityManager
import com.example.linkora.model.*
import com.example.linkora.routing.MeshPacketLog
import com.example.linkora.routing.MeshRouter
import com.example.linkora.routing.MeshRouterListener
import com.example.linkora.storage.MeshDatabaseHelper
import com.example.linkora.transport.MeshTransport
import com.example.linkora.transport.MeshTransportListener
import com.example.linkora.transport.NearbyConnectionsTransport
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Main application service coordinating crypto, storage, routing, and wireless transport.
 */
class MeshChatService private constructor(private val context: Context) :
    MeshTransportListener, MeshRouterListener {

    val identityManager = IdentityManager(context)
    val databaseHelper = MeshDatabaseHelper(context)

    private val realTransport = NearbyConnectionsTransport(context)
    var activeTransport: MeshTransport = realTransport
        private set

    val router = MeshRouter(identityManager, databaseHelper, activeTransport, context)
    val callManager = com.example.linkora.call.MeshCallManager(context, identityManager, router, activeTransport, databaseHelper)

    private val serviceListeners = CopyOnWriteArrayList<ServiceListener>()
    private val mainHandler = Handler(Looper.getMainLooper())

    interface ServiceListener {
        fun onPeersUpdated(peers: List<PeerDevice>)
        fun onConversationsUpdated()
        fun onNewChatMessage(message: ChatMessage)
        fun onDeliveryStateChanged(messageId: String, newState: DeliveryState)
        fun onPacketLogAdded(log: MeshPacketLog)
        fun onCallStateChanged(call: com.example.linkora.call.ActiveCall?) {}
    }

    init {
        router.callManager = callManager
        callManager.addListener(object : com.example.linkora.call.CallManagerListener {
            override fun onCallStateChanged(call: com.example.linkora.call.ActiveCall?) {
                serviceListeners.forEach { it.onCallStateChanged(call) }
            }
        })
        realTransport.addListener(this)
        router.addListener(this)
        databaseHelper.purgeDemoData()
    }

    fun addServiceListener(listener: ServiceListener) {
        serviceListeners.add(listener)
    }

    fun removeServiceListener(listener: ServiceListener) {
        serviceListeners.remove(listener)
    }

    private val relayFlushRunnable = object : Runnable {
        override fun run() {
            if (activeTransport.isRunning()) {
                router.flushRelayQueue()
                mainHandler.postDelayed(this, 10000L) // Flush pending store-and-forward relay queue every 10s
            }
        }
    }

    fun startMesh() {
        val uniqueNodeName = "${identityManager.identityId}|${identityManager.displayName}"
        activeTransport.start(uniqueNodeName)
        mainHandler.removeCallbacks(relayFlushRunnable)
        mainHandler.postDelayed(relayFlushRunnable, 5000L)
    }

    fun stopMesh() {
        mainHandler.removeCallbacks(relayFlushRunnable)
        activeTransport.stop()
    }

    fun isMeshActive(): Boolean = activeTransport.isRunning()

    fun getConnectedPeers(): List<PeerDevice> = activeTransport.getConnectedPeers()

    fun sendMessage(recipientContact: Contact, text: String): ChatMessage {
        val message = router.sendChatMessage(recipientContact, text)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
        return message
    }

    fun sendMediaMessage(
        recipientContact: Contact,
        sourceFile: java.io.File,
        fileName: String,
        mimeType: String,
        messageType: MessageType,
        caption: String = ""
    ): ChatMessage {
        val message = router.sendMediaMessage(recipientContact, sourceFile, fileName, mimeType, messageType, caption)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
        return message
    }

    fun sendGroupMediaMessage(
        groupId: String,
        sourceFile: java.io.File,
        fileName: String,
        mimeType: String,
        messageType: MessageType,
        caption: String = ""
    ): ChatMessage? {
        val message = router.sendGroupMediaMessage(groupId, sourceFile, fileName, mimeType, messageType, caption)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
        return message
    }

    fun getConversations(): List<MeshDatabaseHelper.ConversationSummary> {
        return databaseHelper.getAllConversations()
    }

    fun getMessages(conversationId: String): List<ChatMessage> {
        return databaseHelper.getMessagesForConversation(conversationId)
    }

    fun deleteMessage(messageId: String) {
        databaseHelper.deleteMessage(messageId)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
    }

    fun deleteConversation(conversationId: String) {
        databaseHelper.deleteConversation(conversationId)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
    }

    fun setMessageReaction(messageId: String, reaction: String?) {
        databaseHelper.updateMessageReaction(messageId, reaction)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        databaseHelper.editMessageContent(messageId, newContent)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        databaseHelper.markConversationAsRead(conversationId)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
    }

    fun getAllContacts(): List<Contact> {
        return databaseHelper.getAllContacts()
    }

    fun getFriends(): List<Contact> {
        return databaseHelper.getFriends()
    }

    fun saveContact(contact: Contact) {
        databaseHelper.insertOrUpdateContact(contact)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
    }

    fun getContact(identityId: String): Contact? {
        return databaseHelper.getContact(identityId)
    }

    fun createGroup(name: String, members: List<Contact>): GroupChat {
        val group = router.createGroupAndInvite(name, members)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
        return group
    }

    fun sendGroupMessage(groupId: String, text: String): ChatMessage? {
        val msg = router.sendGroupMessage(groupId, text)
        mainHandler.post {
            serviceListeners.forEach { it.onConversationsUpdated() }
        }
        return msg
    }

    fun getGroup(groupId: String): GroupChat? {
        return databaseHelper.getGroup(groupId)
    }

    fun getAllGroups(): List<GroupChat> {
        return databaseHelper.getAllGroups()
    }

    fun connectToPeer(endpointId: String) {
        activeTransport.connect(endpointId)
    }

    /* ---------------- Transport Callbacks ---------------- */

    override fun onPeerDiscovered(peer: PeerDevice) {
        notifyPeersUpdated()
    }

    override fun onPeerConnected(peer: PeerDevice) {
        notifyPeersUpdated()
        // Broadcast identity announcement to newly connected peer
        broadcastIdentityAnnouncement()
        // Flush pending store-and-forward relay queue
        router.flushRelayQueue()
    }

    override fun onPeerDisconnected(endpointId: String) {
        notifyPeersUpdated()
    }

    override fun onPayloadReceived(fromEndpointId: String, payload: ByteArray) {
        router.handleIncomingPayload(fromEndpointId, payload)
    }

    override fun onTransportStateChanged(isAdvertising: Boolean, isDiscovering: Boolean) {
        notifyPeersUpdated()
    }

    override fun onTransportError(message: String) {
        // Log transport error
    }

    fun broadcastIdentityAnnouncement() {
        val announcementPayload = com.example.linkora.model.ContactUtils.formatContactPayload(
            identityManager.identityId,
            identityManager.safetyFingerprint,
            identityManager.publicKeyBase64,
            identityManager.displayName
        )
        val envelope = MeshEnvelope(
            protocolVersion = 1,
            messageId = "id_${System.currentTimeMillis()}",
            senderDeviceId = identityManager.deviceId,
            senderIdentityId = identityManager.identityId,
            recipientId = "BROADCAST",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 3600000,
            hopLimit = 4,
            type = EnvelopeType.IDENTITY_ANNOUNCE,
            encryptedPayload = announcementPayload
        )
        activeTransport.broadcastPayload(envelope.toJson().toByteArray(Charsets.UTF_8))
    }

    private fun notifyPeersUpdated() {
        mainHandler.post {
            val peers = activeTransport.getConnectedPeers()
            serviceListeners.forEach { it.onPeersUpdated(peers) }
        }
    }

    /* ---------------- Router Callbacks ---------------- */

    override fun onNewChatMessage(message: ChatMessage) {
        mainHandler.post {
            serviceListeners.forEach {
                it.onNewChatMessage(message)
                it.onConversationsUpdated()
            }
        }
    }

    override fun onMessageDeliveryUpdated(messageId: String, newState: DeliveryState) {
        mainHandler.post {
            serviceListeners.forEach {
                it.onDeliveryStateChanged(messageId, newState)
                it.onConversationsUpdated()
            }
        }
    }

    override fun onPacketAudited(log: MeshPacketLog) {
        mainHandler.post {
            serviceListeners.forEach { it.onPacketLogAdded(log) }
        }
    }

    override fun onContactLearned(contact: Contact) {
        // Associate connected peer if any matches this contact
        activeTransport.getConnectedPeers().forEach { peer ->
            if (peer.displayName == contact.username || peer.endpointId == contact.identityId || peer.identityId == contact.identityId) {
                peer.identityId = contact.identityId
            }
        }
        mainHandler.post {
            serviceListeners.forEach {
                it.onConversationsUpdated()
                it.onPeersUpdated(activeTransport.getConnectedPeers())
            }
        }
    }

    override fun onGroupUpdated(group: GroupChat) {
        mainHandler.post {
            serviceListeners.forEach {
                it.onConversationsUpdated()
            }
        }
    }


    companion object {
        @Volatile
        private var instance: MeshChatService? = null

        fun getInstance(context: Context): MeshChatService {
            return instance ?: synchronized(this) {
                instance ?: MeshChatService(context.applicationContext).also { instance = it }
            }
        }
    }
}
