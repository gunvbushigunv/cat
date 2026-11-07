package com.pawledger.app.di

import android.content.Context
import com.pawledger.app.data.AppDatabase
import com.pawledger.app.data.PreferencesManager
import com.pawledger.app.domain.AppRepository
import com.pawledger.app.domain.GameRepository

/**
 * 手动依赖容器（避免引入 DI 框架，保持简单）。
 */
class AppContainer(context: Context) {

    val database: AppDatabase = AppDatabase.get(context)
    val preferences: PreferencesManager = PreferencesManager(context)
    val gameRepository: GameRepository = GameRepository(database, preferences)
    val appRepository: AppRepository = AppRepository(database, preferences, gameRepository, context.applicationContext)
    val seeder: Seeder = Seeder(database)

    suspend fun initialize() {
        seeder.seedCategories()
        seeder.seedAccounts()
        gameRepository.seedInitialData()
    }
}
