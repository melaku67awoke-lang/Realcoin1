package com.example.data.repository

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
import com.example.data.model.UserLevelTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class RealCoinRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val marketRateDao = database.marketRateDao()
    private val p2pAdDao = database.p2pAdDao()
    private val escrowOrderDao = database.escrowOrderDao()
    private val txDao = database.walletTransactionDao()
    private val chatDao = database.chatMessageDao()
    private val postDao = database.postDao()
    private val helpDao = database.helpTicketDao()
    private val depositDao = database.depositRequestDao()
    private val withdrawDao = database.withdrawRequestDao()

    val userFlow: Flow<UserEntity?> = userDao.getUserFlow()
    val marketRateFlow: Flow<MarketRateEntity?> = marketRateDao.getMarketRateFlow()
    val activeAdsFlow: Flow<List<P2PAdEntity>> = p2pAdDao.getAllActiveAdsFlow()
    val allOrdersFlow: Flow<List<EscrowOrderEntity>> = escrowOrderDao.getAllOrdersFlow()
    val transactionsFlow: Flow<List<WalletTransactionEntity>> = txDao.getAllTransactionsFlow()
    val allPostsFlow: Flow<List<PostEntity>> = postDao.getAllPostsFlow()
    val allTicketsFlow: Flow<List<HelpTicketEntity>> = helpDao.getAllTicketsFlow()
    val allDepositRequestsFlow: Flow<List<DepositRequestEntity>> = depositDao.getAllRequestsFlow()
    val pendingDepositRequestsFlow: Flow<List<DepositRequestEntity>> = depositDao.getPendingRequestsFlow()
    val allWithdrawRequestsFlow: Flow<List<WithdrawRequestEntity>> = withdrawDao.getAllWithdrawRequestsFlow()
    val pendingWithdrawRequestsFlow: Flow<List<WithdrawRequestEntity>> = withdrawDao.getPendingWithdrawRequestsFlow()

    fun getChatMessagesFlow(user: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForConversationFlow(user)

    fun getDisputeMessagesFlow(orderId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForOrderFlow(orderId)

    val allChatMessagesFlow: Flow<List<ChatMessageEntity>> =
        chatDao.getAllMessagesFlow()

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUser()
        if (currentUser == null) {
            userDao.insertOrUpdateUser(
                UserEntity(
                    userId = "usr_fikeru_01",
                    username = "Fikeru",
                    fullName = "Fikeru Kebede",
                    email = "fikerukebede16@gmail.com",
                    phone = "+251911000000",
                    country = "Ethiopia",
                    joinedDate = "04/08/2026",
                    realCoinBalance = 500.0,
                    holdBalance = 0.0,
                    usdtDepositTotal = 45.0,
                    level = "Starter",
                    kycStatus = "APPROVED",
                    kycDocumentNumber = "ETH-99428-ID",
                    lastDailyRewardTimestamp = 0L,
                    isCreator = true,
                    isAdmin = true
                )
            )
        } else if (currentUser.email == "melaku61awoke@gmail.com" || currentUser.username == "hab" || currentUser.userId == "hab_user_01") {
            userDao.insertOrUpdateUser(
                currentUser.copy(
                    username = "Fikeru",
                    fullName = "Fikeru Kebede",
                    email = "fikerukebede16@gmail.com",
                    phone = "+251911000000",
                    isAdmin = true
                )
            )
        }

        val rate = marketRateDao.getMarketRate()
        if (rate == null) {
            marketRateDao.setMarketRate(
                MarketRateEntity(
                    id = 1,
                    coinSymbol = "REAL",
                    realCoinToUsd = 0.028470,
                    usdToEtb = 186.0,
                    dailyGrowthPct = 0.4,
                    dailyRange = "0.3% - 9%",
                    monthlyRange = "52% - 165%",
                    yearlyRange = "725% - 2345%"
                )
            )
        } else if (rate.realCoinToUsd != 0.028470 || rate.usdToEtb != 186.0) {
            // Update to requested startup rate: 0.028470 x 186 = 5.29 ETB
            marketRateDao.updateMarketPrice(0.028470, 186.0)
        }

        // Seed P2P Ads matching platform verified liquidity desks (No fake personal names)
        p2pAdDao.insertAll(
            listOf(
                P2PAdEntity(
                    id = "ad_01",
                    userId = "usr_merchant_01",
                    username = "RealCoin Official Desk",
                    userLevel = "Starter",
                    type = "SELL",
                    priceEtb = 5.29,
                    minReal = 50.0,
                    maxReal = 300.0,
                    giftReal = 10.0,
                    paymentMethods = "CBE BIRR, Telebirr",
                    completionRate = 100,
                    completedOrders = 24,
                    lastSeenText = "Online"
                ),
                P2PAdEntity(
                    id = "ad_02",
                    userId = "usr_merchant_02",
                    username = "Telebirr P2P Desk",
                    userLevel = "Starter",
                    type = "SELL",
                    priceEtb = 5.29,
                    minReal = 50.0,
                    maxReal = 500.0,
                    giftReal = 10.0,
                    paymentMethods = "Telebirr, Awash Bank",
                    completionRate = 100,
                    completedOrders = 19,
                    lastSeenText = "Online"
                ),
                P2PAdEntity(
                    id = "ad_03",
                    userId = "usr_merchant_03",
                    username = "CBE Birr Liquidity",
                    userLevel = "Starter",
                    type = "BUY",
                    priceEtb = 5.29,
                    minReal = 70.0,
                    maxReal = 700.0,
                    giftReal = 5.0,
                    paymentMethods = "CBE, BOA",
                    completionRate = 100,
                    completedOrders = 31,
                    lastSeenText = "Online"
                ),
                P2PAdEntity(
                    id = "ad_04",
                    userId = "usr_merchant_04",
                    username = "Addis Merchant Hub",
                    userLevel = "Starter",
                    type = "SELL",
                    priceEtb = 5.40,
                    minReal = 100.0,
                    maxReal = 1500.0,
                    giftReal = 0.0,
                    paymentMethods = "CBE BIRR, Telebirr",
                    completionRate = 99,
                    completedOrders = 15,
                    lastSeenText = "Online"
                ),
                P2PAdEntity(
                    id = "ad_05",
                    userId = "usr_merchant_05",
                    username = "Fast Escrow Merchant",
                    userLevel = "Starter",
                    type = "BUY",
                    priceEtb = 5.35,
                    minReal = 50.0,
                    maxReal = 1000.0,
                    giftReal = 2.0,
                    paymentMethods = "CBE BIRR, Telebirr",
                    completionRate = 100,
                    completedOrders = 42,
                    lastSeenText = "Online"
                )
            )
        )

        // Seed Orders with clean names
        val effectiveUser = userDao.getUser()
        val myUserId = effectiveUser?.userId ?: "usr_fikeru_01"
        val myUsername = effectiveUser?.username ?: "Fikeru"

        escrowOrderDao.insertAll(
            listOf(
                EscrowOrderEntity(
                    orderId = "736dd973a812",
                    adId = "ad_01",
                    buyerId = myUserId,
                    buyerUsername = myUsername,
                    sellerId = "usr_merchant_01",
                    sellerUsername = "RealCoin Official Desk",
                    type = "BUY",
                    realAmount = 200.0,
                    giftRealAmount = 5.0,
                    pricePerCoinEtb = 5.29,
                    totalEtb = 1058.00,
                    paymentMethod = "Telebirr",
                    paymentAccountDetails = "Telebirr: 0911000000",
                    status = "COMPLETED",
                    completedAt = System.currentTimeMillis() - 86400000L
                ),
                EscrowOrderEntity(
                    orderId = "disp9821af34",
                    adId = "ad_02",
                    buyerId = myUserId,
                    buyerUsername = myUsername,
                    sellerId = "usr_merchant_02",
                    sellerUsername = "Telebirr P2P Desk",
                    type = "BUY",
                    realAmount = 250.0,
                    giftRealAmount = 10.0,
                    pricePerCoinEtb = 5.29,
                    totalEtb = 1322.50,
                    paymentMethod = "Telebirr",
                    paymentAccountDetails = "Telebirr: 0911000000",
                    status = "DISPUTED",
                    disputeReason = "Payment sent via Telebirr (Tx #TB98231), waiting for coin release verification.",
                    createdAt = System.currentTimeMillis() - 14400000L
                )
            )
        )

        // Seed initial transactions
        txDao.insertAll(
            listOf(
                WalletTransactionEntity(
                    txId = "tx_01",
                    type = "P2P_SELL",
                    title = "P2P Sale - 142.86 REAL + 10.00 Gift",
                    amountReal = 152.86,
                    bonusReal = 10.0,
                    amountEtb = 2000.0,
                    isPositive = false,
                    status = "Completed",
                    timestamp = System.currentTimeMillis() - 3600000L * 5
                ),
                WalletTransactionEntity(
                    txId = "tx_02",
                    type = "DAILY_REWARD",
                    title = "Daily Reward Bonus",
                    amountReal = 5.0,
                    bonusReal = 5.0,
                    isPositive = true,
                    status = "Completed",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
        )

        // Seed Community Posts & Shorts
        postDao.insertAll(
            listOf(
                PostEntity(
                    postId = "post_01",
                    authorUsername = "OfficialRealCoin",
                    authorLevel = "VIP",
                    contentText = "🔥 Benefits that live stream broadcasters receive during live streaming hours! Trade and earn Real Coins every hour.",
                    mediaType = "IMAGE",
                    mediaUrl = "",
                    likesCount = 142,
                    commentsCount = 28,
                    isLiked = true
                ),
                PostEntity(
                    postId = "post_02",
                    authorUsername = "RealCoin Academy",
                    authorLevel = "Gold",
                    contentText = "Fast P2P payment with Telebirr and CBE. Instant release with Real Coin Escrow safety guarantee! ⚡️",
                    mediaType = "VIDEO_SHORT",
                    mediaUrl = "",
                    durationSeconds = 24,
                    likesCount = 89,
                    commentsCount = 12,
                    isLiked = false
                ),
                PostEntity(
                    postId = "post_03",
                    authorUsername = "RealCoin Community",
                    authorLevel = "Silver",
                    contentText = "Just won 100 REAL on the lucky spinning wheel game! Amazing bonus rewards!",
                    mediaType = "IMAGE",
                    mediaUrl = "",
                    likesCount = 230,
                    commentsCount = 45,
                    isLiked = true
                )
            )
        )

        // Seed Chat Messages (Only official verified support greeting)
        chatDao.insertAll(
            listOf(
                ChatMessageEntity(
                    messageId = "support_welcome_01",
                    senderUsername = "RealCoin Official Support",
                    receiverUsername = "Fikeru",
                    text = "Welcome to Real Coin! Verified 24/7 support is available here. Contact us anytime for deposit approvals, P2P escrow assistance, or account inquiries.",
                    timestamp = System.currentTimeMillis() - 3600000L,
                    isFromMe = false,
                    senderRole = "SUPPORT"
                )
            )
        )

        // Ensure sample dispute order & messages exist for quick testing
        if (escrowOrderDao.getOrderById("disp9821af34") == null) {
            escrowOrderDao.insertOrder(
                EscrowOrderEntity(
                    orderId = "disp9821af34",
                    adId = "ad_02",
                    buyerId = myUserId,
                    buyerUsername = myUsername,
                    sellerId = "usr_merchant_02",
                    sellerUsername = "Telebirr P2P Desk",
                    type = "BUY",
                    realAmount = 250.0,
                    giftRealAmount = 10.0,
                    pricePerCoinEtb = 5.29,
                    totalEtb = 1322.50,
                    paymentMethod = "Telebirr",
                    paymentAccountDetails = "Telebirr: 0911000000",
                    status = "DISPUTED",
                    disputeReason = "Payment sent via Telebirr (Tx #TB98231), waiting for coin release verification.",
                    createdAt = System.currentTimeMillis() - 14400000L
                )
            )
            chatDao.insertAll(
                listOf(
                    ChatMessageEntity(
                        messageId = "disp_msg_01",
                        senderUsername = "Escrow Smart Contract",
                        receiverUsername = myUsername,
                        text = "🔒 ESCROW DISPUTE OPENED: Order #disp9821 is frozen by Escrow Smart Contract. 260.0 REAL is secured. Both buyer and seller can submit payment receipts and statements below.",
                        timestamp = System.currentTimeMillis() - 14000000L,
                        isFromMe = false,
                        orderId = "disp9821af34",
                        senderRole = "SYSTEM"
                    ),
                    ChatMessageEntity(
                        messageId = "disp_msg_02",
                        senderUsername = myUsername,
                        receiverUsername = "Telebirr P2P Desk",
                        text = "I sent 1,322.50 ETB to your Telebirr (0911000000) with reference TB98231. Please check your SMS and release the escrow.",
                        timestamp = System.currentTimeMillis() - 13000000L,
                        isFromMe = true,
                        orderId = "disp9821af34",
                        senderRole = "USER"
                    ),
                    ChatMessageEntity(
                        messageId = "disp_msg_03",
                        senderUsername = "Telebirr P2P Desk",
                        receiverUsername = myUsername,
                        text = "Checking my Telebirr SMS notifications now. Network had a delay in Addis Ababa.",
                        timestamp = System.currentTimeMillis() - 9000000L,
                        isFromMe = false,
                        orderId = "disp9821af34",
                        senderRole = "COUNTERPARTY"
                    ),
                    ChatMessageEntity(
                        messageId = "disp_msg_04",
                        senderUsername = "Escrow Arbitrator (Support)",
                        receiverUsername = myUsername,
                        text = "⚖️ Arbitrator Advisory: Reference TB98231 has been submitted. If seller does not confirm or dispute with a valid bank statement within 1 hour, escrow will auto-release to buyer.",
                        timestamp = System.currentTimeMillis() - 5000000L,
                        isFromMe = false,
                        orderId = "disp9821af34",
                        senderRole = "ARBITRATOR"
                    )
                )
            )
        }
        // Seed initial withdraw requests
        withdrawDao.insertAllWithdrawRequests(
            listOf(
                WithdrawRequestEntity(
                    id = "wth_req_01",
                    userId = myUserId,
                    username = myUsername,
                    amountReal = 1000.0,
                    amountUsdt = 28.47,
                    destinationAddress = "0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4",
                    network = "BSC (BNB Smart Chain BEP20)",
                    status = "PENDING",
                    timestamp = System.currentTimeMillis() - 7200000L
                ),
                WithdrawRequestEntity(
                    id = "wth_req_02",
                    userId = "usr_member_02",
                    username = "Verified Member",
                    amountReal = 1500.0,
                    amountUsdt = 42.71,
                    destinationAddress = "0x71c59b23b10b98f2441a679234b9d5c41efb1092",
                    network = "BSC (BNB Smart Chain BEP20)",
                    status = "APPROVED",
                    adminTxHash = "0x98f234120bb314e9124411899120ba019a1288c12fba41c9",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    processedTimestamp = System.currentTimeMillis() - 82800000L
                )
            )
        )
    }

    // --- DEPOSIT USDT WITH 10% BONUS & PROOF REVIEW ---
    suspend fun submitDepositRequest(
        usdtAmount: Double,
        screenshotUri: String?,
        txHash: String?
    ): Result<DepositRequestEntity> = withContext(Dispatchers.IO) {
        if (usdtAmount < 25.0) {
            return@withContext Result.failure(IllegalArgumentException("Minimum deposit is 25 USD"))
        }
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val rate = marketRateDao.getMarketRate() ?: MarketRateEntity()

        val baseRealCoin = usdtAmount / rate.realCoinToUsd
        val bonusRealCoin = baseRealCoin * 0.10 // 10% bonus
        val totalRealCoin = baseRealCoin + bonusRealCoin

        val reqId = "dep_req_${System.currentTimeMillis()}"
        val depositReq = DepositRequestEntity(
            id = reqId,
            userId = user.userId,
            username = user.username,
            amountUsd = usdtAmount,
            network = "BSC (BNB Smart Chain BEP20)",
            depositAddress = "0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4",
            baseRealCoin = baseRealCoin,
            bonusRealCoin = bonusRealCoin,
            totalRealCoin = totalRealCoin,
            screenshotUri = screenshotUri,
            txHash = txHash,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        depositDao.insertRequest(depositReq)

        // Insert pending transaction record
        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "tx_$reqId",
                type = "DEPOSIT_USDT",
                title = "USDT Deposit (Under Admin Review)",
                amountReal = totalRealCoin,
                bonusReal = bonusRealCoin,
                amountUsdt = usdtAmount,
                amountEtb = usdtAmount * rate.usdToEtb,
                isPositive = true,
                status = "Pending Review",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(depositReq)
    }

    suspend fun approveDepositRequest(
        requestId: String,
        adminNotes: String? = null
    ): Result<Double> = withContext(Dispatchers.IO) {
        val request = depositDao.getRequestById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Deposit request not found"))
        if (request.status != "PENDING") {
            return@withContext Result.failure(IllegalStateException("Request is already ${request.status}"))
        }

        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val rate = marketRateDao.getMarketRate() ?: MarketRateEntity()

        // 10% bonus is included in request.totalRealCoin
        val newRealBalance = user.realCoinBalance + request.totalRealCoin
        val newUsdtTotal = user.usdtDepositTotal + request.amountUsd
        val newLevel = calculateLevel(newUsdtTotal)

        val updatedUser = user.copy(
            realCoinBalance = newRealBalance,
            usdtDepositTotal = newUsdtTotal,
            level = newLevel
        )
        userDao.insertOrUpdateUser(updatedUser)

        val updatedRequest = request.copy(
            status = "APPROVED",
            adminNote = adminNotes ?: "Approved by Admin. Real Coin (+10% Bonus) credited to balance.",
            processedTimestamp = System.currentTimeMillis()
        )
        depositDao.updateRequest(updatedRequest)

        // Update or insert completed transaction
        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "tx_${request.id}",
                type = "DEPOSIT_USDT",
                title = "USDT Deposit (+10% Bonus Approved)",
                amountReal = request.totalRealCoin,
                bonusReal = request.bonusRealCoin,
                amountUsdt = request.amountUsd,
                amountEtb = request.amountUsd * rate.usdToEtb,
                isPositive = true,
                status = "Completed",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(request.totalRealCoin)
    }

    suspend fun rejectDepositRequest(
        requestId: String,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val request = depositDao.getRequestById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Deposit request not found"))
        if (request.status != "PENDING") {
            return@withContext Result.failure(IllegalStateException("Request is already ${request.status}"))
        }

        val updatedRequest = request.copy(
            status = "REJECTED",
            adminNote = reason,
            processedTimestamp = System.currentTimeMillis()
        )
        depositDao.updateRequest(updatedRequest)

        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "tx_${request.id}",
                type = "DEPOSIT_USDT",
                title = "Deposit Rejected: $reason",
                amountReal = request.totalRealCoin,
                bonusReal = 0.0,
                amountUsdt = request.amountUsd,
                amountEtb = 0.0,
                isPositive = false,
                status = "Rejected",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(Unit)
    }

    suspend fun manualAddRealCoinBalance(
        amount: Double,
        reason: String
    ): Result<Double> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val newBalance = user.realCoinBalance + amount
        val updatedUser = user.copy(realCoinBalance = newBalance)
        userDao.insertOrUpdateUser(updatedUser)

        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "admin_adj_${System.currentTimeMillis()}",
                type = "ADMIN_CREDIT",
                title = "Admin Balance Credit: $reason",
                amountReal = amount,
                bonusReal = 0.0,
                amountUsdt = 0.0,
                amountEtb = 0.0,
                isPositive = amount >= 0,
                status = "Completed",
                timestamp = System.currentTimeMillis()
            )
        )
        Result.success(newBalance)
    }

    // --- DIRECT DEPOSIT USDT (Instant fallback) WITH 10% BONUS ---
    suspend fun depositUsdt(usdtAmount: Double): Result<Double> = withContext(Dispatchers.IO) {
        if (usdtAmount < 25.0) {
            return@withContext Result.failure(IllegalArgumentException("Minimum deposit is 25 USDT"))
        }
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val rate = marketRateDao.getMarketRate() ?: MarketRateEntity()

        // 1 USDT buys: usdtAmount / rate.realCoinToUsd
        val baseRealCoin = usdtAmount / rate.realCoinToUsd
        val bonusRealCoin = baseRealCoin * 0.10 // 10% bonus
        val totalRealCoinReceived = baseRealCoin + bonusRealCoin

        val newRealBalance = user.realCoinBalance + totalRealCoinReceived
        val newUsdtTotal = user.usdtDepositTotal + usdtAmount
        val newLevel = calculateLevel(newUsdtTotal)

        val updatedUser = user.copy(
            realCoinBalance = newRealBalance,
            usdtDepositTotal = newUsdtTotal,
            level = newLevel
        )
        userDao.insertOrUpdateUser(updatedUser)

        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "dep_${System.currentTimeMillis()}",
                type = "DEPOSIT_USDT",
                title = "USDT Deposit + 10% Bonus",
                amountReal = totalRealCoinReceived,
                bonusReal = bonusRealCoin,
                amountUsdt = usdtAmount,
                amountEtb = usdtAmount * rate.usdToEtb,
                isPositive = true,
                status = "Completed"
            )
        )

        Result.success(totalRealCoinReceived)
    }

    // --- WITHDRAW REAL COIN TO USDT (BEP-20) ---
    suspend fun withdrawRealCoinToUsdt(realAmount: Double, usdtAddress: String): Result<Double> = withContext(Dispatchers.IO) {
        if (realAmount < 1000.0) {
            return@withContext Result.failure(IllegalArgumentException("Minimum withdrawal is 1000 Real Coin"))
        }
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        if (user.realCoinBalance < realAmount) {
            return@withContext Result.failure(IllegalArgumentException("Insufficient Real Coin balance"))
        }
        val rate = marketRateDao.getMarketRate() ?: MarketRateEntity()
        val usdtValue = realAmount * rate.realCoinToUsd

        val updatedUser = user.copy(
            realCoinBalance = user.realCoinBalance - realAmount
        )
        userDao.insertOrUpdateUser(updatedUser)

        val reqId = "wth_req_${System.currentTimeMillis()}"
        val withdrawReq = WithdrawRequestEntity(
            id = reqId,
            userId = user.userId,
            username = user.username,
            amountReal = realAmount,
            amountUsdt = usdtValue,
            destinationAddress = usdtAddress,
            network = "BSC (BNB Smart Chain BEP20)",
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        withdrawDao.insertWithdrawRequest(withdrawReq)

        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = reqId,
                type = "WITHDRAW_USDT",
                title = "Withdrawal to BEP-20 (${usdtAddress.take(8)}...)",
                amountReal = realAmount,
                bonusReal = 0.0,
                amountUsdt = usdtValue,
                amountEtb = usdtValue * rate.usdToEtb,
                isPositive = false,
                status = "Pending Approval"
            )
        )

        Result.success(usdtValue)
    }

    suspend fun approveWithdrawRequest(
        requestId: String,
        txHash: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val request = withdrawDao.getWithdrawRequestById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Withdrawal request not found"))
        if (request.status != "PENDING") {
            return@withContext Result.failure(IllegalStateException("Request is already ${request.status}"))
        }

        val finalTxHash = if (txHash.isNotBlank()) txHash.trim() else "0x" + UUID.randomUUID().toString().replace("-", "")
        val updatedRequest = request.copy(
            status = "APPROVED",
            adminTxHash = finalTxHash,
            processedTimestamp = System.currentTimeMillis()
        )
        withdrawDao.updateWithdrawRequest(updatedRequest)

        // Update wallet transaction record to completed
        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = request.id,
                type = "WITHDRAW_USDT",
                title = "Withdrawal to BEP-20 (${request.destinationAddress.take(8)}...)",
                amountReal = request.amountReal,
                bonusReal = 0.0,
                amountUsdt = request.amountUsdt,
                amountEtb = 0.0,
                isPositive = false,
                status = "Completed",
                timestamp = request.timestamp
            )
        )

        Result.success(Unit)
    }

    suspend fun rejectWithdrawRequest(
        requestId: String,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val request = withdrawDao.getWithdrawRequestById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Withdrawal request not found"))
        if (request.status != "PENDING") {
            return@withContext Result.failure(IllegalStateException("Request is already ${request.status}"))
        }

        // Refund user balance
        val user = userDao.getUser()
        if (user != null) {
            userDao.insertOrUpdateUser(
                user.copy(
                    realCoinBalance = user.realCoinBalance + request.amountReal
                )
            )
        }

        val updatedRequest = request.copy(
            status = "REJECTED",
            adminNote = reason.ifBlank { "Rejected by admin. Real Coin refunded to balance." },
            processedTimestamp = System.currentTimeMillis()
        )
        withdrawDao.updateWithdrawRequest(updatedRequest)

        // Update transaction status
        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = request.id,
                type = "WITHDRAW_USDT",
                title = "Withdrawal to BEP-20 (Refunded)",
                amountReal = request.amountReal,
                bonusReal = 0.0,
                amountUsdt = request.amountUsdt,
                amountEtb = 0.0,
                isPositive = false,
                status = "Rejected (Refunded)",
                timestamp = request.timestamp
            )
        )

        Result.success(Unit)
    }

    // --- DAILY REWARD CLAIM (Every 24h) ---
    suspend fun claimDailyReward(): Result<Double> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val now = System.currentTimeMillis()
        val cooldownMs = 24 * 60 * 60 * 1000L
        if (now - user.lastDailyRewardTimestamp < cooldownMs) {
            val remainingHours = ((cooldownMs - (now - user.lastDailyRewardTimestamp)) / (1000 * 3600)).coerceAtLeast(1)
            return@withContext Result.failure(IllegalStateException("Reward available in $remainingHours hours"))
        }

        val rewardAmount = UserLevelTier.getDailyRewardForDeposit(user.usdtDepositTotal)
        val currentTier = UserLevelTier.getTierForDeposit(user.usdtDepositTotal)
        val updatedUser = user.copy(
            realCoinBalance = user.realCoinBalance + rewardAmount,
            lastDailyRewardTimestamp = now,
            level = currentTier.levelName
        )
        userDao.insertOrUpdateUser(updatedUser)

        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "rwd_${System.currentTimeMillis()}",
                type = "DAILY_REWARD",
                title = "Daily Reward (+${String.format("%.0f", rewardAmount)} REAL - ${currentTier.levelName} Tier ${currentTier.multiplierDisplay})",
                amountReal = rewardAmount,
                bonusReal = rewardAmount,
                isPositive = true,
                status = "Completed"
            )
        )

        Result.success(rewardAmount)
    }

    // --- P2P CREATE AD ---
    suspend fun createP2PAd(
        type: String,
        priceEtb: Double,
        minReal: Double,
        maxReal: Double,
        giftReal: Double,
        paymentMethods: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val ad = P2PAdEntity(
            id = "ad_${UUID.randomUUID().toString().take(8)}",
            userId = user.userId,
            username = user.username,
            userLevel = user.level,
            type = type,
            priceEtb = priceEtb,
            minReal = minReal,
            maxReal = maxReal,
            giftReal = giftReal,
            paymentMethods = paymentMethods,
            completionRate = 100,
            completedOrders = 0,
            lastSeenText = "Online"
        )
        p2pAdDao.insertAd(ad)
        Result.success(Unit)
    }

    suspend fun deleteP2PAd(adId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val orders = escrowOrderDao.getOrdersByAdId(adId)
        val activeOrders = orders.filter {
            it.status == "PENDING_PAYMENT" || it.status == "PAID_WAITING_RELEASE" || it.status.equals("DISPUTED", ignoreCase = true)
        }
        if (activeOrders.isNotEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Cannot delete advertisement while ${activeOrders.size} active escrow order(s) are in progress. Please complete or resolve active orders first.")
            )
        }
        p2pAdDao.deleteAd(adId)
        Result.success(Unit)
    }

    // --- ESCROW ORDER WORKFLOW ---
    suspend fun initiateEscrowOrder(
        ad: P2PAdEntity,
        realAmount: Double,
        paymentMethod: String,
        accountDetails: String
    ): Result<EscrowOrderEntity> = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val totalEtb = realAmount * ad.priceEtb
        val isUserBuying = ad.type == "BUY" // creator of ad is buying, so currentUser is seller, OR ad is SELL so currentUser is buyer

        // If current user is selling, lock realAmount in Escrow holdBalance
        if (ad.type == "BUY") {
            // ad creator wants to buy, so currentUser is selling coins
            if (currentUser.realCoinBalance < realAmount) {
                return@withContext Result.failure(IllegalArgumentException("Insufficient Real Coin balance to lock in Escrow"))
            }
            userDao.updateBalances(
                userId = currentUser.userId,
                balance = currentUser.realCoinBalance - realAmount,
                hold = currentUser.holdBalance + realAmount
            )
        }

        val order = EscrowOrderEntity(
            orderId = UUID.randomUUID().toString().replace("-", "").take(12),
            adId = ad.id,
            buyerId = if (ad.type == "SELL") currentUser.userId else ad.userId,
            buyerUsername = if (ad.type == "SELL") currentUser.username else ad.username,
            sellerId = if (ad.type == "SELL") ad.userId else currentUser.userId,
            sellerUsername = if (ad.type == "SELL") ad.username else currentUser.username,
            type = if (ad.type == "SELL") "BUY" else "SELL",
            realAmount = realAmount,
            giftRealAmount = ad.giftReal,
            pricePerCoinEtb = ad.priceEtb,
            totalEtb = totalEtb,
            paymentMethod = paymentMethod,
            paymentAccountDetails = accountDetails,
            status = "PENDING_PAYMENT"
        )
        escrowOrderDao.insertOrder(order)
        Result.success(order)
    }

    suspend fun markOrderPaid(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        escrowOrderDao.updateOrderStatus(orderId, "PAID_WAITING_RELEASE")
        Result.success(Unit)
    }

    suspend fun releaseEscrow(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = escrowOrderDao.getOrderById(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))

        // Complete order
        escrowOrderDao.updateOrder(
            order.copy(
                status = "COMPLETED",
                completedAt = System.currentTimeMillis()
            )
        )

        // If current user was the buyer, credit the coins
        if (order.buyerId == user.userId) {
            val totalReceived = order.realAmount + order.giftRealAmount
            userDao.insertOrUpdateUser(
                user.copy(realCoinBalance = user.realCoinBalance + totalReceived)
            )
            txDao.insertTransaction(
                WalletTransactionEntity(
                    txId = "p2p_${System.currentTimeMillis()}",
                    type = "P2P_BUY",
                    title = "P2P Buy - ${order.realAmount} REAL (+${order.giftRealAmount} Gift)",
                    amountReal = totalReceived,
                    bonusReal = order.giftRealAmount,
                    amountEtb = order.totalEtb,
                    isPositive = true,
                    status = "Completed"
                )
            )
        } else if (order.sellerId == user.userId) {
            // Release from hold balance
            userDao.updateBalances(
                userId = user.userId,
                balance = user.realCoinBalance,
                hold = (user.holdBalance - order.realAmount).coerceAtLeast(0.0)
            )
        }

        Result.success(Unit)
    }

    suspend fun disputeOrder(orderId: String, reason: String, evidenceUri: String?): Result<Unit> = withContext(Dispatchers.IO) {
        escrowOrderDao.disputeOrder(orderId, reason, evidenceUri)
        val user = userDao.getUser()
        val username = user?.username ?: "hab"
        val order = escrowOrderDao.getOrderById(orderId)
        val otherUser = if (order?.buyerId == user?.userId) order?.sellerUsername ?: "Seller" else order?.buyerUsername ?: "Buyer"

        // Post system announcement and user dispute message in dispute chat
        chatDao.insertMessage(
            ChatMessageEntity(
                messageId = "disp_sys_${System.currentTimeMillis()}_1",
                senderUsername = "Escrow Smart Contract",
                receiverUsername = username,
                text = "🔒 ESCROW DISPUTE OPENED: Order #${orderId.take(8)} funds are locked in Escrow Contract. Both parties must communicate here and provide proof.",
                timestamp = System.currentTimeMillis(),
                isFromMe = false,
                orderId = orderId,
                senderRole = "SYSTEM"
            )
        )
        chatDao.insertMessage(
            ChatMessageEntity(
                messageId = "disp_user_${System.currentTimeMillis()}_2",
                senderUsername = username,
                receiverUsername = otherUser,
                text = "Dispute Reason filed: $reason",
                timestamp = System.currentTimeMillis() + 100,
                isFromMe = true,
                orderId = orderId,
                senderRole = "USER"
            )
        )
        Result.success(Unit)
    }

    suspend fun sendDisputeChatMessage(
        orderId: String,
        receiverUsername: String,
        text: String,
        senderRole: String = "USER"
    ): Result<ChatMessageEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val msgId = "disp_msg_${System.currentTimeMillis()}_${(100..999).random()}"
        val message = ChatMessageEntity(
            messageId = msgId,
            senderUsername = user.username,
            receiverUsername = receiverUsername,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            orderId = orderId,
            senderRole = senderRole
        )
        chatDao.insertMessage(message)

        // Generate automated interactive response for simulated dispute counterparty or arbitrator
        CoroutineScope(Dispatchers.IO).launch {
            delay(1300)
            val lower = text.lowercase()
            val (replyText, replyRole) = when {
                lower.contains("receipt") || lower.contains("telebirr") || lower.contains("cbe") || lower.contains("tx") || lower.contains("statement") -> {
                    Pair("Received payment reference details. Refreshing banking statement notifications now.", "COUNTERPARTY")
                }
                lower.contains("arbitrator") || lower.contains("support") || lower.contains("help") -> {
                    Pair("⚖️ Escrow Arbitrator: Support has acknowledged your ticket. The transaction logs are currently being audited with bank APIs.", "ARBITRATOR")
                }
                lower.contains("release") || lower.contains("coins") -> {
                    Pair("Verifying transaction balance. Will click 'Release Escrow' as soon as the credit appears in Telebirr.", "COUNTERPARTY")
                }
                else -> {
                    Pair("Message noted. Please attach the payment transaction reference so the Escrow Arbitrator can verify.", "COUNTERPARTY")
                }
            }
            val replySender = if (replyRole == "ARBITRATOR") "Escrow Arbitrator (Support)" else receiverUsername
            chatDao.insertMessage(
                ChatMessageEntity(
                    messageId = "disp_rep_${System.currentTimeMillis()}_${(100..999).random()}",
                    senderUsername = replySender,
                    receiverUsername = user.username,
                    text = replyText,
                    timestamp = System.currentTimeMillis(),
                    isFromMe = false,
                    orderId = orderId,
                    senderRole = replyRole
                )
            )
        }

        Result.success(message)
    }

    // --- CHAT SYSTEM ---
    suspend fun sendChatMessage(receiverUsername: String, text: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val message = ChatMessageEntity(
            messageId = "msg_${System.currentTimeMillis()}",
            senderUsername = user.username,
            receiverUsername = receiverUsername,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        chatDao.insertMessage(message)
        Result.success(Unit)
    }

    // --- COMMUNITY POSTS & SHORTS ---
    suspend fun createPost(contentText: String, mediaType: String, mediaUri: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val post = PostEntity(
            postId = "post_${UUID.randomUUID().toString().take(8)}",
            authorUsername = user.username,
            authorLevel = user.level,
            contentText = contentText,
            mediaType = mediaType,
            mediaUrl = mediaUri ?: "",
            durationSeconds = if (mediaType == "VIDEO_SHORT") 20 else 0,
            likesCount = 1,
            commentsCount = 0,
            isLiked = true
        )
        postDao.insertPost(post)
        Result.success(Unit)
    }

    suspend fun toggleLikePost(postId: String, currentLiked: Boolean) = withContext(Dispatchers.IO) {
        postDao.toggleLike(postId, !currentLiked)
    }

    // --- HELP CENTER ---
    suspend fun submitHelpTicket(category: String, subject: String, message: String, imageUri: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val ticket = HelpTicketEntity(
            ticketId = "tkt_${UUID.randomUUID().toString().take(8)}",
            category = category,
            subject = subject,
            message = message,
            imageUri = imageUri,
            status = "OPEN",
            adminReply = "Ticket received. Support agent is reviewing your inquiry.",
            timestamp = System.currentTimeMillis()
        )
        helpDao.insertTicket(ticket)
        Result.success(Unit)
    }

    // --- SPINNING WHEEL GAME (10 RC Per Spin, 1 Free Spin Per Day) ---
    val spinCost = 10.0
    val freeSpinCooldownMs = 24 * 60 * 60 * 1000L

    suspend fun executeWheelSpin(isFreeSpin: Boolean, prizeWon: Double): Result<Double> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val now = System.currentTimeMillis()

        if (isFreeSpin) {
            // Validate free spin eligibility (24h cooldown)
            if (user.lastFreeSpinTimestamp != 0L && (now - user.lastFreeSpinTimestamp < freeSpinCooldownMs)) {
                return@withContext Result.failure(IllegalStateException("Daily free spin already used. Each spin costs 10 Realcoin."))
            }
            val newBalance = user.realCoinBalance + prizeWon
            userDao.insertOrUpdateUser(user.copy(
                realCoinBalance = newBalance,
                lastFreeSpinTimestamp = now
            ))

            txDao.insertTransaction(
                WalletTransactionEntity(
                    txId = "free_spin_${System.currentTimeMillis()}",
                    type = "FREE_SPIN_BONUS",
                    title = "Daily Free Spin Reward (+$prizeWon RC)",
                    amountReal = prizeWon,
                    bonusReal = prizeWon,
                    isPositive = true,
                    status = "Completed"
                )
            )
            Result.success(prizeWon)
        } else {
            // Paid spin costs exactly 10 Realcoin
            val cost = 10.0
            if (user.realCoinBalance < cost) {
                return@withContext Result.failure(IllegalArgumentException("Insufficient balance. 1 spin costs 10 Realcoin."))
            }

            val newBalance = user.realCoinBalance - cost + prizeWon
            userDao.insertOrUpdateUser(user.copy(realCoinBalance = newBalance))

            val netProfit = prizeWon - cost
            txDao.insertTransaction(
                WalletTransactionEntity(
                    txId = "spin_paid_${System.currentTimeMillis()}",
                    type = if (netProfit >= 0) "SPIN_WIN" else "SPIN_COST",
                    title = "Wheel Spin (Cost: 10 RC, Won: +$prizeWon RC)",
                    amountReal = prizeWon,
                    bonusReal = if (netProfit > 0) netProfit else 0.0,
                    isPositive = netProfit >= 0,
                    status = "Completed"
                )
            )
            Result.success(prizeWon)
        }
    }

    suspend fun resetDailyFreeSpin(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        userDao.insertOrUpdateUser(user.copy(lastFreeSpinTimestamp = 0L))
        Result.success(Unit)
    }

    suspend fun playSpinningWheel(betAmount: Double, multiplier: Double): Result<Double> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        if (user.realCoinBalance < betAmount) {
            return@withContext Result.failure(IllegalArgumentException("Insufficient Real Coin balance for bet"))
        }

        val winAmount = betAmount * multiplier
        val netGain = winAmount - betAmount
        val newBalance = user.realCoinBalance + netGain

        userDao.insertOrUpdateUser(user.copy(realCoinBalance = newBalance))

        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "spin_${System.currentTimeMillis()}",
                type = if (multiplier > 1.0) "SPIN_WIN" else "SPIN_BET",
                title = if (multiplier > 1.0) "Spin Win (${multiplier}x)!" else "Spin Wheel Bet (${betAmount} RC)",
                amountReal = if (multiplier > 1.0) winAmount else betAmount,
                bonusReal = 0.0,
                isPositive = multiplier > 1.0,
                status = "Completed"
            )
        )

        Result.success(winAmount)
    }

    suspend fun claimSpinBonus(bonusAmount: Double, reason: String = "Lucky Wheel Bonus"): Result<Double> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        val newBalance = user.realCoinBalance + bonusAmount
        userDao.insertOrUpdateUser(user.copy(realCoinBalance = newBalance))

        txDao.insertTransaction(
            WalletTransactionEntity(
                txId = "spin_bonus_${System.currentTimeMillis()}",
                type = "SPIN_BONUS",
                title = "Lucky Wheel Bonus (+$bonusAmount REAL)",
                amountReal = bonusAmount,
                bonusReal = bonusAmount,
                isPositive = true,
                status = "Completed"
            )
        )
        Result.success(bonusAmount)
    }

    // --- KYC SUBMISSION & STATUS ---
    suspend fun submitKyc(docNum: String, frontUri: String?, backUri: String?, status: String = "PENDING"): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        userDao.updateKyc(
            userId = user.userId,
            status = status,
            front = frontUri,
            back = backUri,
            docNum = docNum
        )
        Result.success(Unit)
    }

    suspend fun updateKycStatus(newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        userDao.updateKyc(
            userId = user.userId,
            status = newStatus,
            front = user.kycIdFrontUri,
            back = user.kycIdBackUri,
            docNum = user.kycDocumentNumber
        )
        Result.success(Unit)
    }

    // --- ADMIN MARKET RATE UPDATE ---
    suspend fun updateMarketRate(newUsdRate: Double): Result<Unit> = withContext(Dispatchers.IO) {
        marketRateDao.updateRealCoinRate(newUsdRate)
        Result.success(Unit)
    }

    suspend fun updateMarketPrice(newUsdRate: Double, newUsdToEtb: Double): Result<Unit> = withContext(Dispatchers.IO) {
        marketRateDao.updateMarketPrice(newUsdRate, newUsdToEtb)
        Result.success(Unit)
    }

    suspend fun setUserAdminRole(isAdmin: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        userDao.insertOrUpdateUser(user.copy(isAdmin = isAdmin))
        Result.success(Unit)
    }

    // --- ADMIN DISPUTE RESOLUTION ---
    suspend fun adminResolveDispute(
        orderId: String,
        releaseToBuyer: Boolean,
        adminNote: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val order = escrowOrderDao.getOrderById(orderId)
            ?: return@withContext Result.failure(IllegalArgumentException("Order not found"))

        val currentUser = userDao.getUser()
        if (releaseToBuyer) {
            // Transfer escrow to buyer
            if (currentUser != null && currentUser.username == order.buyerUsername) {
                userDao.insertOrUpdateUser(
                    currentUser.copy(
                        realCoinBalance = currentUser.realCoinBalance + order.realAmount + order.giftRealAmount
                    )
                )
            }
            escrowOrderDao.updateOrder(
                order.copy(
                    status = "COMPLETED",
                    completedAt = System.currentTimeMillis()
                )
            )
            txDao.insertTransaction(
                WalletTransactionEntity(
                    txId = "disp_res_${System.currentTimeMillis()}",
                    type = "DISPUTE_RELEASE",
                    title = "Dispute Resolved: Escrow Released to Buyer (Order #${order.orderId.take(8)})",
                    amountReal = order.realAmount + order.giftRealAmount,
                    bonusReal = order.giftRealAmount,
                    amountEtb = order.totalEtb,
                    isPositive = true,
                    status = "Completed"
                )
            )
        } else {
            // Cancel and refund to seller
            if (currentUser != null && currentUser.username == order.sellerUsername) {
                userDao.insertOrUpdateUser(
                    currentUser.copy(
                        realCoinBalance = currentUser.realCoinBalance + order.realAmount,
                        holdBalance = (currentUser.holdBalance - order.realAmount).coerceAtLeast(0.0)
                    )
                )
            }
            escrowOrderDao.updateOrder(
                order.copy(
                    status = "CANCELLED",
                    completedAt = System.currentTimeMillis()
                )
            )
            txDao.insertTransaction(
                WalletTransactionEntity(
                    txId = "disp_cnl_${System.currentTimeMillis()}",
                    type = "DISPUTE_REFUND",
                    title = "Dispute Resolved: Escrow Returned to Seller (Order #${order.orderId.take(8)})",
                    amountReal = order.realAmount,
                    bonusReal = 0.0,
                    amountEtb = order.totalEtb,
                    isPositive = true,
                    status = "Cancelled/Refunded"
                )
            )
        }

        // Add admin message in dispute chat
        chatDao.insertMessage(
            ChatMessageEntity(
                messageId = "admin_disp_${System.currentTimeMillis()}",
                senderUsername = "Real Coin Escrow Administrator",
                receiverUsername = order.buyerUsername,
                text = "⚖️ Admin Final Resolution: " +
                        (if (releaseToBuyer) "Escrow released to buyer." else "Escrow refunded to seller.") +
                        " Reason: $adminNote",
                timestamp = System.currentTimeMillis(),
                isFromMe = false,
                orderId = order.orderId,
                senderRole = "ARBITRATOR"
            )
        )

        Result.success(Unit)
    }

    suspend fun adminUpdateKyc(
        userId: String,
        newStatus: String,
        note: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext Result.failure(IllegalStateException("User not found"))
        userDao.updateKyc(
            userId = user.userId,
            status = newStatus,
            front = user.kycIdFrontUri,
            back = user.kycIdBackUri,
            docNum = user.kycDocumentNumber
        )
        Result.success(Unit)
    }

    // --- LEVEL UPGRADE RULE ---
    // Starter: $50 (15 RC, 1.0x), Silver: $75 (30 RC, 2.0x), Gold: $100 (50 RC, 3.33x), Platinum: $125 (75 RC, 5.0x), Diamond: $150 (100 RC, 6.67x), VIP: $200 (150 RC, 10.0x)
    private fun calculateLevel(totalUsdtDeposits: Double): String {
        return UserLevelTier.getTierForDeposit(totalUsdtDeposits).levelName
    }
}
