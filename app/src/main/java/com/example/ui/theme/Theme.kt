package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LevelUpDarkColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = Mint200,
    secondary = ForestGreenLight,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Mint200,
    tertiary = AmberStreak,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutlineSubtle,
    error = RoseError,
    onError = Color.White
)

private val LevelUpLightColorScheme = lightColorScheme(
    primary = ForestGreenDark,
    onPrimary = Color.White,
    primaryContainer = Mint50,
    onPrimaryContainer = ForestGreenDark,
    secondary = ForestGreen,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = ForestGreenDark,
    tertiary = AmberStreak,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    outlineVariant = LightOutlineSubtle,
    error = RoseError,
    onError = Color.White
)

val LevelUpShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LevelUpTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LevelUpDarkColorScheme else LevelUpLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LevelUpTypography,
        shapes = LevelUpShapes,
        content = content
    )
}
