package com.example.linkora.transport

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.linkora.model.PeerDevice
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Android implementation of MeshTransport backed by Google Play Services Nearby Connections.
 * Uses Strategy.P2P_CLUSTER with aggressive auto-reconnect, stale-endpoint cleanup,
 * and periodic discovery cycling to overcome Android BLE scan throttling.
 */
class NearbyConnectionsTransport(private val context: Context) : MeshTransport {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val connectedPeers = ConcurrentHashMap<String, PeerDevice>()
    private val endpointNames = ConcurrentHashMap<String, String>()
    private val discoveredEndpoints = ConcurrentHashMap<String, DiscoveredEndpointInfo>()
    private val pendingConnections = ConcurrentHashMap<String, Long>()
    private val connectingEndpoints = ConcurrentHashMap<String, Long>()
    private val connectionAttempts = ConcurrentHashMap<String, AtomicInteger>()
    private val listeners = CopyOnWriteArrayList<MeshTransportListener>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isAdvertising = false
    private var isDiscovering = false
    private var isStarted = false
    private var localEndpointName: String = "Linkora-Node"
    private var advertisingRetryCount = 0
    private var discoveryRetryCount = 0

    override fun start(localName: String) {
        if (isStarted) return
        isStarted = true
        localEndpointName = localName
        advertisingRetryCount = 0
        discoveryRetryCount = 0
        startAdvertising()
        startDiscovery()
        // Start background maintenance loops
        mainHandler.post(staleConnectionCleanup)
        mainHandler.postDelayed(discoveryRefreshRunnable, DISCOVERY_REFRESH_INTERVAL)
        mainHandler.postDelayed(reconnectionSweep, RECONNECTION_SWEEP_INTERVAL)
    }

    override fun stop() {
        if (!isStarted) return
        isStarted = false
        mainHandler.removeCallbacks(staleConnectionCleanup)
        mainHandler.removeCallbacks(discoveryRefreshRunnable)
        mainHandler.removeCallbacks(reconnectionSweep)
        try {
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
            connectionsClient.stopAllEndpoints()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping nearby transport", e)
        }
        connectedPeers.clear()
        discoveredEndpoints.clear()
        pendingConnections.clear()
        connectingEndpoints.clear()
        connectionAttempts.clear()
        isAdvertising = false
        isDiscovering = false
        notifyStateChanged()
    }

    override fun isRunning(): Boolean = isStarted

    private fun startAdvertising() {
        if (!isStarted) return
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setLowPower(false)
            .build()

        connectionsClient.startAdvertising(
            localEndpointName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            isAdvertising = true
            advertisingRetryCount = 0
            notifyStateChanged()
            Log.d(TAG, "Advertising started ($localEndpointName)")
        }.addOnFailureListener { e ->
            isAdvertising = false
            notifyStateChanged()
            Log.e(TAG, "Advertising failed: ${e.message}")
            listeners.forEach { it.onTransportError("Advertising failed: ${e.message}") }
            // Auto-retry with exponential backoff (max 30s)
            if (isStarted && advertisingRetryCount < MAX_AD_RETRIES) {
                advertisingRetryCount++
                val delay = minOf(1000L * (1 shl advertisingRetryCount), 30000L)
                Log.d(TAG, "Retrying advertising in ${delay}ms (attempt $advertisingRetryCount)")
                mainHandler.postDelayed({ startAdvertising() }, delay)
            }
        }
    }

    private fun startDiscovery() {
        if (!isStarted) return
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setLowPower(false)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            isDiscovering = true
            discoveryRetryCount = 0
            notifyStateChanged()
            Log.d(TAG, "Discovery started")
        }.addOnFailureListener { e ->
            isDiscovering = false
            notifyStateChanged()
            Log.e(TAG, "Discovery failed: ${e.message}")
            listeners.forEach { it.onTransportError("Discovery failed: ${e.message}") }
            // Auto-retry with exponential backoff (max 30s)
            if (isStarted && discoveryRetryCount < MAX_AD_RETRIES) {
                discoveryRetryCount++
                val delay = minOf(1000L * (1 shl discoveryRetryCount), 30000L)
                Log.d(TAG, "Retrying discovery in ${delay}ms (attempt $discoveryRetryCount)")
                mainHandler.postDelayed({ startDiscovery() }, delay)
            }
        }
    }

    /**
     * Periodically restart discovery to work around Android's BLE scan throttling,
     * which silently stops returning results after ~30s of continuous scanning.
     */
    private val discoveryRefreshRunnable = object : Runnable {
        override fun run() {
            if (!isStarted) return
            Log.d(TAG, "Cycling discovery to refresh BLE scan window")
            try {
                connectionsClient.stopDiscovery()
            } catch (_: Exception) {}
            isDiscovering = false
            // Brief pause before restarting to let the BLE stack reset
            mainHandler.postDelayed({
                if (isStarted) {
                    startDiscovery()
                }
            }, 500L)
            mainHandler.postDelayed(this, DISCOVERY_REFRESH_INTERVAL)
        }
    }

    /**
     * Periodically clean up stale entries in connectingEndpoints that have been stuck
     * for longer than the timeout, unblocking new connection attempts.
     */
    private val staleConnectionCleanup = object : Runnable {
        override fun run() {
            if (!isStarted) return
            val now = System.currentTimeMillis()
            val staleIds = connectingEndpoints.entries
                .filter { now - it.value > CONNECTING_TIMEOUT_MS }
                .map { it.key }
            staleIds.forEach { id ->
                Log.w(TAG, "Cleaning up stale connecting endpoint: $id")
                connectingEndpoints.remove(id)
                pendingConnections.remove(id)
            }
            mainHandler.postDelayed(this, STALE_CLEANUP_INTERVAL)
        }
    }

    /**
     * Periodically attempt to connect to discovered-but-not-connected endpoints.
     * This handles cases where initial auto-connect failed silently.
     */
    private val reconnectionSweep = object : Runnable {
        override fun run() {
            if (!isStarted) return
            for ((endpointId, info) in discoveredEndpoints) {
                if (!connectedPeers.containsKey(endpointId) && !connectingEndpoints.containsKey(endpointId)) {
                    val attempts = connectionAttempts.getOrPut(endpointId) { AtomicInteger(0) }
                    if (attempts.get() < MAX_CONNECTION_RETRIES) {
                        Log.d(TAG, "Reconnection sweep: attempting connection to $endpointId (attempt ${attempts.get() + 1})")
                        if (isInitiator(localEndpointName, info.endpointName)) {
                            connect(endpointId)
                        }
                    }
                }
            }
            mainHandler.postDelayed(this, RECONNECTION_SWEEP_INTERVAL)
        }
    }

    override fun sendPayload(endpointId: String, payload: ByteArray): Boolean {
        return try {
            val nearbyPayload = Payload.fromBytes(payload)
            connectionsClient.sendPayload(endpointId, nearbyPayload)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send payload to $endpointId", e)
            false
        }
    }

    override fun broadcastPayload(payload: ByteArray, excludeEndpointId: String?): Int {
        var sentCount = 0
        for ((endpointId, _) in connectedPeers) {
            if (endpointId != excludeEndpointId) {
                if (sendPayload(endpointId, payload)) {
                    sentCount++
                }
            }
        }
        return sentCount
    }

    override fun getConnectedPeers(): List<PeerDevice> {
        return connectedPeers.values.toList()
    }

    override fun connect(endpointId: String) {
        if (!isStarted || connectedPeers.containsKey(endpointId) || connectingEndpoints.containsKey(endpointId)) {
            return
        }

        // If another connection handshake is actively in progress, stagger briefly
        if (connectingEndpoints.isNotEmpty()) {
            val staggerDelay = 500L + (Math.random() * 500L).toLong()
            mainHandler.postDelayed({
                if (isStarted && !connectedPeers.containsKey(endpointId) && !connectingEndpoints.containsKey(endpointId)) {
                    connect(endpointId)
                }
            }, staggerDelay)
            return
        }

        val now = System.currentTimeMillis()
        val lastAttempt = pendingConnections[endpointId] ?: 0L
        if (now - lastAttempt < CONNECTION_DEBOUNCE_MS) {
            return
        }
        pendingConnections[endpointId] = now
        connectingEndpoints[endpointId] = now

        val connectionOptions = ConnectionOptions.Builder()
            .setLowPower(false)
            .build()

        connectionsClient.requestConnection(
            localEndpointName,
            endpointId,
            connectionLifecycleCallback,
            connectionOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Connection requested to $endpointId")
        }.addOnFailureListener { e ->
            Log.w(TAG, "Failed requesting connection to $endpointId: ${e.message}")
            pendingConnections.remove(endpointId)
            connectingEndpoints.remove(endpointId)
            // Quick retry with jitter for transient failures
            val attempts = connectionAttempts.getOrPut(endpointId) { AtomicInteger(0) }
            val attemptNum = attempts.incrementAndGet()
            if (isStarted && attemptNum <= MAX_CONNECTION_RETRIES && discoveredEndpoints.containsKey(endpointId)) {
                val retryDelay = 800L + (Math.random() * 800L).toLong() + (attemptNum * 500L)
                Log.d(TAG, "Will retry connection to $endpointId in ${retryDelay}ms (attempt $attemptNum)")
                mainHandler.postDelayed({
                    if (isStarted && !connectedPeers.containsKey(endpointId)) {
                        connect(endpointId)
                    }
                }, retryDelay)
            }
        }
    }

    override fun addListener(listener: MeshTransportListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: MeshTransportListener) {
        listeners.remove(listener)
    }

    private fun notifyStateChanged() {
        listeners.forEach { it.onTransportStateChanged(isAdvertising, isDiscovering) }
    }

    /* ---------------- Nearby Callbacks ---------------- */

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, "Connection initiated by $endpointId (${connectionInfo.endpointName})")
            val cleanName = parseDisplayName(connectionInfo.endpointName)
            endpointNames[endpointId] = cleanName
            connectingEndpoints[endpointId] = System.currentTimeMillis()

            // Auto-accept incoming connection instantly
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed accepting connection from $endpointId: ${e.message}")
                    connectingEndpoints.remove(endpointId)
                }
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            pendingConnections.remove(endpointId)
            connectingEndpoints.remove(endpointId)

            if (resolution.status.isSuccess) {
                val peerName = endpointNames[endpointId] ?: "Peer-$endpointId"
                val peer = PeerDevice(
                    endpointId = endpointId,
                    deviceId = endpointId,
                    displayName = peerName,
                    isConnected = true,
                    lastSeen = System.currentTimeMillis()
                )
                connectedPeers[endpointId] = peer
                // Reset attempt counter on success
                connectionAttempts.remove(endpointId)
                listeners.forEach { it.onPeerConnected(peer) }
                Log.d(TAG, "Connected to peer $endpointId ($peerName)")
            } else {
                Log.w(TAG, "Connection failed for $endpointId: ${resolution.status}")
                val info = discoveredEndpoints[endpointId]
                if (info != null && isInitiator(localEndpointName, info.endpointName)) {
                    // Retry with faster jitter backoff
                    val attempts = connectionAttempts.getOrPut(endpointId) { AtomicInteger(0) }
                    val attemptNum = attempts.incrementAndGet()
                    if (attemptNum <= MAX_CONNECTION_RETRIES) {
                        val jitterDelay = 1000L + (Math.random() * 1500L).toLong() + (attemptNum * 300L)
                        mainHandler.postDelayed({
                            if (isStarted && !connectedPeers.containsKey(endpointId) && discoveredEndpoints.containsKey(endpointId)) {
                                connect(endpointId)
                            }
                        }, jitterDelay)
                    }
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedPeers.remove(endpointId)
            pendingConnections.remove(endpointId)
            connectingEndpoints.remove(endpointId)
            connectionAttempts.remove(endpointId)
            listeners.forEach { it.onPeerDisconnected(endpointId) }
            Log.d(TAG, "Disconnected from peer $endpointId")

            // Attempt reconnection if endpoint is still discovered
            if (isStarted && discoveredEndpoints.containsKey(endpointId)) {
                val info = discoveredEndpoints[endpointId]!!
                if (isInitiator(localEndpointName, info.endpointName)) {
                    mainHandler.postDelayed({
                        if (isStarted && !connectedPeers.containsKey(endpointId) && discoveredEndpoints.containsKey(endpointId)) {
                            Log.d(TAG, "Auto-reconnecting to disconnected peer $endpointId")
                            connect(endpointId)
                        }
                    }, 1500L)
                }
            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val cleanName = parseDisplayName(info.endpointName)
            Log.d(TAG, "Found endpoint: $endpointId ($cleanName)")
            endpointNames[endpointId] = cleanName
            discoveredEndpoints[endpointId] = info
            connectionAttempts.remove(endpointId) // Reset attempts for freshly discovered endpoint
            val peer = PeerDevice(
                endpointId = endpointId,
                deviceId = endpointId,
                displayName = cleanName,
                isConnected = false,
                lastSeen = System.currentTimeMillis()
            )
            listeners.forEach { it.onPeerDiscovered(peer) }

            // Deterministic Asymmetric Auto-Connect:
            // Exactly ONE device evaluates isInitiator == true, preventing bilateral collision.
            if (!connectedPeers.containsKey(endpointId) && !connectingEndpoints.containsKey(endpointId)) {
                if (isInitiator(localEndpointName, info.endpointName)) {
                    Log.d(TAG, "Initiator for $endpointId - connecting immediately")
                    connect(endpointId)
                } else {
                    Log.d(TAG, "Listener for $endpointId - waiting for peer initiation")
                    // Fallback if peer fails to initiate within 3 seconds
                    mainHandler.postDelayed({
                        if (isStarted && !connectedPeers.containsKey(endpointId) && !connectingEndpoints.containsKey(endpointId)) {
                            Log.d(TAG, "Fallback: initiating connection to $endpointId")
                            connect(endpointId)
                        }
                    }, LISTENER_FALLBACK_DELAY)
                }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Lost endpoint: $endpointId")
            discoveredEndpoints.remove(endpointId)
            pendingConnections.remove(endpointId)
            connectingEndpoints.remove(endpointId)
            connectionAttempts.remove(endpointId)
        }
    }

    private fun isInitiator(localName: String, remoteName: String): Boolean {
        val localKey = localName.substringBefore("|")
        val remoteKey = remoteName.substringBefore("|")
        return if (localKey != remoteKey) {
            localKey > remoteKey
        } else {
            localName > remoteName
        }
    }

    private fun parseDisplayName(rawName: String): String {
        return if (rawName.contains("|")) {
            rawName.substringAfter("|")
        } else {
            rawName
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes()
                if (bytes != null && bytes.isNotEmpty()) {
                    listeners.forEach { it.onPayloadReceived(endpointId, bytes) }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Can be used for transfer progress of large files
        }
    }

    companion object {
        private const val TAG = "NearbyMeshTransport"
        const val SERVICE_ID = "com.example.linkora.mesh.service"

        // Timing constants
        private const val CONNECTION_DEBOUNCE_MS = 800L
        private const val LISTENER_FALLBACK_DELAY = 3000L
        private const val CONNECTING_TIMEOUT_MS = 12000L
        private const val STALE_CLEANUP_INTERVAL = 8000L
        private const val DISCOVERY_REFRESH_INTERVAL = 45000L
        private const val RECONNECTION_SWEEP_INTERVAL = 8000L
        private const val MAX_CONNECTION_RETRIES = 5
        private const val MAX_AD_RETRIES = 8
    }
}
