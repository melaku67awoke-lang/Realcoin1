package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val messageId: String,
    val senderUsername: String,
    val receiverUsername: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false,
    val orderId: String? = null,
    val senderRole: String = "USER" // "USER", "COUNTERPARTY", "SYSTEM", "ARBITRATOR"
)
