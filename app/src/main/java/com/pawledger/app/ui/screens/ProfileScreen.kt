package com.pawledger.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pawledger.app.service.PermissionUtils
import com.pawledger.app.ui.MainViewModel
import com.pawledger.app.ui.components.SectionTitle
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.SoftRed
import com.pawledger.app.ui.theme.WarmText
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val overspend by vm.preferences.reminderOverspend.collectAsState(initial = true)
    val goalReminder by vm.preferences.reminderGoal.collectAsState(initial = true)
    val billReminder by vm.preferences.reminderBill.collectAsState(initial = true)

    var checkInStreak by remember { mutableStateOf(0) }
    var checkedToday by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }
    var showRestore by remember { mutableStateOf(false) }
    var showBills by remember { mutableStateOf(false) }
    var exportPath by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        checkInStreak = vm.gameRepository.getCheckInStreak()
        checkedToday = vm.gameRepository.isCheckedInToday()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("我的", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmText)

        // 签到
        Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("每日签到", fontWeight = FontWeight.Bold, color = WarmText)
                Text("已连续签到 $checkInStreak 天", style = MaterialTheme.typography.bodyMedium, color = WarmText)
                Button(
                    onClick = {
                        scope.launch {
                            val gained = vm.gameRepository.checkInToday()
                            checkInStreak = vm.gameRepository.getCheckInStreak()
                            checkedToday = vm.gameRepository.isCheckedInToday()
                            if (gained > 0) Toast.makeText(context, "签到成功 +$gained 积分", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !checkedToday
                ) { Text(if (checkedToday) "今日已签到" else "签到 +3 积分") }
            }
        }

        // 提醒开关
        SectionTitle("轻提醒")
        Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(12.dp)) {
                SettingSwitch("超支提醒", overspend) { scope.launch { vm.preferences.setReminderOverspend(it) } }
                SettingSwitch("存钱目标达成提醒", goalReminder) { scope.launch { vm.preferences.setReminderGoal(it) } }
                SettingSwitch("周期账单提醒", billReminder) { scope.launch { vm.preferences.setReminderBill(it) } }
                Button(onClick = { showBills = true }, modifier = Modifier.fillMaxWidth()) { Text("管理周期账单") }
            }
        }

        // 数据导出备份
        SectionTitle("数据导出与备份")
        Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = {
                    scope.launch {
                        val csv = vm.appRepository.exportCsv()
                        val path = saveToFile(context, csv, "账本导出.csv")
                        exportPath = path
                        showExport = true
                    }
                }) { Text("导出 CSV") }
                Button(onClick = {
                    scope.launch {
                        val json = vm.appRepository.exportJson()
                        val path = saveToFile(context, json, "账本备份.json")
                        exportPath = path
                        showExport = true
                    }
                }) { Text("导出 JSON 备份") }
                Button(onClick = { showRestore = true }) { Text("从 JSON 恢复") }
            }
        }

        // 权限管理
        SectionTitle("权限管理")
        PermissionStatusCard(context)

        Text("萌宠记账 v1.0.0 · 本地优先，数据不上传", style = MaterialTheme.typography.labelSmall, color = WarmText)
    }

    if (showExport) {
        Dialog(onDismissRequest = { showExport = false }) {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("导出成功", style = MaterialTheme.typography.titleMedium, color = WarmText)
                    Text("文件已保存到：\n$exportPath", style = MaterialTheme.typography.bodySmall, color = WarmText)
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = {
                            exportPath?.let { copyToClipboard(context, it) }
                            Toast.makeText(context, "路径已复制", Toast.LENGTH_SHORT).show()
                        }) { Text("复制路径") }
                        TextButton(onClick = { showExport = false }) { Text("关闭") }
                    }
                }
            }
        }
    }

    if (showRestore) {
        RestoreDialog(
            onRestore = { json ->
                scope.launch {
                    val count = vm.appRepository.importJson(json)
                    Toast.makeText(context, "已导入 $count 条记录", Toast.LENGTH_SHORT).show()
                }
                showRestore = false
            },
            onDismiss = { showRestore = false }
        )
    }

    if (showBills) {
        RecurringBillsDialog(vm = vm, onDismiss = { showBills = false })
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = WarmText, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun PermissionStatusCard(context: Context) {
    Card(colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PermissionRow("通知使用权", PermissionUtils.isNotificationAccessEnabled(context)) {
                PermissionUtils.openNotificationAccessSettings(context)
            }
            PermissionRow("悬浮窗", PermissionUtils.canDrawOverlays(context)) {
                PermissionUtils.openOverlaySettings(context)
            }
            PermissionRow("无障碍（可选）", PermissionUtils.isAccessibilityEnabled(context)) {
                PermissionUtils.openAccessibilitySettings(context)
            }
            PermissionRow("电池优化白名单", PermissionUtils.isIgnoringBatteryOptimizations(context)) {
                PermissionUtils.openBatteryOptimizationSettings(context)
            }
        }
    }
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = WarmText, modifier = Modifier.weight(1f))
        Text(
            if (granted) "已开启" else "未开启",
            color = if (granted) SageGreen else SoftRed,
            style = MaterialTheme.typography.labelMedium
        )
        TextButton(onClick = onClick) { Text("去开启") }
    }
}

@Composable
private fun RestoreDialog(onRestore: (String) -> Unit, onDismiss: () -> Unit) {
    var json by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("从 JSON 恢复", style = MaterialTheme.typography.titleMedium, color = WarmText)
                OutlinedTextField(value = json, onValueChange = { json = it }, label = { Text("粘贴备份 JSON") }, minLines = 4)
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = { onRestore(json) }) { Text("恢复") }
                }
            }
        }
    }
}

private suspend fun saveToFile(context: Context, content: String, fileName: String): String {
    val dir = context.getExternalFilesDir("exports") ?: context.filesDir
    dir.mkdirs()
    val file = File(dir, fileName)
    file.writeText(content)
    return file.absolutePath
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("path", text))
}
