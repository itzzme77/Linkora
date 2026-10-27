package com.example.linkora.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.example.linkora.crypto.IdentityManager

/**
 * LinkoraHceService:
 * Android Host Card Emulation (HCE) service enabling real phone-to-phone NFC contact exchange.
 * When a friend's phone touches this device with NFC enabled, it selects AID F0010203040506
 * and receives this user's real cryptographic identity payload.
 */
class LinkoraHceService : HostApduService() {

    private lateinit var identityManager: IdentityManager

    override fun onCreate() {
        super.onCreate()
        identityManager = IdentityManager(applicationContext)
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return STATUS_FAILED

        Log.d(TAG, "processCommandApdu: received ${commandApdu.size} bytes")

        // Check if command is SELECT AID for Linkora
        if (isSelectAidCommand(commandApdu)) {
            val payload = com.example.linkora.model.ContactUtils.formatContactPayload(
                identityManager.identityId,
                identityManager.safetyFingerprint,
                identityManager.publicKeyBase64,
                identityManager.displayName
            )
            val payloadBytes = payload.toByteArray(Charsets.UTF_8)
            Log.d(TAG, "Responding with real identity payload (${payloadBytes.size} bytes) for ${identityManager.displayName}")

            val response = ByteArray(payloadBytes.size + 2)
            System.arraycopy(payloadBytes, 0, response, 0, payloadBytes.size)
            response[payloadBytes.size] = 0x90.toByte()     // SW1: Status OK
            response[payloadBytes.size + 1] = 0x00.toByte() // SW2: Status OK
            return response
        }

        // Mutual 2-Way Handshake: Reader sends its identity back to this HCE device
        if (isExchangeCommand(commandApdu)) {
            try {
                val dataLen = commandApdu[4].toInt() and 0xFF
                val peerBytes = if (commandApdu.size >= 5 + dataLen && dataLen > 0) {
                    commandApdu.copyOfRange(5, 5 + dataLen)
                } else if (commandApdu.size > 5) {
                    commandApdu.copyOfRange(5, commandApdu.size)
                } else {
                    null
                }
                if (peerBytes != null) {
                    val peerPayload = String(peerBytes, Charsets.UTF_8)
                    Log.d(TAG, "Mutual NFC Handshake received on HCE: $peerPayload")

                    // Broadcast to MainActivity so this phone also immediately displays the contact & chat option
                    val intent = android.content.Intent(ACTION_NFC_MUTUAL_CONTACT).apply {
                        putExtra(EXTRA_CONTACT_PAYLOAD, peerPayload)
                        setPackage(packageName)
                    }
                    sendBroadcast(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed processing mutual exchange APDU", e)
            }
            return STATUS_SUCCESS
        }

        return STATUS_SUCCESS
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE Service deactivated (reason: $reason)")
    }

    private fun isSelectAidCommand(apdu: ByteArray): Boolean {
        if (apdu.size < 5 + LINKORA_AID_BYTES.size) return false
        // CLA=00, INS=A4, P1=04, P2=00
        val isSelect = apdu[0] == 0x00.toByte() &&
                apdu[1] == 0xA4.toByte() &&
                apdu[2] == 0x04.toByte() &&
                apdu[3] == 0x00.toByte()
        if (!isSelect) return false

        // Check AID
        val aidLength = apdu[4].toInt() and 0xFF
        if (aidLength != LINKORA_AID_BYTES.size || apdu.size < 5 + aidLength) return false

        for (i in LINKORA_AID_BYTES.indices) {
            if (apdu[5 + i] != LINKORA_AID_BYTES[i]) return false
        }
        return true
    }

    private fun isExchangeCommand(apdu: ByteArray): Boolean {
        if (apdu.size < 5) return false
        return apdu[0] == CLA_EXCHANGE && apdu[1] == INS_EXCHANGE
    }

    companion object {
        private const val TAG = "LinkoraHceService"
        const val ACTION_NFC_MUTUAL_CONTACT = "com.example.linkora.ACTION_NFC_MUTUAL_CONTACT"
        const val EXTRA_CONTACT_PAYLOAD = "contact_payload"

        const val CLA_EXCHANGE: Byte = 0x80.toByte()
        const val INS_EXCHANGE: Byte = 0x10.toByte()

        val LINKORA_AID_BYTES = byteArrayOf(
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(),
            0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )
        val SELECT_APDU_COMMAND = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x07.toByte(), // AID length
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(),
            0x04.toByte(), 0x05.toByte(), 0x06.toByte(),
            0x00.toByte()  // Le
        )
        private val STATUS_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val STATUS_FAILED = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }
}
