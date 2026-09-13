package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.data.model.UserLevelProgress
import com.example.data.model.UserLevelTier
import com.example.ui.theme.CryptoCardBg
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardVariant
import com.example.ui.theme.CryptoDarkBg
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.RealGoldLight
import com.example.ui.theme.RealGoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UserLevelScreen(
    user: UserEntity?,
    onDismiss: () -> Unit,
    onNavigateDeposit: () -> Unit,
    onClaimDailyReward: () -> Unit
) {
    val totalDeposit = user?.usdtDepositTotal ?: 0.0
    val levelProgress = remember(totalDeposit) {
        UserLevelTier.calculateProgress(totalDeposit)
    }

    // Daily reward cooldown calculation
    val now = System.currentTimeMillis()
    val lastReward = user?.lastDailyRewardTimestamp ?: 0L
    val canClaimReward = now - lastReward >= 24 * 3600 * 1000L

    // Deposit Calculator state
    var simulatedAddDepositInput by remember { mutableStateOf("") }
    val simulatedAdditionalAmount = simulatedAddDepositInput.toDoubleOrNull() ?: 0.0
    val simulatedTotalDeposit = (totalDeposit + simulatedAdditionalAmount).coerceAtLeast(0.0)
    val simulatedProgress = remember(simulatedTotalDeposit) {
        UserLevelTier.calculateProgress(simulatedTotalDeposit)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CryptoDarkBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("level_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "VIP Levels & Rewards",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Volume-based daily reward multiplier",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Hero Level & Multiplier Card
                item {
                    UserHeroLevelCard(
                        user = user,
                        progress = levelProgress,
                        canClaimReward = canClaimReward,
                        onClaimDailyReward = onClaimDailyReward
                    )
                }

                // 2. Interactive Track Bar / Progress Bar Card
                item {
                    LevelTrackBarCard(
                        progress = levelProgress,
                        onNavigateDeposit = onNavigateDeposit
                    )
                }

                // 3. Overall VIP Milestone Roadmap Track
                item {
                    MilestoneRoadmapTrackCard(
                        totalDeposit = totalDeposit,
                        currentTier = levelProgress.currentTier
                    )
                }

                // 4. Interactive Deposit Volume Calculator
                item {
                    DepositVolumeCalculatorCard(
                        currentDeposit = totalDeposit,
                        simulatedInput = simulatedAddDepositInput,
                        onInputChange = { simulatedAddDepositInput = it },
                        simulatedProgress = simulatedProgress,
                        onQuickAdd = { add ->
                            simulatedAddDepositInput = add.toString()
                        },
                        onDepositNow = onNavigateDeposit
                    )
                }

                // 5. Tier Breakdown Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level Tiers & Multipliers",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "6 Tiers Available",
                            color = RealGoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 6. Tier Cards List (Starter to VIP)
                items(UserLevelTier.ALL_TIERS) { tier ->
                    TierDetailCard(
                        tier = tier,
                        userDeposit = totalDeposit,
                        isCurrentTier = tier == levelProgress.currentTier && (tier != UserLevelTier.STARTER || levelProgress.isStarterUnlocked),
                        onDepositClick = onNavigateDeposit
                    )
                }
            }
        }
    }
}

@Composable
private fun UserHeroLevelCard(
    user: UserEntity?,
    progress: UserLevelProgress,
    canClaimReward: Boolean,
    onClaimDailyReward: () -> Unit
) {
    val tier = progress.currentTier

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(tier.gradientStart, tier.gradientEnd))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(tier.gradientStart, tier.gradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Tier Badge",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${tier.levelName} Level",
                                color = TextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tier.badgeColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = progress.rewardMultiplierDisplay,
                                    color = tier.badgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "@${user?.username ?: "hab"}",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                // Daily Reward Claim Button
                Button(
                    onClick = onClaimDailyReward,
                    enabled = canClaimReward,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RealGoldPrimary,
                        contentColor = Color.Black,
                        disabledContainerColor = CryptoCardVariant,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("hero_claim_daily_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Redeem,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (canClaimReward) "Claim" else "Claimed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Key Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F131C))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    label = "Total Deposit",
                    value = "$${String.format("%.2f", progress.totalDepositUsd)}",
                    accentColor = RealGoldPrimary
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(CryptoCardBorder)
                )
                StatColumn(
                    label = "Daily Reward",
                    value = "${String.format("%.0f", progress.dailyRewardReal)} REAL",
                    accentColor = CryptoGreen
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(CryptoCardBorder)
                )
                StatColumn(
                    label = "Multiplier",
                    value = progress.rewardMultiplierDisplay,
                    accentColor = tier.badgeColor
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun LevelTrackBarCard(
    progress: UserLevelProgress,
    onNavigateDeposit: () -> Unit
) {
    val nextTier = progress.nextTier
    val percentInt = (progress.progressToNextTier * 100).toInt().coerceIn(0, 100)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        border = BorderStroke(1.dp, CryptoCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = RealGoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (nextTier != null) "Level Progress: Next ${nextTier.levelName}" else "Max VIP Tier Achieved!",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$percentInt%",
                    color = RealGoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Custom Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(CryptoCardVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.progressToNextTier.coerceIn(0.02f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(RealGoldPrimary, RealGoldLight)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deposit: $${String.format("%.2f", progress.totalDepositUsd)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = if (nextTier != null) {
                        "Target: $${String.format("%.0f", progress.nextTierTargetDeposit)}"
                    } else {
                        "Target: $200 (Completed)"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (nextTier != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RealGoldPrimary.copy(alpha = 0.1f))
                        .border(1.dp, RealGoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Deposit $${String.format("%.2f", progress.amountNeededForNextTier)} USD more",
                            color = RealGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Unlock ${nextTier.levelName} (${String.format("%.0f", nextTier.dailyRewardReal)} RC/day, ${nextTier.multiplierDisplay})",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onNavigateDeposit,
                        colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("track_bar_deposit_btn")
                    ) {
                        Text(
                            text = "Deposit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneRoadmapTrackCard(
    totalDeposit: Double,
    currentTier: UserLevelTier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        border = BorderStroke(1.dp, CryptoCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Deposit Roadmap Track",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Track your total volume milestones from Starter ($50) to VIP ($200)",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Visual Milestone Nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserLevelTier.ALL_TIERS.forEach { tier ->
                    val isReached = totalDeposit >= tier.minDepositUsd
                    val isCurrent = tier == currentTier && isReached

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> tier.badgeColor
                                        isReached -> CryptoGreen
                                        else -> CryptoCardVariant
                                    }
                                )
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) Color.White else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isReached) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Unlocked",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = tier.levelName.take(4),
                            color = if (isReached) TextPrimary else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "$${tier.minDepositUsd.toInt()}",
                            color = if (isCurrent) RealGoldPrimary else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositVolumeCalculatorCard(
    currentDeposit: Double,
    simulatedInput: String,
    onInputChange: (String) -> Unit,
    simulatedProgress: UserLevelProgress,
    onQuickAdd: (Double) -> Unit,
    onDepositNow: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131824)),
        border = BorderStroke(1.dp, RealGoldPrimary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = RealGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Deposit Volume Calculator",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calculate which level you unlock by adding USDT",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(25.0, 50.0, 75.0, 100.0).forEach { amount ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CryptoCardVariant)
                            .clickable { onQuickAdd(amount) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$${amount.toInt()}",
                            color = RealGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input field
            OutlinedTextField(
                value = simulatedInput,
                onValueChange = onInputChange,
                placeholder = { Text("Enter additional deposit in USD...", color = TextMuted, fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_deposit_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealGoldPrimary,
                    unfocusedBorderColor = CryptoCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CryptoCardBg,
                    unfocusedContainerColor = CryptoCardBg
                ),
                leadingIcon = {
                    Text(text = "$", color = RealGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation Result Box
            val simTier = simulatedProgress.currentTier
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C1017))
                    .border(1.dp, simTier.badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resulting Volume: $${String.format("%.2f", simulatedProgress.totalDepositUsd)} USD",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Level: ${simTier.levelName} (${simTier.multiplierDisplay} Multiplier)",
                        color = simTier.badgeColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Daily Reward: ${String.format("%.0f", simulatedProgress.dailyRewardReal)} REAL / day",
                        color = CryptoGreen,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onDepositNow,
                    colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("calc_deposit_now_btn")
                ) {
                    Text("Deposit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TierDetailCard(
    tier: UserLevelTier,
    userDeposit: Double,
    isCurrentTier: Boolean,
    onDepositClick: () -> Unit
) {
    val isUnlocked = userDeposit >= tier.minDepositUsd
    val remainingToUnlock = (tier.minDepositUsd - userDeposit).coerceAtLeast(0.0)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTier) Color(0xFF1A1F2C) else CryptoCardBg
        ),
        border = BorderStroke(
            width = if (isCurrentTier) 1.5.dp else 1.dp,
            color = if (isCurrentTier) tier.badgeColor else CryptoCardBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(tier.gradientStart.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = tier.badgeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${tier.levelName} Level",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tier.badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tier.multiplierDisplay,
                                    color = tier.badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "Required Deposit: $${tier.minDepositUsd.toInt()} USD",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Status Badge
                when {
                    isCurrentTier -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tier.badgeColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CURRENT",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                    isUnlocked -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CryptoGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CryptoGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "UNLOCKED",
                                color = CryptoGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CryptoCardVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+$${remainingToUnlock.toInt()} USD",
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlight Daily Reward Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F141F))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Redeem,
                        contentDescription = null,
                        tint = RealGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daily Reward:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "${tier.dailyRewardReal.toInt()} Real coin / day",
                    color = RealGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Perks list
            tier.perks.forEach { perk ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isUnlocked) CryptoGreen else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = perk,
                        color = if (isUnlocked) TextPrimary else TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CryptoCardVariant,
                        contentColor = RealGoldLight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text(
                        text = "Deposit $${remainingToUnlock.toInt()} USD to Unlock ${tier.levelName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
