package com.pawledger.app

import android.app.Application
import com.pawledger.app.di.AppContainer
import com.pawledger.app.work.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PawLedgerApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            container.initialize()
            WorkScheduler.scheduleAll(this@PawLedgerApp)
        }
    }
}
