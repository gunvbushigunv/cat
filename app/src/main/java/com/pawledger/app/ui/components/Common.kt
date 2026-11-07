package com.pawledger.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawledger.app.domain.CategoryStat
import com.pawledger.app.ui.theme.Cream
import com.pawledger.app.ui.theme.CreamBg
import com.pawledger.app.ui.theme.WarmText

/** Long(0xAARRGGBB) → Color */
fun colorOf(long: Long): Color = Color(long)

fun money(v: Double): String = "¥%.2f".format(v)

/** 甜甜圈式占比图。 */
@Composable
fun SimpleDonut(
    progress: Float,
    color: Color,
    trackColor: Color = Color(0xFFE8E0D0),
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        if (text != null) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = WarmText)
        }
    }
}

/** 分类占比甜甜圈。 */
@Composable
fun CategoryDonut(stats: List<CategoryStat>, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val total = stats.sumOf { it.amount }.toFloat()
            val stroke = 16.dp.toPx()
            val inset = stroke / 2
            var start = -90f
            if (total <= 0f) {
                drawArc(
                    color = Color(0xFFE8E0D0), startAngle = -90f, sweepAngle = 360f,
                    useCenter = false, topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            } else {
                for (s in stats) {
                    val sweep = (s.amount.toFloat() / total) * 360f
                    drawArc(
                        color = colorOf(s.color), startAngle = start, sweepAngle = sweep,
                        useCenter = false, topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke)
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("本月支出", style = MaterialTheme.typography.labelSmall, color = WarmText)
            Text(money(stats.sumOf { it.amount }), fontWeight = FontWeight.Bold, color = WarmText)
        }
    }
}

/** 伙伴头像（emoji 圆形背景）。 */
@Composable
fun PetAvatar(emoji: String, size: Int = 48, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Cream),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size * 0.5f).sp)
    }
}

/** 伙伴文字泡。 */
@Composable
fun SpeechBubble(emoji: String, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        PetAvatar(emoji, size = 40)
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Cream)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = WarmText)
        }
    }
}

/** 信息小卡片。 */
@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = WarmText)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmText)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = WarmText,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
