package com.example.linkora.ui

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkora.R
import com.example.linkora.model.*
import com.example.linkora.routing.MeshPacketLog
import com.example.linkora.service.MeshChatService
import com.example.linkora.ui.adapters.ChatMessageAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatDetailActivity : AppCompatActivity(), MeshChatService.ServiceListener {

    private lateinit var meshService: MeshChatService
    private var contact: Contact? = null
    private var contactId: String = ""

    private lateinit var rvMessages: RecyclerView
    private lateinit var messageAdapter: ChatMessageAdapter
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var tvContactName: TextView
    private lateinit var tvFingerprint: TextView
    private lateinit var tvVerificationState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_detail)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chatDetailRoot)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        meshService = MeshChatService.getInstance(this)
        contactId = intent.getStringExtra(EXTRA_CONTACT_ID) ?: ""

        initViews()
        loadMessages()
    }

    override fun onStart() {
        super.onStart()
        meshService.addServiceListener(this)
        refreshChatHeader()
    }

    override fun onStop() {
        super.onStop()
        meshService.removeServiceListener(this)
    }

    private fun refreshChatHeader() {
        val group = meshService.getGroup(contactId)
        if (group != null) {
            tvContactName.text = "👥 ${group.name}"
            tvFingerprint.text = "${group.memberIds.size} members • Mesh E2EE"
            tvVerificationState.text = "GROUP"
            tvVerificationState.setTextColor(getColor(R.color.primary))
            return
        }

        contact = meshService.getContact(contactId) ?: meshService.getAllContacts().find { it.identityId == contactId }
        tvContactName.text = contact?.username ?: contactId
        tvFingerprint.text = if (contact != null) "Safety: ${contact?.safetyFingerprint?.take(14)}..." else "Nearby Mesh Peer"
        updateVerificationBadge()
    }

    private fun initViews() {
        rvMessages = findViewById(R.id.rvChatMessages)
        etMessageInput = findViewById(R.id.etMessageInput)
        btnSend = findViewById(R.id.btnSendMessage)
        tvContactName = findViewById(R.id.tvChatContactName)
        tvFingerprint = findViewById(R.id.tvChatFingerprint)
        tvVerificationState = findViewById(R.id.tvChatVerificationState)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        refreshChatHeader()

        tvVerificationState.setOnClickListener {
            contact?.let { c ->
                ContactVerificationDialog.show(this, c) { updated ->
                    meshService.saveContact(updated)
                    contact = updated
                    updateVerificationBadge()
                }
            }
        }

        messageAdapter = ChatMessageAdapter()
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.layoutManager = layoutManager
        rvMessages.adapter = messageAdapter

        btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun updateVerificationBadge() {
        if (meshService.getGroup(contactId) != null) {
            tvVerificationState.text = "GROUP"
            tvVerificationState.setTextColor(getColor(R.color.primary))
            return
        }

        when (contact?.verificationState) {
            VerificationState.VERIFIED -> {
                tvVerificationState.text = "VERIFIED"
                tvVerificationState.setTextColor(getColor(R.color.status_verified))
            }
            VerificationState.UNVERIFIED, null -> {
                tvVerificationState.text = "UNVERIFIED"
                tvVerificationState.setTextColor(getColor(R.color.status_unverified))
            }
            VerificationState.CHANGED -> {
                tvVerificationState.text = "KEY CHANGED"
                tvVerificationState.setTextColor(getColor(R.color.status_changed))
            }
        }
    }

    private fun loadMessages() {
        if (contactId.isNotEmpty()) {
            val list = meshService.getMessages(contactId)
            messageAdapter.submitList(list)
            if (list.isNotEmpty()) {
                rvMessages.scrollToPosition(list.size - 1)
            }
        }
    }

    private fun sendMessage() {
        val text = etMessageInput.text.toString().trim()
        if (text.isEmpty()) return

        val group = meshService.getGroup(contactId)
        if (group != null) {
            meshService.sendGroupMessage(group.groupId, text)
            etMessageInput.text.clear()
            loadMessages()
            return
        }

        val c = contact ?: meshService.getContact(contactId)
        if (c != null) {
            meshService.sendMessage(c, text)
            etMessageInput.text.clear()
            loadMessages()
        } else {
            Toast.makeText(this, "Exchanging keys with peer... Please wait a moment", Toast.LENGTH_SHORT).show()
            meshService.broadcastIdentityAnnouncement()
        }
    }

    /* ---------------- Service Callbacks ---------------- */

    override fun onPeersUpdated(peers: List<PeerDevice>) {}

    override fun onConversationsUpdated() {
        loadMessages()
    }

    override fun onNewChatMessage(message: ChatMessage) {
        if (message.conversationId == contactId) {
            loadMessages()
        }
    }

    override fun onDeliveryStateChanged(messageId: String, newState: DeliveryState) {
        loadMessages()
    }

    override fun onPacketLogAdded(log: MeshPacketLog) {}

    companion object {
        const val EXTRA_CONTACT_ID = "extra_contact_id"
    }
}
