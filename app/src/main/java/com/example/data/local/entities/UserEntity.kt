package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String = "usr_main_01",
    val username: String = "Fikeru",
    val fullName: String = "Fikeru Kebede",
    val email: String = "fikerukebede16@gmail.com",
    val phone: String = "+251911000000",
    val country: String = "Ethiopia",
    val joinedDate: String = "04/08/2026",
    val avatarUrl: String = "",
    val realCoinBalance: Double = 500.0,
    val holdBalance: Double = 0.0,
    val usdtDepositTotal: Double = 45.0,
    val level: String = "Starter",
    val kycStatus: String = "APPROVED", // NOT_SUBMITTED, PENDING, APPROVED, REJECTED
    val kycIdFrontUri: String? = null,
    val kycIdBackUri: String? = null,
    val kycDocumentNumber: String? = "ETH-99428-ID",
    val lastDailyRewardTimestamp: Long = 0L,
    val lastFreeSpinTimestamp: Long = 0L,
    val isCreator: Boolean = true,
    val isAdmin: Boolean = true
)
