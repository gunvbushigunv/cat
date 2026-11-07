package com.pawledger.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawledger.app.PawLedgerApp
import com.pawledger.app.data.entity.Account
import com.pawledger.app.data.entity.Budget
import com.pawledger.app.data.entity.Category
import com.pawledger.app.data.entity.GameState
import com.pawledger.app.data.entity.Goal
import com.pawledger.app.data.entity.Memo
import com.pawledger.app.data.entity.NotificationLog
import com.pawledger.app.data.entity.OwnedFurniture
import com.pawledger.app.data.entity.Pet
import com.pawledger.app.data.entity.PointsLedger
import com.pawledger.app.data.entity.Postcard
import com.pawledger.app.data.entity.RecurringBill
import com.pawledger.app.data.entity.Schedule
import com.pawledger.app.data.entity.Transaction
import com.pawledger.app.data.entity.TravelRecord
import com.pawledger.app.di.AppContainer
import com.pawledger.app.domain.AppRepository
import com.pawledger.app.domain.GameRepository
import com.pawledger.app.data.PreferencesManager
import kotlinx.coroutines.flow.Flow

/**
 * 主 ViewModel：暴露数据流与业务对象。
 */
class MainViewModel(
    private val container: AppContainer
) : ViewModel() {

    val appRepository: AppRepository = container.appRepository
    val gameRepository: GameRepository = container.gameRepository
    val preferences: PreferencesManager = container.preferences

    val transactions: Flow<List<Transaction>> = appRepository.observeTransactions()
    val monthTransactions: Flow<List<Transaction>> = appRepository.observeMonthTransactions()
    val drafts: Flow<List<Transaction>> = appRepository.observeDrafts()
    val categories: Flow<List<Category>> = appRepository.observeCategories()
    val accounts: Flow<List<Account>> = appRepository.observeAccounts()
    val budget: Flow<Budget?> = appRepository.observeBudget()
    val goals: Flow<List<Goal>> = appRepository.observeGoals()
    val recurringBills: Flow<List<RecurringBill>> = appRepository.observeRecurringBills()
    val schedules: Flow<List<Schedule>> = appRepository.observeSchedules()
    val memos: Flow<List<Memo>> = appRepository.observeMemos()
    val notificationLogs: Flow<List<NotificationLog>> = appRepository.observeNotificationLogs()

    val pets: Flow<List<Pet>> = gameRepository.observePets()
    val gameState: Flow<GameState?> = gameRepository.observeGameState()
    val ownedSkins: Flow<List<String>> = gameRepository.observeOwnedSkins()
    val ownedFurniture: Flow<List<OwnedFurniture>> = gameRepository.observeOwnedFurniture()
    val pointsLedger: Flow<List<PointsLedger>> = gameRepository.observeLedger()
    val ongoingTravels: Flow<List<TravelRecord>> = gameRepository.observeOngoingTravels()
    val returnedTravels: Flow<List<TravelRecord>> = gameRepository.observeReturnedTravels()
    val postcards: Flow<List<Postcard>> = gameRepository.observeAllPostcards()
}

@Composable
fun mainViewModel(): MainViewModel {
    val app = LocalContext.current.applicationContext as PawLedgerApp
    return viewModel { MainViewModel(app.container) }
}
