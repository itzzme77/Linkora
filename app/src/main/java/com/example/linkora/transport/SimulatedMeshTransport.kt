package com.example.linkora.transport

import android.os.Handler
import android.os.Looper
import com.example.linkora.model.PeerDevice
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory virtual mesh transport for testing and in-app demonstration of
 * multi-hop store-and-forward relaying (e.g. Node A -> Relay B -> Node C).
 */
class SimulatedMeshTransport(
    val nodeId: String,
    val nodeDisplayName: String
) : MeshTransport {

    private val connectedPeers = ConcurrentHashMap<String, PeerDevice>()
    private val listeners = CopyOnWriteArrayList<MeshTransportListener>()
    private var isStarted = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun start(localName: String) {
        isStarted = true
        notifyStateChanged()
    }

    override fun stop() {
        isStarted = false
        connectedPeers.clear()
        notifyStateChanged()
    }

    override fun isRunning(): Boolean = isStarted

    /**
     * Manually attach another simulated transport node as a link.
     */
    fun attachPeer(peerEndpointId: String, peerDisplayName: String, peerTransport: SimulatedMeshTransport) {
        val peer = PeerDevice(
            endpointId = peerEndpointId,
            deviceId = peerEndpointId,
            displayName = peerDisplayName,
            isConnected = true,
            isSimulated = true
        )
        connectedPeers[peerEndpointId] = peer
        virtualLinks[peerEndpointId] = peerTransport

        mainHandler.post {
            listeners.forEach { it.onPeerConnected(peer) }
        }
    }

    fun detachPeer(peerEndpointId: String) {
        connectedPeers.remove(peerEndpointId)
        virtualLinks.remove(peerEndpointId)
        mainHandler.post {
            listeners.forEach { it.onPeerDisconnected(peerEndpointId) }
        }
    }

    override fun sendPayload(endpointId: String, payload: ByteArray): Boolean {
        if (!isStarted) return false
        val targetTransport = virtualLinks[endpointId] ?: return false

        // Simulate wireless transmission latency (50ms)
        mainHandler.postDelayed({
            targetTransport.deliverIncomingPayload(this.nodeId, payload)
        }, 50)
        return true
    }

    fun deliverIncomingPayload(fromEndpointId: String, payload: ByteArray) {
        if (!isStarted) return
        mainHandler.post {
            listeners.forEach { it.onPayloadReceived(fromEndpointId, payload) }
        }
    }

    override fun broadcastPayload(payload: ByteArray, excludeEndpointId: String?): Int {
        var count = 0
        for ((endpointId, _) in connectedPeers) {
            if (endpointId != excludeEndpointId) {
                if (sendPayload(endpointId, payload)) {
                    count++
                }
            }
        }
        return count
    }

    override fun getConnectedPeers(): List<PeerDevice> = connectedPeers.values.toList()

    override fun addListener(listener: MeshTransportListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: MeshTransportListener) {
        listeners.remove(listener)
    }

    private fun notifyStateChanged() {
        mainHandler.post {
            listeners.forEach { it.onTransportStateChanged(isStarted, isStarted) }
        }
    }

    private val virtualLinks = ConcurrentHashMap<String, SimulatedMeshTransport>()
}
