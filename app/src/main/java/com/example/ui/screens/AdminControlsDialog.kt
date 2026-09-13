package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.entities.DepositRequestEntity
import com.example.data.local.entities.EscrowOrderEntity
import com.example.data.local.entities.MarketRateEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WithdrawRequestEntity
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
fun AdminControlsDialog(
    user: UserEntity?,
    rate: MarketRateEntity? = null,
    depositRequests: List<DepositRequestEntity>,
    withdrawRequests: List<WithdrawRequestEntity>,
    orders: List<EscrowOrderEntity>,
    onDismiss: () -> Unit,
    // Deposit callbacks
    onApproveDeposit: (String) -> Unit,
    onRejectDeposit: (String, String) -> Unit,
    // Withdraw callbacks
    onApproveWithdraw: (String, String) -> Unit,
    onRejectWithdraw: (String, String) -> Unit,
    // Dispute callbacks
    onResolveDispute: (String, Boolean, String) -> Unit,
    // KYC callbacks
    onUpdateKycStatus: (String, String) -> Unit,
    // Real Coin controls
    onUpdateMarketPrice: ((Double, Double) -> Unit)? = null,
    onManualAddRealCoin: ((Double, String) -> Unit)? = null,
    onSetAdminRole: ((Boolean) -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0=Deposits, 1=Withdrawals, 2=Disputes, 3=KYC, 4=Real Coin

    val pendingDeposits = remember(depositRequests) { depositRequests.filter { it.status == "PENDING" } }
    val pendingWithdrawals = remember(withdrawRequests) { withdrawRequests.filter { it.status == "PENDING" } }
    val disputedOrders = remember(orders) { orders.filter { it.status.equals("DISPUTED", ignoreCase = true) } }

    val clipboardManager = LocalClipboardManager.current

    // Action Dialog States
    var showRejectDepositDialogForId by remember { mutableStateOf<String?>(null) }
    var depositRejectReason by remember { mutableStateOf("") }

    var showApproveWithdrawDialogForId by remember { mutableStateOf<String?>(null) }
    var withdrawTxHashInput by remember { mutableStateOf("") }
    var showRejectWithdrawDialogForId by remember { mutableStateOf<String?>(null) }
    var withdrawRejectReason by remember { mutableStateOf("") }

    var showResolveDisputeDialogForOrder by remember { mutableStateOf<EscrowOrderEntity?>(null) }
    var disputeReleaseToBuyer by remember { mutableStateOf(true) }
    var disputeResolutionNote by remember { mutableStateOf("Verified transaction proof via arbitrator audit.") }

    var previewScreenshotUri by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CryptoDarkBg.copy(alpha = 0.95f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101725)),
                border = BorderStroke(1.5.dp, RealGoldPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.98f)
                    .testTag("admin_controls_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // --- HEADER ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(RealGoldPrimary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin",
                                    tint = RealGoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Admin Control Center",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Confirmations, Escrow Arbitrations & KYC",
                                    color = RealGoldPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CryptoCardVariant)
                                .testTag("close_admin_controls_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- 4 TABS: DEPOSITS, WITHDRAWALS, DISPUTES, KYC ---
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color(0xFF162032),
                        contentColor = RealGoldPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = RealGoldPrimary,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        // 1. Deposits Tab
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Deposits",
                                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    if (pendingDeposits.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(CryptoRed)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${pendingDeposits.size}",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )

                        // 2. Withdrawals Tab
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Withdraw",
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    if (pendingWithdrawals.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(CryptoRed)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${pendingWithdrawals.size}",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )

                        // 3. Disputes Tab
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Disputes",
                                        fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    if (disputedOrders.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(CryptoRed)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${disputedOrders.size}",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )

                        // 4. KYC Tab
                        Tab(
                            selected = selectedTabIndex == 3,
                            onClick = { selectedTabIndex = 3 },
                            text = {
                                Text(
                                    text = "KYC",
                                    fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )

                        // 5. Real Coin Tab
                        Tab(
                            selected = selectedTabIndex == 4,
                            onClick = { selectedTabIndex = 4 },
                            text = {
                                Text(
                                    text = "Real Coin",
                                    fontWeight = if (selectedTabIndex == 4) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- TAB CONTENT ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (selectedTabIndex) {
                            0 -> AdminDepositsView(
                                depositRequests = depositRequests,
                                onApprove = onApproveDeposit,
                                onRejectClick = { id ->
                                    showRejectDepositDialogForId = id
                                    depositRejectReason = ""
                                },
                                onPreviewScreenshot = { uri -> previewScreenshotUri = uri }
                            )
                            1 -> AdminWithdrawalsView(
                                withdrawRequests = withdrawRequests,
                                onApproveClick = { id ->
                                    showApproveWithdrawDialogForId = id
                                    withdrawTxHashInput = "0x" + (1..32).map { "0123456789abcdef".random() }.joinToString("")
                                },
                                onRejectClick = { id ->
                                    showRejectWithdrawDialogForId = id
                                    withdrawRejectReason = ""
                                },
                                onCopyAddress = { addr ->
                                    clipboardManager.setText(AnnotatedString(addr))
                                }
                            )
                            2 -> AdminDisputesView(
                                orders = orders,
                                onResolveClick = { order, releaseToBuyer ->
                                    showResolveDisputeDialogForOrder = order
                                    disputeReleaseToBuyer = releaseToBuyer
                                    disputeResolutionNote = if (releaseToBuyer) {
                                        "Buyer provided verified bank transfer proof. Escrow released to buyer."
                                    } else {
                                        "Buyer failed to provide payment confirmation proof. Escrow returned to seller."
                                    }
                                }
                            )
                            3 -> AdminKycView(
                                user = user,
                                onUpdateStatus = { status ->
                                    if (user != null) {
                                        onUpdateKycStatus(user.userId, status)
                                    }
                                },
                                onPreviewImage = { uri -> previewScreenshotUri = uri }
                            )
                            4 -> AdminRealCoinControlView(
                                user = user,
                                rate = rate,
                                onUpdateMarketPrice = onUpdateMarketPrice,
                                onManualAddRealCoin = onManualAddRealCoin,
                                onSetAdminRole = onSetAdminRole
                            )
                        }
                    }
                }
            }
        }
    }

    // --- REJECT DEPOSIT DIALOG ---
    if (showRejectDepositDialogForId != null) {
        val reqId = showRejectDepositDialogForId!!
        AlertDialog(
            onDismissRequest = { showRejectDepositDialogForId = null },
            containerColor = Color(0xFF131C2D),
            title = { Text("Reject Deposit Request", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Please specify the reason for rejecting this deposit proof:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = depositRejectReason,
                        onValueChange = { depositRejectReason = it },
                        placeholder = { Text("e.g. Invalid txHash, screenshot unreadable", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CryptoRed,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = depositRejectReason.ifBlank { "Unverified BEP-20 transaction" }
                        onRejectDeposit(reqId, reason)
                        showRejectDepositDialogForId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRed)
                ) {
                    Text("Reject Deposit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDepositDialogForId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // --- APPROVE WITHDRAWAL DIALOG ---
    if (showApproveWithdrawDialogForId != null) {
        val reqId = showApproveWithdrawDialogForId!!
        val req = withdrawRequests.find { it.id == reqId }
        AlertDialog(
            onDismissRequest = { showApproveWithdrawDialogForId = null },
            containerColor = Color(0xFF131C2D),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CryptoGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm & Dispatch Withdrawal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    if (req != null) {
                        Text(
                            text = "Approving withdrawal of ${String.format("%.2f", req.amountReal)} REAL ($${String.format("%.2f", req.amountUsdt)} USDT) to address:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C1322))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = req.destinationAddress,
                                color = RealGoldLight,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Text("BSC BEP-20 Transaction Hash (Optional):", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = withdrawTxHashInput,
                        onValueChange = { withdrawTxHashInput = it },
                        placeholder = { Text("0x... hash", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CryptoGreen,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApproveWithdraw(reqId, withdrawTxHashInput.trim())
                        showApproveWithdrawDialogForId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
                ) {
                    Text("Confirm & Dispatch", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveWithdrawDialogForId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // --- REJECT WITHDRAWAL DIALOG ---
    if (showRejectWithdrawDialogForId != null) {
        val reqId = showRejectWithdrawDialogForId!!
        val req = withdrawRequests.find { it.id == reqId }
        AlertDialog(
            onDismissRequest = { showRejectWithdrawDialogForId = null },
            containerColor = Color(0xFF131C2D),
            title = { Text("Reject Withdrawal & Refund", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Rejecting will return ${req?.amountReal?.let { String.format("%.2f", it) } ?: ""} Real Coins back to user balance.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = withdrawRejectReason,
                        onValueChange = { withdrawRejectReason = it },
                        placeholder = { Text("e.g. Invalid BEP-20 address, KYC required", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CryptoRed,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = withdrawRejectReason.ifBlank { "Invalid BEP-20 destination address" }
                        onRejectWithdraw(reqId, reason)
                        showRejectWithdrawDialogForId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRed)
                ) {
                    Text("Reject & Refund", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectWithdrawDialogForId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // --- RESOLVE DISPUTE DIALOG ---
    if (showResolveDisputeDialogForOrder != null) {
        val order = showResolveDisputeDialogForOrder!!
        val isBuyer = disputeReleaseToBuyer
        AlertDialog(
            onDismissRequest = { showResolveDisputeDialogForOrder = null },
            containerColor = Color(0xFF131C2D),
            title = {
                Text(
                    text = if (isBuyer) "Release Escrow to Buyer" else "Refund Escrow to Seller",
                    color = if (isBuyer) CryptoGreen else RealGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Order #${order.orderId.take(8)}: ${String.format("%.2f", order.realAmount)} REAL (${String.format("%.2f", order.totalEtb)} ETB)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Buyer: @${order.buyerUsername} | Seller: @${order.sellerUsername}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Official Arbitrator Resolution Note:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = disputeResolutionNote,
                        onValueChange = { disputeResolutionNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RealGoldPrimary,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveDispute(order.orderId, isBuyer, disputeResolutionNote)
                        showResolveDisputeDialogForOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBuyer) CryptoGreen else RealGoldPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Execute Resolution", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDisputeDialogForOrder = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // --- SCREENSHOT PREVIEW DIALOG ---
    if (previewScreenshotUri != null) {
        Dialog(onDismissRequest = { previewScreenshotUri = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { previewScreenshotUri = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        AsyncImage(
                            model = previewScreenshotUri,
                            contentDescription = "Document Screenshot Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { previewScreenshotUri = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black)
                        ) {
                            Text("Close Preview")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 1: DEPOSITS
// -------------------------------------------------------------------------------------------------
@Composable
private fun AdminDepositsView(
    depositRequests: List<DepositRequestEntity>,
    onApprove: (String) -> Unit,
    onRejectClick: (String) -> Unit,
    onPreviewScreenshot: (String) -> Unit
) {
    if (depositRequests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No deposit requests found.", color = TextMuted, fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(depositRequests, key = { it.id }) { req ->
            val isPending = req.status == "PENDING"
            val statusColor = when (req.status) {
                "APPROVED" -> CryptoGreen
                "REJECTED" -> CryptoRed
                else -> RealGoldPrimary
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = BorderStroke(1.dp, if (isPending) RealGoldPrimary.copy(alpha = 0.5f) else CryptoCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CryptoGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = CryptoGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$${String.format("%.2f", req.amountUsd)} USDT",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = req.status,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "To Credit: ${String.format("%.2f", req.totalRealCoin)} REAL (+10% Bonus)",
                            color = CryptoGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(req.timestamp)),
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (!req.txHash.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BEP-20 TX: ${req.txHash}",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!req.screenshotUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onPreviewScreenshot(req.screenshotUri) },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF2E3D52)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = RealGoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Payment Screenshot Proof", color = TextPrimary, fontSize = 12.sp)
                        }
                    }

                    if (isPending) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(req.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_approve_deposit_${req.id}")
                            ) {
                                Text("Approve (+10%)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onRejectClick(req.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoRed),
                                border = BorderStroke(1.dp, CryptoRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_reject_deposit_${req.id}")
                            ) {
                                Text("Reject", color = CryptoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 2: WITHDRAWALS
// -------------------------------------------------------------------------------------------------
@Composable
private fun AdminWithdrawalsView(
    withdrawRequests: List<WithdrawRequestEntity>,
    onApproveClick: (String) -> Unit,
    onRejectClick: (String) -> Unit,
    onCopyAddress: (String) -> Unit
) {
    if (withdrawRequests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No withdrawal requests found.", color = TextMuted, fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(withdrawRequests, key = { it.id }) { req ->
            val isPending = req.status == "PENDING"
            val statusColor = when (req.status) {
                "APPROVED" -> CryptoGreen
                "REJECTED" -> CryptoRed
                else -> RealGoldPrimary
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = BorderStroke(1.dp, if (isPending) RealGoldPrimary.copy(alpha = 0.5f) else CryptoCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RealGoldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = RealGoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${String.format("%.2f", req.amountReal)} REAL",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Payout: $${String.format("%.2f", req.amountUsdt)} USDT (BEP-20)",
                                    color = CryptoGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = req.status,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Destination Address with Copy Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .border(0.8.dp, Color(0xFF222F42), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Destination BEP-20 Address:", color = TextSecondary, fontSize = 10.5.sp)
                                Text(
                                    text = req.destinationAddress,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { onCopyAddress(req.destinationAddress) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = RealGoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    if (!req.adminTxHash.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dispatched TX: ${req.adminTxHash}",
                            color = CryptoGreen,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!req.adminNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reject Reason: ${req.adminNote}",
                            color = CryptoRed,
                            fontSize = 11.sp
                        )
                    }

                    if (isPending) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApproveClick(req.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_approve_withdraw_${req.id}")
                            ) {
                                Text("Approve & Dispatch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onRejectClick(req.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoRed),
                                border = BorderStroke(1.dp, CryptoRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_reject_withdraw_${req.id}")
                            ) {
                                Text("Reject & Refund", color = CryptoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 3: DISPUTES
// -------------------------------------------------------------------------------------------------
@Composable
private fun AdminDisputesView(
    orders: List<EscrowOrderEntity>,
    onResolveClick: (EscrowOrderEntity, Boolean) -> Unit
) {
    val disputedList = remember(orders) {
        orders.filter { it.status.equals("DISPUTED", ignoreCase = true) }
    }

    if (disputedList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No active dispute arbitrations.", color = TextMuted, fontSize = 14.sp)
                Text("All P2P escrow orders are running smoothly.", color = TextSecondary, fontSize = 12.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(disputedList, key = { it.orderId }) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = BorderStroke(1.dp, CryptoRed.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = CryptoRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dispute: Order #${order.orderId.take(8)}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CryptoRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("DISPUTED", color = CryptoRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Amount: ${String.format("%.2f", order.realAmount)} REAL",
                            color = RealGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Total ETB: ${String.format("%.2f", order.totalEtb)} ETB",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Buyer: @${order.buyerUsername} | Seller: @${order.sellerUsername}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    if (!order.disputeReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E1724))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Reason: ${order.disputeReason}",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Arbitrator Action:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onResolveClick(order, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Release to Buyer", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                        OutlinedButton(
                            onClick = { onResolveClick(order, false) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RealGoldPrimary),
                            border = BorderStroke(1.dp, RealGoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Refund to Seller", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 4: KYC VERIFICATION
// -------------------------------------------------------------------------------------------------
@Composable
private fun AdminKycView(
    user: UserEntity?,
    onUpdateStatus: (String) -> Unit,
    onPreviewImage: (String) -> Unit
) {
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User data not loaded", color = TextMuted)
        }
        return
    }

    val kycStatus = user.kycStatus
    val statusColor = when (kycStatus) {
        "APPROVED" -> CryptoGreen
        "REJECTED" -> CryptoRed
        else -> RealGoldPrimary
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                border = BorderStroke(1.dp, CryptoCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                    .background(RealGoldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = RealGoldPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = user.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "@${user.username} • Level: ${user.level}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = kycStatus, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Document Number: ${user.kycDocumentNumber?.ifBlank { "Not provided" } ?: "Not provided"}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ID Front & Back Photo Affordances
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                user.kycIdFrontUri?.let { onPreviewImage(it) }
                            },
                            enabled = !user.kycIdFrontUri.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (user.kycIdFrontUri.isNullOrBlank()) "No Front ID" else "Front ID", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                user.kycIdBackUri?.let { onPreviewImage(it) }
                            },
                            enabled = !user.kycIdBackUri.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (user.kycIdBackUri.isNullOrBlank()) "No Back ID" else "Back ID", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Change KYC Status:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onUpdateStatus("APPROVED") },
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus("REJECTED") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoRed),
                            border = BorderStroke(1.dp, CryptoRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus("PENDING") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RealGoldPrimary),
                            border = BorderStroke(1.dp, RealGoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pending", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRealCoinControlView(
    user: UserEntity?,
    rate: MarketRateEntity?,
    onUpdateMarketPrice: ((Double, Double) -> Unit)?,
    onManualAddRealCoin: ((Double, String) -> Unit)?,
    onSetAdminRole: ((Boolean) -> Unit)?
) {
    val currentUsdRate = rate?.realCoinToUsd ?: 0.084481
    val currentUsdToEtb = rate?.usdToEtb ?: 186.0

    var usdRateInput by remember { mutableStateOf(String.format(Locale.US, "%.6f", currentUsdRate)) }
    var usdToEtbInput by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentUsdToEtb)) }

    var injectAmountInput by remember { mutableStateOf("") }
    var injectReasonInput by remember { mutableStateOf("Liquidity Reserve Injection") }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val parsedUsd = usdRateInput.toDoubleOrNull() ?: currentUsdRate
    val parsedUsdToEtb = usdToEtbInput.toDoubleOrNull() ?: currentUsdToEtb
    val calculatedEtb = parsedUsd * parsedUsdToEtb

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 6.dp)
    ) {
        // Feedback message
        if (feedbackMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoGreen.copy(alpha = 0.15f))
                    .border(1.dp, CryptoGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = feedbackMessage!!,
                    color = CryptoGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section 1: Role Authority Overview
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            border = BorderStroke(1.dp, RealGoldPrimary.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (user?.isAdmin == true) RealGoldPrimary else CryptoGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (user?.isAdmin == true) "Active Role: Administrator" else "Active Role: Standard User",
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onSetAdminRole != null) {
                        Button(
                            onClick = {
                                val next = !(user?.isAdmin ?: false)
                                onSetAdminRole(next)
                                feedbackMessage = if (next) "Granted Administrator permissions." else "Switched to standard User mode."
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user?.isAdmin == true) CryptoCardVariant else RealGoldPrimary,
                                contentColor = if (user?.isAdmin == true) TextPrimary else Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (user?.isAdmin == true) "Switch to User" else "Grant Admin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Admins alone can adjust the Real Coin market valuation, mint/credit liquidity, resolve disputed escrow contracts, and verify KYC deposits.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Real Coin Market Valuation Control
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            border = BorderStroke(1.dp, CryptoCardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Real Coin Market Rate Valuation",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Controls live pricing for P2P trading, wallets, and exchange swaps.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = usdRateInput,
                    onValueChange = { usdRateInput = it },
                    label = { Text("1 Real Coin in USD") },
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

                OutlinedTextField(
                    value = usdToEtbInput,
                    onValueChange = { usdToEtbInput = it },
                    label = { Text("1 USD in ETB Peg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RealGoldPrimary,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rate calculation preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardVariant)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Computed Rate (1 REAL):", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "${String.format(Locale.US, "%.2f", calculatedEtb)} ETB",
                            color = RealGoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val usd = usdRateInput.toDoubleOrNull()
                        val etbPeg = usdToEtbInput.toDoubleOrNull()
                        if (usd != null && usd > 0.0 && etbPeg != null && etbPeg > 0.0) {
                            val computed = usd * etbPeg
                            onUpdateMarketPrice?.invoke(usd, computed)
                            feedbackMessage = "Real Coin rate successfully updated: $usd USD = ${String.format(Locale.US, "%.2f", computed)} ETB"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply & Broadcast Real Coin Rate", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Mint / Inject Real Coin Supply
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
            border = BorderStroke(1.dp, CryptoCardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Supply & Balance Management",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Credit Real Coin liquidity to current account or treasury.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = injectAmountInput,
                    onValueChange = { injectAmountInput = it },
                    label = { Text("Real Coin Amount (REAL)") },
                    placeholder = { Text("e.g. 500") },
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

                OutlinedTextField(
                    value = injectReasonInput,
                    onValueChange = { injectReasonInput = it },
                    label = { Text("Reason / Reference Note") },
                    placeholder = { Text("e.g. Liquidity Provision") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RealGoldPrimary,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val amount = injectAmountInput.toDoubleOrNull()
                        if (amount != null && amount > 0.0) {
                            val reason = injectReasonInput.ifBlank { "Admin Supply Credit" }
                            onManualAddRealCoin?.invoke(amount, reason)
                            injectAmountInput = ""
                            feedbackMessage = "Successfully credited $amount REAL coins for '$reason'!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Inject Real Coin Balance", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
