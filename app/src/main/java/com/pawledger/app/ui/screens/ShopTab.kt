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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.pawledger.app.data.entity.OwnedFurniture
import com.pawledger.app.data.entity.Pet
import com.pawledger.app.data.entity.PetSpecies
import com.pawledger.app.domain.catalog.Catalog
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.components.SectionTitle
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.WarmText
import kotlinx.coroutines.launch

@Composable
fun ShopTab(
    vm: MainViewModel,
    pets: List<Pet>,
    ownedSkins: List<String>,
    ownedFurniture: List<OwnedFurniture>
) {
    val scope = rememberCoroutineScope()
    val gameState by vm.gameState.collectAsState(initial = null)
    val points = gameState?.points ?: 0
    var naming by remember { mutableStateOf<PetSpecies?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("积分余额：$points", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmText)
        }

        item { SectionTitle("伙伴本体") }
        items(Catalog.species) { info ->
            val unlocked = pets.any { it.species == info.species && it.unlocked }
            Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(info.emoji, style = MaterialTheme.typography.titleLarge)
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text(info.defaultName, fontWeight = FontWeight.Bold, color = WarmText)
                        Text(info.description, style = MaterialTheme.typography.bodySmall, color = WarmText)
                    }
                    if (unlocked) {
                        Text("已拥有", color = SageGreen)
                    } else {
                        Button(onClick = {
                            scope.launch {
                                if (vm.gameRepository.unlockSpecies(info.species)) naming = info.species
                            }
                        }, enabled = points >= info.unlockCost) { Text("解锁 ${info.unlockCost}") }
                    }
                }
            }
        }

        item { SectionTitle("皮肤") }
        items(Catalog.skins) { skin ->
            val owned = ownedSkins.contains(skin.key)
            Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(skin.emoji, style = MaterialTheme.typography.titleLarge)
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(skin.name, fontWeight = FontWeight.Bold, color = WarmText)
                            if (skin.isLimited) {
                                Text(" 限定", style = MaterialTheme.typography.labelSmall, color = com.pawledger.app.ui.theme.SoftRed)
                            }
                        }
                        Text(skin.description, style = MaterialTheme.typography.bodySmall, color = WarmText)
                    }
                    if (owned) {
                        Text("已拥有", color = SageGreen)
                    } else {
                        Button(onClick = { scope.launch { vm.gameRepository.buySkin(skin.key) } }, enabled = points >= skin.price) {
                            Text("${skin.price}")
                        }
                    }
                }
            }
        }

        item { SectionTitle("家具") }
        items(Catalog.furniture) { f ->
            val owned = ownedFurniture.any { it.furnitureKey == f.key }
            Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(f.emoji, style = MaterialTheme.typography.titleLarge)
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text(f.name, fontWeight = FontWeight.Bold, color = WarmText)
                        Text(f.description, style = MaterialTheme.typography.bodySmall, color = WarmText)
                    }
                    if (owned) {
                        Text("已拥有", color = SageGreen)
                    } else {
                        Button(onClick = { scope.launch { vm.gameRepository.buyFurniture(f.key) } }, enabled = points >= f.price) {
                            Text("${f.price}")
                        }
                    }
                }
            }
        }
    }

    naming?.let { sp ->
        NameDialog(
            defaultName = Catalog.speciesOf(sp).defaultName,
            onConfirm = { name ->
                scope.launch { vm.gameRepository.renamePet(sp, name, charge = false) }
                naming = null
            },
            onDismiss = { naming = null }
        )
    }
}

@Composable
private fun NameDialog(defaultName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(defaultName) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("给新伙伴取个名字吧", style = MaterialTheme.typography.titleMedium, color = WarmText)
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("名字") })
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = { onConfirm(name) }) { Text("确定") }
                }
            }
        }
    }
}
