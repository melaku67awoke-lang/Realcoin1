package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.WithdrawRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawRequestDao {
    @Query("SELECT * FROM withdraw_requests ORDER BY timestamp DESC")
    fun getAllWithdrawRequestsFlow(): Flow<List<WithdrawRequestEntity>>

    @Query("SELECT * FROM withdraw_requests WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingWithdrawRequestsFlow(): Flow<List<WithdrawRequestEntity>>

    @Query("SELECT * FROM withdraw_requests WHERE id = :id LIMIT 1")
    suspend fun getWithdrawRequestById(id: String): WithdrawRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawRequest(request: WithdrawRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWithdrawRequests(requests: List<WithdrawRequestEntity>)

    @Update
    suspend fun updateWithdrawRequest(request: WithdrawRequestEntity)
}
