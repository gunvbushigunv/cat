package com.pawledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pawledger.app.data.entity.Pet
import com.pawledger.app.data.entity.PetMood
import com.pawledger.app.data.entity.PetSpecies
import com.pawledger.app.data.entity.TravelRecord
import com.pawledger.app.domain.catalog.Catalog
import com.pawledger.app.domain.catalog.Dialogues
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.components.PetAvatar
import com.pawledger.app.ui.components.SpeechBubble
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.WarmText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun fmt(epoch: Long): String {
    val dt = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
    return DateTimeFormatter.ofPattern("MM-dd HH:mm").format(dt)
}

@Composable
fun PetScreen(vm: MainViewModel) {
    val pets by vm.pets.collectAsState(initial = emptyList())
    val gameState by vm.gameState.collectAsState(initial = null)
    val ownedSkins by vm.ownedSkins.collectAsState(initial = emptyList())
    val ownedFurniture by vm.ownedFurniture.collectAsState(initial = emptyList())
    val ongoingTravels by vm.ongoingTravels.collectAsState(initial = emptyList())
    val returnedTravels by vm.returnedTravels.collectAsState(initial = emptyList())
    val postcards by vm.postcards.collectAsState(initial = emptyList())

    var tab by remember { mutableStateOf(0) }
    var renamePet by remember { mutableStateOf<Pet?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("伙伴", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmText)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("积分 ${gameState?.points ?: 0} · 小鱼干 ${gameState?.fishSnacks ?: 0}",
                style = MaterialTheme.typography.labelLarge, color = WarmText, modifier = Modifier.weight(1f))
        }
        TabRow(selectedTabIndex = tab, containerColor = Cream) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("养成") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("旅行") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("商城") })
        }
        when (tab) {
            0 -> NurtureTab(vm, pets, ownedSkins, ownedFurniture, onRename = { renamePet = it })
            1 -> TravelTab(vm, pets, ongoingTravels, returnedTravels, postcards)
            2 -> ShopTab(vm, pets, ownedSkins, ownedFurniture)
        }
    }

    renamePet?.let { pet ->
        RenameDialog(
            current = pet.name,
            species = pet.species,
            onConfirm = { newName ->
                scope.launch {
                    vm.gameRepository.renamePet(pet.species, newName, charge = true)
                }
                renamePet = null
            },
            onDismiss = { renamePet = null }
        )
    }
}

@Composable
private fun NurtureTab(
    vm: MainViewModel,
    pets: List<Pet>,
    ownedSkins: List<String>,
    ownedFurniture: List<com.pawledger.app.data.entity.OwnedFurniture>,
    onRename: (Pet) -> Unit
) {
    val scope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        items(pets.filter { it.unlocked }, key = { it.id }) { pet ->
            PetCard(pet, ownedSkins, ownedFurniture, isCompanion = pet.isCompanion,
                onSetCompanion = { scope.launch { vm.gameRepository.setCompanion(pet.species) } },
                onRename = { onRename(pet) },
                onEquip = { skin -> scope.launch { vm.gameRepository.equipSkin(pet.species, skin) } },
                onToggleFurniture = { key -> scope.launch { vm.gameRepository.toggleFurniturePlaced(key) } }
            )
        }
    }
}

@Composable
private fun PetCard(
    pet: Pet,
    ownedSkins: List<String>,
    ownedFurniture: List<com.pawledger.app.data.entity.OwnedFurniture>,
    isCompanion: Boolean,
    onSetCompanion: () -> Unit,
    onRename: () -> Unit,
    onEquip: (String) -> Unit,
    onToggleFurniture: (String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PetAvatar(Catalog.skin(pet.currentSkin).emoji, size = 56)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("${pet.name}（${Catalog.skin(pet.currentSkin).name}）", fontWeight = FontWeight.Bold, color = WarmText)
                    Text("Lv.${pet.level} · ${moodText(pet.mood)}${if (isCompanion) " · 陪伴中" else ""}",
                        style = MaterialTheme.typography.labelMedium, color = WarmText)
                    LinearProgressIndicator(
                        progress = { (pet.exp % 100) / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
                TextButton(onClick = onRename) { Text("改名(5积分)") }
            }
            if (!isCompanion) {
                Button(onClick = onSetCompanion) { Text("设为陪伴") }
            }
            // 皮肤
            Text("皮肤", style = MaterialTheme.typography.labelLarge, color = WarmText)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Catalog.skinsOf(pet.species).filter { ownedSkins.contains(it.key) }.forEach { skin ->
                    FilterChip(
                        selected = pet.currentSkin == skin.key,
                        onClick = { onEquip(skin.key) },
                        label = { Text("${skin.emoji} ${skin.name}") }
                    )
                }
            }
            // 家具
            Text("小屋家具", style = MaterialTheme.typography.labelLarge, color = WarmText)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ownedFurniture.forEach { f ->
                    val info = Catalog.furniture(f.furnitureKey)
                    FilterChip(
                        selected = f.placed,
                        onClick = { onToggleFurniture(f.furnitureKey) },
                        label = { Text("${info.emoji} ${info.name}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelTab(
    vm: MainViewModel,
    pets: List<Pet>,
    ongoing: List<TravelRecord>,
    returned: List<TravelRecord>,
    postcards: List<com.pawledger.app.data.entity.Postcard>
) {
    val scope = rememberCoroutineScope()
    val now = System.currentTimeMillis()
    val ready = ongoing.filter { it.returnAt <= now }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("让谁出门旅行？（一次只带一位）", style = MaterialTheme.typography.bodyMedium, color = WarmText)
        }
        items(pets.filter { it.unlocked }, key = { it.id }) { pet ->
            Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    PetAvatar(Catalog.skin(pet.currentSkin).emoji, size = 44)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(pet.name, fontWeight = FontWeight.SemiBold, color = WarmText)
                        Text(if (pet.isTraveling) "旅行中…" else "在家", style = MaterialTheme.typography.labelSmall, color = WarmText)
                    }
                    if (!pet.isTraveling) {
                        Button(onClick = { scope.launch { vm.gameRepository.startTravel(pet.species) } }) { Text("出门") }
                    }
                }
            }
        }

        if (ready.isNotEmpty()) {
            item { Text("有伙伴回来啦！", fontWeight = FontWeight.Bold, color = SageGreen) }
            items(ready, key = { it.id }) { t ->
                Card(colors = CardDefaults.cardColors(containerColor = SageGreen.copy(alpha = 0.15f)), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val dest = Catalog.destination(t.destinationKey)
                        Text("${dest.emoji} ${dest.name} 回来了", color = WarmText, modifier = Modifier.weight(1f))
                        Button(onClick = { scope.launch { vm.gameRepository.collectTravel(t.id) } }) { Text("收下") }
                    }
                }
            }
        }

        if (returned.isNotEmpty()) {
            item { Text("旅行记录", fontWeight = FontWeight.Bold, color = WarmText) }
            items(returned.take(10), key = { it.id }) { t ->
                val dest = Catalog.destination(t.destinationKey)
                Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(10.dp)) {
                        Text("${dest.emoji} ${dest.name}", color = WarmText, modifier = Modifier.weight(1f))
                        Text("+${t.rewardPoints}积分 +${t.rewardFish}小鱼干", style = MaterialTheme.typography.labelSmall, color = WarmText)
                    }
                }
            }
        }

        if (postcards.isNotEmpty()) {
            item { Text("旅行相册（${postcards.size}）", fontWeight = FontWeight.Bold, color = WarmText) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    postcards.take(12).forEach { p ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PetAvatar(p.image, size = 40)
                            Text(p.title, style = MaterialTheme.typography.labelSmall, color = WarmText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenameDialog(
    current: String,
    species: PetSpecies,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(current) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("给伙伴改个名字（消耗 5 积分）", style = MaterialTheme.typography.titleMedium, color = WarmText)
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("名字") })
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = { onConfirm(name) }) { Text("确定") }
                }
            }
        }
    }
}

private fun moodText(mood: PetMood): String = when (mood) {
    PetMood.HAPPY -> "开心"
    PetMood.NORMAL -> "平静"
    PetMood.HUNGRY -> "饿肚子"
    PetMood.SAD -> "难过"
}
