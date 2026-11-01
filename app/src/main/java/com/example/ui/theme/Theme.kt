package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RichGoldAccent,
    onPrimary = DeepEmerald,
    primaryContainer = EmeraldGreen,
    onPrimaryContainer = WarmGoldContainer,
    secondary = RegalGold,
    onSecondary = Color.Black,
    secondaryContainer = DeepEmerald,
    onSecondaryContainer = WarmGoldContainer,
    background = Color(0xFF121513),
    surface = Color(0xFF1A1D1B),
    onBackground = Color(0xFFF1F1EF),
    onSurface = Color(0xFFF1F1EF),
    surfaceVariant = Color(0xFF242926),
    onSurfaceVariant = Color(0xFFC7CEC8),
    outline = Color(0xFF3F4741)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = PureWhite,
    primaryContainer = MintEmeraldContainer,
    onPrimaryContainer = OnMintEmeraldContainer,
    secondary = RegalGold,
    onSecondary = PureWhite,
    secondaryContainer = WarmGoldContainer,
    onSecondaryContainer = OnWarmGoldContainer,
    tertiary = SoftSand,
    background = OffWhiteBackground,
    surface = PureWhite,
    onBackground = CharcoalText,
    onSurface = CharcoalText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MediumGrayText,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
