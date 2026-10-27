package com.example.linkora.ui.screens.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.linkora.crypto.IdentityManager
import com.example.linkora.ui.QrCodeUtil
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

/**
 * MyQrCodeDialog: Displays user's personal identity QR code for friends to scan.
 */
@Composable
fun MyQrCodeDialog(
    identityManager: IdentityManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val glassColors = LocalGlassColors.current

    val qrPayload = remember(identityManager.displayName) {
        com.example.linkora.model.ContactUtils.formatContactPayload(
            identityManager.identityId,
            identityManager.safetyFingerprint,
            identityManager.publicKeyBase64,
            identityManager.displayName
        )
    }

    val qrBitmap = remember(qrPayload) {
        QrCodeUtil.generateQrBitmap(qrPayload, 480)
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LinkoraSpacing.space8),
            shape = RoundedCornerShape(28.dp),
            cornerRadius = 28.dp,
            isElevated = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = LinkoraColors.OnlineStatus,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                        Text(
                            text = "My Friend QR Code",
                            style = LinkoraTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.textPrimary
                        )
                    }

                    GlassIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        buttonSize = 32.dp,
                        iconSize = 16.dp
                    )
                }

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                // Avatar and Display Name
                GlassAvatar(
                    name = identityManager.displayName,
                    size = 68.dp
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space8))

                Text(
                    text = identityManager.displayName,
                    style = LinkoraTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textPrimary
                )

                Text(
                    text = "ID: ${identityManager.identityId.take(18)}...",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                // QR Code box with clean white rounded frame for scan readability
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "My Identity QR Code",
                            modifier = Modifier.size(186.dp)
                        )
                    } else {
                        Text(
                            text = "Error generating QR code",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                // Safety Fingerprint
                Text(
                    text = "Safety Fingerprint",
                    style = LinkoraTypography.labelSmall,
                    color = Color(0xFFD1D5DB)
                )
                Text(
                    text = identityManager.safetyFingerprint,
                    style = LinkoraTypography.labelLarge,
                    color = Color(0xFF67E8F9),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                // Copy Identity payload button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Linkora Identity", qrPayload))
                        Toast.makeText(context, "Identity code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(
                                    Color(0xFF6366F1), // Royal Indigo
                                    Color(0xFF8B5CF6)  // Vibrant Violet
                                )
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                        Text(
                            text = "Copy Friend Code",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * AddFriendDialog: Allows scanning friend's QR or pasting friend code manually.
 */
@Composable
fun AddFriendDialog(
    onScanQrClick: () -> Unit,
    onFriendCodeSubmitted: (String) -> Unit,
    onNfcTouchClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    var inputCode by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LinkoraSpacing.space8),
            shape = RoundedCornerShape(28.dp),
            cornerRadius = 28.dp,
            isElevated = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Friend",
                        style = LinkoraTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary
                    )

                    GlassIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        buttonSize = 32.dp,
                        iconSize = 16.dp
                    )
                }

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                // Scan with Camera Action
                Button(
                    onClick = {
                        onDismiss()
                        onScanQrClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(
                                    Color(0xFF10B981),
                                    Color(0xFF059669)
                                )
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                        Text(
                            text = "Scan Friend's QR Code",
                            style = LinkoraTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Touch with NFC Action (NameDrop)
                Button(
                    onClick = {
                        onDismiss()
                        onNfcTouchClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Nfc,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                        Text(
                            text = "Touch Phones (NFC NameDrop)",
                            style = LinkoraTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                Text(
                    text = "— OR PASTE FRIEND CODE —",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    placeholder = {
                        Text(
                            text = "Paste friend code here (usr_...|...)",
                            style = LinkoraTypography.bodyMedium,
                            color = glassColors.textSecondary.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LinkoraColors.Emerald,
                        unfocusedBorderColor = glassColors.border,
                        focusedTextColor = glassColors.textPrimary,
                        unfocusedTextColor = glassColors.textPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LinkoraSpacing.space12)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = glassColors.textSecondary)
                    }

                    Button(
                        onClick = {
                            if (inputCode.isNotBlank()) {
                                onFriendCodeSubmitted(inputCode.trim())
                                onDismiss()
                            }
                        },
                        enabled = inputCode.isNotBlank(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF059669)
                                    )
                                ),
                                CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Text("Add Friend", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
