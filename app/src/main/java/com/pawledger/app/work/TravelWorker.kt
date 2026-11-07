package com.pawledger.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawledger.app.PawLedgerApp
import com.pawledger.app.service.Notifier
import kotlin.random.Random

/**
 * 旅行检查：伙伴归来提醒 + 自主触发旅行。
 */
class TravelWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PawLedgerApp).container

        val ready = container.gameRepository.getReadyTravels()
        if (ready.isNotEmpty()) {
            Notifier.post(applicationContext, 2300, "伙伴回来啦 ✈️", "去「伙伴」页收下旅行奖励吧～")
        }

        // 没有伙伴在旅行时，偶尔自主触发一次旅行
        if (container.gameRepository.getTravelingPet() == null && Random.nextInt(100) < 40) {
            val pets = container.gameRepository.getPets().filter { it.unlocked }
            if (pets.isNotEmpty()) {
                val pet = pets[Random.nextInt(pets.size)]
                container.gameRepository.startTravel(pet.species)
            }
        }
        return Result.success()
    }
}
