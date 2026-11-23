package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Level Up — Modern Premium Design Tokens
// ==========================================

// Core Brand Accents (Luminous Emerald & Mint)
val ForestGreen = Color(0xFF10B981)       // Emerald 500 - Radiant primary
val ForestGreenDark = Color(0xFF059669)   // Emerald 600 - Deeper accent
val ForestGreenLight = Color(0xFF34D399)  // Emerald 400 - Vivid glow
val StudyEmeraldGlow = Color(0xFF0D9488)  // Teal 600 - Secondary depth
val SageTintDark = Color(0xFF0F291E)      // Ambient green glow for dark mode
val SageTintLight = Color(0xFFDCFCE7)     // Soft mint wash for light mode

// Neutral Dark Surfaces (Midnight Obsidian & Rich Indigo Slate)
val DarkBackground = Color(0xFF0A0D14)       // Deep luxury midnight (never muddy gray)
val DarkSurface = Color(0xFF121622)          // Layer 1 card surface
val DarkSurfaceElevated = Color(0xFF181F2E)  // Layer 2 elevated dialogs, floating cards
val DarkSurfaceVariant = Color(0xFF222B3D)   // Interactive chip tracks & buttons
val DarkOutline = Color(0xFF232D3F)          // Refined 0.5dp/1dp hairline border
val DarkOutlineSubtle = Color(0xFF18202D)    // Subtlest divider
val DarkOnSurface = Color(0xFFF8FAFC)        // Slate 50 - Ultra-crisp primary text
val DarkOnSurfaceMuted = Color(0xFF94A3B8)   // Slate 400 - Readable secondary text
val DarkOnSurfaceSubtle = Color(0xFF64748B)  // Slate 500 - Auxiliary labels

// Neutral Light Surfaces (Crisp Apple Alabaster & Warm Paper)
val LightBackground = Color(0xFFF6F8FA)      // Clean warm alabaster canvas
val LightSurface = Color(0xFFFFFFFF)         // Pure white cards
val LightSurfaceElevated = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)  // Slate 100 soft chip background
val LightOutline = Color(0xFFE2E8F0)         // Slate 200 hairline border
val LightOutlineSubtle = Color(0xFFF1F5F9)
val LightOnSurface = Color(0xFF0F172A)       // Slate 900 primary text
val LightOnSurfaceMuted = Color(0xFF64748B)  // Slate 500 secondary text
val LightOnSurfaceSubtle = Color(0xFF94A3B8) // Slate 400 caption text

// Semantic Accents
val AmberStreak = Color(0xFFF59E0B)       // Amber 500 (Streak fire & priority goals)
val AmberStreakSubtle = Color(0xFF78350F)
val SkyFreeze = Color(0xFF0EA5E9)         // Sky 500 (Cool down & info)
val SkyFreezeSubtle = Color(0xFF0C4A6E)
val RoseError = Color(0xFFF43F5E)         // Rose 500 (Cancel & delete actions)
val RoseErrorSubtle = Color(0xFF881337)
val IndigoCode = Color(0xFF818CF8)        // Indigo 400
val PurpleDesign = Color(0xFFC084FC)      // Purple 400
val GoldSuperDay = Color(0xFFFBBF24)      // Gold 400

// Gradients
val EmeraldGradient = Brush.horizontalGradient(
    colors = listOf(ForestGreen, ForestGreenLight)
)
val AmberGradient = Brush.horizontalGradient(
    colors = listOf(AmberStreak, GoldSuperDay)
)

// Backwards-compatibility aliases
val Emerald950 = DarkBackground
val Emerald900 = DarkSurface
val Emerald800 = DarkSurfaceElevated
val Emerald700 = DarkSurfaceVariant
val Emerald600 = ForestGreenDark
val Emerald500 = ForestGreen
val Mint400 = ForestGreenLight
val Mint300 = Color(0xFF6EE7B7)
val Mint200 = Color(0xFFA7F3D0)
val Mint50 = Color(0xFFECFDF5)
val Gold400 = GoldSuperDay
val Amber500 = AmberStreak
val Crimson500 = RoseError
val Cyan400 = SkyFreeze
val Sapphire400 = SkyFreeze
