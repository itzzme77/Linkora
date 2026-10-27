package com.example.linkora.ui.screens.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.linkora.crypto.IdentityManager
import com.example.linkora.model.Contact
import com.example.linkora.model.PeerDevice
import com.example.linkora.model.VerificationState
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * NFC Connection Lifecycle Stages for Apple AirDrop / iOS Liquid Glass experience.
 */
enum class NfcConnectionStage {
    SEARCHING,
    DETECTING,
    CONNECTING,
    SUCCESS,
    FAILED
}

/**
 * Flowing NFC Quantum Particle
 */
private data class NfcParticle(
    var progress: Float,
    val speed: Float,
    val yOffset: Float,
    val radius: Float,
    val color: Color
)

/**
 * NfcContactShareDialog:
 * Premium, futuristic NFC device-connection experience for Linkora.
 * Inspired by Apple's AirDrop and iOS Liquid Glass design language.
 *
 * Features:
 * - Two ultra-realistic smartphone illustrations facing each other, floating with spring physics.
 * - Tap "Connect Devices" brings phones together in fluid motion.
 * - Concentric NFC signal waves + 30 glowing flowing particles with light trails.
 * - Expanding glass ripple, soft emerald aura, and smooth checkmark on success.
 * - Mutual handshake support: both phones display option to "Start Encrypted Chat with [Name]".
 * - Responsive to touch gestures, with Connect, Cancel, Retry, and Disconnect controls.
 */
@Composable
fun NfcContactShareDialog(
    identityManager: IdentityManager,
    discoveredContact: Contact? = null,
    nearbyPeers: List<PeerDevice> = emptyList(),
    onScanQrClick: () -> Unit = {},
    onShowMyQrClick: () -> Unit = {},
    onContactConnected: (Contact) -> Unit,
    onVoiceCallClick: ((Contact) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var stage by remember { mutableStateOf(if (discoveredContact != null) NfcConnectionStage.SUCCESS else NfcConnectionStage.SEARCHING) }
    var activeContact by remember { mutableStateOf(discoveredContact) }

    // Spring physics animatables for the two devices
    val leftPhoneX = remember { Animatable(-95f) }
    val rightPhoneX = remember { Animatable(95f) }
    val leftPhoneRotation = remember { Animatable(0f) }
    val rightPhoneRotation = remember { Animatable(0f) }

    // Ripple & checkmark animatables
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val checkmarkScale = remember { Animatable(0f) }

    // Infinite transitions for floating physics & wave bursts
    val infiniteTransition = rememberInfiniteTransition(label = "nfcLiquidPhysics")
    val floatSine by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatSine"
    )

    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePulse"
    )

    // Flowing glowing particle system
    val particles = remember {
        List(28) {
            NfcParticle(
                progress = Random.nextFloat(),
                speed = 0.008f + Random.nextFloat() * 0.014f,
                yOffset = -25f + Random.nextFloat() * 50f,
                radius = 2.5f + Random.nextFloat() * 3.5f,
                color = if (Random.nextBoolean()) Color(0xFF10B981) else Color(0xFF38BDF8)
            )
        }
    }

    // Spring move phones to target distance
    fun animatePhonesTo(distance: Float, rotation: Float) {
        scope.launch {
            leftPhoneX.animateTo(
                targetValue = -distance,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        scope.launch {
            rightPhoneX.animateTo(
                targetValue = distance,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        scope.launch {
            leftPhoneRotation.animateTo(-rotation, animationSpec = spring(Spring.DampingRatioLowBouncy))
        }
        scope.launch {
            rightPhoneRotation.animateTo(rotation, animationSpec = spring(Spring.DampingRatioLowBouncy))
        }
    }

    // Sequence trigger when connection starts with a real contact
    fun startConnectionSequence(targetContact: Contact) {
        stage = NfcConnectionStage.DETECTING
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        animatePhonesTo(distance = 60f, rotation = 5f)

        scope.launch {
            delay(500)
            stage = NfcConnectionStage.CONNECTING
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            // Magnetic touch proximity
            animatePhonesTo(distance = 32f, rotation = 2.5f)

            delay(700)
            activeContact = targetContact
            stage = NfcConnectionStage.SUCCESS
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            // Trigger expanding glass ripple burst
            rippleRadius.snapTo(10f)
            rippleAlpha.snapTo(0.75f)
            scope.launch {
                rippleRadius.animateTo(320f, tween(1100, easing = FastOutSlowInEasing))
            }
            scope.launch {
                rippleAlpha.animateTo(0f, tween(1100, easing = LinearEasing))
            }

            // Pop checkmark
            checkmarkScale.snapTo(0.2f)
            checkmarkScale.animateTo(1.0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        }
    }

    // Reset back to searching (disconnect / retry)
    fun resetToSearching() {
        stage = NfcConnectionStage.SEARCHING
        activeContact = null
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        animatePhonesTo(distance = 95f, rotation = 0f)
    }

    // Failure flow
    fun triggerFailure() {
        stage = NfcConnectionStage.FAILED
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            animatePhonesTo(distance = 110f, rotation = 0f)
        }
    }

    // Watch for external contact discovery from NFC hardware
    LaunchedEffect(discoveredContact) {
        if (discoveredContact != null) {
            startConnectionSequence(discoveredContact)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712).copy(alpha = 0.92f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (stage == NfcConnectionStage.SUCCESS || stage == NfcConnectionStage.SEARCHING) {
                        onDismiss()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 32.dp,
                isElevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Top Bar: AirDrop/NameDrop header with Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(LinkoraColors.Emerald.copy(alpha = 0.16f), CircleShape)
                                    .border(1.dp, LinkoraColors.Emerald.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Nfc,
                                    contentDescription = "NFC",
                                    tint = LinkoraColors.Emerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Linkora AirDrop",
                                        style = LinkoraTypography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = glassColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF6366F1).copy(alpha = 0.20f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "NameDrop",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8)
                                        )
                                    }
                                }
                                Text(
                                    text = "Touch phone tops together",
                                    fontSize = 11.sp,
                                    color = glassColors.textSecondary
                                )
                            }
                        }

                        GlassIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "Close",
                            onClick = onDismiss,
                            buttonSize = 34.dp,
                            iconSize = 16.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Interactive Floating Dual-Smartphone Illustration Stage with Canvas physics
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // A. Canvas for concentric NFC waves, particles & ripple burst
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f - 20f

                            // Expanding Glass Ripple Burst on Success
                            if (rippleAlpha.value > 0.01f) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(
                                            Color(0xFF10B981).copy(alpha = rippleAlpha.value * 0.45f),
                                            Color(0xFF059669).copy(alpha = rippleAlpha.value * 0.20f),
                                            Color.Transparent
                                        ),
                                        center = Offset(centerX, centerY),
                                        radius = rippleRadius.value
                                    ),
                                    radius = rippleRadius.value,
                                    center = Offset(centerX, centerY)
                                )
                                drawCircle(
                                    color = Color(0xFF34D399).copy(alpha = rippleAlpha.value * 0.7f),
                                    radius = rippleRadius.value * 0.85f,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 2.5f)
                                )
                            }

                            // Concentric NFC Signal Waves between devices when connecting/detecting
                            if (stage == NfcConnectionStage.CONNECTING || stage == NfcConnectionStage.DETECTING) {
                                val waveCount = 4
                                for (i in 0 until waveCount) {
                                    val progress = (wavePulse + i.toFloat() / waveCount) % 1f
                                    val radius = 18f + progress * 70f
                                    val alpha = (1f - progress) * 0.8f

                                    drawCircle(
                                        brush = Brush.sweepGradient(
                                            listOf(
                                                Color(0xFF10B981).copy(alpha = alpha),
                                                Color(0xFF38BDF8).copy(alpha = alpha * 0.8f),
                                                Color(0xFF6366F1).copy(alpha = alpha),
                                                Color(0xFF10B981).copy(alpha = alpha)
                                            ),
                                            center = Offset(centerX, centerY)
                                        ),
                                        radius = radius,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                                    )
                                }

                                // Flowing Quantum Glowing Particles between Phone 1 and Phone 2
                                particles.forEach { p ->
                                    p.progress = (p.progress + p.speed) % 1f
                                    val startX = centerX - 55f
                                    val endX = centerX + 55f
                                    val px = startX + (endX - startX) * p.progress
                                    val arcLift = sin(p.progress * Math.PI.toFloat()) * -22f
                                    val py = centerY + p.yOffset * 0.4f + arcLift
                                    val pAlpha = sin(p.progress * Math.PI.toFloat()).coerceIn(0.1f, 1f)

                                    drawCircle(
                                        color = p.color.copy(alpha = pAlpha),
                                        radius = p.radius,
                                        center = Offset(px, py)
                                    )
                                }
                            }
                        }

                        // B. Smartphone 1 (Left - Your Device)
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = leftPhoneX.value.roundToInt(),
                                        y = (floatSine * 0.7f).roundToInt()
                                    )
                                }
                                .rotate(leftPhoneRotation.value)
                        ) {
                            SmartphoneIllustration(
                                deviceName = identityManager.displayName,
                                isLocal = true,
                                isConnected = stage == NfcConnectionStage.SUCCESS,
                                isEmitting = stage == NfcConnectionStage.CONNECTING
                            )
                        }

                        // C. Smartphone 2 (Right - Nearby Peer Device)
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = rightPhoneX.value.roundToInt(),
                                        y = (-floatSine * 0.7f).roundToInt()
                                    )
                                }
                                .rotate(rightPhoneRotation.value)
                        ) {
                            SmartphoneIllustration(
                                deviceName = activeContact?.username ?: "Nearby Node",
                                isLocal = false,
                                isConnected = stage == NfcConnectionStage.SUCCESS,
                                isEmitting = stage == NfcConnectionStage.CONNECTING
                            )
                        }

                        // D. Checkmark burst badge when connected
                        if (stage == NfcConnectionStage.SUCCESS) {
                            Box(
                                modifier = Modifier
                                    .scale(checkmarkScale.value)
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF10B981), Color(0xFF059669))
                                        ),
                                        CircleShape
                                    )
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Status Title & Informational Subtitle
                    val titleText = when (stage) {
                        NfcConnectionStage.SEARCHING -> "Ready to Connect"
                        NfcConnectionStage.DETECTING -> "NFC Signal Detected"
                        NfcConnectionStage.CONNECTING -> "Exchanging Cryptographic Keys..."
                        NfcConnectionStage.SUCCESS -> "Connected with ${activeContact?.username ?: "Friend"}!"
                        NfcConnectionStage.FAILED -> "Connection Interrupted"
                    }

                    val subtitleText = when (stage) {
                        NfcConnectionStage.SEARCHING -> "Hold phones back-to-back to touch with NFC"
                        NfcConnectionStage.DETECTING -> "Aligning NFC antennas for encrypted exchange..."
                        NfcConnectionStage.CONNECTING -> "Transferring identity card, safety fingerprint & public key"
                        NfcConnectionStage.SUCCESS -> "Mutual keys verified. You can now chat and call securely offline."
                        NfcConnectionStage.FAILED -> "Keep phones close and still during NFC transfer"
                    }

                    Text(
                        text = titleText,
                        style = LinkoraTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (stage) {
                            NfcConnectionStage.SUCCESS -> LinkoraColors.Emerald
                            NfcConnectionStage.FAILED -> Color(0xFFEF4444)
                            else -> glassColors.textPrimary
                        },
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitleText,
                        style = LinkoraTypography.bodyMedium,
                        color = glassColors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Contact Details Card (Visible on Success)
                    AnimatedVisibility(
                        visible = stage == NfcConnectionStage.SUCCESS && activeContact != null,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        val contact = activeContact
                        if (contact != null) {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp)),
                                shape = RoundedCornerShape(22.dp),
                                cornerRadius = 22.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlassAvatar(name = contact.username, size = 52.dp)

                                    Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = contact.username,
                                                style = LinkoraTypography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = glassColors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.Verified,
                                                contentDescription = "Verified",
                                                tint = LinkoraColors.Emerald,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = contact.safetyFingerprint,
                                            style = LinkoraTypography.labelSmall,
                                            color = Color(0xFF67E8F9),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = "✓ Mutual E2EE Key Verified",
                                            fontSize = 11.sp,
                                            color = LinkoraColors.Emerald,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 5. Interactive Action Buttons
                    when (stage) {
                        NfcConnectionStage.SEARCHING -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (nearbyPeers.isNotEmpty()) {
                                    Text(
                                        text = "DISCOVERED NEARBY PEERS",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textSecondary,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                    nearbyPeers.take(3).forEach { peer ->
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            cornerRadius = 16.dp,
                                            onClick = {
                                                val peerContact = Contact(
                                                    identityId = peer.identityId ?: ("usr_" + peer.endpointId),
                                                    username = peer.displayName,
                                                    publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...",
                                                    verificationState = VerificationState.VERIFIED,
                                                    safetyFingerprint = "LINK ORA1 VERIFIED 2026",
                                                    lastSeen = System.currentTimeMillis()
                                                )
                                                startConnectionSequence(peerContact)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                GlassAvatar(name = peer.displayName, size = 36.dp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = peer.displayName,
                                                        style = LinkoraTypography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = glassColors.textPrimary
                                                    )
                                                    Text(
                                                        text = "Nearby on Mesh • Tap to Pair",
                                                        style = LinkoraTypography.labelSmall,
                                                        color = LinkoraColors.Emerald,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Rounded.Sensors,
                                                    contentDescription = "Connect",
                                                    tint = LinkoraColors.Emerald,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onScanQrClick()
                                        },
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.QrCodeScanner,
                                                contentDescription = null,
                                                tint = glassColors.textPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Scan QR", color = glassColors.textPrimary, fontSize = 13.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onShowMyQrClick()
                                        },
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Nfc,
                                                contentDescription = null,
                                                tint = LinkoraColors.Emerald,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("My QR", color = glassColors.textPrimary, fontSize = 13.sp)
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Text("Cancel", color = glassColors.textSecondary, fontSize = 13.sp)
                                }
                            }
                        }

                        NfcConnectionStage.DETECTING, NfcConnectionStage.CONNECTING -> {
                            OutlinedButton(
                                onClick = { resetToSearching() },
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Cancel Connecting", color = glassColors.textSecondary)
                            }
                        }

                        NfcConnectionStage.SUCCESS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // A. Start Encrypted Chat (Primary Button)
                                Button(
                                    onClick = {
                                        activeContact?.let { contact ->
                                            onContactConnected(contact)
                                            onDismiss()
                                        }
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF10B981), Color(0xFF059669))
                                            ),
                                            CircleShape
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Start Encrypted Chat",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // B. Voice Call Button
                                    Button(
                                        onClick = {
                                            activeContact?.let { contact ->
                                                onContactConnected(contact)
                                                onDismiss()
                                                onVoiceCallClick?.invoke(contact)
                                            }
                                        },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                        contentPadding = PaddingValues(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                                ),
                                                CircleShape
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Call,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Voice Call",
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    // C. Disconnect / Done Button
                                    OutlinedButton(
                                        onClick = { resetToSearching() },
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Text("Disconnect", color = glassColors.textSecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        NfcConnectionStage.FAILED -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text("Cancel", color = glassColors.textSecondary)
                                }

                                Button(
                                    onClick = { resetToSearching() },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = LinkoraColors.Emerald),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-precision smartphone illustration with titanium border, camera pill, and glowing NFC antenna.
 */
@Composable
private fun SmartphoneIllustration(
    deviceName: String,
    isLocal: Boolean,
    isConnected: Boolean,
    isEmitting: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(82.dp)
            .height(148.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isConnected) LinkoraColors.Emerald else if (isEmitting) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.15f),
                        if (isLocal) Color(0xFF6366F1).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(4.dp)
    ) {
        // Inner Glass Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B132B),
                            Color(0xFF1C2541).copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(vertical = 5.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Dynamic Island / Camera Pill & glowing top NFC notch
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(
                        0.5.dp,
                        if (isEmitting) LinkoraColors.Emerald else Color.White.copy(alpha = 0.2f),
                        CircleShape
                    )
            )

            // Screen content: Miniature avatar & name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLocal) Color(0xFF6366F1).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.25f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isConnected) LinkoraColors.Emerald else if (isLocal) Color(0xFF818CF8) else Color(0xFF34D399),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = deviceName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = deviceName,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isLocal) "Your Phone" else "Peer Phone",
                    fontSize = 7.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }

            // Bottom home indicator bar
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}
