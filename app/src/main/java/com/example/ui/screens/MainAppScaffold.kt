package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.CryptoCardBg
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoDarkBg
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.RealGoldLight
import com.example.ui.theme.RealGoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNotification
import com.example.ui.viewmodel.NotificationType
import com.example.ui.viewmodel.RealCoinViewModel
import kotlinx.coroutines.delay

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    TRADE("Trade", Icons.Filled.CurrencyExchange, Icons.Outlined.CurrencyExchange),
    COMMUNITY("Community", Icons.Filled.Public, Icons.Outlined.Public),
    NEWS("Rates", Icons.Filled.ShowChart, Icons.Outlined.ShowChart),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

enum class ActiveModal {
    NONE,
    DEPOSIT_USDT,
    WITHDRAW_REAL,
    CHAT,
    SPIN_WHEEL,
    KYC,
    HELP_CENTER,
    ADMIN_DEPOSIT_REVIEW,
    ADMIN_CONTROLS,
    NOTIFICATIONS,
    USER_LEVEL
}

@Composable
fun MainAppScaffold(viewModel: RealCoinViewModel) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var activeModal by remember { mutableStateOf(ActiveModal.NONE) }

    val user by viewModel.user.collectAsState()
    val rate by viewModel.marketRate.collectAsState()
    val ads by viewModel.activeAds.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val appNotification by viewModel.appNotification.collectAsState()
    val selectedChatUser by viewModel.selectedChatUser.collectAsState()
    val chatMessages by viewModel.currentChatMessages.collectAsState()
    val depositAddress by viewModel.bep20DepositAddress.collectAsState()
    val allDepositRequests by viewModel.allDepositRequests.collectAsState()
    val pendingDepositRequests by viewModel.pendingDepositRequests.collectAsState()
    val allWithdrawRequests by viewModel.allWithdrawRequests.collectAsState()
    val notificationHistory by viewModel.notificationHistory.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger Native Android Toast popup & snackbar auto-dismiss
    LaunchedEffect(appNotification) {
        val notif = appNotification ?: return@LaunchedEffect
        // 1. Android Native Toast popup notification
        Toast.makeText(context, notif.message, Toast.LENGTH_SHORT).show()
        // 2. Compose Material 3 Snackbar in SnackbarHost
        snackbarHostState.showSnackbar(
            message = notif.message,
            actionLabel = "Dismiss",
            duration = SnackbarDuration.Short
        )
        // 3. Auto dismiss after 4.5 seconds
        delay(4500)
        if (viewModel.appNotification.value?.id == notif.id) {
            viewModel.clearNotification()
        }
    }

    Scaffold(
        containerColor = CryptoDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("app_snackbar_host")
            ) { data ->
                val currentNotif = appNotification
                if (currentNotif != null) {
                    AppActionSnackbar(
                        notification = currentNotif,
                        onDismiss = {
                            data.dismiss()
                            viewModel.clearNotification()
                        }
                    )
                } else {
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF131D2D),
                        contentColor = TextPrimary,
                        actionColor = RealGoldPrimary,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (activeModal == ActiveModal.NONE) {
                NavigationBar(
                    containerColor = CryptoCardBg,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .height(68.dp)
                        .border(0.5.dp, CryptoCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RealGoldPrimary,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = RealGoldPrimary,
                                unselectedTextColor = TextMuted,
                                indicatorColor = RealGoldPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main tab content
            when (selectedTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        user = user,
                        rate = rate,
                        transactions = transactions,
                        onDepositClick = { activeModal = ActiveModal.DEPOSIT_USDT },
                        onWithdrawClick = { activeModal = ActiveModal.WITHDRAW_REAL },
                        onNavigateP2P = { selectedTab = MainTab.TRADE },
                        onNavigateSpinWheel = { activeModal = ActiveModal.SPIN_WHEEL },
                        onNavigateKYC = { activeModal = ActiveModal.KYC },
                        onNavigateChat = { activeModal = ActiveModal.CHAT },
                        onNavigateHelp = { activeModal = ActiveModal.HELP_CENTER },
                        onNavigateNotifications = { activeModal = ActiveModal.NOTIFICATIONS },
                        onNavigateCommunity = { selectedTab = MainTab.COMMUNITY },
                        onNavigateNewsRates = { selectedTab = MainTab.NEWS },
                        onClaimDailyReward = { viewModel.claimDailyReward() },
                        onNavigateAdminDeposits = { activeModal = ActiveModal.ADMIN_CONTROLS },
                        onNavigateLevel = { activeModal = ActiveModal.USER_LEVEL },
                        pendingDepositCount = pendingDepositRequests.size
                    )
                }
                MainTab.TRADE -> {
                    P2PScreen(
                        user = user,
                        ads = ads,
                        orders = orders,
                        onCreateAd = { type, price, min, max, gift, payMethods ->
                            viewModel.createAd(type, price, min, max, gift, payMethods)
                        },
                        onInitiateOrder = { ad, amount, method, details ->
                            viewModel.initiateEscrowOrder(ad, amount, method, details) {}
                        },
                        onMarkPaid = { orderId -> viewModel.markOrderPaid(orderId) },
                        onReleaseEscrow = { orderId -> viewModel.releaseEscrow(orderId) },
                        onDisputeOrder = { orderId, reason -> viewModel.disputeOrder(orderId, reason) },
                        onChatWithUser = { username ->
                            viewModel.selectChatUser(username)
                            activeModal = ActiveModal.CHAT
                        },
                        onGetDisputeMessages = { orderId ->
                            viewModel.getDisputeMessages(orderId)
                        },
                        onSendDisputeMessage = { orderId, receiver, text ->
                            viewModel.sendDisputeMessage(orderId, receiver, text)
                        },
                        onDeleteAd = { adId ->
                            viewModel.deleteAd(adId)
                        }
                    )
                }
                MainTab.COMMUNITY -> {
                    CommunityScreen(
                        posts = posts,
                        onCreatePost = { caption, type -> viewModel.createPost(caption, type) },
                        onToggleLike = { id, isLiked -> viewModel.toggleLike(id, isLiked) }
                    )
                }
                MainTab.NEWS -> {
                    NewsRatesScreen(
                        rate = rate,
                        onUpdateRate = { newRate -> viewModel.updateMarketRate(newRate) }
                    )
                }
                MainTab.PROFILE -> {
                    ProfileScreen(
                        user = user,
                        onNavigateKYC = { activeModal = ActiveModal.KYC },
                        onNavigateSpinWheel = { activeModal = ActiveModal.SPIN_WHEEL },
                        onNavigateHelp = { activeModal = ActiveModal.HELP_CENTER },
                        onNavigateAdminDeposits = { activeModal = ActiveModal.ADMIN_CONTROLS },
                        onNavigateLevel = { activeModal = ActiveModal.USER_LEVEL },
                        onNavigateNotifications = { activeModal = ActiveModal.NOTIFICATIONS },
                        onSetAdminRole = { isAdmin -> viewModel.setUserAdminRole(isAdmin) }
                    )
                }
            }

            // Modals & Fullscreen overlays
            when (activeModal) {
                ActiveModal.USER_LEVEL -> {
                    UserLevelScreen(
                        user = user,
                        onDismiss = { activeModal = ActiveModal.NONE },
                        onNavigateDeposit = { activeModal = ActiveModal.DEPOSIT_USDT },
                        onClaimDailyReward = { viewModel.claimDailyReward() }
                    )
                }
                ActiveModal.DEPOSIT_USDT -> {
                    DepositUsdtDialog(
                        user = user,
                        rate = rate,
                        depositAddress = depositAddress,
                        onDismiss = { activeModal = ActiveModal.NONE },
                        onSubmitProof = { usdtAmount, screenshotUri, txHash ->
                            viewModel.submitDepositProof(usdtAmount, screenshotUri, txHash)
                        },
                        onConfirmDepositDirect = { usdtAmount ->
                            viewModel.depositUsdt(usdtAmount)
                        }
                    )
                }
                ActiveModal.WITHDRAW_REAL -> {
                    WithdrawRealDialog(
                        user = user,
                        rate = rate,
                        onDismiss = { activeModal = ActiveModal.NONE },
                        onConfirmWithdraw = { realAmount, addr -> viewModel.withdrawRealCoin(realAmount, addr) }
                    )
                }
                ActiveModal.CHAT -> {
                    ChatScreen(
                        currentSelectedUser = selectedChatUser,
                        messages = chatMessages,
                        onSelectUser = { viewModel.selectChatUser(it) },
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onBackClick = { activeModal = ActiveModal.NONE }
                    )
                }
                ActiveModal.SPIN_WHEEL -> {
                    SpinWheelScreen(
                        user = user,
                        onBackClick = { activeModal = ActiveModal.NONE },
                        onExecuteSpin = { isFree, prizeWon, onComplete ->
                            viewModel.executeSpin(isFree, prizeWon, onComplete)
                        },
                        onResetDailySpin = {
                            viewModel.resetFreeSpin()
                        }
                    )
                }
                ActiveModal.KYC -> {
                    KYCScreen(
                        user = user,
                        onBackClick = { activeModal = ActiveModal.NONE },
                        onSubmitKyc = { docNum, front, back, status ->
                            viewModel.submitKyc(docNum, front, back, status)
                        },
                        onStatusChange = { newStatus ->
                            viewModel.setKycStatus(newStatus)
                        }
                    )
                }
                ActiveModal.HELP_CENTER -> {
                    HelpCenterScreen(
                        tickets = tickets,
                        onBackClick = { activeModal = ActiveModal.NONE },
                        onSubmitTicket = { cat, sub, msg, img ->
                            viewModel.submitHelpTicket(cat, sub, msg, img)
                        }
                    )
                }
                ActiveModal.ADMIN_CONTROLS, ActiveModal.ADMIN_DEPOSIT_REVIEW -> {
                    AdminControlsDialog(
                        user = user,
                        rate = rate,
                        depositRequests = allDepositRequests,
                        withdrawRequests = allWithdrawRequests,
                        orders = orders,
                        onDismiss = { activeModal = ActiveModal.NONE },
                        onApproveDeposit = { reqId -> viewModel.approveDeposit(reqId) },
                        onRejectDeposit = { reqId, reason -> viewModel.rejectDeposit(reqId, reason) },
                        onApproveWithdraw = { reqId, txHash -> viewModel.approveWithdraw(reqId, txHash) },
                        onRejectWithdraw = { reqId, reason -> viewModel.rejectWithdraw(reqId, reason) },
                        onResolveDispute = { orderId, releaseToBuyer, note ->
                            viewModel.adminResolveDispute(orderId, releaseToBuyer, note)
                        },
                        onUpdateKycStatus = { userId, status ->
                            viewModel.adminUpdateKyc(userId, status)
                        },
                        onUpdateMarketPrice = { usdReal, usdEtb ->
                            viewModel.updateMarketPrice(usdReal, usdEtb)
                        },
                        onManualAddRealCoin = { amount, reason ->
                            viewModel.manualAddRealCoin(amount, reason)
                        },
                        onSetAdminRole = { isAdmin ->
                            viewModel.setUserAdminRole(isAdmin)
                        }
                    )
                }
                ActiveModal.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notificationHistory,
                        onDismiss = { activeModal = ActiveModal.NONE },
                        onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                        onClearAll = { viewModel.clearNotificationHistory() }
                    )
                }
                ActiveModal.NONE -> {}
            }

            // Top Floating Notification Banner
            AnimatedVisibility(
                visible = appNotification != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                appNotification?.let { notif ->
                    AppActionSnackbar(
                        notification = notif,
                        onDismiss = { viewModel.clearNotification() }
                    )
                }
            }
        }
    }
}

@Composable
fun AppActionSnackbar(
    notification: AppNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, tint, borderColor, title) = when (notification.type) {
        NotificationType.SUCCESS -> Quadruple(
            Icons.Default.CheckCircle,
            CryptoGreen,
            CryptoGreen.copy(alpha = 0.7f),
            "Success"
        )
        NotificationType.ERROR -> Quadruple(
            Icons.Default.Error,
            CryptoRed,
            CryptoRed.copy(alpha = 0.7f),
            "Notice"
        )
        NotificationType.WARNING -> Quadruple(
            Icons.Default.Warning,
            Color(0xFFF59E0B),
            Color(0xFFF59E0B).copy(alpha = 0.7f),
            "Notice"
        )
        NotificationType.INFO -> Quadruple(
            Icons.Default.Info,
            RealGoldPrimary,
            RealGoldPrimary.copy(alpha = 0.7f),
            "Info"
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2D)),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("action_confirmation_snackbar")
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = tint,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.message,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("dismiss_snackbar_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

