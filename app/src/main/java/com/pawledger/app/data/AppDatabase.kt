package com.pawledger.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pawledger.app.data.dao.GameDao
import com.pawledger.app.data.dao.GoalDao
import com.pawledger.app.data.dao.LedgerDao
import com.pawledger.app.data.dao.NoticeDao
import com.pawledger.app.data.dao.PetDao
import com.pawledger.app.data.dao.PlanDao
import com.pawledger.app.data.dao.TravelDao
import com.pawledger.app.data.entity.Account
import com.pawledger.app.data.entity.Budget
import com.pawledger.app.data.entity.Category
import com.pawledger.app.data.entity.CheckIn
import com.pawledger.app.data.entity.DiaryEntry
import com.pawledger.app.data.entity.GameState
import com.pawledger.app.data.entity.Goal
import com.pawledger.app.data.entity.Memo
import com.pawledger.app.data.entity.MerchantRule
import com.pawledger.app.data.entity.NotificationLog
import com.pawledger.app.data.entity.OwnedFurniture
import com.pawledger.app.data.entity.OwnedSkin
import com.pawledger.app.data.entity.Pet
import com.pawledger.app.data.entity.PointsLedger
import com.pawledger.app.data.entity.Postcard
import com.pawledger.app.data.entity.RecurringBill
import com.pawledger.app.data.entity.Schedule
import com.pawledger.app.data.entity.Transaction
import com.pawledger.app.data.entity.TravelRecord

@Database(
    entities = [
        Transaction::class, Category::class, Account::class, Budget::class, RecurringBill::class,
        Goal::class, Pet::class, OwnedSkin::class, OwnedFurniture::class, GameState::class,
        TravelRecord::class, Postcard::class, DiaryEntry::class,
        PointsLedger::class, CheckIn::class,
        NotificationLog::class, MerchantRule::class,
        Schedule::class, Memo::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ledgerDao(): LedgerDao
    abstract fun goalDao(): GoalDao
    abstract fun petDao(): PetDao
    abstract fun travelDao(): TravelDao
    abstract fun gameDao(): GameDao
    abstract fun noticeDao(): NoticeDao
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pawledger.db"
                ).build().also { INSTANCE = it }
            }
    }
}
