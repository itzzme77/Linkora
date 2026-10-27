package com.example.linkora.ui.screens.radar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.SensorsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.model.PeerDevice
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
 * RadarScreen: Shows network connectivity status, peer discovery radar, and node connection controls.
 * Accessed under User Profile -> Settings.
 */
@Composable
fun RadarScreen(
    isMeshActive: Boolean,
    peers: List<PeerDevice>,
    onToggleMesh: (Boolean) -> Unit,
    onConnectPeer: (endpointId: String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header with optional Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LinkoraSpacing.space16, vertical = LinkoraSpacing.space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to Settings",
                    onClick = onBackClick
                )
                Spacer(modifier = Modifier.width(LinkoraSpacing.space12))
            }

            Column {
                Text(
                    text = "Network & Connectivity",
                    style = LinkoraTypography.displayLarge,
                    color = glassColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = if (isMeshActive) "Mesh active • Automatically connected to nearby devices" else "Offline • Tap switch to enable mesh",
                    style = LinkoraTypography.bodyMedium,
                    color = if (isMeshActive) LinkoraColors.OnlineStatus else glassColors.textSecondary,
                    fontSize = 13.sp
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
            // Main Connection Status & Toggle Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    cornerRadius = 28.dp,
                    elevation = LinkoraElevation.elevationCard,
                    isElevated = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isMeshActive) LinkoraColors.OnlineStatus.copy(alpha = 0.25f)
                                        else glassColors.surfaceElevated,
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (isMeshActive) LinkoraColors.OnlineStatus else glassColors.border,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMeshActive) Icons.Rounded.Sensors else Icons.Rounded.SensorsOff,
                                    contentDescription = null,
                                    tint = if (isMeshActive) LinkoraColors.OnlineStatus else glassColors.iconTint,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                            Column {
                                Text(
                                    text = if (isMeshActive) "Mesh Connection: ON" else "Mesh Connection: OFF",
                                    style = LinkoraTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = "${peers.size} nearby node(s) connected",
                                    style = LinkoraTypography.labelSmall,
                                    color = glassColors.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isMeshActive,
                            onCheckedChange = onToggleMesh,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }

            // Radar Visualizer
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(28.dp),
                    cornerRadius = 28.dp
                ) {
                    RadarCanvasView(isScanning = isMeshActive, peerCount = peers.size)
                }
            }

            // Network Information Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    cornerRadius = 20.dp,
                    elevation = LinkoraElevation.elevationSubtle
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "AUTOMATIC OFFLINE MESH",
                            style = LinkoraTypography.labelSmall,
                            color = Color(0xFF67E8F9),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Linkora automatically links directly with nearby devices over encrypted Bluetooth & Wi-Fi Direct. Messages securely hop from peer to peer with zero internet, cellular data, or servers required.",
                            style = LinkoraTypography.bodyMedium,
                            color = glassColors.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Nearby Discovered Peers Section
            item {
                Text(
                    text = "NEARBY PEERS (${peers.size})",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            if (peers.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        cornerRadius = 24.dp
                    ) {
                        Text(
                            text = if (isMeshActive) "Scanning for nearby Bluetooth LE & Wi-Fi Direct devices..." else "Turn on Mesh Connection to discover peers.",
                            style = LinkoraTypography.bodyMedium,
                            color = glassColors.textPrimary,
                            modifier = Modifier.padding(LinkoraSpacing.space16)
                        )
                    }
                }
            } else {
                items(peers, key = { it.endpointId }) { peer ->
                    PeerRowItem(
                        peer = peer,
                        onConnectClick = { onConnectPeer(peer.endpointId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RadarCanvasView(isScanning: Boolean, peerCount: Int) {
    val glassColors = LocalGlassColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) * 0.42f

            // Concentric radar circles
            for (i in 1..3) {
                drawCircle(
                    color = glassColors.border.copy(alpha = 0.35f),
                    radius = maxRadius * (i / 3f),
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Crosshair lines
            drawLine(
                color = glassColors.border.copy(alpha = 0.25f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = glassColors.border.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )

            // Scanning sweep arm
            if (isScanning) {
                val angleRad = Math.toRadians(sweepAngle.toDouble())
                val endX = center.x + (maxRadius * kotlin.math.cos(angleRad)).toFloat()
                val endY = center.y + (maxRadius * kotlin.math.sin(angleRad)).toFloat()

                drawLine(
                    color = glassColors.accent.copy(alpha = 0.8f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Center device node
            drawCircle(
                color = glassColors.accent,
                radius = 6.dp.toPx(),
                center = center
            )
        }

        Text(
            text = if (isScanning) "RADAR ACTIVE • $peerCount NODES" else "RADAR PAUSED",
            style = LinkoraTypography.labelSmall,
            color = if (isScanning) LinkoraColors.OnlineStatus else glassColors.textTertiary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun PeerRowItem(
    peer: PeerDevice,
    onConnectClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        cornerRadius = 24.dp,
        elevation = LinkoraElevation.elevationSubtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                GlassAvatar(name = peer.displayName, size = 46.dp)

                Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                Column {
                    Text(
                        text = peer.displayName,
                        style = LinkoraTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary
                    )

                    Text(
                        text = "Endpoint: ${peer.endpointId} • ${if (peer.isConnected) "Connected" else "Discovered"}",
                        style = LinkoraTypography.labelSmall,
                        color = if (peer.isConnected) LinkoraColors.OnlineStatus else glassColors.textSecondary
                    )
                }
            }

            if (!peer.isConnected) {
                Button(
                    onClick = onConnectClick,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Connect", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LinkoraColors.OnlineStatus.copy(alpha = 0.25f), CircleShape)
                        .border(1.dp, LinkoraColors.OnlineStatus, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "PAIRED",
                        color = LinkoraColors.OnlineStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
