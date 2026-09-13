package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdraw_requests")
data class WithdrawRequestEntity(
    @PrimaryKey val id: String = "wth_req_${System.currentTimeMillis()}",
    val userId: String = "hab_user_01",
    val username: String = "hab",
    val amountReal: Double,
    val amountUsdt: Double,
    val destinationAddress: String,
    val network: String = "BSC (BNB Smart Chain BEP20)",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val adminTxHash: String? = null,
    val adminNote: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val processedTimestamp: Long? = null
)
