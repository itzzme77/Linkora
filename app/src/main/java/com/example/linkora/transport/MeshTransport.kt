package com.example.linkora.transport

import com.example.linkora.model.PeerDevice

interface MeshTransportListener {
    fun onPeerDiscovered(peer: PeerDevice)
    fun onPeerConnected(peer: PeerDevice)
    fun onPeerDisconnected(endpointId: String)
    fun onPayloadReceived(fromEndpointId: String, payload: ByteArray)
    fun onTransportStateChanged(isAdvertising: Boolean, isDiscovering: Boolean)
    fun onTransportError(message: String)
}

interface MeshTransport {
    fun start(localName: String)
    fun stop()
    fun isRunning(): Boolean
    fun sendPayload(endpointId: String, payload: ByteArray): Boolean
    fun broadcastPayload(payload: ByteArray, excludeEndpointId: String? = null): Int
    fun getConnectedPeers(): List<PeerDevice>
    fun connect(endpointId: String) {}
    fun addListener(listener: MeshTransportListener)
    fun removeListener(listener: MeshTransportListener)
}
