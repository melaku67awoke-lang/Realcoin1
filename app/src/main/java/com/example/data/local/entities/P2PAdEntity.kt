package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "p2p_ads")
data class P2PAdEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val userLevel: String = "Starter",
    val type: String, // "BUY" or "SELL"
    val priceEtb: Double,
    val minReal: Double,
    val maxReal: Double,
    val giftReal: Double = 0.0,
    val paymentMethods: String, // comma separated e.g. "CBE BIRR, Telebirr, Awash Bank"
    val completionRate: Int = 100,
    val completedOrders: Int = 12,
    val lastSeenText: String = "Online",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)
