package com.pawledger.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawledger.app.data.entity.Pet
import com.pawledger.app.data.entity.PetMood
import com.pawledger.app.data.entity.TransactionType
import com.pawledger.app.domain.catalog.Catalog
import com.pawledger.app.domain.catalog.Dialogues
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.components.CategoryDonut
import com.pawledger.app.ui.components.PetAvatar
import com.pawledger.app.ui.components.SectionTitle
import com.pawledger.app.ui.components.SpeechBubble
import com.pawledger.app.ui.components.StatCard
import com.pawledger.app.ui.components.money
import com.pawledger.app.ui.theme.Apricot
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.SoftRed
import com.pawledger.app.ui.theme.WarmText
import com.pawledger.app.util.Dates

@Composable
fun HomeScreen(vm: MainViewModel) {
    val monthTxs by vm.monthTransactions.collectAsState(initial = emptyList())
    val budget by vm.budget.collectAsState(initial = null)
    val pets by vm.pets.collectAsState(initial = emptyList())
    val categories by vm.categories.collectAsState(initial = emptyList())
    val gameState by vm.gameState.collectAsState(initial = null)

    val expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
        monthTxs.filter { it.type == TransactionType.REFUND }.sumOf { it.amount }
    val income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalBudget = budget?.total ?: 0.0
    val remaining = totalBudget - expense
    val days = Dates.remainingDaysInMonth()
    val safeDaily = if (days > 0) (remaining / days).coerceAtLeast(0.0) else 0.0

    val catStats = remember(monthTxs, categories) {
        val map = categories.associateBy { it.id }
        monthTxs.filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
            .groupBy { it.categoryId!! }
            .map { (cid, list) ->
                val c = map[cid]
                com.pawledger.app.domain.CategoryStat(
                    categoryId = cid,
                    name = c?.name ?: "未分类",
                    icon = c?.icon ?: "❓",
                    color = c?.color ?: 0xFFB0A896,
                    amount = list.sumOf { it.amount }
                )
            }.sortedByDescending { it.amount }
    }

    val companion = pets.firstOrNull { it.isCompanion }
    val companionEmoji = companion?.let { Catalog.skin(it.currentSkin).emoji } ?: "🐱"

    var bubble by remember { mutableStateOf(Dialogues.pick(Dialogues.encourage)) }
    var showGoals by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(8000)
            bubble = Dialogues.pick(Dialogues.encourage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 顶部问候 + 陪伴气泡
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("你好呀，铲屎官", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmText)
                Text(
                    "积分 ${gameState?.points ?: 0} · 小鱼干 ${gameState?.fishSnacks ?: 0}",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmText
                )
            }
            PetAvatar(companionEmoji, size = 52)
        }
        SpeechBubble(emoji = companionEmoji, text = bubble)

        // 预算总览
        Card(
            colors = CardDefaults.cardColors(containerColor = Cream),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBudget = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("总预算", style = MaterialTheme.typography.labelSmall, color = WarmText)
                    Text(money(totalBudget), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = WarmText)
                    Text("本月已花 ${money(expense)}", style = MaterialTheme.typography.bodyMedium, color = WarmText)
                    Text("剩余 ${money(remaining)}", style = MaterialTheme.typography.bodyMedium, color = if (remaining >= 0) SageGreen else SoftRed)
                }
                CategoryDonut(catStats)
            }
        }

        // 统计小卡
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("剩余天数", "$days 天", SageGreen, Modifier.weight(1f))
            StatCard("本月收入", money(income), Apricot, Modifier.weight(1f))
            StatCard("安全日预算", money(safeDaily), com.pawledger.app.ui.theme.SkyBlue, Modifier.weight(1f))
        }

        // 存钱目标入口
        androidx.compose.material3.Button(
            onClick = { showGoals = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("🎯 存钱目标") }

        // 小屋（所有已解锁伙伴）
        SectionTitle("我的小屋")
        Card(
            colors = CardDefaults.cardColors(containerColor = Cream),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    pets.filter { it.unlocked }.forEach { pet ->
                        PetInHouse(pet = pet, onTap = { bubble = randomPetDialogue(pet) })
                    }
                }
                if (pets.none { it.unlocked }) {
                    Text("还没有伙伴，去「伙伴」页看看吧～", style = MaterialTheme.typography.bodyMedium, color = WarmText)
                }
            }
        }

        // 旅行状态
        val traveling = pets.firstOrNull { it.isTraveling }
        if (traveling != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Cream),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("✈️", fontSize = 24.sp)
                    Text(
                        "  ${traveling.name} 正在旅行中，回来会带明信片哦～",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmText,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showGoals) {
        GoalsDialog(vm = vm, onDismiss = { showGoals = false })
    }
    if (showBudget) {
        BudgetDialog(vm = vm, onDismiss = { showBudget = false })
    }
}

@Composable
private fun PetInHouse(pet: Pet, onTap: () -> Unit) {
    var bounce by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (bounce) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "petScale"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                bounce = !bounce
                onTap()
            }
    ) {
        Box(Modifier.scale(scale)) {
            PetAvatar(Catalog.skin(pet.currentSkin).emoji, size = 56)
        }
        Text(pet.name, style = MaterialTheme.typography.labelSmall, color = WarmText)
        val moodTag = when (pet.mood) {
            PetMood.HAPPY -> "开心"
            PetMood.NORMAL -> "平静"
            PetMood.HUNGRY -> "饿肚子"
            PetMood.SAD -> "难过"
        }
        Text(
            if (pet.isTraveling) "旅行中" else moodTag,
            style = MaterialTheme.typography.labelSmall,
            color = WarmText
        )
    }
}

private fun randomPetDialogue(pet: Pet): String {
    return if (pet.isTraveling) {
        Dialogues.pick(Dialogues.travel)
    } else if (pet.mood == PetMood.HUNGRY || pet.mood == PetMood.SAD) {
        Dialogues.pick(Dialogues.hungry)
    } else {
        listOf(Dialogues.encourage, Dialogues.coquettish).random().let { Dialogues.pick(it) }
    }
}
