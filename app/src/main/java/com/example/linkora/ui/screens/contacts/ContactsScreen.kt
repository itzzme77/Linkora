package com.example.linkora.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.crypto.IdentityManager
import com.example.linkora.model.Contact
import com.example.linkora.model.VerificationState
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraElevation
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.ui.graphics.Brush

/**
 * ContactsScreen: Displays verified contacts, My QR code, friend scanner, NFC share, and group creation.
 */
@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    identityManager: IdentityManager,
    onContactClick: (Contact) -> Unit,
    onShowMyQr: () -> Unit,
    onAddFriendClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onNfcShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LinkoraSpacing.space20, vertical = LinkoraSpacing.space12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Contacts & Friends",
                style = LinkoraTypography.displayLarge,
                color = glassColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(LinkoraSpacing.space8)) {
                GlassIconButton(
                    icon = Icons.Rounded.Nfc,
                    contentDescription = "NFC NameDrop Share",
                    onClick = onNfcShareClick
                )
                GlassIconButton(
                    icon = Icons.Rounded.QrCodeScanner,
                    contentDescription = "My QR Code",
                    onClick = onShowMyQr
                )
                GlassIconButton(
                    icon = Icons.Rounded.PersonAdd,
                    contentDescription = "Add Friend",
                    onClick = onAddFriendClick
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = LinkoraSpacing.space16,
                end = LinkoraSpacing.space16,
                top = LinkoraSpacing.space8,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(LinkoraSpacing.space12)
        ) {
            // My QR Card Banner
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    cornerRadius = 28.dp,
                    elevation = LinkoraElevation.elevationCard,
                    isElevated = true,
                    onClick = onShowMyQr
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LinkoraColors.Emerald.copy(alpha = 0.20f), CircleShape)
                                .border(1.dp, LinkoraColors.Emerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = null,
                                tint = LinkoraColors.Emerald,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "My Linkora QR Code",
                                style = LinkoraTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "Show this to nearby friends to pair instantly",
                                style = LinkoraTypography.bodyMedium,
                                color = glassColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // NFC Touch & Connect Hero Card (NameDrop / AirDrop)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    cornerRadius = 28.dp,
                    elevation = LinkoraElevation.elevationCard,
                    isElevated = true,
                    onClick = onNfcShareClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            LinkoraColors.Emerald.copy(alpha = 0.25f),
                                            Color(0xFF6366F1).copy(alpha = 0.25f)
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(1.dp, LinkoraColors.Emerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Nfc,
                                contentDescription = null,
                                tint = LinkoraColors.Emerald,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NFC Touch & Connect",
                                    style = LinkoraTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = glassColors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(LinkoraColors.Emerald.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NameDrop",
                                        color = LinkoraColors.Emerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Touch phones together to exchange keys & connect",
                                style = LinkoraTypography.bodyMedium,
                                color = glassColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Quick Action Buttons (Add Friend & Create Group)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LinkoraSpacing.space12)
                ) {
                    Button(
                        onClick = onAddFriendClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
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
                                imageVector = Icons.Rounded.PersonAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                            Text("Add Friend", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = onCreateGroupClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.28f),
                                        Color.White.copy(alpha = 0.14f)
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
                                imageVector = Icons.Rounded.GroupAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                            Text(
                                "New Group",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "SAVED CONTACTS (${contacts.size})",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = LinkoraSpacing.space8, start = 4.dp)
                )
            }

            // Contacts List
            if (contacts.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        cornerRadius = 24.dp
                    ) {
                        Text(
                            text = "No contacts saved yet. Tap 'Add Friend' to scan a friend's QR code!",
                            style = LinkoraTypography.bodyMedium,
                            color = glassColors.textPrimary,
                            modifier = Modifier.padding(LinkoraSpacing.space16)
                        )
                    }
                }
            } else {
                items(contacts, key = { it.identityId }) { contact ->
                    ContactRowItem(
                        contact = contact,
                        onClick = { onContactClick(contact) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactRowItem(
    contact: Contact,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val isVerified = contact.verificationState == VerificationState.VERIFIED

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        cornerRadius = 24.dp,
        elevation = LinkoraElevation.elevationSubtle,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassAvatar(name = contact.username, size = 48.dp)

            Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.username,
                        style = LinkoraTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary
                    )

                    Spacer(modifier = Modifier.width(LinkoraSpacing.space4))

                    if (isVerified) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified",
                            tint = LinkoraColors.OnlineStatus,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Fingerprint: ${contact.safetyFingerprint}",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = "Message",
                tint = LinkoraColors.Emerald,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}
