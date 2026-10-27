package com.example.linkora.model

import com.example.linkora.crypto.CryptoEngine

/**
 * Contact payload formatting and parsing utilities for real device-to-device contact exchange.
 * Ensures consistent serialization across NFC HCE, QR codes, and Mesh announcements.
 */
object ContactUtils {

    /**
     * Formats an identity payload for QR codes, NFC HCE, and mesh announcements.
     * Format: identityId|fingerprint|publicKeyBase64|displayName
     */
    fun formatContactPayload(
        identityId: String,
        fingerprint: String,
        publicKeyBase64: String,
        displayName: String
    ): String {
        return "$identityId|$fingerprint|$publicKeyBase64|$displayName"
    }

    /**
     * Robust parser for Contact payload received from NFC, QR code, or Friend Code.
     * Accurately distinguishes between:
     * - Public key (Base64 string > 50 characters without spaces)
     * - Safety Fingerprint (Hexadecimal groups with spaces, e.g. "XXXX XXXX ...")
     * - Friend Display Name (Real human name like "Alex", "Upadesh", etc.)
     * - User ID (starting with "usr_")
     */
    fun parseContactPayload(rawInput: String): Contact? {
        val clean = rawInput.trim()
            .removePrefix("LINKORA:")
            .removePrefix("\u0002en")
            .removePrefix("\u0002")
            .trim()

        if (clean.isBlank()) return null

        val parts = clean.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size < 3) {
            return null
        }

        // 1. Identity ID: parts[0]
        val id = parts[0]

        // 2. Identify Public Key: longest string (EC P-256 / RSA Base64 is > 50 chars) without spaces
        val remaining = parts.drop(1)
        val pubKey = remaining.firstOrNull { it.length > 50 && !it.contains(" ") }
            ?: remaining.maxByOrNull { it.length }
            ?: return null

        // 3. The other parts are name and fingerprint
        val others = remaining.filter { it != pubKey }

        // Helper to detect if a string is a safety fingerprint:
        val isFingerprint: (String) -> Boolean = { str ->
            val noSpaces = str.replace(" ", "")
            (str.contains(" ") && noSpaces.length >= 8 && noSpaces.all { c -> c.isDigit() || c in 'A'..'F' || c in 'a'..'f' }) ||
                    str.startsWith("LINK ORA", ignoreCase = true)
        }

        val fingerprintPart = others.firstOrNull { isFingerprint(it) }
        val namePart = others.firstOrNull { it != fingerprintPart }

        // Determine user display name:
        val resolvedName = if (!namePart.isNullOrBlank()) {
            namePart
        } else if (parts.size >= 4 && parts.last() != pubKey && parts.last() != fingerprintPart) {
            parts.last()
        } else {
            "Friend-${id.takeLast(4)}"
        }

        // Determine or compute safety fingerprint:
        val resolvedFingerprint = fingerprintPart ?: try {
            val pub = CryptoEngine.base64ToPublicKey(pubKey)
            CryptoEngine.formatSafetyFingerprint(pub)
        } catch (_: Exception) {
            "LINK ORA1 VERIFIED"
        }

        return Contact(
            identityId = id,
            username = resolvedName,
            publicKeyBase64 = pubKey,
            verificationState = VerificationState.VERIFIED,
            safetyFingerprint = resolvedFingerprint,
            lastSeen = System.currentTimeMillis()
        )
    }
}
