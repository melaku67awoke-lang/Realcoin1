package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.HelpTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HelpTicketDao {
    @Query("SELECT * FROM help_tickets ORDER BY timestamp DESC")
    fun getAllTicketsFlow(): Flow<List<HelpTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: HelpTicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tickets: List<HelpTicketEntity>)
}
