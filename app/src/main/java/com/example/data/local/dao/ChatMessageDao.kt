package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE senderUsername = :user OR receiverUsername = :user ORDER BY timestamp ASC")
    fun getMessagesForConversationFlow(user: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getMessagesForOrderFlow(orderId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(msgs: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE senderUsername IN (:fakeNames) OR receiverUsername IN (:fakeNames)")
    suspend fun deleteMessagesWithUsers(fakeNames: List<String>)
}
