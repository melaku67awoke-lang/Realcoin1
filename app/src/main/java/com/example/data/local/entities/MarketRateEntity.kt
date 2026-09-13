package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_rates")
data class MarketRateEntity(
    @PrimaryKey val id: Int = 1,
    val coinSymbol: String = "REAL",
    val realCoinToUsd: Double = 0.028470,
    val usdToEtb: Double = 186.0,
    val dailyGrowthPct: Double = 0.4,
    val dailyRange: String = "0.3% - 9%",
    val monthlyRange: String = "52% - 165%",
    val yearlyRange: String = "725% - 2345%",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val realCoinToEtb: Double
        get() = realCoinToUsd * usdToEtb
}
