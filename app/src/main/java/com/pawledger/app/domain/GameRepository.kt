package com.pawledger.app.domain

import com.pawledger.app.data.AppDatabase
import com.pawledger.app.data.PreferencesManager
import com.pawledger.app.data.entity.CheckIn
import com.pawledger.app.data.entity.DiaryEntry
import com.pawledger.app.data.entity.GameState
import com.pawledger.app.data.entity.OwnedFurniture
import com.pawledger.app.data.entity.OwnedSkin
import com.pawledger.app.data.entity.Pet
import com.pawledger.app.data.entity.PetMood
import com.pawledger.app.data.entity.PetSpecies
import com.pawledger.app.data.entity.PointsLedger
import com.pawledger.app.data.entity.Postcard
import com.pawledger.app.data.entity.TravelRecord
import com.pawledger.app.domain.catalog.Catalog
import com.pawledger.app.domain.catalog.Dialogues
import com.pawledger.app.util.Dates
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * 伙伴养成 + 旅行 + 积分 / 商城 / 签到 游戏逻辑。
 */
class GameRepository(
    private val db: AppDatabase,
    private val prefs: PreferencesManager
) {

    companion object {
        const val BOOKKEEPING_POINTS = 2
        const val BOOKKEEPING_EXP = 10
        const val NOT_OVER_BUDGET_POINTS = 5
        const val CHECKIN_POINTS = 3
        const val DAILY_GOAL_POINTS = 5
        const val WEEKLY_GOAL_POINTS = 50
        const val MONTHLY_GOAL_POINTS = 100
        const val RENAME_COST = 5
        val STREAK_MILESTONES = mapOf(3 to 10, 7 to 30, 14 to 60, 30 to 150)
    }

    private val petDao = db.petDao()
    private val travelDao = db.travelDao()
    private val gameDao = db.gameDao()

    // ---- 观察流 ----
    fun observePets(): Flow<List<Pet>> = petDao.observePets()
    fun observeGameState(): Flow<GameState?> = petDao.observeGameState()
    fun observeOwnedSkins(): Flow<List<String>> = petDao.observeOwnedSkins()
    fun observeOwnedFurniture(): Flow<List<OwnedFurniture>> = petDao.observeOwnedFurniture()
    fun observeLedger(): Flow<List<PointsLedger>> = gameDao.observeLedger()
    fun observeOngoingTravels(): Flow<List<TravelRecord>> = travelDao.observeOngoingTravels()
    fun observeReturnedTravels(): Flow<List<TravelRecord>> = travelDao.observeReturnedTravels()
    fun observePostcards(species: PetSpecies): Flow<List<Postcard>> = travelDao.observePostcards(species)
    fun observeAllPostcards(): Flow<List<Postcard>> = travelDao.observeAllPostcards()
    fun observeDiary(species: PetSpecies): Flow<List<DiaryEntry>> = travelDao.observeDiary(species)

    suspend fun getPets(): List<Pet> = petDao.getAllPets()
    suspend fun getCompanion(): Pet? = petDao.getCompanion()
    suspend fun getGameStateValue(): GameState? = petDao.getGameState()

    // ---- 初始化 ----
    suspend fun seedInitialData() {
        if (petDao.getPet(PetSpecies.CAT) == null) {
            petDao.insertPet(
                Pet(
                    species = PetSpecies.CAT,
                    name = Catalog.speciesOf(PetSpecies.CAT).defaultName,
                    currentSkin = baseSkin(PetSpecies.CAT),
                    unlocked = true,
                    isCompanion = true
                )
            )
            petDao.insertOwnedSkin(OwnedSkin(baseSkin(PetSpecies.CAT)))
        }
        if (petDao.getGameState() == null) {
            petDao.upsertGameState(GameState(id = 1))
        }
    }

    private fun baseSkin(s: PetSpecies): String = s.name.lowercase() + "_base"

    /** 首次进入赠送 300 积分，返回是否本次发放。 */
    suspend fun grantInitialPoints(): Boolean {
        val gs = petDao.getGameState() ?: GameState(id = 1)
        if (!gs.initialGranted) {
            petDao.upsertGameState(gs.copy(points = gs.points + 300, initialGranted = true))
            logPoints(300, "新手见面礼")
            return true
        }
        return false
    }

    suspend fun addPoints(delta: Int, reason: String) {
        if (delta == 0) return
        val gs = petDao.getGameState() ?: GameState(id = 1)
        petDao.upsertGameState(gs.copy(points = gs.points + delta))
        logPoints(delta, reason)
    }

    suspend fun spendPoints(cost: Int, reason: String): Boolean {
        val gs = petDao.getGameState() ?: return false
        if (gs.points < cost) return false
        petDao.upsertGameState(gs.copy(points = gs.points - cost))
        logPoints(-cost, reason)
        return true
    }

    private suspend fun logPoints(delta: Int, reason: String) {
        gameDao.insertLedger(
            PointsLedger(delta = delta, reason = reason, timestamp = System.currentTimeMillis())
        )
    }

    // ---- 伙伴：解锁 / 命名 / 换肤 / 陪伴 ----
    suspend fun unlockSpecies(species: PetSpecies): Boolean {
        val info = Catalog.speciesOf(species)
        if (info.unlockCost <= 0) return false
        if (petDao.getPet(species) != null) return false
        if (!spendPoints(info.unlockCost, "解锁伙伴·${info.defaultName}")) return false
        petDao.insertPet(
            Pet(
                species = species,
                name = info.defaultName,
                currentSkin = baseSkin(species),
                unlocked = true
            )
        )
        petDao.insertOwnedSkin(OwnedSkin(baseSkin(species)))
        return true
    }

    /** 命名（首次免费）或改名（扣 5 积分）。 */
    suspend fun renamePet(species: PetSpecies, newName: String, charge: Boolean): Boolean {
        val pet = petDao.getPet(species) ?: return false
        val name = newName.trim().ifEmpty { return false }
        if (charge && !spendPoints(RENAME_COST, "改名·${name}")) return false
        petDao.updatePet(pet.copy(name = name))
        return true
    }

    suspend fun equipSkin(species: PetSpecies, skinKey: String): Boolean {
        val pet = petDao.getPet(species) ?: return false
        if (!petDao.getOwnedSkins().contains(skinKey)) return false
        petDao.updatePet(pet.copy(currentSkin = skinKey))
        return true
    }

    suspend fun setCompanion(species: PetSpecies): Boolean {
        val pet = petDao.getPet(species) ?: return false
        petDao.clearCompanion()
        petDao.updatePet(pet.copy(isCompanion = true))
        return true
    }

    suspend fun buySkin(skinKey: String): Boolean {
        val skin = Catalog.skin(skinKey)
        if (petDao.getOwnedSkins().contains(skinKey)) return false
        if (!spendPoints(skin.price, "兑换皮肤·${skin.name}")) return false
        petDao.insertOwnedSkin(OwnedSkin(skinKey))
        return true
    }

    suspend fun buyFurniture(key: String): Boolean {
        val f = Catalog.furniture(key)
        if (petDao.getOwnedFurniture().any { it.furnitureKey == key }) return false
        if (!spendPoints(f.price, "兑换家具·${f.name}")) return false
        petDao.upsertFurniture(OwnedFurniture(furnitureKey = key, placed = true))
        return true
    }

    suspend fun toggleFurniturePlaced(key: String) {
        val owned = petDao.getOwnedFurniture().firstOrNull { it.furnitureKey == key } ?: return
        petDao.upsertFurniture(owned.copy(placed = !owned.placed))
    }

    // ---- 经验 / 升级 / 心情 ----
    suspend fun addExp(species: PetSpecies, amount: Int) {
        val pet = petDao.getPet(species) ?: return
        var exp = pet.exp + amount
        var level = pet.level
        var threshold = level * 100
        while (exp >= threshold) {
            exp -= threshold
            level += 1
            threshold = level * 100
        }
        petDao.updatePet(pet.copy(level = level, exp = exp))
    }

    suspend fun improveMood(species: PetSpecies) {
        val pet = petDao.getPet(species) ?: return
        val next = when (pet.mood) {
            PetMood.SAD -> PetMood.HUNGRY
            PetMood.HUNGRY -> PetMood.NORMAL
            PetMood.NORMAL -> PetMood.HAPPY
            PetMood.HAPPY -> PetMood.HAPPY
        }
        petDao.updatePet(pet.copy(mood = next))
    }

    suspend fun dropMood(species: PetSpecies) {
        val pet = petDao.getPet(species) ?: return
        val next = when (pet.mood) {
            PetMood.HAPPY -> PetMood.NORMAL
            PetMood.NORMAL -> PetMood.HUNGRY
            PetMood.HUNGRY -> PetMood.SAD
            PetMood.SAD -> PetMood.SAD
        }
        petDao.updatePet(pet.copy(mood = next))
    }

    suspend fun setHungry(species: PetSpecies) {
        val pet = petDao.getPet(species) ?: return
        petDao.updatePet(pet.copy(mood = PetMood.HUNGRY))
    }

    // ---- 记账奖励钩子 ----
    /** 记账后调用：+积分、+经验、连续记账判定与里程碑奖励。 */
    suspend fun onBookkeepingRecorded() {
        addPoints(BOOKKEEPING_POINTS, "记账奖励")
        val companion = petDao.getCompanion()
        if (companion != null) {
            addExp(companion.species, BOOKKEEPING_EXP)
            improveMood(companion.species)
        }
        val gs = petDao.getGameState() ?: GameState(id = 1)
        val today = Dates.dayKey()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val newStreak = when (gs.lastBookkeepingDay) {
            today -> gs.bookkeepingStreak
            yesterday -> gs.bookkeepingStreak + 1
            else -> 1
        }
        petDao.upsertGameState(
            gs.copy(lastBookkeepingDay = today, bookkeepingStreak = newStreak)
        )
        STREAK_MILESTONES[newStreak]?.let { bonus ->
            addPoints(bonus, "连续记账 ${newStreak} 天")
        }
    }

    suspend fun getBookkeepingStreak(): Int =
        petDao.getGameState()?.bookkeepingStreak ?: 0

    /** 每日结算：不超预算 +积分；断记 3 天 → 饿肚子。 */
    suspend fun settleDaily() {
        val gs = petDao.getGameState() ?: GameState(id = 1)
        val today = Dates.dayKey()
        if (gs.lastSettleDay != today) {
            if (!isOverBudget()) {
                addPoints(NOT_OVER_BUDGET_POINTS, "不超预算·每日奖励")
            }
            petDao.upsertGameState(gs.copy(lastSettleDay = today))
        }
        // 断记 3 天 → 饿肚子
        val lastBook = gs.lastBookkeepingDay
        if (lastBook.isNotBlank()) {
            val daysSince = runCatching {
                java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.parse(lastBook), LocalDate.now()
                )
            }.getOrDefault(0L)
            if (daysSince >= 3) {
                val companion = petDao.getCompanion()
                if (companion != null && companion.mood != PetMood.HUNGRY) {
                    setHungry(companion.species)
                }
            }
        }
    }

    // ---- 每日签到 ----
    /** 每日签到，返回本次获得积分（0 表示今日已签到）。 */
    suspend fun checkInToday(): Int {
        val today = Dates.dayKey()
        if (gameDao.getCheckIn(today) != null) return 0
        val latest = gameDao.getLatestCheckIn()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val streak = if (latest?.date == yesterday) latest.streak + 1 else 1
        gameDao.insertCheckIn(CheckIn(date = today, streak = streak))
        var gained = CHECKIN_POINTS
        STREAK_MILESTONES[streak]?.let { gained += it }
        addPoints(gained, "签到·连续 $streak 天")
        return gained
    }

    suspend fun getCheckInStreak(): Int = gameDao.getLatestCheckIn()?.streak ?: 0
    suspend fun isCheckedInToday(): Boolean = gameDao.getCheckIn(Dates.dayKey()) != null

    // ---- 旅行 ----
    /** 尝试让某伙伴出门旅行。返回 null 表示本次不想出门。 */
    suspend fun startTravel(species: PetSpecies): TravelRecord? {
        val pet = petDao.getPet(species) ?: return null
        if (pet.isTraveling) return null
        if (petDao.getTravelingPet() != null) return null

        // 超支或中断记账时，可能不想出门
        val gs = petDao.getGameState() ?: GameState(id = 1)
        val badState = gs.bookkeepingStreak == 0 || isOverBudget()
        if (badState && Random.nextInt(100) < 30) return null

        val destination = pickDestination(species)
        val goodState = gs.bookkeepingStreak >= 3 && !isOverBudget()
        // 旅行时长 4–12 小时，好状态倾向于更久
        val hours = if (goodState) Random.nextInt(6, 13) else Random.nextInt(4, 10)
        val durationMs = hours * 3600_000L
        val record = TravelRecord(
            species = species,
            destinationKey = destination.key,
            startTime = System.currentTimeMillis(),
            returnAt = System.currentTimeMillis() + durationMs
        )
        val id = travelDao.insertTravel(record)
        petDao.updatePet(pet.copy(isTraveling = true))
        return travelDao.getTravel(id)
    }

    suspend fun getTravelingPet(): Pet? = petDao.getTravelingPet()

    suspend fun getReadyTravels(): List<TravelRecord> =
        travelDao.getOngoingTravels().filter { it.returnAt <= System.currentTimeMillis() }

    /** 收取旅行结果，返回该条记录（已结算）。 */
    suspend fun collectTravel(travelId: Long): TravelRecord? {
        val record = travelDao.getTravel(travelId) ?: return null
        if (record.endTime != null) return null
        val durationHours = ((System.currentTimeMillis() - record.startTime) / 3600_000L).coerceAtLeast(1)
        val points = Random.nextInt(8, 20) + durationHours.toInt() * 2
        val fish = Random.nextInt(1, 4) + (durationHours.toInt() / 4)
        addPoints(points, "旅行归来·${Catalog.destination(record.destinationKey).name}")
        val gs = petDao.getGameState() ?: GameState(id = 1)
        petDao.upsertGameState(gs.copy(fishSnacks = gs.fishSnacks + fish))
        addExp(record.species, Random.nextInt(15, 40))

        val finished = record.copy(
            endTime = System.currentTimeMillis(),
            rewardPoints = points,
            rewardFish = fish,
            note = Catalog.destination(record.destinationKey).name
        )
        travelDao.updateTravel(finished)

        // 收集明信片 + 旅行日记
        val dest = Catalog.destination(record.destinationKey)
        if (travelDao.hasPostcard(record.species, dest.key) == 0) {
            travelDao.insertPostcard(
                Postcard(
                    species = record.species,
                    destinationKey = dest.key,
                    title = dest.name,
                    image = dest.emoji,
                    collectedAt = System.currentTimeMillis()
                )
            )
        }
        travelDao.insertDiary(
            DiaryEntry(
                species = record.species,
                content = Dialogues.pick(Dialogues.travel),
                createdAt = System.currentTimeMillis()
            )
        )

        val pet = petDao.getPet(record.species)
        if (pet != null) petDao.updatePet(pet.copy(isTraveling = false))
        return finished
    }

    private suspend fun isOverBudget(): Boolean {
        val month = Dates.monthKey()
        val budget = db.ledgerDao().getBudget(month)?.total ?: return false
        val start = Dates.startOfMonth()
        val end = Dates.endOfMonth()
        val txs = db.ledgerDao().getAllTransactions()
        val expense = txs.filter {
            it.timestamp in start until end &&
                (it.type == com.pawledger.app.data.entity.TransactionType.EXPENSE)
        }.sumOf { it.amount }
        return expense > budget
    }

    private fun pickDestination(species: PetSpecies): Catalog.DestinationInfo {
        val common = Catalog.commonDestinations
        val exclusive = Catalog.exclusiveDestinations[species].orEmpty()
        return if (Random.nextInt(100) < 40 && exclusive.isNotEmpty()) {
            exclusive[Random.nextInt(exclusive.size)]
        } else {
            common[Random.nextInt(common.size)]
        }
    }

    // ---- 工具 ----
    private fun zone(): ZoneId = ZoneId.systemDefault()
    private fun date(epoch: Long): LocalDate = Instant.ofEpochMilli(epoch).atZone(zone()).toLocalDate()
}
