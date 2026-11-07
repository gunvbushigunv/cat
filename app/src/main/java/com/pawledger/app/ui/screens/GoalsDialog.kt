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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pawledger.app.data.entity.Goal
import com.pawledger.app.data.entity.GoalPeriod
import com.pawledger.app.domain.GoalProgress
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.components.money
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.WarmText
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * 存钱目标：日 / 周 / 月 / 长期 / 自定义。
 */
@Composable
fun GoalsDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val goals by vm.goals.collectAsState(initial = emptyList())
    var progress by remember { mutableStateOf<List<GoalProgress>>(emptyList()) }
    var showNew by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(goals.size) {
        progress = vm.appRepository.computeGoalProgress()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("存钱目标", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmText)
                if (progress.isEmpty()) {
                    Text("还没有目标，先定一个小目标吧～", color = WarmText)
                }
                progress.forEach { gp ->
                    GoalRow(gp)
                }
                Button(onClick = { showNew = true }, modifier = Modifier.fillMaxWidth()) { Text("新建目标") }
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        }
    }

    if (showNew) {
        NewGoalDialog(
            onSave = { name, amount, period, endDate ->
                scope.launch {
                    vm.appRepository.addGoal(
                        Goal(
                            name = name, targetAmount = amount, period = period,
                            startDate = System.currentTimeMillis(), endDate = endDate
                        )
                    )
                }
                showNew = false
            },
            onDismiss = { showNew = false }
        )
    }
}

@Composable
private fun GoalRow(gp: GoalProgress) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row {
                Text(gp.goal.name, fontWeight = FontWeight.Bold, color = WarmText, modifier = Modifier.weight(1f))
                Text(periodLabel(gp.goal.period), style = MaterialTheme.typography.labelSmall, color = WarmText)
            }
            Text("${money(gp.progress)} / ${money(gp.goal.targetAmount)}", style = MaterialTheme.typography.bodyMedium, color = WarmText)
            val ratio = if (gp.goal.targetAmount > 0) (gp.progress / gp.goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
            if (gp.goal.isCompleted) {
                Text("已达成 ✓", color = SageGreen, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun NewGoalDialog(
    onSave: (String, Double, GoalPeriod, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(GoalPeriod.MONTHLY) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(3).toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("新建目标", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmText)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("目标名称") }, singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("目标金额") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        GoalPeriod.DAILY to "日目标",
                        GoalPeriod.WEEKLY to "周目标",
                        GoalPeriod.MONTHLY to "月目标",
                        GoalPeriod.LONG_TERM to "长期",
                        GoalPeriod.CUSTOM to "自定义"
                    ).forEach { (p, l) ->
                        FilterChip(selected = period == p, onClick = { period = p }, label = { Text(l) })
                    }
                }
                if (period == GoalPeriod.CUSTOM) {
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("截止日期 yyyy-MM-dd") }, singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            val end = if (period == GoalPeriod.CUSTOM) {
                                runCatching { LocalDate.parse(endDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                            } else null
                            onSave(name.ifBlank { "存钱目标" }, amt, period, end)
                        }
                    }) { Text("保存") }
                }
            }
        }
    }
}

private fun periodLabel(p: GoalPeriod): String = when (p) {
    GoalPeriod.DAILY -> "日目标"
    GoalPeriod.WEEKLY -> "周目标"
    GoalPeriod.MONTHLY -> "月目标"
    GoalPeriod.LONG_TERM -> "长期目标"
    GoalPeriod.CUSTOM -> "自定义目标"
}
