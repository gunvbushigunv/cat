package com.pawledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import com.pawledger.app.data.entity.Account
import com.pawledger.app.data.entity.Category
import com.pawledger.app.data.entity.TransactionType
import com.pawledger.app.ui.components.colorOf
import com.pawledger.app.ui.theme.WarmText

/**
 * 手动记一笔（尽量短：金额 + 分类即可保存）。
 */
@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    accounts: List<Account>,
    defaultCategoryId: Long? = null,
    defaultAmount: String = "",
    defaultMerchant: String? = null,
    onSave: (TransactionType, Double, Long?, Long?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(defaultAmount) }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var categoryId by remember { mutableStateOf(defaultCategoryId) }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var note by remember { mutableStateOf(defaultMerchant ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("记一笔", style = MaterialTheme.typography.titleLarge, color = WarmText)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 类型
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val types = listOf(
                        TransactionType.EXPENSE to "支出",
                        TransactionType.INCOME to "收入",
                        TransactionType.REFUND to "退款",
                        TransactionType.TRANSFER to "转账",
                        TransactionType.REPAY to "还款",
                        TransactionType.SAVE to "存钱"
                    )
                    types.forEach { (t, label) ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(label) }
                        )
                    }
                }

                // 分类
                Text("分类", style = MaterialTheme.typography.labelLarge, color = WarmText)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.take(8).forEach { c ->
                        FilterChip(
                            selected = categoryId == c.id,
                            onClick = { categoryId = c.id },
                            label = { Text("${c.icon} ${c.name}") },
                            leadingIcon = null
                        )
                    }
                }

                // 账户
                Text("账户", style = MaterialTheme.typography.labelLarge, color = WarmText)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.take(4).forEach { a ->
                        FilterChip(
                            selected = accountId == a.id,
                            onClick = { accountId = a.id },
                            label = { Text(a.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onSave(type, amt, categoryId, accountId, note.ifBlank { null })
                            }
                        }
                    ) { Text("保存") }
                }
            }
        }
    }
}
