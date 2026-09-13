package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "escrow_orders")
data class EscrowOrderEntity(
    @PrimaryKey val orderId: String,
    val adId: String,
    val buyerId: String,
    val buyerUsername: String,
    val sellerId: String,
    val sellerUsername: String,
    val type: String, // "BUY" or "SELL" from creator perspective
    val realAmount: Double,
    val giftRealAmount: Double = 0.0,
    val pricePerCoinEtb: Double,
    val totalEtb: Double,
    val paymentMethod: String,
    val paymentAccountDetails: String,
    val status: String, // "PENDING_PAYMENT", "PAID_WAITING_RELEASE", "COMPLETED", "DISPUTED", "CANCELLED"
    val disputeReason: String? = null,
    val disputeEvidence: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
