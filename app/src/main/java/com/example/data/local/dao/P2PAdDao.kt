package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.P2PAdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface P2PAdDao {
    @Query("SELECT * FROM p2p_ads WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getAllActiveAdsFlow(): Flow<List<P2PAdEntity>>

    @Query("SELECT * FROM p2p_ads WHERE status = 'ACTIVE' AND type = :type ORDER BY createdAt DESC")
    fun getAdsByTypeFlow(type: String): Flow<List<P2PAdEntity>>

    @Query("SELECT * FROM p2p_ads WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAdsByUserFlow(userId: String): Flow<List<P2PAdEntity>>

    @Query("SELECT * FROM p2p_ads WHERE id = :id LIMIT 1")
    suspend fun getAdById(id: String): P2PAdEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: P2PAdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ads: List<P2PAdEntity>)

    @Query("DELETE FROM p2p_ads WHERE id = :id")
    suspend fun deleteAd(id: String)
}
