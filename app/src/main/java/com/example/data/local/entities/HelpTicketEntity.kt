package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "help_tickets")
data class HelpTicketEntity(
    @PrimaryKey val ticketId: String,
    val category: String, // "Deposit/Withdraw", "P2P & Escrow", "KYC", "Spin Game", "Other"
    val subject: String,
    val message: String,
    val imageUri: String? = null,
    val status: String = "OPEN", // "OPEN", "ANSWERED", "RESOLVED"
    val adminReply: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
