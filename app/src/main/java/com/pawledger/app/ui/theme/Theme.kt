package com.pawledger.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SageGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EFE1),
    onPrimaryContainer = Color(0xFF2F4A2C),
    secondary = WarmWood,
    onSecondary = Color.White,
    secondaryContainer = Cream,
    onSecondaryContainer = WarmText,
    tertiary = Apricot,
    onTertiary = Color.White,
    background = CreamBg,
    onBackground = WarmText,
    surface = Color.White,
    onSurface = WarmText,
    surfaceVariant = Color(0xFFF2ECE0),
    onSurfaceVariant = Color(0xFF7A6F5E),
    error = SoftRed,
    onError = Color.White
)

@Composable
fun PawLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 本应用为治愈系浅色风格，统一使用浅色主题
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
