package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = RealGoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = RealGoldSecondary,
    onPrimaryContainer = Color.White,
    secondary = RealGoldLight,
    onSecondary = Color.Black,
    tertiary = CryptoGreen,
    onTertiary = Color.White,
    background = CryptoDarkBg,
    onBackground = TextPrimary,
    surface = CryptoCardBg,
    onSurface = TextPrimary,
    surfaceVariant = CryptoCardVariant,
    onSurfaceVariant = TextSecondary,
    outline = CryptoCardBorder,
    error = CryptoRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}

