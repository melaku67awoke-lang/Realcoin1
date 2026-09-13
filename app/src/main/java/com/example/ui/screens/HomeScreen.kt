package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MarketRateEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WalletTransactionEntity
import com.example.data.model.UserLevelTier
import com.example.ui.theme.CryptoCardBg
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardVariant
import com.example.ui.theme.CryptoDarkBg
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.RealGoldLight
import com.example.ui.theme.RealGoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    user: UserEntity?,
    rate: MarketRateEntity?,
    transactions: List<WalletTransactionEntity>,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onNavigateP2P: () -> Unit,
    onNavigateSpinWheel: () -> Unit,
    onNavigateKYC: () -> Unit,
    onNavigateChat: () -> Unit,
    onNavigateHelp: () -> Unit,
    onNavigateNotifications: () -> Unit = {},
    onNavigateCommunity: () -> Unit,
    onNavigateNewsRates: () -> Unit,
    onClaimDailyReward: () -> Unit,
    onNavigateAdminDeposits: () -> Unit = {},
    onNavigateLevel: () -> Unit = {},
    pendingDepositCount: Int = 0,
    onShowNotification: ((String) -> Unit)? = null
) {
    val usdRate = rate?.realCoinToUsd ?: 0.084481
    val usdToEtb = rate?.usdToEtb ?: 186.0
    val realBalance = user?.realCoinBalance ?: 0.0
    val holdBalance = user?.holdBalance ?: 0.0
    val depositTotal = user?.usdtDepositTotal ?: 0.0
    val levelProgress = remember(depositTotal) {
        UserLevelTier.calculateProgress(depositTotal)
    }
    val currentTier = levelProgress.currentTier
    val nextTier = levelProgress.nextTier

    // 24 Hour reward calculation
    val now = System.currentTimeMillis()
    val lastReward = user?.lastDailyRewardTimestamp ?: 0L
    val canClaimReward = now - lastReward >= 24 * 3600 * 1000L
    val hoursRemaining = if (canClaimReward) 0 else ((24 * 3600 * 1000L - (now - lastReward)) / (3600 * 1000L) + 1).coerceAtLeast(1)

    val progress = levelProgress.progressToNextTier

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CryptoDarkBg)
    ) {
        // 1. Top Bar with User Info & Chat Button
        item {
            TopUserHeader(
                user = user,
                onChatClick = onNavigateChat,
                onNotificationClick = onNavigateNotifications,
                onCopySuccess = { onShowNotification?.invoke("User ID copied to clipboard!") }
            )
        }

        // 2. Orange Gradient Balance Card (Real Coin, USD, ETB, Hold)
        item {
            BalanceGradientCard(
                realBalance = realBalance,
                usdRate = usdRate,
                usdToEtb = usdToEtb,
                holdBalance = holdBalance,
                onDepositClick = onDepositClick,
                onWithdrawClick = onWithdrawClick
            )
        }

        // 3. Daily Reward Banner (Feature 13)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RealGoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Daily Reward", tint = RealGoldPrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Daily Reward (24h)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = if (canClaimReward) "Claim ${String.format("%.0f", levelProgress.dailyRewardReal)} REAL! (${levelProgress.rewardMultiplierDisplay} Multiplier)" else "Next claim in ~$hoursRemaining hrs",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = onClaimDailyReward,
                        enabled = canClaimReward,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RealGoldPrimary,
                            contentColor = Color.Black,
                            disabledContainerColor = CryptoCardVariant,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("claim_daily_reward_btn")
                    ) {
                        Text(
                            text = if (canClaimReward) "Claim ${String.format("%.0f", levelProgress.dailyRewardReal)} RC" else "Claimed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 4. User Level Tier Card with Track Bar & Multiplier
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = BorderStroke(1.dp, currentTier.badgeColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onNavigateLevel() }
                    .testTag("home_level_tier_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = "Level", tint = currentTier.badgeColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${currentTier.levelName} Tier", color = currentTier.badgeColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RealGoldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = levelProgress.rewardMultiplierDisplay,
                                    color = RealGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = if (nextTier != null) {
                                "$${String.format("%.0f", depositTotal)} / $${levelProgress.nextTierTargetDeposit.toInt()} USD"
                            } else {
                                "Max VIP ($200 USD)"
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = currentTier.badgeColor,
                        trackColor = CryptoCardVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (nextTier != null) {
                                "Deposit $${String.format("%.1f", levelProgress.amountNeededForNextTier)} more to unlock ${nextTier?.levelName}"
                            } else {
                                "150 RC/day VIP Benefits Unlocked"
                            },
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "View Track Bar →",
                            color = RealGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Admin Deposit Verification Banner (Only shown when user has admin privileges)
        if (user?.isAdmin == true) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                    border = BorderStroke(1.dp, RealGoldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onNavigateAdminDeposits() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(RealGoldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Admin",
                                    tint = RealGoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Admin Controls Center",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (pendingDepositCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CryptoGreen)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$pendingDepositCount PENDING",
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Confirm Deposits & Withdrawals, Arbitrate Disputes, Verify KYC",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open",
                            tint = RealGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 5. Top Services Section Title
        item {
            PaddingHeader(title = "Top Services")
        }

        // 7. Grid of Services matching features
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "P2P Trade", icon = Icons.Default.CurrencyExchange, iconTint = RealGoldPrimary, onClick = onNavigateP2P)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "Deposit", icon = Icons.Default.ArrowDownward, iconTint = CryptoGreen, onClick = onDepositClick)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "Withdraw", icon = Icons.Default.ArrowUpward, iconTint = CryptoRed, onClick = onWithdrawClick)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "Spin Wheel", icon = Icons.Default.Casino, iconTint = RealGoldLight, onClick = onNavigateSpinWheel)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "KYC Verify", icon = Icons.Default.MilitaryTech, iconTint = CryptoGreen, onClick = onNavigateKYC)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "Community", icon = Icons.Default.Videocam, iconTint = RealGoldPrimary, onClick = onNavigateCommunity)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "Rates & News", icon = Icons.Default.ShowChart, iconTint = RealGoldLight, onClick = onNavigateNewsRates)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceIconItem(title = "Help Center", icon = Icons.Default.HelpOutline, iconTint = TextSecondary, onClick = onNavigateHelp)
                    }
                }
            }
        }

        // 8. Recent Wallet Transactions Section
        item {
            PaddingHeader(title = "Recent Transactions")
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No transactions yet", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(transactions.take(5)) { tx ->
                TransactionCardItem(tx = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun PaddingHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun TransactionCardItem(tx: WalletTransactionEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(tx.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (tx.isPositive) CryptoGreen.copy(alpha = 0.15f) else CryptoRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = tx.type,
                        tint = if (tx.isPositive) CryptoGreen else CryptoRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = tx.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = dateStr, color = TextMuted, fontSize = 11.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (tx.isPositive) "+" else "-"}${String.format("%.2f", tx.amountReal)} REAL",
                    color = if (tx.isPositive) CryptoGreen else CryptoRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (tx.amountEtb > 0) {
                    Text(
                        text = "${String.format("%.2f", tx.amountEtb)} ETB",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
