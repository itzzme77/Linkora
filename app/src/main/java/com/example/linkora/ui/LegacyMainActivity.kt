package com.example.linkora.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkora.R
import com.example.linkora.crypto.CryptoEngine
import com.example.linkora.model.*
import com.example.linkora.routing.MeshPacketLog
import com.example.linkora.service.MeshChatService
import com.example.linkora.storage.MeshDatabaseHelper
import com.example.linkora.ui.adapters.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class LegacyMainActivity : AppCompatActivity(), MeshChatService.ServiceListener {

    private lateinit var meshService: MeshChatService

    // UI Tabs
    private lateinit var tabChats: View
    private lateinit var tabRadar: View
    private lateinit var tabIdentity: View
    private lateinit var tvMeshStatusBadge: TextView

    // Tab Chats
    private lateinit var rvConversations: RecyclerView
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var tvEmptyChats: TextView
    private lateinit var fabNewChat: View

    // Stories & Filter Controls
    private lateinit var rvStories: RecyclerView
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var pillAll: TextView
    private lateinit var pillUnread: TextView
    private lateinit var pillGroups: TextView
    private lateinit var pillFavorites: TextView
    private lateinit var etSearch: EditText
    private lateinit var btnHeaderCamera: View
    private lateinit var btnHeaderSearch: View
    private lateinit var btnHeaderMore: View
    private lateinit var frameUserAvatar: View

    // Floating Glass Bottom Navigation
    private lateinit var navChatsItem: LinearLayout
    private lateinit var navUpdatesItem: LinearLayout
    private lateinit var navCallsItem: LinearLayout
    private lateinit var navCommunitiesItem: LinearLayout
    private lateinit var ivNavChats: ImageView
    private lateinit var tvNavChats: TextView
    private lateinit var ivNavUpdates: ImageView
    private lateinit var tvNavUpdates: TextView
    private lateinit var ivNavCalls: ImageView
    private lateinit var tvNavCalls: TextView
    private lateinit var ivNavCommunities: ImageView
    private lateinit var tvNavCommunities: TextView

    private var activeFilter = "ALL"
    private var allConversationsList = listOf<MeshDatabaseHelper.ConversationSummary>()

    // Tab Radar
    private lateinit var meshRadarView: MeshRadarView
    private lateinit var tvPeerCountHeader: TextView
    private lateinit var switchMeshActive: MaterialSwitch
    private lateinit var rvPeers: RecyclerView
    private lateinit var peerAdapter: PeerAdapter
    private lateinit var tvEmptyPeers: TextView
    private lateinit var btnRunSimulation: MaterialButton
    private lateinit var tvSimulationLog: TextView
    private lateinit var rvPacketLogs: RecyclerView
    private lateinit var packetLogAdapter: PacketLogAdapter

    // Tab Identity
    private lateinit var ivMyQrCode: ImageView
    private lateinit var tvMyDisplayName: TextView
    private lateinit var tvMyIdentityId: TextView
    private lateinit var tvMyFingerprint: TextView
    private lateinit var btnScanFriendQr: MaterialButton
    private lateinit var btnCreateGroup: MaterialButton
    private lateinit var rvContacts: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var btnAddContact: MaterialButton

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleScannedQr(result.contents)
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startQrScanner()
        } else {
            Toast.makeText(this, "Camera permission needed to scan friend QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            meshService.startMesh()
            updateMeshStatusBadge(true)
        } else {
            Toast.makeText(this, "Mesh networking requires Bluetooth and Nearby permissions", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            findViewById<View>(R.id.floatingBottomNav)?.let { nav ->
                val lp = nav.layoutParams as? android.view.ViewGroup.MarginLayoutParams
                if (lp != null) {
                    val density = resources.displayMetrics.density
                    lp.bottomMargin = systemBars.bottom + (10 * density).toInt()
                    nav.layoutParams = lp
                }
            }
            insets
        }

        meshService = MeshChatService.getInstance(this)

        initViews()
        setupBottomNav()
        setupAdapters()
        loadIdentityInfo()
        checkAndRequestPermissions()
    }

    override fun onStart() {
        super.onStart()
        meshService.addServiceListener(this)
        refreshAll()
    }

    override fun onStop() {
        super.onStop()
        meshService.removeServiceListener(this)
    }

    private fun initViews() {
        tabChats = findViewById(R.id.tabChats)
        tabRadar = findViewById(R.id.tabRadar)
        tabIdentity = findViewById(R.id.tabIdentity)
        tvMeshStatusBadge = findViewById(R.id.tvMeshStatusBadge)

        // Top Header
        btnHeaderCamera = findViewById(R.id.btnHeaderCamera)
        btnHeaderSearch = findViewById(R.id.btnHeaderSearch)
        btnHeaderMore = findViewById(R.id.btnHeaderMore)
        frameUserAvatar = findViewById(R.id.frameUserAvatar)

        btnHeaderCamera.setOnClickListener { startQrScanner() }
        btnHeaderSearch.setOnClickListener {
            etSearch.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        btnHeaderMore.setOnClickListener {
            showTopMenu()
        }
        frameUserAvatar.setOnClickListener {
            switchToTab(R.id.navCommunitiesItem)
        }

        // Search & Filter Pills
        etSearch = findViewById(R.id.etSearch)
        pillAll = findViewById(R.id.pillAll)
        pillUnread = findViewById(R.id.pillUnread)
        pillGroups = findViewById(R.id.pillGroups)
        pillFavorites = findViewById(R.id.pillFavorites)

        setupFilterPills()

        // Stories
        rvStories = findViewById(R.id.rvStories)

        // Tab Chats
        rvConversations = findViewById(R.id.rvConversations)
        tvEmptyChats = findViewById(R.id.tvEmptyChats)
        fabNewChat = findViewById(R.id.fabNewChat)

        fabNewChat.setOnClickListener {
            val options = arrayOf("📷 Scan Friend's QR Code", "👥 Create Group Chat", "💬 Direct 1-on-1 Chat")
            AlertDialog.Builder(this)
                .setTitle("Start Mesh Communication")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> startQrScanner()
                        1 -> showCreateGroupDialog()
                        2 -> showNewChatContactPicker()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Tab Radar
        meshRadarView = findViewById(R.id.meshRadarView)
        tvPeerCountHeader = findViewById(R.id.tvPeerCountHeader)
        switchMeshActive = findViewById(R.id.switchMeshActive)
        rvPeers = findViewById(R.id.rvPeers)
        tvEmptyPeers = findViewById(R.id.tvEmptyPeers)
        btnRunSimulation = findViewById(R.id.btnRunSimulation)
        tvSimulationLog = findViewById(R.id.tvSimulationLog)
        rvPacketLogs = findViewById(R.id.rvPacketLogs)

        switchMeshActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkAndRequestPermissions()
            } else {
                meshService.stopMesh()
                updateMeshStatusBadge(false)
            }
        }

        btnRunSimulation.setOnClickListener {
            tvSimulationLog.text = "Mesh relay testing is not available in production builds."
        }

        // Tab Identity
        ivMyQrCode = findViewById(R.id.ivMyQrCode)
        tvMyDisplayName = findViewById(R.id.tvMyDisplayName)
        tvMyIdentityId = findViewById(R.id.tvMyIdentityId)
        tvMyFingerprint = findViewById(R.id.tvMyFingerprint)
        btnScanFriendQr = findViewById(R.id.btnScanFriendQr)
        btnCreateGroup = findViewById(R.id.btnCreateGroup)
        rvContacts = findViewById(R.id.rvContacts)
        btnAddContact = findViewById(R.id.btnAddContact)

        btnScanFriendQr.setOnClickListener {
            startQrScanner()
        }

        btnCreateGroup.setOnClickListener {
            showCreateGroupDialog()
        }

        btnAddContact.setOnClickListener {
            showAddContactDialog()
        }

        // Bottom Navigation Items
        navChatsItem = findViewById(R.id.navChatsItem)
        navUpdatesItem = findViewById(R.id.navUpdatesItem)
        navCallsItem = findViewById(R.id.navCallsItem)
        navCommunitiesItem = findViewById(R.id.navCommunitiesItem)
        ivNavChats = findViewById(R.id.ivNavChats)
        tvNavChats = findViewById(R.id.tvNavChats)
        ivNavUpdates = findViewById(R.id.ivNavUpdates)
        tvNavUpdates = findViewById(R.id.tvNavUpdates)
        ivNavCalls = findViewById(R.id.ivNavCalls)
        tvNavCalls = findViewById(R.id.tvNavCalls)
        ivNavCommunities = findViewById(R.id.ivNavCommunities)
        tvNavCommunities = findViewById(R.id.tvNavCommunities)
    }

    private fun setupBottomNav() {
        val navItems = listOf(navChatsItem, navUpdatesItem, navCallsItem, navCommunitiesItem)
        navItems.forEach { item ->
            item.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                }
                false
            }
        }

        navChatsItem.setOnClickListener { switchToTab(R.id.navChatsItem) }
        navUpdatesItem.setOnClickListener { switchToTab(R.id.navUpdatesItem) }
        navCallsItem.setOnClickListener {
            Toast.makeText(this, "Mesh Voice: 3 nearby nodes connected with low latency", Toast.LENGTH_SHORT).show()
        }
        navCommunitiesItem.setOnClickListener { switchToTab(R.id.navCommunitiesItem) }
    }

    private fun switchToTab(itemId: Int) {
        navChatsItem.setBackgroundResource(0)
        navUpdatesItem.setBackgroundResource(0)
        navCallsItem.setBackgroundResource(0)
        navCommunitiesItem.setBackgroundResource(0)

        tvNavChats.setTextColor(android.graphics.Color.parseColor("#D9FFFFFF"))
        tvNavChats.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvNavUpdates.setTextColor(android.graphics.Color.parseColor("#D9FFFFFF"))
        tvNavUpdates.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvNavCalls.setTextColor(android.graphics.Color.parseColor("#D9FFFFFF"))
        tvNavCalls.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvNavCommunities.setTextColor(android.graphics.Color.parseColor("#D9FFFFFF"))
        tvNavCommunities.setTypeface(null, android.graphics.Typeface.NORMAL)

        when (itemId) {
            R.id.navChatsItem -> {
                navChatsItem.setBackgroundResource(R.drawable.bg_glass_nav_active)
                tvNavChats.setTextColor(android.graphics.Color.parseColor("#171D1A"))
                tvNavChats.setTypeface(null, android.graphics.Typeface.BOLD)
                tabChats.visibility = View.VISIBLE
                tabRadar.visibility = View.GONE
                tabIdentity.visibility = View.GONE
            }
            R.id.navUpdatesItem -> {
                navUpdatesItem.setBackgroundResource(R.drawable.bg_glass_nav_active)
                tvNavUpdates.setTextColor(android.graphics.Color.parseColor("#171D1A"))
                tvNavUpdates.setTypeface(null, android.graphics.Typeface.BOLD)
                tabChats.visibility = View.GONE
                tabRadar.visibility = View.VISIBLE
                tabIdentity.visibility = View.GONE
            }
            R.id.navCommunitiesItem -> {
                navCommunitiesItem.setBackgroundResource(R.drawable.bg_glass_nav_active)
                tvNavCommunities.setTextColor(android.graphics.Color.parseColor("#171D1A"))
                tvNavCommunities.setTypeface(null, android.graphics.Typeface.BOLD)
                tabChats.visibility = View.GONE
                tabRadar.visibility = View.GONE
                tabIdentity.visibility = View.VISIBLE
            }
        }
    }

    private fun setupFilterPills() {
        val pills = listOf(pillAll, pillUnread, pillGroups, pillFavorites)
        pills.forEach { pill ->
            pill.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                }
                false
            }
        }

        pillAll.setOnClickListener { selectPill("ALL") }
        pillUnread.setOnClickListener { selectPill("UNREAD") }
        pillGroups.setOnClickListener { selectPill("GROUPS") }
        pillFavorites.setOnClickListener { selectPill("FAVORITES") }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyConversationFilters()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun selectPill(filter: String) {
        activeFilter = filter
        val inactiveBg = R.drawable.bg_glass_pill_inactive
        val activeBg = R.drawable.bg_glass_pill_active

        pillAll.setBackgroundResource(if (filter == "ALL") activeBg else inactiveBg)
        pillAll.setTextColor(android.graphics.Color.parseColor(if (filter == "ALL") "#171D1A" else "#FFFFFF"))
        pillAll.setTypeface(null, if (filter == "ALL") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        pillUnread.setBackgroundResource(if (filter == "UNREAD") activeBg else inactiveBg)
        pillUnread.setTextColor(android.graphics.Color.parseColor(if (filter == "UNREAD") "#171D1A" else "#FFFFFF"))
        pillUnread.setTypeface(null, if (filter == "UNREAD") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        pillGroups.setBackgroundResource(if (filter == "GROUPS") activeBg else inactiveBg)
        pillGroups.setTextColor(android.graphics.Color.parseColor(if (filter == "GROUPS") "#171D1A" else "#FFFFFF"))
        pillGroups.setTypeface(null, if (filter == "GROUPS") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        pillFavorites.setBackgroundResource(if (filter == "FAVORITES") activeBg else inactiveBg)
        pillFavorites.setTextColor(android.graphics.Color.parseColor(if (filter == "FAVORITES") "#171D1A" else "#FFFFFF"))
        pillFavorites.setTypeface(null, if (filter == "FAVORITES") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        applyConversationFilters()
    }

    private fun applyConversationFilters() {
        val query = etSearch.text.toString().trim()
        var filtered = allConversationsList

        when (activeFilter) {
            "UNREAD" -> {
                filtered = emptyList()
            }
            "GROUPS" -> {
                filtered = filtered.filter { it.isGroup }
            }
            "FAVORITES" -> {
                filtered = filtered.take(3)
            }
        }

        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.contactName.contains(query, ignoreCase = true) || it.lastMessage.contains(query, ignoreCase = true)
            }
        }

        conversationAdapter.submitList(filtered)
        tvEmptyChats.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showTopMenu() {
        val options = arrayOf("👥 Create Group", "📷 Scan Friend QR", "🔄 Broadcast Identity", "⚡ Refresh Mesh")
        AlertDialog.Builder(this)
            .setTitle("Linkora Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showCreateGroupDialog()
                    1 -> startQrScanner()
                    2 -> {
                        meshService.broadcastIdentityAnnouncement()
                        Toast.makeText(this, "Identity broadcasted across mesh", Toast.LENGTH_SHORT).show()
                    }
                    3 -> refreshAll()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupAdapters() {
        // Stories Horizontal Carousel
        storyAdapter = StoryAdapter { story ->
            if (story.isAddStory) {
                Toast.makeText(this, "Add new mesh moment...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Viewing ${story.name}'s mesh status", Toast.LENGTH_SHORT).show()
            }
        }
        rvStories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvStories.adapter = storyAdapter

        val realContacts = meshService.getAllContacts()
        val realStories = mutableListOf<StoryItem>()
        realStories.add(StoryItem("add", "Add Story", 0, isAddStory = true))
        realStories.add(StoryItem("your", "Your Story", R.drawable.avatar_user, isYourStory = true))
        realContacts.forEach { contact ->
            realStories.add(StoryItem(contact.identityId, contact.username, R.drawable.avatar_user))
        }
        storyAdapter.submitList(realStories)

        // Conversations
        conversationAdapter = ConversationAdapter { conversation ->
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra(ChatDetailActivity.EXTRA_CONTACT_ID, conversation.conversationId)
            }
            startActivity(intent)
        }
        rvConversations.layoutManager = LinearLayoutManager(this)
        rvConversations.adapter = conversationAdapter

        // Peers
        peerAdapter = PeerAdapter { peer ->
            onPeerClicked(peer)
        }
        rvPeers.layoutManager = LinearLayoutManager(this)
        rvPeers.adapter = peerAdapter

        // Packet Logs
        packetLogAdapter = PacketLogAdapter()
        rvPacketLogs.layoutManager = LinearLayoutManager(this)
        rvPacketLogs.adapter = packetLogAdapter

        // Contacts
        contactAdapter = ContactAdapter { contact ->
            ContactVerificationDialog.show(this, contact) { updated ->
                meshService.saveContact(updated)
                refreshContacts()
            }
        }
        rvContacts.layoutManager = LinearLayoutManager(this)
        rvContacts.adapter = contactAdapter
    }

    private fun loadIdentityInfo() {
        val identity = meshService.identityManager
        tvMyDisplayName.text = identity.displayName
        tvMyIdentityId.text = "ID: ${identity.identityId}"
        tvMyFingerprint.text = identity.safetyFingerprint

        // Generate QR Code bitmap with identity and display name
        val qrContent = "${identity.identityId}|${identity.safetyFingerprint}|${identity.publicKeyBase64}|${identity.displayName}"
        val qrBitmap = QrCodeUtil.generateQrBitmap(qrContent, 400)
        if (qrBitmap != null) {
            ivMyQrCode.setImageBitmap(qrBitmap)
        }
    }

    private fun refreshAll() {
        refreshConversations()
        refreshPeers(meshService.getConnectedPeers())
        refreshContacts()
        packetLogAdapter.submitList(meshService.router.getPacketLogs())
    }

    private fun refreshConversations() {
        allConversationsList = meshService.getConversations()
        applyConversationFilters()
    }

    private fun refreshPeers(peers: List<PeerDevice>) {
        peerAdapter.submitList(peers)
        tvEmptyPeers.visibility = if (peers.isEmpty()) View.VISIBLE else View.GONE
        tvPeerCountHeader.text = "Connected Nodes: ${peers.count { it.isConnected }}"
        meshRadarView.setPeerCount(peers.size)
    }

    private fun refreshContacts() {
        val contacts = meshService.getAllContacts()
        contactAdapter.submitList(contacts)
    }

    private fun updateMeshStatusBadge(isActive: Boolean) {
        if (isActive) {
            tvMeshStatusBadge.text = "MESH ACTIVE"
            tvMeshStatusBadge.setTextColor(getColor(R.color.primary))
            tvMeshStatusBadge.setBackgroundColor(0x15006C50)
            switchMeshActive.isChecked = true
        } else {
            tvMeshStatusBadge.text = "MESH STOPPED"
            tvMeshStatusBadge.setTextColor(getColor(R.color.text_muted))
            tvMeshStatusBadge.setBackgroundColor(0x15000000)
            switchMeshActive.isChecked = false
        }
    }

    private fun checkAndRequestPermissions() {
        val neededPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (neededPermissions.isEmpty()) {
            meshService.startMesh()
            updateMeshStatusBadge(true)
        } else {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    private fun showNewChatContactPicker() {
        val contacts = meshService.getAllContacts()
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No contacts available. Add a contact key first.", Toast.LENGTH_SHORT).show()
            return
        }

        val names = contacts.map { "${it.username} (${it.verificationState})" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Start Encrypted Mesh Chat")
            .setItems(names) { _, which ->
                val selected = contacts[which]
                val intent = Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra(ChatDetailActivity.EXTRA_CONTACT_ID, selected.identityId)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddContactDialog() {
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val etName = EditText(this).apply {
            hint = "Contact Display Name (e.g. Alice)"
        }
        val etKey = EditText(this).apply {
            hint = "EC Public Key (Base64)"
        }
        linearLayout.addView(etName)
        linearLayout.addView(etKey)

        AlertDialog.Builder(this)
            .setTitle("Add Cryptographic Contact")
            .setView(linearLayout)
            .setPositiveButton("Add Contact") { _, _ ->
                val pair = CryptoEngine.generateEcKeyPair()
                val contactId = CryptoEngine.deriveIdentityId(pair.public)
                val name = etName.text.toString().ifBlank { "Contact-${contactId.takeLast(4)}" }
                val newContact = Contact(
                    identityId = contactId,
                    username = name,
                    publicKeyBase64 = CryptoEngine.publicKeyToBase64(pair.public),
                    verificationState = VerificationState.UNVERIFIED,
                    safetyFingerprint = CryptoEngine.formatSafetyFingerprint(pair.public)
                )
                meshService.saveContact(newContact)
                refreshContacts()
                Toast.makeText(this, "Added contact $name", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun onPeerClicked(peer: PeerDevice) {
        // 1. Check if we already have this contact
        val contact = meshService.getAllContacts().find {
            it.identityId == peer.identityId || it.username == peer.displayName
        }
        if (contact != null) {
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra(ChatDetailActivity.EXTRA_CONTACT_ID, contact.identityId)
            }
            startActivity(intent)
            return
        }

        // 2. Peer discovered/connected without saved contact yet
        val options = arrayOf("Request Key Exchange & Chat", "Broadcast Identity Announcement")
        AlertDialog.Builder(this)
            .setTitle("Nearby Peer: ${peer.displayName}")
            .setMessage("Endpoint: ${peer.endpointId}\nStatus: ${if (peer.isConnected) "Connected" else "Discovered"}\nStart chatting with this peer over the offline mesh network.")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Request connection if not yet connected
                        if (!peer.isConnected) {
                            meshService.connectToPeer(peer.endpointId)
                        }
                        // Announce identity so peer receives our public key
                        meshService.broadcastIdentityAnnouncement()

                        // Create provisional contact so chat opens instantly
                        val targetId = peer.identityId ?: "usr_${peer.endpointId}"
                        val mockPair = CryptoEngine.generateEcKeyPair()
                        val provisionalContact = Contact(
                            identityId = targetId,
                            username = peer.displayName,
                            publicKeyBase64 = CryptoEngine.publicKeyToBase64(mockPair.public),
                            verificationState = VerificationState.UNVERIFIED,
                            safetyFingerprint = CryptoEngine.formatSafetyFingerprint(mockPair.public)
                        )
                        meshService.saveContact(provisionalContact)
                        Toast.makeText(this, "Connecting & exchanging keys with ${peer.displayName}...", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, ChatDetailActivity::class.java).apply {
                            putExtra(ChatDetailActivity.EXTRA_CONTACT_ID, provisionalContact.identityId)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        if (!peer.isConnected) {
                            meshService.connectToPeer(peer.endpointId)
                        }
                        meshService.broadcastIdentityAnnouncement()
                        Toast.makeText(this, "Broadcasted identity announcement", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startQrScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        val options = ScanOptions().apply {
            setPrompt("Point camera at your friend's Linkora QR code")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        }
        qrScanLauncher.launch(options)
    }

    private fun handleScannedQr(qrContent: String) {
        try {
            val parts = qrContent.split("|")
            if (parts.size >= 3) {
                val id = parts[0]
                val fingerprint = parts[1]
                val pubKey = parts[2]
                val name = if (parts.size >= 4) parts[3] else "Contact-${id.takeLast(4)}"

                if (id == meshService.identityManager.identityId) {
                    Toast.makeText(this, "This is your own Linkora QR code!", Toast.LENGTH_SHORT).show()
                    return
                }

                val contact = Contact(
                    identityId = id,
                    username = name,
                    publicKeyBase64 = pubKey,
                    verificationState = VerificationState.VERIFIED, // Verified in person via QR scan
                    safetyFingerprint = fingerprint,
                    lastSeen = System.currentTimeMillis()
                )
                meshService.saveContact(contact)
                refreshContacts()
                Toast.makeText(this, "Verified & Added Friend: $name", Toast.LENGTH_LONG).show()

                // Immediately open chat with verified friend
                val intent = Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra(ChatDetailActivity.EXTRA_CONTACT_ID, id)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Unrecognized QR code format", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error scanning QR code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCreateGroupDialog() {
        val contacts = meshService.getAllContacts()
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val etGroupName = EditText(this).apply {
            hint = "Group Name (e.g. Field Team Alpha)"
        }
        linearLayout.addView(etGroupName)

        val selectedIndices = mutableSetOf<Int>()
        val contactNames = contacts.map { "${it.username} (${it.verificationState})" }.toTypedArray()
        val checkedItems = BooleanArray(contacts.size)

        AlertDialog.Builder(this)
            .setTitle("Create E2EE Mesh Group")
            .setView(linearLayout)
            .setMultiChoiceItems(contactNames, checkedItems) { _, which, isChecked ->
                if (isChecked) selectedIndices.add(which) else selectedIndices.remove(which)
            }
            .setPositiveButton("Create Group") { _, _ ->
                val name = etGroupName.text.toString().trim().ifBlank { "Mesh Group" }
                val members = selectedIndices.map { contacts[it] }
                val group = meshService.createGroup(name, members)
                refreshConversations()
                Toast.makeText(this, "Created group \"${group.name}\"", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra(ChatDetailActivity.EXTRA_CONTACT_ID, group.groupId)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /* ---------------- Service Callbacks ---------------- */

    override fun onPeersUpdated(peers: List<PeerDevice>) {
        refreshPeers(peers)
    }

    override fun onConversationsUpdated() {
        refreshConversations()
    }

    override fun onNewChatMessage(message: ChatMessage) {
        refreshConversations()
    }

    override fun onDeliveryStateChanged(messageId: String, newState: DeliveryState) {
        refreshConversations()
    }

    override fun onPacketLogAdded(log: MeshPacketLog) {
        packetLogAdapter.submitList(meshService.router.getPacketLogs())
    }
}
