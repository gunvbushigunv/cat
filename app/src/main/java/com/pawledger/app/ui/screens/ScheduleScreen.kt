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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.pawledger.app.data.entity.Memo
import com.pawledger.app.data.entity.Schedule
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.WarmText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(vm: MainViewModel) {
    val schedules by vm.schedules.collectAsState(initial = emptyList())
    val memos by vm.memos.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(0) }
    var showAddSchedule by remember { mutableStateOf(false) }
    var showAddMemo by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var pendingMemoToSchedule by remember { mutableStateOf<Memo?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (tab == 0) showAddSchedule = true else showAddMemo = true },
                containerColor = SageGreen
            ) { Icon(Icons.Filled.Add, contentDescription = "新增") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("日程", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmText)
            TabRow(selectedTabIndex = tab, containerColor = Cream) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("日程") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("备忘录") })
            }

            if (tab == 0) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(schedules, key = { it.id }) { s ->
                        Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(14.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = s.completed,
                                    onCheckedChange = { c ->
                                        scope.launch { vm.appRepository.updateSchedule(s.copy(completed = c)) }
                                    }
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(s.title, fontWeight = FontWeight.SemiBold, color = WarmText)
                                    Text("${fmtDateTime(s.datetime)} · ${repeatLabel(s.repeatRule)}", style = MaterialTheme.typography.labelSmall, color = WarmText)
                                }
                                TextButton(onClick = { scope.launch { vm.appRepository.deleteSchedule(s) } }) { Text("删除") }
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("搜索备忘") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(memos.filter { query.isBlank() || it.content.contains(query) }, key = { it.id }) { m ->
                        Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (m.pinned) Text("📌 ", color = WarmText)
                                    Text(m.content, color = WarmText, modifier = Modifier.weight(1f))
                                }
                                Row {
                                    TextButton(onClick = { scope.launch { vm.appRepository.updateMemo(m.copy(pinned = !m.pinned)) } }) {
                                        Text(if (m.pinned) "取消置顶" else "置顶")
                                    }
                                    TextButton(onClick = {
                                        showAddSchedule = true
                                        pendingMemoToSchedule = m
                                    }) { Text("转日程") }
                                    TextButton(onClick = { scope.launch { vm.appRepository.deleteMemo(m) } }) { Text("删除") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSchedule) {
        AddScheduleDialog(
            defaultTitle = pendingMemoToSchedule?.content ?: "",
            onSave = { title, datetime, repeat, remind ->
                scope.launch {
                    vm.appRepository.addSchedule(
                        Schedule(title = title, datetime = datetime, repeatRule = repeat, remindBefore = remind)
                    )
                }
                pendingMemoToSchedule = null
                showAddSchedule = false
            },
            onDismiss = { showAddSchedule = false; pendingMemoToSchedule = null }
        )
    }
    if (showAddMemo) {
        AddMemoDialog(
            onSave = { content ->
                scope.launch {
                    val now = System.currentTimeMillis()
                    vm.appRepository.addMemo(Memo(content = content, createdAt = now, updatedAt = now))
                }
                showAddMemo = false
            },
            onDismiss = { showAddMemo = false }
        )
    }
}

private fun repeatLabel(r: String): String = when (r) {
    "DAILY" -> "每天"
    "WEEKLY" -> "每周"
    "MONTHLY" -> "每月"
    else -> "不重复"
}

private fun fmtDateTime(epoch: Long): String {
    val dt = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
    return DateTimeFormatter.ofPattern("MM-dd HH:mm").format(dt)
}

@Composable
private fun AddScheduleDialog(
    defaultTitle: String,
    onSave: (String, Long, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(defaultTitle) }
    var date by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var time by remember { mutableStateOf("09:00") }
    var repeat by remember { mutableStateOf("NONE") }
    var remind by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("添加日程", style = MaterialTheme.typography.titleMedium, color = WarmText)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("标题") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("日期 yyyy-MM-dd") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("时间 HH:mm") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("NONE" to "不重复", "DAILY" to "每天", "WEEKLY" to "每周", "MONTHLY" to "每月").forEach { (v, l) ->
                        FilterChip(selected = repeat == v, onClick = { repeat = v }, label = { Text(l) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "准时", 10 to "提前10分", 60 to "提前1小时").forEach { (v, l) ->
                        FilterChip(selected = remind == v, onClick = { remind = v }, label = { Text(l) })
                    }
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = {
                        val dt = runCatching {
                            LocalDate.parse(date).atTime(LocalTime.parse(time))
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }.getOrDefault(System.currentTimeMillis() + 86_400_000L)
                        onSave(title.ifBlank { "日程" }, dt, repeat, remind)
                    }) { Text("保存") }
                }
            }
        }
    }
}

@Composable
private fun AddMemoDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var content by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("写备忘", style = MaterialTheme.typography.titleMedium, color = WarmText)
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("内容") }, minLines = 3)
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = { onSave(content) }) { Text("保存") }
                }
            }
        }
    }
}
