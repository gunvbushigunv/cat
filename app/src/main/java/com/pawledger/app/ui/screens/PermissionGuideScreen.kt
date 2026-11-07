package com.pawledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.pawledger.app.service.PermissionUtils
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.SoftRed
import com.pawledger.app.ui.theme.WarmText

/**
 * 首次启动权限引导：逐项检测，返回自动重检。
 */
@Composable
fun PermissionGuideScreen(onFinish: () -> Unit) {
    val context = LocalContext.current

    var notif by remember { mutableStateOf(false) }
    var overlay by remember { mutableStateOf(false) }
    var accessibility by remember { mutableStateOf(false) }
    var battery by remember { mutableStateOf(false) }

    fun refresh() {
        notif = PermissionUtils.isNotificationAccessEnabled(context)
        overlay = PermissionUtils.canDrawOverlays(context)
        accessibility = PermissionUtils.isAccessibilityEnabled(context)
        battery = PermissionUtils.isIgnoringBatteryOptimizations(context)
    }
    refresh()

    LifecycleResumeEffect(Unit) {
        refresh()
        onPauseOrDispose { }
    }

    val requiredDone = notif && overlay && battery

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🐾 欢迎来到萌宠记账", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = WarmText)
        Text(
            "为了自动识别支付宝 / 微信的支付、弹出悬浮记账窗，请先开启以下权限（返回后会自动重新检测）：",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmText
        )

        PermissionCard(
            title = "① 通知使用权",
            desc = "识别支付通知，自动帮你记一笔",
            granted = notif,
            required = true,
            onOpen = { PermissionUtils.openNotificationAccessSettings(context) }
        )
        PermissionCard(
            title = "② 悬浮窗权限",
            desc = "支付时在屏幕底部弹出记账窗（各品牌路径：设置 → 应用 → 特殊应用权限 → 在其他应用上层显示）",
            granted = overlay,
            required = true,
            onOpen = { PermissionUtils.openOverlaySettings(context) }
        )
        PermissionCard(
            title = "③ 无障碍服务（可选）",
            desc = "作为通知监听的补充，非必选，不开启也能正常使用",
            granted = accessibility,
            required = false,
            onOpen = { PermissionUtils.openAccessibilitySettings(context) }
        )
        PermissionCard(
            title = "④ 电池优化白名单",
            desc = "防止通知监听和悬浮窗被系统清理",
            granted = battery,
            required = true,
            onOpen = { PermissionUtils.openBatteryOptimizationSettings(context) }
        )

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onFinish,
            enabled = requiredDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (requiredDone) "开始使用" else "请先开启必选权限")
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    desc: String,
    granted: Boolean,
    required: Boolean,
    onOpen: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Cream),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = WarmText)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = WarmText)
            }
            Text(
                if (granted) "已开启" else if (required) "未开启" else "可选",
                color = if (granted) SageGreen else SoftRed,
                style = MaterialTheme.typography.labelMedium
            )
            TextButton(onClick = onOpen) { Text("去开启") }
        }
    }
}
