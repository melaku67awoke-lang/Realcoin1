package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserFlow(userId: String = "hab_user_01"): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUser(userId: String = "hab_user_01"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET realCoinBalance = :balance, holdBalance = :hold WHERE userId = :userId")
    suspend fun updateBalances(userId: String, balance: Double, hold: Double)

    @Query("UPDATE users SET kycStatus = :status, kycIdFrontUri = :front, kycIdBackUri = :back, kycDocumentNumber = :docNum WHERE userId = :userId")
    suspend fun updateKyc(userId: String, status: String, front: String?, back: String?, docNum: String?)

    @Query("UPDATE users SET lastDailyRewardTimestamp = :timestamp, realCoinBalance = realCoinBalance + :rewardAmount WHERE userId = :userId")
    suspend fun claimDailyReward(userId: String, timestamp: Long, rewardAmount: Double)

    @Query("UPDATE users SET username = :username, fullName = :fullName, email = :email, phone = :phone WHERE userId = :userId")
    suspend fun updateProfile(userId: String, username: String, fullName: String, email: String, phone: String)
}
