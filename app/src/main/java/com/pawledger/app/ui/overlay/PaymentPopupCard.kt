package com.pawledger.app.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pawledger.app.data.entity.Category
import com.pawledger.app.domain.CategoryStat
import com.pawledger.app.domain.MonthStats
import com.pawledger.app.ui.components.CategoryDonut
import com.pawledger.app.ui.components.PetAvatar
import com.pawledger.app.ui.components.money
import com.pawledger.app.ui.theme.SageGreen
import com.pawledger.app.ui.theme.SoftRed
import com.pawledger.app.ui.theme.WarmText

/**
 * 悬浮支付弹窗卡片。
 */
@Composable
fun PaymentPopupCard(
    amount: Double,
    source: String,
    categories: List<Category>,
    stats: MonthStats?,
    categoryStats: List<CategoryStat>,
    companionEmoji: String?,
    onSave: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf<Long?>(null) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 伙伴气泡
            Row(verticalAlignment = Alignment.CenterVertically) {
                PetAvatar(companionEmoji ?: "🐱", size = 40)
                Text(
                    " 检测到一笔 $source 支付",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmText
                )
            }

            Text(
                money(amount),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = WarmText
            )

            stats?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("总预算 ${money(it.budget)}", style = MaterialTheme.typography.bodySmall, color = WarmText)
                        Text("本月已花 ${money(it.expense)}", style = MaterialTheme.typography.bodySmall, color = SoftRed)
                        Text("剩余 ${money(it.remaining)}", style = MaterialTheme.typography.bodySmall, color = SageGreen)
                    }
                    CategoryDonut(categoryStats)
                }
            }

            Text("选择分类", style = MaterialTheme.typography.labelLarge, color = WarmText)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.take(6).forEach { c ->
                    FilterChip(
                        selected = selected == c.id,
                        onClick = { selected = c.id },
                        label = { Text("${c.icon} ${c.name}") }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) { Text("稍后") }
                Button(onClick = { onSave(selected) }) { Text("保存记账") }
            }
        }
    }
}
