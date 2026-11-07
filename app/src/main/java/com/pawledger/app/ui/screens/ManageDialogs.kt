package com.pawledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import com.pawledger.app.data.entity.Budget
import com.pawledger.app.data.entity.RecurringBill
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.components.money
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.WarmText
import com.pawledger.app.util.Dates
import kotlinx.coroutines.launch

@Composable
fun BudgetDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("设置本月预算", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmText)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("预算金额") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = {
                        amount.toDoubleOrNull()?.let { a ->
                            scope.launch { vm.appRepository.upsertBudget(Budget(month = Dates.monthKey(), total = a)) }
                        }
                        onDismiss()
                    }) { Text("保存") }
                }
            }
        }
    }
}

@Composable
fun RecurringBillsDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val bills by vm.recurringBills.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("周期账单", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmText)
                bills.forEach { b ->
                    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(b.name, fontWeight = FontWeight.Bold, color = WarmText)
                                Text("${money(b.amount)} · 每月 ${b.dueDay} 日", style = MaterialTheme.typography.labelSmall, color = WarmText)
                            }
                            TextButton(onClick = { scope.launch { vm.appRepository.deleteRecurringBill(b) } }) { Text("删除") }
                        }
                    }
                }
                Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) { Text("添加周期账单") }
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        }
    }

    if (showAdd) {
        NewBillDialog(
            onSave = { name, amt, dueDay ->
                scope.launch {
                    vm.appRepository.addRecurringBill(
                        RecurringBill(name = name, amount = amt, dueDay = dueDay)
                    )
                }
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }
}

@Composable
private fun NewBillDialog(onSave: (String, Double, Int) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("1") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("添加周期账单", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmText)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称（如房租）") }, singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("金额") }, singleLine = true)
                OutlinedTextField(value = dueDay, onValueChange = { dueDay = it }, label = { Text("每月到期日（1-28）") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = {
                        val a = amount.toDoubleOrNull()
                        val d = dueDay.toIntOrNull()
                        if (a != null && a > 0 && d != null) {
                            onSave(name.ifBlank { "账单" }, a, d.coerceIn(1, 28))
                        }
                    }) { Text("保存") }
                }
            }
        }
    }
}
