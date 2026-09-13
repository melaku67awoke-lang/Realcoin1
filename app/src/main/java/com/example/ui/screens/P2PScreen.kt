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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.EscrowOrderEntity
import com.example.data.local.entities.P2PAdEntity
import com.example.data.local.entities.UserEntity
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
import kotlinx.coroutines.flow.Flow

@Composable
fun P2PScreen(
    user: UserEntity?,
    ads: List<P2PAdEntity>,
    orders: List<EscrowOrderEntity>,
    onCreateAd: (type: String, priceEtb: Double, minReal: Double, maxReal: Double, giftReal: Double, paymentMethods: String) -> Unit,
    onInitiateOrder: (ad: P2PAdEntity, amountReal: Double, paymentMethod: String, details: String) -> Unit,
    onMarkPaid: (orderId: String) -> Unit,
    onReleaseEscrow: (orderId: String) -> Unit,
    onDisputeOrder: (orderId: String, reason: String) -> Unit,
    onChatWithUser: (username: String) -> Unit,
    onGetDisputeMessages: (orderId: String) -> Flow<List<ChatMessageEntity>>,
    onSendDisputeMessage: (orderId: String, receiverUsername: String, text: String) -> Unit,
    onDeleteAd: (adId: String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: BUY, 1: SELL, 2: ORDERS, 3: MY ADS
    var showCreateAdDialog by remember { mutableStateOf(false) }
    var selectedAdForOrder by remember { mutableStateOf<P2PAdEntity?>(null) }
    var selectedOrderForDispute by remember { mutableStateOf<EscrowOrderEntity?>(null) }
    var selectedOrderForDisputeChat by remember { mutableStateOf<EscrowOrderEntity?>(null) }
    var adPendingDelete by remember { mutableStateOf<P2PAdEntity?>(null) }
    var activeOrdersNoticeMessage by remember { mutableStateOf<String?>(null) }

    if (selectedOrderForDisputeChat != null) {
        val currentOrder = selectedOrderForDisputeChat!!
        val disputeMessages by onGetDisputeMessages(currentOrder.orderId).collectAsState(initial = emptyList())
        P2PDisputeChatScreen(
            order = currentOrder,
            currentUser = user,
            messages = disputeMessages,
            onSendMessage = { text ->
                val receiver = if (user?.userId == currentOrder.buyerId) currentOrder.sellerUsername else currentOrder.buyerUsername
                onSendDisputeMessage(currentOrder.orderId, receiver, text)
            },
            onReleaseEscrow = { orderId ->
                onReleaseEscrow(orderId)
            },
            onBackClick = {
                selectedOrderForDisputeChat = null
            }
        )
        return
    }

    val tabs = listOf("BUY", "SELL", "ORDERS", "MY ADS")
    val filteredAds = remember(ads, selectedTab, user) {
        when (selectedTab) {
            // When user wants to BUY, they see advertisements posted by users who want to SELL
            0 -> ads.filter { it.type == "SELL" }
            // When user wants to SELL, they see advertisements posted by users who want to BUY
            1 -> ads.filter { it.type == "BUY" }
            3 -> ads.filter { it.userId == user?.userId }
            else -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CryptoDarkBg)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "P2P Escrow Exchange", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "100% Escrow Protection • 0% Fee", color = CryptoGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = { showCreateAdDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("create_ad_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Ad", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Post Ad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CryptoDarkBg,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RealGoldPrimary,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = if (index == 2 && orders.count { it.status == "PENDING_PAYMENT" || it.status == "PAID_WAITING_RELEASE" } > 0)
                                "ORDERS (${orders.count { it.status == "PENDING_PAYMENT" || it.status == "PAID_WAITING_RELEASE" }})"
                            else title,
                            color = if (selectedTab == index) RealGoldPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        if (selectedTab == 2) {
            // Escrow Orders List
            val disputedOrders = orders.filter { it.status == "DISPUTED" }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (disputedOrders.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CryptoRed.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, CryptoRed.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOrderForDisputeChat = disputedOrders.first()
                                }
                                .testTag("active_disputes_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(CryptoRed.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Dispute Warning",
                                            tint = CryptoRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "⚠️ ${disputedOrders.size} Active Escrow Dispute${if (disputedOrders.size > 1) "s" else ""}",
                                            color = CryptoRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Order #${disputedOrders.first().orderId.take(8)} • Tap to open Dispute Chat",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Button(
                                    onClick = { selectedOrderForDisputeChat = disputedOrders.first() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CryptoRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text(text = "Chat 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (orders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No P2P orders yet.", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(orders) { order ->
                        EscrowOrderCard(
                            order = order,
                            currentUserId = user?.userId ?: "",
                            onMarkPaid = { onMarkPaid(order.orderId) },
                            onReleaseEscrow = { onReleaseEscrow(order.orderId) },
                            onOpenDispute = { selectedOrderForDispute = order },
                            onChat = {
                                selectedOrderForDisputeChat = order
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        } else {
            // Ads List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredAds.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No active advertisements available.", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(filteredAds, key = { it.id }) { ad ->
                        val adOrders = remember(orders, ad.id) { orders.filter { it.adId == ad.id } }
                        val activeOrders = remember(adOrders) {
                            adOrders.filter {
                                it.status == "PENDING_PAYMENT" || it.status == "PAID_WAITING_RELEASE" || it.status.equals("DISPUTED", ignoreCase = true)
                            }
                        }
                        val hasActiveOrders = activeOrders.isNotEmpty()

                        P2PAdCard(
                            ad = ad,
                            isMyAd = selectedTab == 3,
                            hasActiveOrders = hasActiveOrders,
                            activeOrdersCount = activeOrders.size,
                            onActionClick = { selectedAdForOrder = ad },
                            onChatClick = { onChatWithUser(ad.username) },
                            onDeleteClick = {
                                if (hasActiveOrders) {
                                    activeOrdersNoticeMessage = "Cannot delete advertisement #${ad.id.take(6)}. There are ${activeOrders.size} active escrow order(s) currently in progress for this ad. Please finalize or release the orders before deleting."
                                } else {
                                    adPendingDelete = ad
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Dialogs
    if (showCreateAdDialog) {
        CreateP2PAdDialog(
            onDismiss = { showCreateAdDialog = false },
            onCreate = { type, price, min, max, gift, payMethods ->
                onCreateAd(type, price, min, max, gift, payMethods)
                showCreateAdDialog = false
            }
        )
    }

    selectedAdForOrder?.let { ad ->
        EscrowCheckoutDialog(
            ad = ad,
            user = user,
            onDismiss = { selectedAdForOrder = null },
            onConfirmOrder = { amount, method, details ->
                onInitiateOrder(ad, amount, method, details)
                selectedAdForOrder = null
                selectedTab = 2 // switch to Orders tab
            }
        )
    }

    selectedOrderForDispute?.let { order ->
        DisputeOrderDialog(
            order = order,
            onDismiss = { selectedOrderForDispute = null },
            onConfirmDispute = { reason ->
                onDisputeOrder(order.orderId, reason)
                selectedOrderForDispute = null
                selectedOrderForDisputeChat = order.copy(status = "DISPUTED", disputeReason = reason)
            }
        )
    }

    // Delete confirmation dialog
    if (adPendingDelete != null) {
        val targetAd = adPendingDelete!!
        AlertDialog(
            onDismissRequest = { adPendingDelete = null },
            containerColor = CryptoCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = CryptoRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Advertisement", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this ${targetAd.type} advertisement?",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Price: ${String.format("%.2f", targetAd.priceEtb)} ETB • Limits: ${String.format("%.0f", targetAd.minReal)}-${String.format("%.0f", targetAd.maxReal)} REAL",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This advertisement has 0 active orders and will be permanently removed from the P2P market.",
                        color = CryptoGreen,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAd(targetAd.id)
                        adPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Ad", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { adPendingDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Active orders warning dialog
    if (activeOrdersNoticeMessage != null) {
        AlertDialog(
            onDismissRequest = { activeOrdersNoticeMessage = null },
            containerColor = CryptoCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = RealGoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Active Orders Protected", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(activeOrdersNoticeMessage!!, color = TextSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { activeOrdersNoticeMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun P2PAdCard(
    ad: P2PAdEntity,
    isMyAd: Boolean = false,
    hasActiveOrders: Boolean = false,
    activeOrdersCount: Int = 0,
    onActionClick: () -> Unit,
    onChatClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // User row
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
                            .background(RealGoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = ad.username.first().uppercase(), color = RealGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (isMyAd) "${ad.username} (You)" else ad.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = CryptoGreen, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = if (isMyAd) "Ad Type: I want to ${ad.type}" else "${ad.completionRate}% completion • ${ad.completedOrders} orders",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isMyAd) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (hasActiveOrders) RealGoldPrimary.copy(alpha = 0.18f) else CryptoGreen.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasActiveOrders) "$activeOrdersCount Active Orders" else "0 Active Orders",
                            color = if (hasActiveOrders) RealGoldLight else CryptoGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = ad.lastSeenText,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Limits
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "Price", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${String.format("%.2f", ad.priceEtb)} ETB",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Limits", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${String.format("%.2f", ad.minReal)} - ${String.format("%.2f", ad.maxReal)} REAL",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (ad.giftReal > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(RealGoldPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎁 +${String.format("%.2f", ad.giftReal)} Gift REAL Bonus", color = RealGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment methods & Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ad.paymentMethods.split(",").take(2).forEach { method ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CryptoCardVariant)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(text = method.trim(), color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (isMyAd) {
                    // Delete Button for My Ads
                    Button(
                        onClick = { onDeleteClick?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasActiveOrders) CryptoCardVariant else CryptoRed.copy(alpha = 0.85f),
                            contentColor = if (hasActiveOrders) TextMuted else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("delete_ad_btn_${ad.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasActiveOrders) "Locked (${activeOrdersCount})" else "Delete Ad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onChatClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoCardVariant, contentColor = TextPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "Chat", fontSize = 11.sp)
                        }

                        // If the ad is SELL, the viewer buys REAL (CryptoGreen).
                        // If the ad is BUY, the viewer sells REAL (CryptoRed).
                        val isAdSelling = ad.type == "SELL"
                        Button(
                            onClick = onActionClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAdSelling) CryptoGreen else CryptoRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("ad_action_btn_${ad.id}")
                        ) {
                            Text(
                                text = if (isAdSelling) "Buy REAL" else "Sell REAL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EscrowOrderCard(
    order: EscrowOrderEntity,
    currentUserId: String,
    onMarkPaid: () -> Unit,
    onReleaseEscrow: () -> Unit,
    onOpenDispute: () -> Unit,
    onChat: () -> Unit
) {
    val isBuyer = order.buyerId == currentUserId
    val statusColor = when (order.status) {
        "COMPLETED" -> CryptoGreen
        "PAID_WAITING_RELEASE" -> RealGoldPrimary
        "DISPUTED" -> CryptoRed
        "CANCELLED" -> TextMuted
        else -> RealGoldLight // PENDING_PAYMENT
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoCardBorder)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Escrow", tint = RealGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Order #${order.orderId.take(8)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (order.status) {
                            "PENDING_PAYMENT" -> "Pending Payment"
                            "PAID_WAITING_RELEASE" -> "Waiting Release"
                            "COMPLETED" -> "Completed"
                            "DISPUTED" -> "Disputed ⚠️"
                            else -> order.status
                        },
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Amount", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${String.format("%.2f", order.realAmount)} REAL${if (order.giftRealAmount > 0) " (+${String.format("%.2f", order.giftRealAmount)} Gift)" else ""}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total ETB", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${String.format("%.2f", order.totalEtb)} ETB",
                        color = RealGoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Buyer: @${order.buyerUsername}  •  Seller: @${order.sellerUsername}",
                color = TextMuted,
                fontSize = 12.sp
            )
            Text(
                text = "Payment: ${order.paymentMethod} (${order.paymentAccountDetails})",
                color = TextSecondary,
                fontSize = 12.sp
            )

            if (order.disputeReason != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CryptoRed.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Text(text = "Dispute Reason: ${order.disputeReason}", color = CryptoRed, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.status == "DISPUTED") {
                    Button(
                        onClick = onChat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CryptoRed.copy(alpha = 0.12f),
                            contentColor = CryptoRed
                        ),
                        border = BorderStroke(1.dp, CryptoRed.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("dispute_chat_btn_${order.orderId}")
                    ) {
                        Text(text = "Dispute Chat 💬", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onChat,
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoCardVariant, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("order_chat_btn_${order.orderId}")
                    ) {
                        Text(text = "Chat", fontSize = 12.sp)
                    }
                }

                // If buyer & pending payment -> can mark paid
                if (isBuyer && order.status == "PENDING_PAYMENT") {
                    Button(
                        onClick = onMarkPaid,
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("mark_paid_btn_${order.orderId}")
                    ) {
                        Text(text = "I Have Paid", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // If seller & paid -> can release escrow
                if (!isBuyer && order.status == "PAID_WAITING_RELEASE") {
                    Button(
                        onClick = onReleaseEscrow,
                        colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("release_escrow_btn_${order.orderId}")
                    ) {
                        Text(text = "Release Escrow", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Can dispute if paid or pending
                if (order.status == "PAID_WAITING_RELEASE" || order.status == "PENDING_PAYMENT") {
                    Button(
                        onClick = onOpenDispute,
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoRed.copy(alpha = 0.2f), contentColor = CryptoRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text(text = "Dispute", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateP2PAdDialog(
    onDismiss: () -> Unit,
    onCreate: (type: String, priceEtb: Double, minReal: Double, maxReal: Double, giftReal: Double, paymentMethods: String) -> Unit
) {
    var type by remember { mutableStateOf("SELL") }
    var priceInput by remember { mutableStateOf("") }
    var minInput by remember { mutableStateOf("") }
    var maxInput by remember { mutableStateOf("") }
    var giftInput by remember { mutableStateOf("") }
    var methodsInput by remember { mutableStateOf("Telebirr, CBE BIRR") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoCardBg,
        title = {
            Text(text = "Post P2P Advertisement", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("SELL", "BUY").forEach { t ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (type == t) (if (t == "SELL") CryptoRed else CryptoGreen) else CryptoCardVariant)
                                .clickable {
                                    type = t
                                    validationError = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "I Want to $t",
                                color = if (type == t) Color.White else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (type == "SELL") {
                        "💡 Your ad will be visible in the 'BUY' tab for other traders to buy Real Coin from you."
                    } else {
                        "💡 Your ad will be visible in the 'SELL' tab for other traders to sell Real Coin to you."
                    },
                    color = RealGoldLight,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError!!,
                        color = CryptoRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        validationError = null
                    },
                    label = { Text("Price per Real Coin (ETB)") },
                    placeholder = { Text("e.g. 15.75") },
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minInput,
                        onValueChange = {
                            minInput = it
                            validationError = null
                        },
                        label = { Text("Min REAL") },
                        placeholder = { Text("e.g. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RealGoldPrimary,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = maxInput,
                        onValueChange = {
                            maxInput = it
                            validationError = null
                        },
                        label = { Text("Max REAL") },
                        placeholder = { Text("e.g. 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RealGoldPrimary,
                            unfocusedBorderColor = CryptoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = giftInput,
                    onValueChange = { giftInput = it },
                    label = { Text("Bonus Gift in REAL (Optional)") },
                    placeholder = { Text("e.g. 5.0") },
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
                    value = methodsInput,
                    onValueChange = {
                        methodsInput = it
                        validationError = null
                    },
                    label = { Text("Accepted Payment Methods") },
                    placeholder = { Text("e.g. Telebirr, CBE BIRR, Awash") },
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
                    val price = priceInput.toDoubleOrNull()
                    val min = minInput.toDoubleOrNull()
                    val max = maxInput.toDoubleOrNull()
                    val gift = giftInput.toDoubleOrNull() ?: 0.0

                    if (price == null || price <= 0.0) {
                        validationError = "Please enter a valid price per Real Coin."
                    } else if (min == null || min <= 0.0) {
                        validationError = "Please enter a valid minimum REAL amount."
                    } else if (max == null || max < min) {
                        validationError = "Maximum amount must be greater than or equal to minimum amount."
                    } else if (methodsInput.isBlank()) {
                        validationError = "Please enter at least one payment method."
                    } else {
                        onCreate(type, price, min, max, gift, methodsInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Publish Advertisement", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun EscrowCheckoutDialog(
    ad: P2PAdEntity,
    user: UserEntity?,
    onDismiss: () -> Unit,
    onConfirmOrder: (amountReal: Double, method: String, details: String) -> Unit
) {
    var amountInput by remember { mutableStateOf(ad.minReal.toString()) }
    var selectedMethod by remember { mutableStateOf(ad.paymentMethods.split(",").firstOrNull()?.trim() ?: "Telebirr") }
    var accountDetailsInput by remember { mutableStateOf(user?.phone ?: "") }

    val amountReal = amountInput.toDoubleOrNull() ?: 0.0
    val totalEtb = amountReal * ad.priceEtb
    val isValid = amountReal >= ad.minReal && amountReal <= ad.maxReal
    val isBuyingFromAd = ad.type == "SELL" // Poster is selling, so viewer is buying

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Escrow", tint = RealGoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBuyingFromAd) "Buy Real Coin (Escrow)" else "Sell Real Coin (Escrow)",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isBuyingFromAd) {
                        "Seller @${ad.username}'s Real Coins will be locked into the smart Escrow contract until you transfer payment."
                    } else {
                        "Buyer @${ad.username} will transfer ${String.format("%.2f", totalEtb)} ETB. Your Real Coins are safeguarded in escrow."
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Real Coin Amount (${ad.minReal.toInt()}-${ad.maxReal.toInt()})") },
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
                    value = accountDetailsInput,
                    onValueChange = { accountDetailsInput = it },
                    label = { Text("Your Payment Account / Phone ($selectedMethod)") },
                    placeholder = { Text("e.g. +251911000000 / CBE Acc") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RealGoldPrimary,
                        unfocusedBorderColor = CryptoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardVariant)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBuyingFromAd) "Total ETB to Transfer:" else "Total ETB to Receive:", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "${String.format("%.2f", totalEtb)} ETB", color = RealGoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        if (ad.giftReal > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Bonus Gift:", color = CryptoGreen, fontSize = 12.sp)
                                Text(text = "+${String.format("%.2f", ad.giftReal)} REAL", color = CryptoGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirmOrder(amountReal, selectedMethod, accountDetailsInput)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = RealGoldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_escrow_order_btn")
            ) {
                Text("Confirm Escrow Order", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun DisputeOrderDialog(
    order: EscrowOrderEntity,
    onDismiss: () -> Unit,
    onConfirmDispute: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("Payment made via Telebirr but seller did not release escrow.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptoCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "Dispute", tint = CryptoRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Open Dispute (#${order.orderId.take(6)})", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "A customer support agent will freeze the escrow funds and investigate transaction receipts.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Dispute") },
                    minLines = 3,
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
                onClick = { onConfirmDispute(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = CryptoRed, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Submit Dispute", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
