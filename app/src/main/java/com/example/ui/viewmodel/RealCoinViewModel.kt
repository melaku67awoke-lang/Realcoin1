package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.DepositRequestEntity
import com.example.data.local.entities.EscrowOrderEntity
import com.example.data.local.entities.HelpTicketEntity
import com.example.data.local.entities.MarketRateEntity
import com.example.data.local.entities.P2PAdEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WalletTransactionEntity
import com.example.data.local.entities.WithdrawRequestEntity
import com.example.data.repository.RealCoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RealCoinViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = RealCoinRepository(database)

    val user: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val marketRate: StateFlow<MarketRateEntity?> = repository.marketRateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeAds: StateFlow<List<P2PAdEntity>> = repository.activeAdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<EscrowOrderEntity>> = repository.allOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<WalletTransactionEntity>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val posts: StateFlow<List<PostEntity>> = repository.allPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tickets: StateFlow<List<HelpTicketEntity>> = repository.allTicketsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDepositRequests: StateFlow<List<DepositRequestEntity>> = repository.allDepositRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDepositRequests: StateFlow<List<DepositRequestEntity>> = repository.pendingDepositRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawRequests: StateFlow<List<WithdrawRequestEntity>> = repository.allWithdrawRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingWithdrawRequests: StateFlow<List<WithdrawRequestEntity>> = repository.pendingWithdrawRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notification History for top notification bell
    private val _notificationHistory = MutableStateFlow<List<NotificationHistoryItem>>(
        listOf(
            NotificationHistoryItem(
                id = "notif_01",
                title = "BNB Smart Chain (BEP-20) Active",
                message = "All USDT deposits & withdrawals are processed strictly on BEP-20 for fast confirmation and low gas fees.",
                category = "SYSTEM",
                timestamp = System.currentTimeMillis() - 1800000L
            ),
            NotificationHistoryItem(
                id = "notif_02",
                title = "10% Deposit Bonus Credited",
                message = "Your recent USDT deposit was confirmed. +10% bonus Real Coins have been added to your balance!",
                category = "DEPOSIT",
                timestamp = System.currentTimeMillis() - 7200000L
            ),
            NotificationHistoryItem(
                id = "notif_03",
                title = "Identity Verification Approved",
                message = "Your KYC verification is active. Level privileges and high P2P limits are unlocked.",
                category = "KYC",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            NotificationHistoryItem(
                id = "notif_04",
                title = "Daily Reward Available",
                message = "Your 24-hour daily reward is ready to claim in the Rewards section!",
                category = "REWARD",
                timestamp = System.currentTimeMillis() - 144000000L
            )
        )
    )
    val notificationHistory: StateFlow<List<NotificationHistoryItem>> = _notificationHistory.asStateFlow()

    // Chat target
    private val _selectedChatUser = MutableStateFlow<String>("RealCoin Official Support")
    val selectedChatUser: StateFlow<String> = _selectedChatUser.asStateFlow()

    val allChatMessages: StateFlow<List<ChatMessageEntity>> = repository.allChatMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentChatMessages: StateFlow<List<ChatMessageEntity>> = _selectedChatUser
        .flatMapLatest { user -> repository.getChatMessagesFlow(user) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Banner & Snackbar/Toast Notifications
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _appNotification = MutableStateFlow<AppNotification?>(null)
    val appNotification: StateFlow<AppNotification?> = _appNotification.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun selectChatUser(username: String) {
        _selectedChatUser.value = username
    }

    // BSC BNB Smart Chain (BEP20) Deposit Address
    private val _bep20DepositAddress = MutableStateFlow("0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4")
    val bep20DepositAddress: StateFlow<String> = _bep20DepositAddress.asStateFlow()

    fun updateBep20DepositAddress(newAddress: String) {
        _bep20DepositAddress.value = newAddress
    }

    fun showNotification(
        message: String,
        type: NotificationType = NotificationType.SUCCESS,
        actionLabel: String? = null,
        title: String? = null,
        category: String = "SYSTEM"
    ) {
        _userMessage.value = message
        _appNotification.value = AppNotification(
            id = System.currentTimeMillis(),
            message = message,
            type = type,
            actionLabel = actionLabel
        )
        val newItem = NotificationHistoryItem(
            id = "notif_${System.currentTimeMillis()}",
            title = title ?: when (type) {
                NotificationType.SUCCESS -> "Action Successful"
                NotificationType.ERROR -> "Error Alert"
                NotificationType.WARNING -> "Notice"
                NotificationType.INFO -> "Information"
            },
            message = message,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        _notificationHistory.value = listOf(newItem) + _notificationHistory.value
    }

    fun markNotificationAsRead(id: String) {
        _notificationHistory.value = _notificationHistory.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notificationHistory.value = _notificationHistory.value.map { it.copy(isRead = true) }
    }

    fun clearNotificationHistory() {
        _notificationHistory.value = emptyList()
    }

    fun clearNotification() {
        _appNotification.value = null
        _userMessage.value = null
    }

    fun clearMessage() {
        clearNotification()
    }

    fun submitDepositProof(
        amountUsdt: Double,
        screenshotUri: String?,
        txHash: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.submitDepositRequest(amountUsdt, screenshotUri, txHash)
            result.onSuccess { req ->
                showNotification(
                    "Deposit submitted successfully! Admin will verify proof and credit ${String.format("%.2f", req.totalRealCoin)} REAL (+10% Bonus).",
                    NotificationType.SUCCESS
                )
                onSuccess()
            }.onFailure { err ->
                showNotification(err.message ?: "Deposit submission failed", NotificationType.ERROR)
            }
        }
    }

    fun approveDeposit(requestId: String, adminNotes: String? = null) {
        viewModelScope.launch {
            val result = repository.approveDepositRequest(requestId, adminNotes)
            result.onSuccess { credited ->
                showNotification(
                    "Deposit approved successfully! Credited ${String.format("%.2f", credited)} REAL (+10% Bonus) to balance.",
                    NotificationType.SUCCESS
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Approval failed", NotificationType.ERROR)
            }
        }
    }

    fun rejectDeposit(requestId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.rejectDepositRequest(requestId, reason)
            result.onSuccess {
                showNotification("Deposit request rejected.", NotificationType.WARNING)
            }.onFailure { err ->
                showNotification(err.message ?: "Rejection failed", NotificationType.ERROR)
            }
        }
    }

    fun manualAddRealCoin(amount: Double, reason: String) {
        viewModelScope.launch {
            val result = repository.manualAddRealCoinBalance(amount, reason)
            result.onSuccess { newBal ->
                showNotification(
                    "Admin adjustment: Credited ${String.format("%.2f", amount)} REAL. New balance: ${String.format("%.2f", newBal)} REAL.",
                    NotificationType.SUCCESS
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Failed to adjust balance", NotificationType.ERROR)
            }
        }
    }

    fun depositUsdt(amountUsdt: Double) {
        viewModelScope.launch {
            val result = repository.depositUsdt(amountUsdt)
            result.onSuccess { coins ->
                showNotification(
                    "Deposit submitted successfully! Credited \$${amountUsdt} USDT (+10% Bonus: +${String.format("%.2f", coins)} REAL).",
                    NotificationType.SUCCESS
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Deposit failed", NotificationType.ERROR)
            }
        }
    }

    fun withdrawRealCoin(amountReal: Double, address: String) {
        viewModelScope.launch {
            val result = repository.withdrawRealCoinToUsdt(amountReal, address)
            result.onSuccess { usdt ->
                showNotification(
                    "Withdrawal submitted successfully! ${String.format("%.2f", amountReal)} REAL for \$${String.format("%.2f", usdt)} USDT.",
                    NotificationType.SUCCESS
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Withdrawal failed", NotificationType.ERROR)
            }
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val result = repository.claimDailyReward()
            result.onSuccess { reward ->
                showNotification(
                    "Daily reward claimed! +${String.format("%.0f", reward)} REAL added to your balance.",
                    NotificationType.SUCCESS
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Daily reward not ready", NotificationType.WARNING)
            }
        }
    }

    fun createAd(type: String, priceEtb: Double, minReal: Double, maxReal: Double, giftReal: Double, paymentMethods: String) {
        viewModelScope.launch {
            val result = repository.createP2PAd(type, priceEtb, minReal, maxReal, giftReal, paymentMethods)
            result.onSuccess {
                showNotification("P2P $type advertisement created successfully!", NotificationType.SUCCESS)
            }.onFailure { err ->
                showNotification(err.message ?: "Could not create ad", NotificationType.ERROR)
            }
        }
    }

    fun deleteAd(adId: String) {
        viewModelScope.launch {
            val result = repository.deleteP2PAd(adId)
            result.onSuccess {
                showNotification("Advertisement deleted successfully.", NotificationType.SUCCESS)
            }.onFailure { err ->
                showNotification(err.message ?: "Could not delete ad", NotificationType.ERROR)
            }
        }
    }

    fun setUserAdminRole(isAdmin: Boolean) {
        viewModelScope.launch {
            val result = repository.setUserAdminRole(isAdmin)
            result.onSuccess {
                showNotification(
                    if (isAdmin) "Admin privileges unlocked." else "Switched to standard user view.",
                    if (isAdmin) NotificationType.SUCCESS else NotificationType.INFO
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Failed to update role", NotificationType.ERROR)
            }
        }
    }

    fun initiateEscrowOrder(ad: P2PAdEntity, amountReal: Double, paymentMethod: String, details: String, onOrderCreated: (EscrowOrderEntity) -> Unit) {
        viewModelScope.launch {
            val result = repository.initiateEscrowOrder(ad, amountReal, paymentMethod, details)
            result.onSuccess { order ->
                showNotification(
                    "Escrow order created successfully! Order #${order.orderId.take(6)} funds locked in Escrow.",
                    NotificationType.SUCCESS
                )
                onOrderCreated(order)
            }.onFailure { err ->
                showNotification(err.message ?: "Escrow order failed", NotificationType.ERROR)
            }
        }
    }

    fun markOrderPaid(orderId: String) {
        viewModelScope.launch {
            repository.markOrderPaid(orderId)
            showNotification("Payment confirmed successfully! Seller notified to release Escrow.", NotificationType.SUCCESS)
        }
    }

    fun releaseEscrow(orderId: String) {
        viewModelScope.launch {
            val result = repository.releaseEscrow(orderId)
            result.onSuccess {
                showNotification("Escrow released successfully! Coins transferred to buyer.", NotificationType.SUCCESS)
            }.onFailure { err ->
                showNotification(err.message ?: "Escrow release failed", NotificationType.ERROR)
            }
        }
    }

    fun disputeOrder(orderId: String, reason: String, evidenceUri: String? = null) {
        viewModelScope.launch {
            repository.disputeOrder(orderId, reason, evidenceUri)
            showNotification("Dispute opened successfully! Order #${orderId.take(6)} is under review.", NotificationType.WARNING)
        }
    }

    fun getDisputeMessages(orderId: String): Flow<List<ChatMessageEntity>> {
        return repository.getDisputeMessagesFlow(orderId)
    }

    fun sendDisputeMessage(orderId: String, receiverUsername: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendDisputeChatMessage(orderId, receiverUsername, text.trim())
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val target = _selectedChatUser.value
            repository.sendChatMessage(target, text.trim())
        }
    }

    fun createPost(content: String, mediaType: String, mediaUri: String? = null) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.createPost(content.trim(), mediaType, mediaUri)
            showNotification("Post shared successfully with the community!", NotificationType.SUCCESS)
        }
    }

    fun toggleLike(postId: String, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLikePost(postId, currentLiked)
        }
    }

    fun submitHelpTicket(category: String, subject: String, message: String, imageUri: String? = null) {
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.submitHelpTicket(category, subject, message, imageUri)
            showNotification("Help ticket submitted successfully! Support agent assigned.", NotificationType.SUCCESS)
        }
    }

    fun playSpinWheel(betAmount: Double, multiplier: Double, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            val result = repository.playSpinningWheel(betAmount, multiplier)
            result.onSuccess { win ->
                onResult(win)
                if (multiplier > 1.0) {
                    showNotification("You won ${String.format("%.2f", win)} REAL! (${multiplier}x)", NotificationType.SUCCESS)
                } else if (multiplier == 0.0) {
                    showNotification("Better luck next spin! Played $betAmount REAL.", NotificationType.INFO)
                } else {
                    showNotification("Returned ${String.format("%.2f", win)} REAL.", NotificationType.INFO)
                }
            }.onFailure { err ->
                showNotification(err.message ?: "Spin failed", NotificationType.ERROR)
            }
        }
    }

    fun executeSpin(isFree: Boolean, prizeWon: Double, onResult: (Result<Double>) -> Unit) {
        viewModelScope.launch {
            val result = repository.executeWheelSpin(isFree, prizeWon)
            result.onSuccess { won ->
                onResult(Result.success(won))
                if (isFree) {
                    showNotification("Daily reward claimed! Free spin won +${String.format("%.2f", won)} REAL!", NotificationType.SUCCESS)
                } else {
                    val net = won - 10.0
                    if (net > 0) {
                        showNotification("Lucky spin won! +${String.format("%.2f", won)} REAL! (Net: +${String.format("%.2f", net)} REAL)", NotificationType.SUCCESS)
                    } else if (net == 0.0) {
                        showNotification("Spin broke even! Won +${String.format("%.2f", won)} REAL.", NotificationType.INFO)
                    } else {
                        showNotification("Spin completed. Won +${String.format("%.2f", won)} REAL (Cost: 10 RC).", NotificationType.INFO)
                    }
                }
            }.onFailure { err ->
                onResult(Result.failure(err))
                showNotification(err.message ?: "Spin failed", NotificationType.ERROR)
            }
        }
    }

    fun resetFreeSpin() {
        viewModelScope.launch {
            repository.resetDailyFreeSpin()
            showNotification("Daily free spin refreshed!", NotificationType.INFO)
        }
    }

    fun claimSpinBonus(bonusAmount: Double, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            val result = repository.claimSpinBonus(bonusAmount)
            result.onSuccess { bonus ->
                onResult(bonus)
                showNotification("Bonus claimed successfully! +${String.format("%.2f", bonus)} REAL added!", NotificationType.SUCCESS)
            }.onFailure { err ->
                showNotification(err.message ?: "Bonus claim failed", NotificationType.ERROR)
            }
        }
    }

    fun submitKyc(docNum: String, frontUri: String?, backUri: String?, status: String = "PENDING") {
        viewModelScope.launch {
            repository.submitKyc(docNum, frontUri, backUri, status)
            if (status == "APPROVED") {
                showNotification("KYC verified successfully! All trading limits unlocked.", NotificationType.SUCCESS)
            } else {
                showNotification("KYC submitted successfully! Documents under review (1-24 hours).", NotificationType.INFO)
            }
        }
    }

    fun setKycStatus(status: String) {
        viewModelScope.launch {
            repository.updateKycStatus(status)
            showNotification("KYC status updated: $status", NotificationType.INFO)
        }
    }

    fun updateMarketRate(newUsdRate: Double) {
        viewModelScope.launch {
            repository.updateMarketRate(newUsdRate)
            showNotification("Market rate updated: 1 REAL = $newUsdRate USD", NotificationType.INFO)
        }
    }

    fun updateMarketPrice(newUsdRate: Double, newUsdToEtb: Double) {
        viewModelScope.launch {
            repository.updateMarketPrice(newUsdRate, newUsdToEtb)
            val etbPrice = newUsdRate * newUsdToEtb
            showNotification(
                "Market price updated: 1 REAL = $newUsdRate USD (${String.format(java.util.Locale.US, "%.2f", etbPrice)} ETB)",
                NotificationType.SUCCESS
            )
        }
    }

    fun approveWithdraw(requestId: String, txHash: String = "") {
        viewModelScope.launch {
            val result = repository.approveWithdrawRequest(requestId, txHash)
            result.onSuccess {
                showNotification(
                    "Withdrawal request approved and dispatched to BEP-20 address!",
                    NotificationType.SUCCESS,
                    title = "Withdrawal Dispatched",
                    category = "WITHDRAW"
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Failed to approve withdrawal", NotificationType.ERROR)
            }
        }
    }

    fun rejectWithdraw(requestId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.rejectWithdrawRequest(requestId, reason)
            result.onSuccess {
                showNotification(
                    "Withdrawal request rejected. Funds refunded to user balance.",
                    NotificationType.INFO,
                    title = "Withdrawal Refunded",
                    category = "WITHDRAW"
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Failed to reject withdrawal", NotificationType.ERROR)
            }
        }
    }

    fun adminResolveDispute(orderId: String, releaseToBuyer: Boolean, adminNote: String) {
        viewModelScope.launch {
            val result = repository.adminResolveDispute(orderId, releaseToBuyer, adminNote)
            result.onSuccess {
                val decision = if (releaseToBuyer) "Escrow released to buyer" else "Escrow refunded to seller"
                showNotification(
                    "Dispute #$orderId resolved: $decision.",
                    NotificationType.SUCCESS,
                    title = "Dispute Arbitrated",
                    category = "P2P"
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Dispute resolution failed", NotificationType.ERROR)
            }
        }
    }

    fun adminUpdateKyc(userId: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            val result = repository.adminUpdateKyc(userId, status, reason)
            result.onSuccess {
                showNotification(
                    "User KYC status updated to $status.",
                    NotificationType.SUCCESS,
                    title = "KYC Admin Update",
                    category = "KYC"
                )
            }.onFailure { err ->
                showNotification(err.message ?: "Failed to update KYC", NotificationType.ERROR)
            }
        }
    }
}

enum class NotificationType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}

data class AppNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val type: NotificationType = NotificationType.SUCCESS,
    val actionLabel: String? = null
)

data class NotificationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "SYSTEM", // "DEPOSIT", "WITHDRAW", "P2P", "KYC", "REWARD", "SECURITY"
    val isRead: Boolean = false
)
