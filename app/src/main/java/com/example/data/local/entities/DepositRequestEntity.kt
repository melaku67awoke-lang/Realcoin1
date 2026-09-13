package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deposit_requests")
data class DepositRequestEntity(
    @PrimaryKey val id: String = "dep_req_${System.currentTimeMillis()}",
    val userId: String = "hab_user_01",
    val username: String = "hab",
    val amountUsd: Double,
    val network: String = "BSC (BNB Smart Chain BEP20)",
    val depositAddress: String = "0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4",
    val baseRealCoin: Double,
    val bonusRealCoin: Double, // 10% bonus
    val totalRealCoin: Double, // base + bonus
    val screenshotUri: String? = null,
    val txHash: String? = null,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val adminNote: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val processedTimestamp: Long? = null
)
