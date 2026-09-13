package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.MarketRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketRateDao {
    @Query("SELECT * FROM market_rates WHERE id = 1 LIMIT 1")
    fun getMarketRateFlow(): Flow<MarketRateEntity?>

    @Query("SELECT * FROM market_rates WHERE id = 1 LIMIT 1")
    suspend fun getMarketRate(): MarketRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMarketRate(rate: MarketRateEntity)

    @Query("UPDATE market_rates SET realCoinToUsd = :newRate, lastUpdated = :now WHERE id = 1")
    suspend fun updateRealCoinRate(newRate: Double, now: Long = System.currentTimeMillis())

    @Query("UPDATE market_rates SET realCoinToUsd = :newUsdRate, usdToEtb = :newUsdToEtb, lastUpdated = :now WHERE id = 1")
    suspend fun updateMarketPrice(newUsdRate: Double, newUsdToEtb: Double, now: Long = System.currentTimeMillis())
}
