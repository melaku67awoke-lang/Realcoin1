package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.EscrowOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EscrowOrderDao {
    @Query("SELECT * FROM escrow_orders ORDER BY createdAt DESC")
    fun getAllOrdersFlow(): Flow<List<EscrowOrderEntity>>

    @Query("SELECT * FROM escrow_orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatusFlow(status: String): Flow<List<EscrowOrderEntity>>

    @Query("SELECT * FROM escrow_orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): EscrowOrderEntity?

    @Query("SELECT * FROM escrow_orders WHERE adId = :adId")
    suspend fun getOrdersByAdId(adId: String): List<EscrowOrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: EscrowOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<EscrowOrderEntity>)

    @Update
    suspend fun updateOrder(order: EscrowOrderEntity)

    @Query("UPDATE escrow_orders SET status = :newStatus WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String)

    @Query("UPDATE escrow_orders SET status = 'DISPUTED', disputeReason = :reason, disputeEvidence = :evidence WHERE orderId = :orderId")
    suspend fun disputeOrder(orderId: String, reason: String, evidence: String?)
}
