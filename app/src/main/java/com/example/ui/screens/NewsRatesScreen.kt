package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MarketRateEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.CryptoCardBg
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardVariant
import com.example.ui.theme.CryptoDarkBg
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenGlow
import com.example.ui.theme.RealGoldLight
import com.example.ui.theme.RealGoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NewsRatesScreen(
    user: UserEntity? = null,
    rate: MarketRateEntity?,
    onUpdateRate: (Double) -> Unit
) {
    val usdRate = rate?.realCoinToUsd ?: 0.084481
    val etbRate = rate?.realCoinToEtb ?: 15.71
    var adminPriceInput by remember { mutableStateOf(String.format("%.6f", usdRate)) }
    var showAdminPanel by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CryptoDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header matching Screenshot 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Market & Rates", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(text = "Real-time Real Coin market valuation", color = TextSecondary, fontSize = 12.sp)
            }

            // Admin button (Only shown to users with Admin role)
            if (user?.isAdmin == true) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardBg)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
                        .clickable { showAdminPanel = !showAdminPanel }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = RealGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Admin Rate", color = RealGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Market Rate Card matching Screenshot 9
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Real coin to ETB", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = "${String.format("%.2f", etbRate)} ETB",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CryptoGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Up", tint = CryptoGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "+${rate?.dailyGrowthPct ?: 0.4}%", color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Real coin to USD", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "$${String.format("%.6f", usdRate)} USD",
                            color = RealGoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "USD to ETB Peg", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "1 USD = ${rate?.usdToEtb ?: 186.0} ETB",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Glowing Neon Upward Growth Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val points = listOf(
                            Offset(0f, height * 0.85f),
                            Offset(width * 0.2f, height * 0.70f),
                            Offset(width * 0.4f, height * 0.65f),
                            Offset(width * 0.6f, height * 0.45f),
                            Offset(width * 0.8f, height * 0.35f),
                            Offset(width, height * 0.15f)
                        )

                        // Path
                        val linePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        // Gradient fill under curve
                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                listOf(CryptoGreen.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )

                        // Neon glow line
                        drawPath(
                            path = linePath,
                            color = CryptoGreenGlow,
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // End point glowing dot
                        drawCircle(
                            color = CryptoGreenGlow,
                            radius = 6.dp.toPx(),
                            center = points.last()
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = points.last()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Growth Ranges matching Screenshot 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Daily
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Daily Range", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = rate?.dailyRange ?: "0.3% - 9%", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Monthly
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Monthly", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = rate?.monthlyRange ?: "52% - 165%", color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Yearly
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Yearly", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = rate?.yearlyRange ?: "725% - 2345%", color = RealGoldLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Admin Rate Incrementer Panel
        if (showAdminPanel) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardVariant),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RealGoldPrimary)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = RealGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Admin Rate Control Panel", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(
                        text = "Increase market price of Real Coin for all users platform-wide.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adminPriceInput,
                        onValueChange = { adminPriceInput = it },
                        label = { Text("Real Coin USD Value ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RealGoldPrimary,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Increase buttons (+5%, +10%, +25%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.05 to "+5%", 0.10 to "+10%", 0.25 to "+25%").forEach { (inc, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CryptoCardBg)
                                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newP = usdRate * (1 + inc)
                                        adminPriceInput = String.format("%.6f", newP)
                                        onUpdateRate(newP)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = label, color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val p = adminPriceInput.toDoubleOrNull()
                            if (p != null && p > 0) {
                                onUpdateRate(p)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_admin_rate_btn")
                    ) {
                        Text(text = "Save & Apply New Market Value", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // News Articles Section matching Screenshot 9
        Text(text = "Ecosystem News & Updates", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "Real Coin Liquidity Pools Exceed 50M ETB Milestone" to "Trading volumes on P2P Escrow reached record highs with zero dispute resolution times.",
            "Telebirr & CBE BIRR Direct Integration Expanded" to "Instant automated deposit matching now enables 10% bonus token crediting in seconds.",
            "Exclusive Creator Program: Go Live & Earn Real Coin" to "Broadcasters can now receive virtual gifts converted directly into withdrawable Real Coins."
        ).forEach { (title, desc) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.ArrowOutward, contentDescription = "Open", tint = RealGoldPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
