package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val postId: String,
    val authorUsername: String,
    val authorLevel: String = "Starter",
    val contentText: String,
    val mediaType: String, // "IMAGE", "VIDEO_SHORT"
    val mediaUrl: String = "",
    val durationSeconds: Int = 15,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
