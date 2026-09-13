package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.entities.MarketRateEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.CryptoCardBg
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardVariant
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.RealGoldLight
import com.example.ui.theme.RealGoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class DepositStep {
    INPUT,
    CREATED
}

@Composable
fun DepositUsdtDialog(
    user: UserEntity?,
    rate: MarketRateEntity?,
    depositAddress: String = "0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4",
    onDismiss: () -> Unit,
    onSubmitProof: (amountUsdt: Double, screenshotUri: String?, txHash: String?) -> Unit,
    onConfirmDepositDirect: (Double) -> Unit = {},
    onShowNotification: ((String) -> Unit)? = null
) {
    var step by remember { mutableStateOf(DepositStep.INPUT) }
    var usdtInput by remember { mutableStateOf("25") }
    var screenshotUri by remember { mutableStateOf<String?>(null) }
    var txHashInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBottomToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("Deposit request created!") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            screenshotUri = uri.toString()
            toastMessage = "Deposit screenshot attached!"
            showBottomToast = true
            onShowNotification?.invoke("Deposit screenshot attached successfully!")
        }
    }

    val clipboardManager = LocalClipboardManager.current
    val usdRate = rate?.realCoinToUsd ?: 0.084734
    val amountUsdt = usdtInput.toDoubleOrNull() ?: 0.0
    val baseReal = if (usdRate > 0) amountUsdt / usdRate else 0.0
    val bonusReal = baseReal * 0.10 // 10% bonus
    val totalReal = baseReal + bonusReal

    LaunchedEffect(showBottomToast) {
        if (showBottomToast) {
            kotlinx.coroutines.delay(4000)
            showBottomToast = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main Modal Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101725)),
                border = BorderStroke(1.dp, RealGoldPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deposit_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Top Bar with Centered Gold Title and Close '✕'
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Make a Deposit",
                            color = RealGoldPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                                .testTag("close_deposit_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (step == DepositStep.INPUT) {
                        // --- STEP 1: INPUT SCREEN ---

                        // Payment Address (BEP-20)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment Address",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "BEP-20 Network",
                                color = CryptoGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF161F2E))
                                .border(1.dp, Color(0xFF26354A), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = depositAddress,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Official BEP-20 Receiver Address",
                                        color = CryptoGreen,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(depositAddress))
                                        toastMessage = "BEP-20 Payment address copied!"
                                        showBottomToast = true
                                        onShowNotification?.invoke("BEP-20 Payment address copied!")
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("copy_bep20_payment_address_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Payment Address",
                                        tint = RealGoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Network
                        Text(
                            text = "Network",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF161F2E))
                                .border(1.5.dp, Color(0xFF3B4D66), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BSC • BNB Smart Chain (BEP20)",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CryptoGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "BEP-20 Only",
                                        tint = CryptoGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "BEP-20 ONLY",
                                        color = CryptoGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B2433))
                                .border(0.8.dp, RealGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Network Notice",
                                tint = RealGoldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Only BEP-20 USDT is supported. Other networks (TRC-20, ERC-20) are NOT accepted.",
                                color = RealGoldLight,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Amount (USD)
                        Text(
                            text = "Amount (USD)",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Minimum deposit: $25.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom Styled Amount Input Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF161F2E))
                                .border(
                                    1.5.dp,
                                    if (errorMessage != null) CryptoRed else Color(0xFF3B4D66),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (usdtInput.isEmpty()) {
                                Text(
                                    text = "Enter amount in USD",
                                    color = TextMuted,
                                    fontSize = 15.sp
                                )
                            }
                            BasicTextField(
                                value = usdtInput,
                                onValueChange = {
                                    usdtInput = it.filter { char -> char.isDigit() || char == '.' }
                                    errorMessage = null
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(RealGoldPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deposit_amount_input")
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = CryptoRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Calculation Breakdown Card with explicit 10% bonus!
                        if (amountUsdt > 0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF172132))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Deposit Amount:",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "$${String.format("%.2f", amountUsdt)} USD",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Base Real Coin:",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${String.format("%.2f", baseReal)} REAL",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "10% Deposit Bonus:",
                                            color = CryptoGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "+${String.format("%.2f", bonusReal)} REAL",
                                            color = CryptoGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total You Receive:",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${String.format("%.2f", totalReal)} REAL",
                                            color = RealGoldPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Confirm Deposit Button
                        Button(
                            onClick = {
                                if (amountUsdt < 25.0) {
                                    errorMessage = "Minimum deposit is $25."
                                } else {
                                    errorMessage = null
                                    step = DepositStep.CREATED
                                    toastMessage = "Deposit order created! Transfer USDT and upload screenshot."
                                    showBottomToast = true
                                    onShowNotification?.invoke(toastMessage)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RealGoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_deposit_button")
                        ) {
                            Text(
                                text = "Confirm Deposit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                    } else {
                        // --- STEP 2: DEPOSIT CREATED WITH QR CODE & SCREENSHOT UPLOAD ---

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Checkmark Badge
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF064E3B))
                                    .border(1.5.dp, Color(0xFF10B981), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Deposit Created!",
                                color = TextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Send USDT (BEP20) and attach screenshot for Admin approval (+10% Bonus)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Crisp BEP20 QR Code View matching uploaded screenshot
                            BscQrCodeView(
                                data = depositAddress,
                                size = 180.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Details Card (Deposit Amount, Send Amount, Deposit Address)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF151E2E))
                                .border(1.dp, Color(0xFF26354A), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // 1. Deposit Amount & Bonus
                                Column {
                                    Text(
                                        text = "Deposit Amount",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, Color(0xFF243044), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$${String.format("%.2f", amountUsdt)} USD",
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "+10% Bonus: ${String.format("%.2f", bonusReal)} REAL",
                                                color = CryptoGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                // 2. Send Amount (USDTBSC)
                                Column {
                                    Text(
                                        text = "Send Amount (USDTBSC)",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, Color(0xFF243044), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${String.format("%.2f", amountUsdt)} USDTBSC",
                                                color = RealGoldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(String.format("%.2f", amountUsdt)))
                                                    toastMessage = "Send amount copied!"
                                                    showBottomToast = true
                                                    onShowNotification?.invoke("Deposit amount copied to clipboard!")
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Amount",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Deposit Address (BEP20)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Deposit Address",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CryptoGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "BEP-20",
                                                tint = CryptoGreen,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "BEP-20 ONLY",
                                                color = CryptoGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, Color(0xFF243044), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = depositAddress,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(depositAddress))
                                                    toastMessage = "Deposit address copied!"
                                                    showBottomToast = true
                                                    onShowNotification?.invoke("BEP20 deposit address copied to clipboard!")
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Address",
                                                    tint = RealGoldPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- SCREENSHOT ATTACHMENT SECTION ---
                        Text(
                            text = "Deposit Payment Proof",
                            color = RealGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (screenshotUri == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF161F2E))
                                    .border(1.5.dp, RealGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(14.dp)
                                    .testTag("attach_screenshot_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Attach Screenshot",
                                        tint = RealGoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Attach Deposit Screenshot",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            // Screenshot Attached Preview Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF161F2E))
                                    .border(1.dp, CryptoGreen, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black)
                                        ) {
                                            AsyncImage(
                                                model = screenshotUri,
                                                contentDescription = "Selected Screenshot",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Attached",
                                                    tint = CryptoGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Screenshot Attached",
                                                    color = CryptoGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = "Ready for Admin verification",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { screenshotUri = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Screenshot",
                                            tint = CryptoRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Optional TX Hash input
                        OutlinedTextField(
                            value = txHashInput,
                            onValueChange = { txHashInput = it },
                            placeholder = { Text("TX Hash / Transaction ID (Optional)", color = TextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = RealGoldPrimary,
                                unfocusedBorderColor = Color(0xFF3B4D66)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("tx_hash_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Warning Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF271C0A))
                                .border(1.dp, Color(0xFF78350F), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = RealGoldPrimary, fontWeight = FontWeight.Bold)) {
                                        append("ADMIN APPROVAL: ")
                                    }
                                    withStyle(SpanStyle(color = Color(0xFFFBBF24))) {
                                        append("Admin will check your screenshot and credit ")
                                    }
                                    withStyle(SpanStyle(color = RealGoldPrimary, fontWeight = FontWeight.Bold)) {
                                        append("${String.format("%.2f", totalReal)} REAL (+10% Bonus)")
                                    }
                                    withStyle(SpanStyle(color = Color(0xFFFBBF24))) {
                                        append(" to your Real Coin balance.")
                                    }
                                },
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Button 1: Submit Proof for Admin Review
                        Button(
                            onClick = {
                                onSubmitProof(amountUsdt, screenshotUri, txHashInput.ifBlank { null })
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RealGoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_deposit_proof_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Submit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit Deposit Proof to Admin",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Button 2: Direct Instant Credit (Testing fallback)
                        Button(
                            onClick = {
                                onConfirmDepositDirect(amountUsdt)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = RealGoldLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("direct_credit_button")
                        ) {
                            Text(
                                text = "Direct Instant Credit (+10% Bonus)",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Button 3: Cancel
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("cancel_deposit_button")
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Floating Green Toast (Matches Screenshot 3 bottom banner)
            AnimatedVisibility(
                visible = showBottomToast,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF065F46))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF065F46),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = toastMessage,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawRealDialog(
    user: UserEntity?,
    rate: MarketRateEntity?,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (Double, String) -> Unit
) {
    var realAmountInput by remember { mutableStateOf("1000") }
    var usdtAddressInput by remember { mutableStateOf("") }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val userBalance = user?.realCoinBalance ?: 0.0
    val usdRate = rate?.realCoinToUsd ?: 0.084481
    val amountReal = realAmountInput.toDoubleOrNull() ?: 0.0
    val usdtCalculated = amountReal * usdRate

    val isMinValid = amountReal >= 1000.0
    val hasSufficientBalance = userBalance >= amountReal
    val isAddressValid = usdtAddressInput.isNotBlank() && usdtAddressInput.length >= 10
    val canSubmit = isMinValid && hasSufficientBalance && isAddressValid

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoCardBg,
        title = {
            Text(
                text = "Withdraw to USDT",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Convert your Real Coin to USDT at the current market rate and receive funds directly in your external wallet.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardVariant)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Available Balance:", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = "${String.format("%.2f", userBalance)} REAL",
                        color = RealGoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = realAmountInput,
                    onValueChange = { realAmountInput = it },
                    label = { Text("Real Coin Amount") },
                    supportingText = {
                        if (!isMinValid) {
                            Text("Minimum withdrawal is 1000 Real Coin", color = CryptoRed)
                        } else if (!hasSufficientBalance) {
                            Text("Insufficient Real Coin balance", color = CryptoRed)
                        } else {
                            Text("Minimum 1000 REAL required", color = TextMuted)
                        }
                    },
                    isError = !isMinValid || !hasSufficientBalance,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RealGoldPrimary,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = usdtAddressInput,
                    onValueChange = { usdtAddressInput = it },
                    label = { Text("BEP-20 Wallet Address") },
                    placeholder = { Text("0x... (BNB Smart Chain)", color = TextMuted) },
                    supportingText = {
                        Text("Only BEP-20 network is supported for USDT withdrawals", color = CryptoGreen, fontSize = 11.sp)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    usdtAddressInput = clip.trim()
                                }
                            },
                            modifier = Modifier.testTag("paste_withdraw_address_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RealGoldPrimary.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste Address",
                                    tint = RealGoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Paste",
                                    color = RealGoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_address_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RealGoldPrimary,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Payout Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CryptoCardVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Market Rate:", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "1 REAL = $${String.format("%.6f", usdRate)}", color = TextPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "USDT to Receive:", color = CryptoGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$${String.format("%.2f", usdtCalculated)} USDT", color = CryptoGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSubmit) {
                        onConfirmWithdraw(amountReal, usdtAddressInput)
                        onDismiss()
                    }
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RealGoldPrimary,
                    contentColor = Color.Black,
                    disabledContainerColor = CryptoCardVariant,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_withdraw_button")
            ) {
                Text("Withdraw USDT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
