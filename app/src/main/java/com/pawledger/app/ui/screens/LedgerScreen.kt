package com.pawledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pawledger.app.data.entity.Transaction
import com.pawledger.app.data.entity.TransactionType
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.components.money
import com.pawledger.app.ui.theme.Apricot
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.SoftRed
import com.pawledger.app.ui.theme.WarmText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun fmtTime(epoch: Long): String {
    val dt = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
    return DateTimeFormatter.ofPattern("MM-dd HH:mm").format(dt)
}

@Composable
fun LedgerScreen(vm: MainViewModel) {
    val transactions by vm.transactions.collectAsState(initial = emptyList())
    val drafts by vm.drafts.collectAsState(initial = emptyList())
    val categories by vm.categories.collectAsState(initial = emptyList())
    val accounts by vm.accounts.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAdd by remember { mutableStateOf(false) }
    var draftToCategorize by remember { mutableStateOf<Transaction?>(null) }
    var query by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<TransactionType?>(null) }

    val visible = transactions.filter { t ->
        val matchQuery = query.isBlank() ||
            (t.merchant?.contains(query) == true) ||
            (t.note?.contains(query) == true) ||
            categories.firstOrNull { it.id == t.categoryId }?.name?.contains(query) == true
        val matchType = filterType == null || t.type == filterType
        matchQuery && matchType
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = SageGreen,
                contentColor = Color.White
            ) { Icon(Icons.Filled.Add, contentDescription = "记一笔") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("记账", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmText)

            // 待分类草稿
            if (drafts.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Apricot.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("有 ${drafts.size} 笔待分类", fontWeight = FontWeight.Bold, color = WarmText)
                        drafts.forEach { d ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "· ${money(d.amount)} ${d.merchant ?: ""}",
                                    modifier = Modifier.weight(1f),
                                    color = WarmText
                                )
                                TextButton(onClick = { draftToCategorize = d }) { Text("补分类") }
                            }
                        }
                    }
                }
            }

            // 搜索 + 筛选
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("搜索账单") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = filterType == null, onClick = { filterType = null }, label = { Text("全部") })
                FilterChip(selected = filterType == TransactionType.EXPENSE, onClick = { filterType = TransactionType.EXPENSE }, label = { Text("支出") })
                FilterChip(selected = filterType == TransactionType.INCOME, onClick = { filterType = TransactionType.INCOME }, label = { Text("收入") })
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(visible, key = { it.id }) { t ->
                    TransactionRow(t, categories.firstOrNull { it.id == t.categoryId }?.name, categories.firstOrNull { it.id == t.categoryId }?.icon)
                }
            }
        }
    }

    if (showAdd) {
        AddTransactionDialog(
            categories = categories,
            accounts = accounts,
            onSave = { type, amount, catId, accId, note ->
                scope.launch {
                    vm.appRepository.addTransaction(
                        Transaction(
                            type = type,
                            amount = amount,
                            categoryId = catId,
                            accountId = accId ?: 0L,
                            merchant = note,
                            note = note,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }

    draftToCategorize?.let { draft ->
        DraftCategoryDialog(
            categories = categories,
            onPick = { catId ->
                scope.launch {
                    vm.appRepository.categorizeDraft(draft.id, catId, draft.merchant)
                }
                draftToCategorize = null
            },
            onDismiss = { draftToCategorize = null }
        )
    }
}

@Composable
private fun TransactionRow(t: Transaction, categoryName: String?, icon: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Cream),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon ?: "💸", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(categoryName ?: t.merchant ?: "未分类", fontWeight = FontWeight.SemiBold, color = WarmText)
                Text(fmtTime(t.timestamp), style = MaterialTheme.typography.labelSmall, color = WarmText)
            }
            val amountText = when (t.type) {
                TransactionType.INCOME -> "+${money(t.amount)}"
                TransactionType.REFUND -> "退 ${money(t.amount)}"
                TransactionType.TRANSFER -> "转 ${money(t.amount)}"
                TransactionType.REPAY -> "还 ${money(t.amount)}"
                TransactionType.SAVE -> "存 ${money(t.amount)}"
                else -> "-${money(t.amount)}"
            }
            val color = when (t.type) {
                TransactionType.INCOME -> SageGreen
                TransactionType.REFUND -> SageGreen
                else -> SoftRed
            }
            Text(amountText, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DraftCategoryDialog(
    categories: List<com.pawledger.app.data.entity.Category>,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("选择分类", style = MaterialTheme.typography.titleMedium, color = WarmText)
                categories.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { c ->
                            FilterChip(selected = false, onClick = { onPick(c.id) }, label = { Text("${c.icon} ${c.name}") })
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    }
}
