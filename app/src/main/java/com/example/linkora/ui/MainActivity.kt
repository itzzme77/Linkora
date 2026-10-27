package com.example.linkora.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import java.io.File
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.linkora.call.ActiveCall
import com.example.linkora.call.CallState
import com.example.linkora.model.ChatMessage
import com.example.linkora.model.Contact
import com.example.linkora.model.DeliveryState
import com.example.linkora.model.MessageType
import com.example.linkora.model.PeerDevice
import com.example.linkora.model.VerificationState
import com.example.linkora.routing.MeshPacketLog
import com.example.linkora.service.MeshChatService
import com.example.linkora.ui.components.GlassBackground
import com.example.linkora.ui.components.GlassBottomBar
import com.example.linkora.ui.components.NavigationTab
import com.example.linkora.ui.screens.chats.ChatsScreen
import com.example.linkora.ui.screens.chats.model.ConversationItem
import com.example.linkora.ui.screens.contacts.ContactsScreen
import com.example.linkora.ui.screens.conversation.ConversationScreen
import com.example.linkora.ui.screens.call.CallScreen
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.crypto.BiometricAuthManager
import com.example.linkora.model.CallLogEntry
import com.example.linkora.model.ContactUtils
import com.example.linkora.crypto.CryptoEngine
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.screens.calls.CallsScreen
import com.example.linkora.ui.theme.LinkoraTypography
import android.nfc.tech.Ndef
import android.util.Log
import com.example.linkora.nfc.LinkoraHceService
import com.example.linkora.ui.screens.dialogs.AddFriendDialog
import com.example.linkora.ui.screens.dialogs.CreateGroupDialog
import com.example.linkora.ui.screens.dialogs.EditProfileDialog
import com.example.linkora.ui.screens.dialogs.MyQrCodeDialog
import com.example.linkora.ui.screens.dialogs.NfcContactShareDialog
import com.example.linkora.ui.screens.profile.ProfileScreen
import com.example.linkora.ui.screens.radar.RadarScreen
import com.example.linkora.ui.theme.LinkoraTheme
import com.example.linkora.notification.LinkoraNotificationHelper
import com.example.linkora.notification.NotificationPreferences
import com.example.linkora.ui.components.GlassNotificationBanner
import com.example.linkora.ui.components.TopNotificationData
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MainActivity: The primary entry point for Linkora.
 * Complete iOS-inspired Glassmorphism UI with real offline mesh networking,
 * personal chats, group chats, QR pairing, NFC NameDrop contact exchange, and self-identity management.
 */
class MainActivity : ComponentActivity(), MeshChatService.ServiceListener {

    private lateinit var meshService: MeshChatService

    // Reactive State
    private var isMeshActiveState by mutableStateOf(false)
    private var messagesTrigger by mutableIntStateOf(0)
    private var peersList = mutableStateListOf<PeerDevice>()
    private var contactsList = mutableStateListOf<Contact>()
    private var conversationsList = mutableStateListOf<ConversationItem>()
    private var callLogsList = mutableStateListOf<CallLogEntry>()
    private var activeConversationId by mutableStateOf<String?>(null)
    private var currentTab by mutableStateOf(NavigationTab.CHATS)
    private var showMeshSettings by mutableStateOf(false)
    private var simulationLogs = mutableStateListOf<String>()

    // App Lock & Biometrics (Feature #45)
    private lateinit var biometricAuthManager: BiometricAuthManager
    private var isAppUnlocked by mutableStateOf(true)

    private val biometricUnlockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isAppUnlocked = true
        } else {
            Toast.makeText(this, "Authentication required to access Linkora", Toast.LENGTH_SHORT).show()
        }
    }

    private fun promptDeviceUnlock() {
        val intent = biometricAuthManager.createConfirmDeviceCredentialIntent()
        if (intent != null) {
            biometricUnlockLauncher.launch(intent)
        } else {
            isAppUnlocked = true
        }
    }

    // Dialog Visibility States
    private var showMyQrDialog by mutableStateOf(false)
    private var showAddFriendDialog by mutableStateOf(false)
    private var showCreateGroupDialog by mutableStateOf(false)
    private var showEditProfileDialog by mutableStateOf(false)
    private var showNfcShareDialog by mutableStateOf(false)
    private var nfcDiscoveredContact by mutableStateOf<Contact?>(null)

    // Heads-Up Top Notification Banner State
    private var topNotificationData by mutableStateOf<TopNotificationData?>(null)

    // Android 13+ Notification Permission Launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // NFC Hardware Adapter
    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null

    // Mutual 2-Way NFC Contact Exchange Receiver
    private val nfcMutualReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            val payload = intent?.getStringExtra(LinkoraHceService.EXTRA_CONTACT_PAYLOAD) ?: return
            runOnUiThread {
                parseAndShowNfcContact(payload)
            }
        }
    }

    // QR Scanner Launcher
    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleScannedQrContent(result.contents)
        }
    }

    // Camera Permission Launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchQrScanner()
        } else {
            Toast.makeText(this, "Camera permission needed to scan QR codes", Toast.LENGTH_SHORT).show()
        }
    }

    // Bluetooth & Nearby Permissions Launcher
    private val meshPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            meshService.startMesh()
            isMeshActiveState = meshService.isMeshActive()
        } else {
            Toast.makeText(this, "Offline messaging requires Bluetooth and Nearby permissions", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio Record Permission Launcher for Voice Calls
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Microphone enabled for encrypted voice calls", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Microphone permission is required for voice calls", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initiateVoiceCall(contact: Contact) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        meshService.callManager.startCall(contact)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        meshService = MeshChatService.getInstance(this)
        meshService.addServiceListener(this)

        // NFC Hardware initialization
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            val nfcIntent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                0
            }
            nfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, flags)
            handleNfcIntent(intent)
        } catch (_: Exception) {}

        reloadAllData()
        meshService.callManager.addListener(object : com.example.linkora.call.CallManagerListener {
            override fun onCallStateChanged(call: ActiveCall?) {
                if (call == null || call.state == CallState.ENDED) {
                    runOnUiThread { reloadAllData() }
                }
            }
        })
        // Connect directly to mesh automatically on application start
        checkAndRequestMeshPermissions(showToastOnStart = false)

        // Initialize Notification channel and check permission
        LinkoraNotificationHelper.initNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        handleIncomingNotificationIntent(intent)

        // Register Mutual NFC Contact Receiver
        val nfcFilter = android.content.IntentFilter(LinkoraHceService.ACTION_NFC_MUTUAL_CONTACT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(nfcMutualReceiver, nfcFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(nfcMutualReceiver, nfcFilter)
        }

        // Biometric Security Initialization (Feature #45)
        biometricAuthManager = BiometricAuthManager(this)
        if (biometricAuthManager.isAppLockEnabled()) {
            isAppUnlocked = false
            promptDeviceUnlock()
        } else {
            isAppUnlocked = true
        }

        setContent {
            LinkoraTheme {
                val activeCall by meshService.callManager.activeCall.collectAsState()

                GlassBackground {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!isAppUnlocked && biometricAuthManager.isAppLockEnabled()) {
                            LockScreenOverlay(onUnlockClick = { promptDeviceUnlock() })
                        } else {
                            MainAppContent()

                            // Heads-Up Top Floating Liquid Glass Notification Banner
                            GlassNotificationBanner(
                                notificationData = topNotificationData,
                                onBannerClick = { convId ->
                                    activeConversationId = convId
                                    topNotificationData = null
                                    LinkoraNotificationHelper.cancelNotification(this@MainActivity, convId)
                                },
                                onDismiss = {
                                    topNotificationData = null
                                },
                                modifier = Modifier.align(Alignment.TopCenter)
                            )

                            // Real-Time Encrypted P2P Voice Call Overlay Screen
                            if (activeCall != null) {
                                CallScreen(
                                    call = activeCall!!,
                                    onAcceptCall = {
                                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            meshService.callManager.acceptCall()
                                        }
                                    },
                                    onRejectCall = {
                                        meshService.callManager.rejectCall()
                                    },
                                    onEndCall = {
                                        meshService.callManager.endCall("Call Ended")
                                    },
                                    onToggleMute = {
                                        meshService.callManager.toggleMute()
                                    },
                                    onToggleSpeaker = {
                                        meshService.callManager.toggleSpeaker()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
            enableNfcReaderMode()
        } catch (_: Exception) {}
        if (!meshService.isMeshActive()) {
            checkAndRequestMeshPermissions(showToastOnStart = false)
        }
        reloadAllData()
    }

    override fun onPause() {
        super.onPause()
        try {
            nfcAdapter?.disableForegroundDispatch(this)
            nfcAdapter?.disableReaderMode(this)
        } catch (_: Exception) {}
    }

    private fun enableNfcReaderMode() {
        try {
            val flags = NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
            nfcAdapter?.enableReaderMode(this, { tag ->
                handleNfcTagDiscovered(tag)
            }, flags, null)
        } catch (_: Exception) {}
    }

    private fun handleNfcTagDiscovered(tag: Tag) {
        // 1. Phone-to-phone Linkora HCE exchange
        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                isoDep.connect()
                val response = isoDep.transceive(LinkoraHceService.SELECT_APDU_COMMAND)
                if (response != null && response.size >= 2) {
                    val sw1 = response[response.size - 2].toInt() and 0xFF
                    val sw2 = response[response.size - 1].toInt() and 0xFF
                    if (sw1 == 0x90 && sw2 == 0x00) {
                        val payloadBytes = response.copyOfRange(0, response.size - 2)
                        val payload = String(payloadBytes, Charsets.UTF_8)

                        // Transmit our identity back to peer so it discovers us simultaneously
                        try {
                            val myPayload = ContactUtils.formatContactPayload(
                                meshService.identityManager.identityId,
                                meshService.identityManager.safetyFingerprint,
                                meshService.identityManager.publicKeyBase64,
                                meshService.identityManager.displayName
                            )
                            val myBytes = myPayload.toByteArray(Charsets.UTF_8)
                            val exchangeCmd = ByteArray(5 + myBytes.size)
                            exchangeCmd[0] = LinkoraHceService.CLA_EXCHANGE
                            exchangeCmd[1] = LinkoraHceService.INS_EXCHANGE
                            exchangeCmd[2] = 0x00.toByte()
                            exchangeCmd[3] = 0x00.toByte()
                            exchangeCmd[4] = (myBytes.size and 0xFF).toByte()
                            System.arraycopy(myBytes, 0, exchangeCmd, 5, myBytes.size)
                            isoDep.transceive(exchangeCmd)
                        } catch (e: Exception) {
                            Log.d("MainActivity", "Exchange APDU error: ${e.message}")
                        }

                        runOnUiThread {
                            parseAndShowNfcContact(payload)
                        }
                        return
                    }
                }
            } catch (e: Exception) {
                Log.d("MainActivity", "IsoDep transceive: ${e.message}")
            } finally {
                try { isoDep.close() } catch (_: Exception) {}
            }
        }

        // 2. Physical NDEF Tag exchange
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val ndefMessage = ndef.ndefMessage
                val record = ndefMessage?.records?.firstOrNull()
                if (record != null) {
                    val rawPayload = String(record.payload, Charsets.UTF_8)
                    val cleanPayload = if (rawPayload.startsWith("\u0002en") || rawPayload.startsWith("\u0002")) {
                        rawPayload.substring(3)
                    } else {
                        rawPayload
                    }
                    runOnUiThread {
                        parseAndShowNfcContact(cleanPayload)
                    }
                    return
                }
            } catch (e: Exception) {
                Log.d("MainActivity", "Ndef read: ${e.message}")
            } finally {
                try { ndef.close() } catch (_: Exception) {}
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
        handleIncomingNotificationIntent(intent)
    }

    private fun handleIncomingNotificationIntent(intent: Intent?) {
        val targetConvId = intent?.getStringExtra(LinkoraNotificationHelper.EXTRA_CONVERSATION_ID)
        if (!targetConvId.isNullOrBlank()) {
            activeConversationId = targetConvId
            LinkoraNotificationHelper.cancelNotification(this, targetConvId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        meshService.removeServiceListener(this)
        try {
            unregisterReceiver(nfcMutualReceiver)
        } catch (_: Exception) {}
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action) {

            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null && rawMessages.isNotEmpty()) {
                val ndefMessage = rawMessages[0] as? NdefMessage
                val record = ndefMessage?.records?.firstOrNull()
                if (record != null) {
                    val payload = String(record.payload, Charsets.UTF_8)
                    val cleanPayload = if (payload.startsWith("\u0002en") || payload.startsWith("\u0002")) {
                        payload.substring(3)
                    } else {
                        payload
                    }
                    parseAndShowNfcContact(cleanPayload)
                    return
                }
            }

            // Tag tapped without NDEF payload - open NFC Touch Share Dialog
            Toast.makeText(this, "NFC Device Detected", Toast.LENGTH_SHORT).show()
            showNfcShareDialog = true
        }
    }

    private fun parseAndShowNfcContact(data: String) {
        try {
            val contact = ContactUtils.parseContactPayload(data)
            if (contact != null) {
                if (contact.identityId == meshService.identityManager.identityId) {
                    Toast.makeText(this, "This is your own device!", Toast.LENGTH_SHORT).show()
                    return
                }
                meshService.saveContact(contact)
                reloadAllData()
                nfcDiscoveredContact = contact
                showNfcShareDialog = true
                Toast.makeText(this, "Verified & Added Friend via NFC: ${contact.username}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "NFC Device Detected", Toast.LENGTH_SHORT).show()
                showNfcShareDialog = true
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error reading NFC: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /* ---------------- Service Callbacks ---------------- */

    override fun onPeersUpdated(peers: List<PeerDevice>) {
        peersList.clear()
        peersList.addAll(peers)
    }

    override fun onConversationsUpdated() {
        reloadAllData()
    }

    override fun onNewChatMessage(message: ChatMessage) {
        reloadAllData()

        if (!message.isOutgoing) {
            val contact = meshService.getContact(message.senderId)
            val group = meshService.getGroup(message.conversationId)
            val senderName = group?.name ?: contact?.username ?: "Mesh Peer ${message.senderId.takeLast(4)}"

            // 1. Android System Tray Notification
            LinkoraNotificationHelper.showNewMessageNotification(this, senderName, message)

            // 2. Heads-Up In-App Top Notification Banner (if enabled)
            if (NotificationPreferences.isHeadsUpBannerEnabled(this)) {
                topNotificationData = TopNotificationData(
                    conversationId = message.conversationId,
                    senderName = senderName,
                    messageText = message.content
                )
            }
        }
    }

    override fun onDeliveryStateChanged(messageId: String, newState: DeliveryState) {
        reloadAllData()
    }

    override fun onPacketLogAdded(log: MeshPacketLog) {
        val line = "[${log.type}] ${log.action} msg:${log.messageId.take(8)} (hops left: ${log.hopLimit})"
        simulationLogs.add(line)
    }

    override fun onCallStateChanged(call: ActiveCall?) {
        // Active call state observed reactively via StateFlow collectAsState in Compose
    }

    private fun reloadAllData() {
        messagesTrigger++
        // Contacts (Only verified friends or active conversations; ambient mesh nodes act as routers without cluttering friend list)
        val dbContacts = meshService.getFriends()
        contactsList.clear()
        contactsList.addAll(dbContacts)

        // Conversations
        val summaries = meshService.getConversations()
        val convItems = summaries.map { summary ->
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(summary.lastTimestamp))
            ConversationItem(
                id = summary.conversationId,
                name = summary.contactName,
                lastMessage = summary.lastMessage,
                timestamp = timeStr,
                unreadCount = 0,
                isGroup = summary.isGroup,
                isOutgoingLast = true,
                isDelivered = summary.deliveryState == DeliveryState.DELIVERED,
                isRead = summary.deliveryState == DeliveryState.DELIVERED,
                isArchived = summary.isArchived
            )
        }
        conversationsList.clear()
        conversationsList.addAll(convItems)

        // Call Logs (Feature #26)
        val logs = meshService.databaseHelper.getCallLogs()
        callLogsList.clear()
        callLogsList.addAll(logs)

        // Peers
        peersList.clear()
        peersList.addAll(meshService.getConnectedPeers())

        // Active State
        isMeshActiveState = meshService.isMeshActive()
    }

    /* ---------------- User Actions ---------------- */

    private fun toggleMesh(enable: Boolean) {
        if (enable) {
            checkAndRequestMeshPermissions(showToastOnStart = true)
        } else {
            meshService.stopMesh()
            isMeshActiveState = false
            Toast.makeText(this, "Mesh networking stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestMeshPermissions(showToastOnStart: Boolean = true) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isEmpty()) {
            if (!meshService.isMeshActive()) {
                meshService.startMesh()
            }
            isMeshActiveState = meshService.isMeshActive()
            if (showToastOnStart) {
                Toast.makeText(this, "Mesh networking started", Toast.LENGTH_SHORT).show()
            }
        } else {
            meshPermissionsLauncher.launch(needed.toTypedArray())
        }
    }

    private fun launchQrScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan friend's Linkora QR Code")
            setCameraId(0)
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        qrScanLauncher.launch(options)
    }

    private fun handleScannedQrContent(qrContent: String) {
        try {
            val contact = ContactUtils.parseContactPayload(qrContent)
            if (contact != null) {
                if (contact.identityId == meshService.identityManager.identityId) {
                    Toast.makeText(this, "This is your own QR code!", Toast.LENGTH_SHORT).show()
                    return
                }

                meshService.saveContact(contact)
                reloadAllData()
                Toast.makeText(this, "Verified & Added Friend: ${contact.username}", Toast.LENGTH_LONG).show()

                // Immediately open personal chat with this friend!
                activeConversationId = contact.identityId
            } else if (qrContent.isNotBlank()) {
                val name = qrContent.trim()
                val id = "usr_" + java.util.UUID.randomUUID().toString().replace("-", "").take(16)
                val mockPair = CryptoEngine.generateEcKeyPair()
                val friendContact = Contact(
                    identityId = id,
                    username = name,
                    publicKeyBase64 = CryptoEngine.publicKeyToBase64(mockPair.public),
                    verificationState = VerificationState.UNVERIFIED,
                    safetyFingerprint = CryptoEngine.formatSafetyFingerprint(mockPair.public),
                    lastSeen = System.currentTimeMillis()
                )
                meshService.saveContact(friendContact)
                reloadAllData()
                Toast.makeText(this, "Added Contact: $name", Toast.LENGTH_SHORT).show()
                activeConversationId = id
            } else {
                Toast.makeText(this, "Please enter a valid friend code or name", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error parsing friend code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createGroupChat(name: String, members: List<Contact>) {
        val group = meshService.createGroup(name, members)
        reloadAllData()
        Toast.makeText(this, "Created group: ${group.name}", Toast.LENGTH_SHORT).show()
        activeConversationId = group.groupId
    }

    /* ---------------- Compose UI Root ---------------- */

    @Composable
    private fun MainAppContent() {
        val activeConvId = activeConversationId

        BackHandler(
            enabled = activeConvId != null || showMeshSettings || showMyQrDialog || showAddFriendDialog ||
                    showCreateGroupDialog || showEditProfileDialog || currentTab != NavigationTab.CHATS
        ) {
            when {
                showMyQrDialog -> showMyQrDialog = false
                showAddFriendDialog -> showAddFriendDialog = false
                showCreateGroupDialog -> showCreateGroupDialog = false
                showEditProfileDialog -> showEditProfileDialog = false
                showMeshSettings -> showMeshSettings = false
                activeConversationId != null -> {
                    activeConversationId = null
                    reloadAllData()
                }
                currentTab != NavigationTab.CHATS -> currentTab = NavigationTab.CHATS
            }
        }

        AnimatedContent(
            targetState = when {
                activeConvId != null -> "conversation"
                showMeshSettings -> "mesh_settings"
                else -> "main_tabs"
            },
            transitionSpec = {
                if (targetState == "conversation") {
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(tween(180)))
                } else if (initialState == "conversation") {
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(tween(180)))
                } else {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(160))
                }
            },
            label = "screenTransition"
        ) { screenState ->
            when (screenState) {
                "conversation" -> {
                    if (activeConvId != null) {
                        val contact = meshService.getContact(activeConvId)
                            ?: contactsList.find { it.identityId == activeConvId }
                        val group = meshService.getGroup(activeConvId)
                        val messages = remember(activeConvId, messagesTrigger) {
                            meshService.getMessages(activeConvId)
                        }
                        val isDirect = peersList.any { peer ->
                            peer.deviceId == activeConvId ||
                            peer.displayName.contains(activeConvId.take(8)) ||
                            (contact != null && (peer.displayName.contains(contact.username) || peer.deviceId == contact.identityId))
                        }

                        // Automatically mark incoming messages as READ upon opening conversation (Feature #20)
                        LaunchedEffect(activeConvId) {
                            meshService.markConversationAsRead(activeConvId)
                            reloadAllData()
                        }

                        ConversationScreen(
                            conversationId = activeConvId,
                            contact = contact,
                            group = group,
                            messages = messages,
                            isDirectlyConnected = isDirect,
                            allContacts = contactsList,
                            allGroups = meshService.getAllGroups(),
                            onVoiceCallClick = if (contact != null) {
                                { initiateVoiceCall(contact) }
                            } else null,
                            onSendMessage = { text ->
                                if (group != null) {
                                    meshService.sendGroupMessage(group.groupId, text)
                                } else if (contact != null) {
                                    meshService.sendMessage(contact, text)
                                }
                                messagesTrigger++
                                reloadAllData()
                            },
                            onSendMedia = { messageType, file, fileName, mimeType, caption ->
                                if (group != null) {
                                    meshService.sendGroupMediaMessage(group.groupId, file, fileName, mimeType, messageType, caption)
                                } else if (contact != null) {
                                    meshService.sendMediaMessage(contact, file, fileName, mimeType, messageType, caption)
                                }
                                messagesTrigger++
                                reloadAllData()
                            },
                            onReactToMessage = { messageId, emoji ->
                                meshService.setMessageReaction(messageId, emoji)
                                messagesTrigger++
                                reloadAllData()
                            },
                            onEditMessage = { messageId, newContent ->
                                meshService.editMessage(messageId, newContent)
                                messagesTrigger++
                                reloadAllData()
                                Toast.makeText(this@MainActivity, "Message edited", Toast.LENGTH_SHORT).show()
                            },
                            onToggleStarMessage = { messageId, isStarred ->
                                meshService.databaseHelper.toggleMessageStarred(messageId, isStarred)
                                messagesTrigger++
                                reloadAllData()
                            },
                            onTogglePinMessage = { messageId, isPinned ->
                                meshService.databaseHelper.toggleMessagePinned(messageId, isPinned)
                                messagesTrigger++
                                reloadAllData()
                            },
                            onForwardMessage = { msg, targetId, isGroupTarget ->
                                if (isGroupTarget) {
                                    if (msg.mediaPath != null && File(msg.mediaPath).exists()) {
                                        val mediaFile = File(msg.mediaPath)
                                        meshService.sendGroupMediaMessage(targetId, mediaFile, mediaFile.name, msg.mimeType ?: "*/*", msg.messageType, msg.content)
                                    } else {
                                        meshService.sendGroupMessage(targetId, msg.content)
                                    }
                                } else {
                                    val targetContact = meshService.getContact(targetId) ?: Contact(
                                        identityId = targetId,
                                        username = "Friend",
                                        publicKeyBase64 = "",
                                        verificationState = VerificationState.UNVERIFIED,
                                        safetyFingerprint = "",
                                        lastSeen = System.currentTimeMillis()
                                    )
                                    if (msg.mediaPath != null && File(msg.mediaPath).exists()) {
                                        val mediaFile = File(msg.mediaPath)
                                        meshService.sendMediaMessage(targetContact, mediaFile, mediaFile.name, msg.mimeType ?: "*/*", msg.messageType, msg.content)
                                    } else {
                                        meshService.sendMessage(targetContact, msg.content)
                                    }
                                }
                                messagesTrigger++
                                reloadAllData()
                                Toast.makeText(this@MainActivity, "Message forwarded", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteMessage = { messageId ->
                                meshService.deleteMessage(messageId)
                                messagesTrigger++
                                reloadAllData()
                            },
                            onClearChat = {
                                meshService.deleteConversation(activeConvId)
                                messagesTrigger++
                                reloadAllData()
                            },
                            onBackClick = {
                                activeConversationId = null
                                reloadAllData()
                            }
                        )
                    }
                }

                "mesh_settings" -> {
                    RadarScreen(
                        isMeshActive = isMeshActiveState,
                        peers = peersList,
                        onToggleMesh = { toggleMesh(it) },
                        onConnectPeer = { endpointId ->
                            meshService.connectToPeer(endpointId)
                            Toast.makeText(this@MainActivity, "Connecting to node...", Toast.LENGTH_SHORT).show()
                        },
                        onBackClick = {
                            showMeshSettings = false
                        }
                    )
                }

                else -> {
                    // Main Bottom Tabbed Navigation Views
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                            label = "tabContent"
                        ) { tab ->
                            when (tab) {
                                NavigationTab.CHATS -> {
                                    ChatsScreen(
                                        conversations = conversationsList,
                                        contacts = contactsList,
                                        isMeshActive = isMeshActiveState,
                                        connectedPeersCount = peersList.size,
                                        onConversationClick = { conv ->
                                            activeConversationId = conv.id
                                        },
                                        onContactClick = { contactId ->
                                            activeConversationId = contactId
                                        },
                                        onArchiveConversation = { convId, isArchived ->
                                            if (isArchived) {
                                                meshService.databaseHelper.archiveConversation(convId)
                                            } else {
                                                meshService.databaseHelper.unarchiveConversation(convId)
                                            }
                                            reloadAllData()
                                            Toast.makeText(
                                                this@MainActivity,
                                                if (isArchived) "Chat archived" else "Chat unarchived",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onDeleteConversation = { convId ->
                                            meshService.deleteConversation(convId)
                                            reloadAllData()
                                            Toast.makeText(this@MainActivity, "Conversation deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        onNewChatClick = {
                                            showAddFriendDialog = true
                                        },
                                        onShowMyQr = {
                                            showMyQrDialog = true
                                        },
                                        onScanQrClick = {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    )
                                }

                                NavigationTab.CALLS -> {
                                    CallsScreen(
                                        callLogs = callLogsList,
                                        onCallClick = { log ->
                                            val contact = meshService.getContact(log.peerId)
                                                ?: Contact(
                                                    identityId = log.peerId,
                                                    username = log.peerName,
                                                    publicKeyBase64 = "",
                                                    verificationState = VerificationState.UNVERIFIED,
                                                    safetyFingerprint = "",
                                                    lastSeen = System.currentTimeMillis()
                                                )
                                            initiateVoiceCall(contact)
                                        },
                                        onClearLogs = {
                                            meshService.databaseHelper.clearCallLogs()
                                            reloadAllData()
                                            Toast.makeText(this@MainActivity, "Call logs cleared", Toast.LENGTH_SHORT).show()
                                        },
                                        onVoiceCallClick = {
                                            currentTab = NavigationTab.CONTACTS
                                        }
                                    )
                                }

                                NavigationTab.CONTACTS -> {
                                    ContactsScreen(
                                        contacts = contactsList,
                                        identityManager = meshService.identityManager,
                                        onContactClick = { contact ->
                                            activeConversationId = contact.identityId
                                        },
                                        onShowMyQr = {
                                            showMyQrDialog = true
                                        },
                                        onAddFriendClick = {
                                            showAddFriendDialog = true
                                        },
                                        onCreateGroupClick = {
                                            showCreateGroupDialog = true
                                        },
                                        onNfcShareClick = {
                                            showNfcShareDialog = true
                                        }
                                    )
                                }

                                NavigationTab.PROFILE -> {
                                    ProfileScreen(
                                        identityManager = meshService.identityManager,
                                        displayName = meshService.identityManager.displayName,
                                        isMeshActive = isMeshActiveState,
                                        connectedPeersCount = peersList.size,
                                        onEditNameClick = {
                                            showEditProfileDialog = true
                                        },
                                        onShowMyQr = {
                                            showMyQrDialog = true
                                        },
                                        onOpenMeshSettings = {
                                            showMeshSettings = true
                                        },
                                        onNfcShareClick = {
                                            showNfcShareDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        // Floating Translucent Glass Bottom Navigation
                        GlassBottomBar(
                            selectedTab = currentTab,
                            onTabSelected = { currentTab = it },
                            onSearchClick = {
                                if (currentTab != NavigationTab.CHATS) {
                                    currentTab = NavigationTab.CHATS
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }

        // Global Dialogs
        if (showMyQrDialog) {
            MyQrCodeDialog(
                identityManager = meshService.identityManager,
                onDismiss = { showMyQrDialog = false }
            )
        }

        if (showAddFriendDialog) {
            AddFriendDialog(
                onScanQrClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onFriendCodeSubmitted = { friendCode ->
                    handleScannedQrContent(friendCode)
                },
                onNfcTouchClick = {
                    showNfcShareDialog = true
                },
                onDismiss = { showAddFriendDialog = false }
            )
        }

        if (showNfcShareDialog) {
            NfcContactShareDialog(
                identityManager = meshService.identityManager,
                discoveredContact = nfcDiscoveredContact,
                nearbyPeers = peersList,
                onScanQrClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onShowMyQrClick = {
                    showMyQrDialog = true
                },
                onContactConnected = { contact ->
                    meshService.saveContact(contact)
                    reloadAllData()
                    activeConversationId = contact.identityId
                },
                onVoiceCallClick = { contact ->
                    initiateVoiceCall(contact)
                },
                onDismiss = {
                    showNfcShareDialog = false
                    nfcDiscoveredContact = null
                }
            )
        }

        if (showCreateGroupDialog) {
            CreateGroupDialog(
                availableContacts = contactsList,
                onCreateGroup = { name, members ->
                    createGroupChat(name, members)
                },
                onDismiss = { showCreateGroupDialog = false }
            )
        }

        if (showEditProfileDialog) {
            EditProfileDialog(
                currentName = meshService.identityManager.displayName,
                onSaveName = { newName ->
                    meshService.identityManager.displayName = newName
                    meshService.broadcastIdentityAnnouncement()
                    reloadAllData()
                    Toast.makeText(this, "Profile updated: $newName", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showEditProfileDialog = false }
            )
        }
    }

    @Composable
    private fun LockScreenOverlay(onUnlockClick: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A).copy(alpha = 0.96f)),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Linkora Locked",
                        style = LinkoraTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Confirm your fingerprint, face, or passcode to access your offline conversations.",
                        style = LinkoraTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onUnlockClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unlock with Biometrics",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
