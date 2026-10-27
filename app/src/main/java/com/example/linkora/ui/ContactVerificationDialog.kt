package com.example.linkora.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.linkora.model.Contact
import com.example.linkora.model.VerificationState

object ContactVerificationDialog {

    fun show(context: Context, contact: Contact, onStateChanged: (Contact) -> Unit) {
        val stateText = when (contact.verificationState) {
            VerificationState.VERIFIED -> "Verified Cryptographic Identity"
            VerificationState.UNVERIFIED -> "Unverified (Verify out-of-band via QR or Fingerprint)"
            VerificationState.CHANGED -> "WARNING: Identity Key Changed! Re-verification required!"
        }

        val message = """
            Contact: ${contact.username}
            Identity ID: ${contact.identityId}
            
            Safety Fingerprint:
            ${contact.safetyFingerprint}
            
            Status: $stateText
            
            Compare this 24-character safety fingerprint with your contact's screen or scan their QR code to verify that no man-in-the-middle or spoofed key exists.
        """.trimIndent()

        AlertDialog.Builder(context)
            .setTitle("Cryptographic Safety Verification")
            .setMessage(message)
            .setPositiveButton(if (contact.verificationState == VerificationState.VERIFIED) "Mark Unverified" else "Mark Verified") { _, _ ->
                contact.verificationState = if (contact.verificationState == VerificationState.VERIFIED) {
                    VerificationState.UNVERIFIED
                } else {
                    VerificationState.VERIFIED
                }
                onStateChanged(contact)
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
