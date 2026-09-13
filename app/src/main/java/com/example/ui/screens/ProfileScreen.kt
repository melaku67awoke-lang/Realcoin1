package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
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
fun ProfileScreen(
    user: UserEntity?,
    onNavigateKYC: () -> Unit,
    onNavigateSpinWheel: () -> Unit,
    onNavigateHelp: () -> Unit,
    onNavigateAdminDeposits: () -> Unit = {},
    onNavigateLevel: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onShowNotification: ((String) -> Unit)? = null,
    onSetAdminRole: ((Boolean) -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val fullId = "8942c88f-40f9-47cc-9cf3-2a6765f31115"
    val totalDeposit = user?.usdtDepositTotal ?: 0.0
    val levelProgress = remember(totalDeposit) {
        UserLevelTier.calculateProgress(totalDeposit)
    }
    val currentTier = levelProgress.currentTier
    val nextTier = levelProgress.nextTier

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CryptoDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "My Profile", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Header Card matching Screenshot 7
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(currentTier.gradientStart, currentTier.gradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user?.username?.firstOrNull() ?: 'H').uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "@${user?.username ?: "hab"}",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Copyable ID
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardVariant)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(fullId))
                            onShowNotification?.invoke("Wallet ID copied to clipboard!")
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "ID: ${fullId.take(16)}...", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = RealGoldPrimary, modifier = Modifier.size(14.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Level Badge & Rewards Multiplier Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable { onNavigateLevel() }
                ) {
                    // Level Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(currentTier.badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = "Level",
                                tint = currentTier.badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentTier.levelName} Member",
                                color = currentTier.badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Rewards Multiplier Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(RealGoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, RealGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ ${levelProgress.rewardMultiplierDisplay} Multiplier",
                            color = RealGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VIP Level Tracking & Deposit Volume Card with Track Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
            border = BorderStroke(1.dp, currentTier.badgeColor.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateLevel() }
                .testTag("profile_vip_level_card")
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(currentTier.gradientStart.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = currentTier.badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VIP Level & Volume Tracking",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Daily: ${String.format("%.0f", levelProgress.dailyRewardReal)} REAL • ${levelProgress.rewardMultiplierDisplay} Multiplier",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Details",
                        tint = RealGoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Track Bar & Total Deposit Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Deposit Volume",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", totalDeposit)} USD",
                            color = RealGoldLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }

                    Text(
                        text = if (nextTier != null) {
                            "Next: ${nextTier.levelName} ($${nextTier.minDepositUsd.toInt()} USD)"
                        } else {
                            "Max VIP Tier ($200 USD)"
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Gradient Track Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CryptoCardVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = levelProgress.progressToNextTier.coerceIn(0.02f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(currentTier.gradientStart, currentTier.badgeColor)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (nextTier != null) {
                            "Need $${String.format("%.2f", levelProgress.amountNeededForNextTier)} USD more to level up"
                        } else {
                            "All VIP benefits unlocked!"
                        },
                        color = if (nextTier != null) RealGoldPrimary else CryptoGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${(levelProgress.progressToNextTier * 100).toInt()}%",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tap to view full level track bar button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CryptoCardVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All 6 Tiers & Progress Roadmap",
                        color = RealGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = RealGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Information section matching Screenshot 7
        Text(text = "Account Details", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                AccountDetailRow(icon = Icons.Default.Phone, label = "Phone", value = user?.phone ?: "+251911000000")
                AccountDetailRow(icon = Icons.Default.Email, label = "Email", value = user?.email ?: "fikerukebede16@gmail.com")
                AccountDetailRow(icon = Icons.Default.Public, label = "Country", value = user?.country ?: "Ethiopia")
                AccountDetailRow(icon = Icons.Default.Person, label = "Joined", value = user?.joinedDate ?: "04/08/2026")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role & Real Coin Control Gating
        Text(text = "Role & Platform Permissions", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
            border = BorderStroke(1.dp, if (user?.isAdmin == true) RealGoldPrimary.copy(alpha = 0.5f) else CryptoCardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user?.isAdmin == true) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (user?.isAdmin == true) RealGoldPrimary else CryptoGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (user?.isAdmin == true) "Administrator Role" else "Trader / User Role",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (user?.isAdmin == true) "Controls Real Coin rate, deposits & disputes" else "Standard trading, P2P exchange & deposits",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    if (onSetAdminRole != null) {
                        Button(
                            onClick = { onSetAdminRole(!(user?.isAdmin ?: false)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user?.isAdmin == true) CryptoCardVariant else RealGoldPrimary,
                                contentColor = if (user?.isAdmin == true) TextPrimary else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (user?.isAdmin == true) "Switch to User" else "Switch to Admin",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Feature Links
        Text(text = "Features & Settings", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                SettingsNavigationItem(
                    icon = Icons.Default.WorkspacePremium,
                    title = "VIP Tier Levels & Rewards",
                    subtitle = "${currentTier.levelName} (${levelProgress.rewardMultiplierDisplay}) • Daily ${String.format("%.0f", levelProgress.dailyRewardReal)} RC • Track Bar",
                    badgeColor = currentTier.badgeColor,
                    onClick = onNavigateLevel
                )
                SettingsNavigationItem(
                    icon = Icons.Default.Security,
                    title = "Identity Verification (KYC)",
                    subtitle = "Status: ${user?.kycStatus ?: "APPROVED"}",
                    badgeColor = CryptoGreen,
                    onClick = onNavigateKYC
                )
                SettingsNavigationItem(
                    icon = Icons.Default.Casino,
                    title = "Lucky Spinning Wheel",
                    subtitle = "1 Free Spin daily • 10 RC per spin",
                    badgeColor = RealGoldPrimary,
                    onClick = onNavigateSpinWheel
                )
                SettingsNavigationItem(
                    icon = Icons.Default.Notifications,
                    title = "Notification Center",
                    subtitle = "Deposits, withdrawals, and platform alerts",
                    badgeColor = RealGoldPrimary,
                    onClick = onNavigateNotifications
                )
                SettingsNavigationItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Help & Support Center",
                    subtitle = "Submit tickets with screenshots",
                    badgeColor = TextSecondary,
                    onClick = onNavigateHelp
                )
                if (user?.isAdmin == true) {
                    SettingsNavigationItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin: Control Center",
                        subtitle = "Dispute arbitration, KYC verification, deposit & withdraw confirmations",
                        badgeColor = RealGoldPrimary,
                        onClick = onNavigateAdminDeposits
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun AccountDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = RealGoldPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, color = TextSecondary, fontSize = 13.sp)
        }
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = badgeColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
            }
        }

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Open", tint = TextMuted, modifier = Modifier.size(14.dp))
    }
}
