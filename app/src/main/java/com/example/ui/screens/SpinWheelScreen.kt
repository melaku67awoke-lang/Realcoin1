package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.CryptoCardBg
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardVariant
import com.example.ui.theme.CryptoDarkBg
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.RealGoldLight
import com.example.ui.theme.RealGoldPrimary
import com.example.ui.theme.RealGoldSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class WheelSliceData(
    val id: Int,
    val label: String,
    val prizeAmount: Double,
    val color: Color,
    val isJackpot: Boolean = false
)

private const val SPIN_COST_REAL = 10.0
private const val FREE_SPIN_COOLDOWN_MS = 24 * 60 * 60 * 1000L

@Composable
fun SpinWheelScreen(
    user: UserEntity?,
    onBackClick: () -> Unit,
    onExecuteSpin: (isFree: Boolean, prizeWon: Double, onComplete: (Result<Double>) -> Unit) -> Unit,
    onResetDailySpin: () -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }

    // Celebration modal state
    var showWinnerModal by remember { mutableStateOf(false) }
    var wonAmount by remember { mutableDoubleStateOf(0.0) }
    var wonLabel by remember { mutableStateOf("") }
    var wasSpinFree by remember { mutableStateOf(false) }
    var isJackpotWin by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val userBalance = user?.realCoinBalance ?: 0.0

    // Current time ticker for remaining time calculation
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMs = System.currentTimeMillis()
        }
    }

    val lastFreeSpin = user?.lastFreeSpinTimestamp ?: 0L
    val isFreeSpinAvailable = lastFreeSpin == 0L || (currentTimeMs - lastFreeSpin >= FREE_SPIN_COOLDOWN_MS)
    val remainingMs = if (!isFreeSpinAvailable) {
        (lastFreeSpin + FREE_SPIN_COOLDOWN_MS - currentTimeMs).coerceAtLeast(0L)
    } else 0L

    val remainingHours = (remainingMs / (1000 * 60 * 60))
    val remainingMinutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
    val remainingSeconds = (remainingMs % (1000 * 60)) / 1000

    // Calibrated wheel sectors for 10 Realcoin cost & prizes
    val wheelSlices = remember {
        listOf(
            WheelSliceData(0, "+15 RC", 15.0, Color(0xFF0284C7)),
            WheelSliceData(1, "+5 RC", 5.0, Color(0xFF475569)),
            WheelSliceData(2, "+25 RC", 25.0, Color(0xFF059669)),
            WheelSliceData(3, "0 RC", 0.0, Color(0xFF334155)),
            WheelSliceData(4, "+10 RC", 10.0, Color(0xFF6366F1)),
            WheelSliceData(5, "+50 RC", 50.0, Color(0xFFD97706)),
            WheelSliceData(6, "+20 RC", 20.0, Color(0xFF0D9488)),
            WheelSliceData(7, "JACKPOT 200★", 200.0, RealGoldPrimary, isJackpot = true)
        )
    }

    // Trigger spinning animation and outcome determination
    fun triggerSpin() {
        if (isSpinning) return

        val isFree = isFreeSpinAvailable
        // If not free spin, must have at least 10 Realcoin
        if (!isFree && userBalance < SPIN_COST_REAL) {
            return
        }

        isSpinning = true
        val targetIndex = Random.nextInt(wheelSlices.size)
        val winningSlice = wheelSlices[targetIndex]
        val sweepAngle = 360f / wheelSlices.size

        // In canvas coordinates:
        // Angle 0 is at 3 o'clock. 270 deg is at 12 o'clock (top pointer needle).
        val sliceCenter = (targetIndex * sweepAngle) + (sweepAngle / 2f)
        var offsetAngle = (270f - sliceCenter) % 360f
        if (offsetAngle < 0) offsetAngle += 360f

        // 6 full revolutions (2160 degrees) + offset angle
        val targetRotation = 360f * 6 + offsetAngle

        scope.launch {
            rotation.snapTo(0f)
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(durationMillis = 3800, easing = FastOutSlowInEasing)
            )

            // Animation done: persist result in database
            onExecuteSpin(isFree, winningSlice.prizeAmount) { result ->
                result.onSuccess { won ->
                    wonAmount = won
                    wonLabel = winningSlice.label
                    wasSpinFree = isFree
                    isJackpotWin = winningSlice.isJackpot
                    isSpinning = false
                    showWinnerModal = true
                }.onFailure {
                    isSpinning = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CryptoDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lucky Spinning Wheel",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cost: 10 RC / spin • 1 Free Spin Daily",
                    color = RealGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Quick reset button for demo convenience
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoCardVariant)
                    .clickable(enabled = !isSpinning) { onResetDailySpin() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Daily Free Spin",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Reset Free", color = TextSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- BALANCE & DAILY FREE STATUS CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Wallet Balance", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${String.format("%.2f", userBalance)} REAL",
                            color = RealGoldPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Spin Pricing Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RealGoldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = RealGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "10 RC / Spin",
                            color = RealGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Free Status Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isFreeSpinAvailable) CryptoGreen.copy(alpha = 0.15f)
                            else CryptoCardVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFreeSpinAvailable) CryptoGreen.copy(alpha = 0.5f) else CryptoCardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFreeSpinAvailable) Icons.Default.Redeem else Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isFreeSpinAvailable) CryptoGreen else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isFreeSpinAvailable) "1 Daily Free Spin Available!" else "Daily Free Spin Used",
                                    color = if (isFreeSpinAvailable) CryptoGreen else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isFreeSpinAvailable) "Cost: 0 Realcoin for this spin"
                                    else "Subsequent spins cost 10 Realcoin each",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!isFreeSpinAvailable) {
                            Text(
                                text = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds),
                                color = RealGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CryptoGreen)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "FREE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- ANIMATED SPINNING WHEEL COMPONENT ---
        SpinningWheelComponent(
            slices = wheelSlices,
            rotationAngle = rotation.value,
            isSpinning = isSpinning,
            onCenterSpinClick = { triggerSpin() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // --- SPIN TRIGGER BUTTON ---
        val canSpin = if (isFreeSpinAvailable) {
            !isSpinning
        } else {
            !isSpinning && userBalance >= SPIN_COST_REAL
        }

        Button(
            onClick = { triggerSpin() },
            enabled = canSpin,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFreeSpinAvailable) CryptoGreen else RealGoldPrimary,
                contentColor = Color.Black,
                disabledContainerColor = CryptoCardVariant,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("spin_wheel_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isFreeSpinAvailable) Icons.Default.Redeem else Icons.Default.Casino,
                    contentDescription = "Spin",
                    tint = if (canSpin) Color.Black else TextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isSpinning -> "WHEEL SPINNING..."
                        isFreeSpinAvailable -> "SPIN NOW (DAILY FREE SPIN)"
                        userBalance < SPIN_COST_REAL -> "INSUFFICIENT BALANCE (NEEDS 10 RC)"
                        else -> "SPIN WHEEL (COST: 10 REALCOIN)"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Contextual rule helper
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isFreeSpinAvailable) {
                    "Your 1 daily free spin is ready. Extra spins cost 10 Realcoin."
                } else {
                    "Each spin deducts 10 RC from your wallet. 1 free spin recharges in 24 hours."
                },
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- PRIZE POOL BREAKDOWN ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Wheel Prizes (10 RC Per Spin)",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        PrizeRowItem("★ Mega Jackpot", "200 REAL", RealGoldPrimary)
                        PrizeRowItem("Big Win", "50 REAL", Color(0xFFD97706))
                        PrizeRowItem("Major Win", "25 REAL", CryptoGreen)
                        PrizeRowItem("Double Win", "20 REAL", Color(0xFF0D9488))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        PrizeRowItem("Profit Win", "15 REAL", Color(0xFF0284C7))
                        PrizeRowItem("Break Even", "10 REAL", Color(0xFF6366F1))
                        PrizeRowItem("Half Return", "5 REAL", Color(0xFF475569))
                        PrizeRowItem("Try Again", "0 REAL", TextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- RECENT SPINS TICKER ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Spin Winners",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live Community",
                        color = CryptoGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    RecentSpinRecord("Verified Trader #924", "Won +200 RC (Jackpot!)", "2m ago", RealGoldPrimary),
                    RecentSpinRecord("VIP Member #118", "Won +50 RC", "6m ago", Color(0xFFD97706)),
                    RecentSpinRecord("Player #305", "Daily Free Spin (+25 RC)", "14m ago", CryptoGreen),
                    RecentSpinRecord("Trader #641", "Won +15 RC", "28m ago", Color(0xFF0284C7))
                ).forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = record.user, color = TextSecondary, fontSize = 12.sp)
                        Text(text = record.outcome, color = record.color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = record.time, color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }

    // --- CELEBRATION MODAL ON TRIGGER OUTCOME ---
    if (showWinnerModal) {
        Dialog(onDismissRequest = { showWinnerModal = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                shape = RoundedCornerShape(22.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(RealGoldPrimary)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy / Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (wonAmount > 0.0) RealGoldPrimary.copy(alpha = 0.2f) else TextMuted.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (wonAmount > 0.0) Icons.Default.EmojiEvents else Icons.Default.Casino,
                            contentDescription = "Trophy",
                            tint = if (wonAmount > 0.0) RealGoldPrimary else TextSecondary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            isJackpotWin -> "🎉 MEGA JACKPOT 200 RC! 🎉"
                            wonAmount > 10.0 -> "🎉 YOU WON REAL COIN! 🎉"
                            wonAmount == 10.0 -> "Break Even Result!"
                            wonAmount > 0.0 -> "Prize Won!"
                            else -> "Nice Try!"
                        },
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (wasSpinFree) "Daily Free Spin claimed (Cost: 0 RC)"
                        else "Paid Spin (Cost: 10 RC deducted)",
                        color = if (wasSpinFree) CryptoGreen else RealGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CryptoCardVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PRIZE WON",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${String.format("%.2f", wonAmount)} REAL",
                                color = RealGoldPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (!wasSpinFree) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val net = wonAmount - SPIN_COST_REAL
                                Text(
                                    text = if (net >= 0) "Net Profit: +${String.format("%.2f", net)} RC"
                                    else "Net Change: ${String.format("%.2f", net)} RC",
                                    color = if (net > 0) CryptoGreen else if (net == 0.0) TextSecondary else CryptoRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showWinnerModal = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RealGoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Claim & Continue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrizeRowItem(label: String, amount: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = amount, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// =========================================================================
// REUSABLE SPINNING WHEEL COMPONENT
// =========================================================================
@Composable
fun SpinningWheelComponent(
    slices: List<WheelSliceData>,
    rotationAngle: Float,
    isSpinning: Boolean,
    onCenterSpinClick: () -> Unit
) {
    // Top needle wobble calculation during rotation
    val needleAngle by animateFloatAsState(
        targetValue = if (isSpinning) (sin(rotationAngle * 0.12f) * 6f) else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "NeedleWobble"
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Outer Rim Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension / 2f - 4.dp.toPx()

            // Outer golden rim
            drawCircle(
                color = Color(0xFFCA8A04),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 12.dp.toPx())
            )

            // Inner gold ring accent
            drawCircle(
                color = RealGoldPrimary,
                radius = outerRadius - 6.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Decorative bulb lights around the perimeter
            val bulbCount = 16
            for (i in 0 until bulbCount) {
                val angle = Math.toRadians((i * (360.0 / bulbCount)))
                val bulbRadius = outerRadius - 6.dp.toPx()
                val bx = center.x + (bulbRadius * cos(angle)).toFloat()
                val by = center.y + (bulbRadius * sin(angle)).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) Color.White else RealGoldLight,
                    radius = 3.dp.toPx(),
                    center = Offset(bx, by)
                )
            }
        }

        // ROTATING CANVAS WHEEL
        Canvas(
            modifier = Modifier
                .size(266.dp)
                .rotate(rotationAngle)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            val sweepAngle = 360f / slices.size

            slices.forEachIndexed { index, slice ->
                val startAngle = index * sweepAngle

                // Slice Arc
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Divider line between slices
                val rad = Math.toRadians(startAngle.toDouble())
                val lineEnd = Offset(
                    center.x + (radius * cos(rad)).toFloat(),
                    center.y + (radius * sin(rad)).toFloat()
                )
                drawLine(
                    color = Color(0xFF0F172A),
                    start = center,
                    end = lineEnd,
                    strokeWidth = 2.dp.toPx()
                )

                // Label drawn onto slice
                val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                val textDist = radius * 0.65f
                val textX = center.x + (textDist * cos(midAngleRad)).toFloat()
                val textY = center.y + (textDist * sin(midAngleRad)).toFloat() + 5.dp.toPx()

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                    drawText(slice.label, textX, textY, paint)
                }
            }

            // Outer perimeter circle stroke
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // CENTER SPIN HUB BUTTON
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(RealGoldLight, RealGoldPrimary, RealGoldSecondary)
                    )
                )
                .border(2.5.dp, Color.White, CircleShape)
                .clickable(enabled = !isSpinning) { onCenterSpinClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SPIN",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }

        // TOP NEEDLE POINTER (Fixed at top, pointing down into the wheel with wobble)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .rotate(needleAngle)
        ) {
            Canvas(modifier = Modifier.size(32.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(size.width * 0.2f, 0f)
                    lineTo(size.width * 0.8f, 0f)
                    close()
                }
                drawPath(path, color = Color.White)
                drawPath(path, color = Color(0xFFDC2626), style = Stroke(width = 2.5.dp.toPx()))
                drawCircle(
                    color = Color(0xFFDC2626),
                    radius = 4.dp.toPx(),
                    center = Offset(size.width / 2f, 8.dp.toPx())
                )
            }
        }
    }
}

data class RecentSpinRecord(
    val user: String,
    val outcome: String,
    val time: String,
    val color: Color
)
