package com.example.linkora.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.SensorsOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.linkora.crypto.BiometricAuthManager
import com.example.linkora.notification.NotificationPreferences
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.crypto.IdentityManager
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraElevation
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

/**
 * ProfileScreen: Self user identity, display name editing, settings & network connectivity, and cryptographic fingerprint.
 */
@Composable
fun ProfileScreen(
    identityManager: IdentityManager,
    displayName: String = identityManager.displayName,
    isMeshActive: Boolean = false,
    connectedPeersCount: Int = 0,
    onEditNameClick: () -> Unit,
    onShowMyQr: () -> Unit,
    onOpenMeshSettings: () -> Unit,
    onNfcShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glassColors = LocalGlassColors.current

    var messageNotifEnabled by remember {
        mutableStateOf(NotificationPreferences.isMessageNotificationsEnabled(context))
    }
    var headsUpBannerEnabled by remember {
        mutableStateOf(NotificationPreferences.isHeadsUpBannerEnabled(context))
    }
    var vibrationSoundEnabled by remember {
        mutableStateOf(NotificationPreferences.isVibrationSoundEnabled(context))
    }

    val biometricAuthManager = remember { BiometricAuthManager(context) }
    var appLockEnabled by remember {
        mutableStateOf(biometricAuthManager.isAppLockEnabled())
    }

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
                text = "Profile & Settings",
                style = LinkoraTypography.displayLarge,
                color = glassColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            GlassIconButton(
                icon = Icons.Rounded.QrCodeScanner,
                contentDescription = "My QR",
                onClick = onShowMyQr
            )
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
            verticalArrangement = Arrangement.spacedBy(LinkoraSpacing.space16)
        ) {
            // Profile Card (Avatar, Name, Edit Button)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    cornerRadius = 28.dp,
                    elevation = LinkoraElevation.elevationCard,
                    isElevated = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlassAvatar(
                            name = displayName,
                            size = 80.dp,
                            isOnline = isMeshActive
                        )

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = displayName,
                                style = LinkoraTypography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = glassColors.textPrimary
                            )

                            Spacer(modifier = Modifier.width(LinkoraSpacing.space8))

                            GlassIconButton(
                                icon = Icons.Rounded.Edit,
                                contentDescription = "Edit Display Name",
                                onClick = onEditNameClick,
                                buttonSize = 34.dp,
                                iconSize = 18.dp
                            )
                        }

                        Text(
                            text = if (isMeshActive) "Offline Mesh Connected ($connectedPeersCount nearby)" else "Offline Mesh Inactive",
                            style = LinkoraTypography.labelSmall,
                            color = if (isMeshActive) LinkoraColors.OnlineStatus else Color(0xFFD1D5DB),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                        Button(
                            onClick = onShowMyQr,
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
                                    imageVector = Icons.Rounded.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                                Text(
                                    text = "Show My QR Code",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Settings & Connectivity Section
            item {
                Text(
                    text = "SETTINGS & CONNECTIVITY",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Mesh & Network Settings Option
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenMeshSettings() },
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp,
                    elevation = LinkoraElevation.elevationSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isMeshActive) LinkoraColors.OnlineStatus.copy(alpha = 0.20f)
                                        else glassColors.surfaceElevated,
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (isMeshActive) LinkoraColors.OnlineStatus.copy(alpha = 0.5f)
                                        else glassColors.border,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMeshActive) Icons.Rounded.Sensors else Icons.Rounded.SensorsOff,
                                    contentDescription = null,
                                    tint = if (isMeshActive) LinkoraColors.OnlineStatus else glassColors.iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                            Column {
                                Text(
                                    text = "Network & Mesh Settings",
                                    style = LinkoraTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = if (isMeshActive) "$connectedPeersCount nearby node(s) connected • Auto-mesh active"
                                    else "Mesh is offline • Tap to manage",
                                    style = LinkoraTypography.labelSmall,
                                    color = if (isMeshActive) LinkoraColors.OnlineStatus else glassColors.textSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Open Settings",
                            tint = glassColors.iconTint.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Notification & SMS Alert Options
            item {
                Text(
                    text = "NOTIFICATION & SMS ALERTS",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp,
                    elevation = LinkoraElevation.elevationCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Message Notifications Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (messageNotifEnabled) LinkoraColors.Emerald.copy(alpha = 0.18f)
                                            else glassColors.surfaceElevated,
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (messageNotifEnabled) LinkoraColors.Emerald.copy(alpha = 0.4f)
                                            else glassColors.border,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (messageNotifEnabled) LinkoraColors.Emerald else glassColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                                Column {
                                    Text(
                                        text = "New SMS & Message Alerts",
                                        style = LinkoraTypography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = glassColors.textPrimary
                                    )
                                    Text(
                                        text = "Show notification icon when new message arrives",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = messageNotifEnabled,
                                onCheckedChange = { checked ->
                                    messageNotifEnabled = checked
                                    NotificationPreferences.setMessageNotificationsEnabled(context, checked)
                                    Toast.makeText(
                                        context,
                                        if (checked) "Message notifications enabled" else "Message notifications silenced",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = LinkoraColors.Emerald,
                                    uncheckedThumbColor = glassColors.textSecondary,
                                    uncheckedTrackColor = glassColors.surfaceElevated
                                )
                            )
                        }

                        // 2. Heads-Up Top Dynamic Island Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (headsUpBannerEnabled) LinkoraColors.AccentSecondary.copy(alpha = 0.18f)
                                            else glassColors.surfaceElevated,
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (headsUpBannerEnabled) LinkoraColors.AccentSecondary.copy(alpha = 0.4f)
                                            else glassColors.border,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Sms,
                                        contentDescription = null,
                                        tint = if (headsUpBannerEnabled) LinkoraColors.AccentSecondary else glassColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                                Column {
                                    Text(
                                        text = "Show Alert on Top",
                                        style = LinkoraTypography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = glassColors.textPrimary
                                    )
                                    Text(
                                        text = "Liquid glass heads-up banner when new SMS arrives",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = headsUpBannerEnabled,
                                onCheckedChange = { checked ->
                                    headsUpBannerEnabled = checked
                                    NotificationPreferences.setHeadsUpBannerEnabled(context, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = LinkoraColors.AccentSecondary,
                                    uncheckedThumbColor = glassColors.textSecondary,
                                    uncheckedTrackColor = glassColors.surfaceElevated
                                )
                            )
                        }

                        // 3. Vibration & Sound
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (vibrationSoundEnabled) Color(0xFF38BDF8).copy(alpha = 0.18f)
                                            else glassColors.surfaceElevated,
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (vibrationSoundEnabled) Color(0xFF38BDF8).copy(alpha = 0.4f)
                                            else glassColors.border,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Vibration,
                                        contentDescription = null,
                                        tint = if (vibrationSoundEnabled) Color(0xFF38BDF8) else glassColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                                Column {
                                    Text(
                                        text = "Vibrate & Sound",
                                        style = LinkoraTypography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = glassColors.textPrimary
                                    )
                                    Text(
                                        text = "Haptic feedback pulse on arrival",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = vibrationSoundEnabled,
                                onCheckedChange = { checked ->
                                    vibrationSoundEnabled = checked
                                    NotificationPreferences.setVibrationSoundEnabled(context, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF38BDF8),
                                    uncheckedThumbColor = glassColors.textSecondary,
                                    uncheckedTrackColor = glassColors.surfaceElevated
                                )
                            )
                        }
                    }
                }
            }

            // Offline AirDrop & NFC NameDrop
            item {
                Text(
                    text = "CONTACTLESS NFC & AIRDROP",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NFC NameDrop & Touch Pairing",
                                    style = LinkoraTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = "Exchange cryptographic identity & safety keys by touching phones",
                                    style = LinkoraTypography.labelSmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Button(
                                onClick = { onNfcShareClick?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Sensors,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Security & App Lock (Biometrics & Passcode)
            item {
                Text(
                    text = "SECURITY & APP LOCK",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (appLockEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else glassColors.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = if (appLockEnabled) Color(0xFF10B981) else glassColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Biometric & Passcode Lock",
                                        style = LinkoraTypography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = glassColors.textPrimary
                                    )
                                    Text(
                                        text = "Require fingerprint, face, or PIN to unlock app",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = appLockEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        if (!biometricAuthManager.isDeviceSecure()) {
                                            Toast.makeText(
                                                context,
                                                "Please set up a screen lock (PIN/fingerprint) in Android Settings first",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            biometricAuthManager.setAppLockEnabled(true)
                                            appLockEnabled = true
                                            Toast.makeText(context, "App lock enabled", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        biometricAuthManager.setAppLockEnabled(false)
                                        appLockEnabled = false
                                        Toast.makeText(context, "App lock disabled", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981),
                                    uncheckedThumbColor = glassColors.textSecondary,
                                    uncheckedTrackColor = glassColors.surface
                                )
                            )
                        }
                    }
                }
            }

            // Cryptographic Fingerprint & Identity Cards
            item {
                Text(
                    text = "CRYPTOGRAPHIC SECURITY",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Safety Fingerprint
            item {
                SecurityInfoCard(
                    icon = Icons.Rounded.Fingerprint,
                    title = "Safety Fingerprint",
                    value = identityManager.safetyFingerprint,
                    description = "12-digit fingerprint verified during out-of-band QR scans",
                    onCopy = {
                        copyToClipboard(context, "Safety Fingerprint", identityManager.safetyFingerprint)
                    }
                )
            }

            // Identity ID
            item {
                SecurityInfoCard(
                    icon = Icons.Rounded.Shield,
                    title = "Identity ID",
                    value = identityManager.identityId,
                    description = "Cryptographically derived SHA-256 identifier",
                    onCopy = {
                        copyToClipboard(context, "Identity ID", identityManager.identityId)
                    }
                )
            }

            // Public Key
            item {
                SecurityInfoCard(
                    icon = Icons.Rounded.Key,
                    title = "ECDH P-256 Public Key",
                    value = identityManager.publicKeyBase64.take(48) + "...",
                    description = "Elliptic-curve key used for pairwise message encryption",
                    onCopy = {
                        copyToClipboard(context, "Public Key", identityManager.publicKeyBase64)
                    }
                )
            }
        }
    }
}

@Composable
private fun SecurityInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    description: String,
    onCopy: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        cornerRadius = 24.dp,
        elevation = LinkoraElevation.elevationSubtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = LinkoraColors.Emerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                    Text(
                        text = title,
                        style = LinkoraTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary
                    )
                }

                GlassIconButton(
                    icon = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    onClick = onCopy,
                    buttonSize = 32.dp,
                    iconSize = 15.dp
                )
            }

            Spacer(modifier = Modifier.height(LinkoraSpacing.space8))

            Text(
                text = value,
                style = LinkoraTypography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = if (glassColors.isDark) Color(0xFF67E8F9) else Color(0xFF0D9488),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = LinkoraTypography.labelSmall,
                color = glassColors.textSecondary
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, content: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, content))
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}
