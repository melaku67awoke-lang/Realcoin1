package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.DepositRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositRequestDao {
    @Query("SELECT * FROM deposit_requests ORDER BY timestamp DESC")
    fun getAllRequestsFlow(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingRequestsFlow(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): DepositRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: DepositRequestEntity)

    @Update
    suspend fun updateRequest(request: DepositRequestEntity)
}
