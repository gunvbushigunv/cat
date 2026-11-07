package com.pawledger.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pawledger.app.ui.screens.HomeScreen
import com.pawledger.app.ui.screens.LedgerScreen
import com.pawledger.app.ui.screens.PermissionGuideScreen
import com.pawledger.app.ui.screens.PetScreen
import com.pawledger.app.ui.screens.ProfileScreen
import com.pawledger.app.ui.screens.ScheduleScreen
import kotlinx.coroutines.launch

/**
 * 应用根：权限引导 → 主界面；并处理首次进入的 300 积分弹窗。
 */
@Composable
fun AppRoot() {
    val vm = mainViewModel()
    val guideDone by vm.preferences.permissionGuideDone.collectAsState(initial = false)

    if (!guideDone) {
        val scope = rememberCoroutineScope()
        PermissionGuideScreen(
            onFinish = {
                scope.launch { vm.preferences.setPermissionGuideDone(true) }
            }
        )
    } else {
        MainScaffold(vm)
        WelcomePointsDialog(vm)
    }
}

@Composable
private fun WelcomePointsDialog(vm: MainViewModel) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (vm.gameRepository.grantInitialPoints()) {
            show = true
        }
    }
    if (show) {
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text("🎉 新手见面礼") },
            text = { Text("送你 300 积分！可以去「伙伴」页给猫猫、狗狗、小狐狸换新皮肤、添置家具啦～") },
            confirmButton = {
                TextButton(onClick = { show = false }) { Text("开心收下") }
            }
        )
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
private fun MainScaffold(vm: MainViewModel) {
    var selected by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("首页", Icons.Outlined.Home),
        TabItem("记账", Icons.Outlined.ReceiptLong),
        TabItem("伙伴", Icons.Outlined.Pets),
        TabItem("日程", Icons.Outlined.CalendarMonth),
        TabItem("我的", Icons.Outlined.Person)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selected) {
                0 -> HomeScreen(vm)
                1 -> LedgerScreen(vm)
                2 -> PetScreen(vm)
                3 -> ScheduleScreen(vm)
                4 -> ProfileScreen(vm)
            }
        }
    }
}
