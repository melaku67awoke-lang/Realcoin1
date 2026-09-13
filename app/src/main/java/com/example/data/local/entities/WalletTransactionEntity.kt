package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val txId: String,
    val type: String, // "DEPOSIT_USDT", "WITHDRAW_USDT", "P2P_BUY", "P2P_SELL", "DAILY_REWARD", "SPIN_WIN", "SPIN_BET"
    val title: String,
    val amountReal: Double,
    val bonusReal: Double = 0.0,
    val amountUsdt: Double = 0.0,
    val amountEtb: Double = 0.0,
    val isPositive: Boolean = true,
    val status: String = "Completed",
    val timestamp: Long = System.currentTimeMillis()
)
