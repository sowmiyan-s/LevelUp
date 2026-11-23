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
    primary = Emerald500,
    onPrimary = Emerald950,
    primaryContainer = Emerald700,
    onPrimaryContainer = Mint200,
    secondary = Mint400,
    onSecondary = Emerald950,
    secondaryContainer = Emerald800,
    onSecondaryContainer = Mint200,
    tertiary = Gold400,
    onTertiary = Emerald950,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    error = Crimson500,
    onError = Color.White
)

private val LevelUpLightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Mint200,
    onPrimaryContainer = Emerald900,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = Emerald800,
    tertiary = Amber500,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    error = Crimson500,
    onError = Color.White
)

val LevelUpShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
