package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.DepositRequestDao
import com.example.data.local.dao.EscrowOrderDao
import com.example.data.local.dao.HelpTicketDao
import com.example.data.local.dao.MarketRateDao
import com.example.data.local.dao.P2PAdDao
import com.example.data.local.dao.PostDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WalletTransactionDao
import com.example.data.local.dao.WithdrawRequestDao
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.DepositRequestEntity
import com.example.data.local.entities.EscrowOrderEntity
import com.example.data.local.entities.HelpTicketEntity
import com.example.data.local.entities.MarketRateEntity
import com.example.data.local.entities.P2PAdEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WalletTransactionEntity
import com.example.data.local.entities.WithdrawRequestEntity

@Database(
    entities = [
        UserEntity::class,
        MarketRateEntity::class,
        P2PAdEntity::class,
        EscrowOrderEntity::class,
        WalletTransactionEntity::class,
        ChatMessageEntity::class,
        PostEntity::class,
        HelpTicketEntity::class,
        DepositRequestEntity::class,
        WithdrawRequestEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun marketRateDao(): MarketRateDao
    abstract fun p2pAdDao(): P2PAdDao
    abstract fun escrowOrderDao(): EscrowOrderDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun postDao(): PostDao
    abstract fun helpTicketDao(): HelpTicketDao
    abstract fun depositRequestDao(): DepositRequestDao
    abstract fun withdrawRequestDao(): WithdrawRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "real_coin_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
