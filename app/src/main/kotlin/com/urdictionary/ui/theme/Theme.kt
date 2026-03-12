package com.urdictionary.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.compose.material3.Typography

// ── Astrology palette ─────────────────────────────────────────────────────────
val UrBlue        = Color(0xFF1565C0)   // Primary: deep Uranian blue
val UrBlueDark    = Color(0xFF0D47A1)
val UrBlueLight   = Color(0xFF42A5F5)
val UrNavy        = Color(0xFF0A1628)   // Background
val UrNavySurf    = Color(0xFF122040)   // Surface
val UrNavySurfVar = Color(0xFF1A3055)   // SurfaceVariant
val UrGold        = Color(0xFFD4A017)   // Tertiary / Jupiter gold
val UrGoldDark    = Color(0xFFB8860B)
val UrPink        = Color(0xFFE91E8C)   // OK button / accent
val UrPurple      = Color(0xFF7B5EA7)   // Secondary
val UrOutline     = Color(0xFF2D4E70)

// Light theme surface tints
val UrBlueLightBg = Color(0xFFF0F6FF)
val UrSurfLight   = Color(0xFFFFFFFF)
val UrSurfVarLight= Color(0xFFE3EEF8)

private val DarkColorScheme = darkColorScheme(
    primary           = UrBlueLight,
    onPrimary         = Color(0xFF001B40),
    primaryContainer  = UrBlueDark,
    onPrimaryContainer= Color(0xFFBDD8FF),
    secondary         = UrPurple,
    onSecondary       = Color.White,
    secondaryContainer= Color(0xFF3D2B6B),
    onSecondaryContainer = Color(0xFFD8C5F8),
    tertiary          = UrGold,
    onTertiary        = Color(0xFF1C1200),
    tertiaryContainer = Color(0xFF3D2E00),
    onTertiaryContainer = Color(0xFFF5DFA0),
    background        = UrNavy,
    onBackground      = Color(0xFFDCE8F8),
    surface           = UrNavySurf,
    onSurface         = Color(0xFFDCE8F8),
    surfaceVariant    = UrNavySurfVar,
    onSurfaceVariant  = Color(0xFFAFC4D9),
    outline           = UrOutline,
    inverseSurface    = Color(0xFFDCE8F8),
    inverseOnSurface  = UrNavy,
    error             = Color(0xFFFF6B6B),
)

private val LightColorScheme = lightColorScheme(
    primary           = UrBlue,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFD0E4FF),
    onPrimaryContainer= Color(0xFF001C3B),
    secondary         = UrPurple,
    onSecondary       = Color.White,
    secondaryContainer= Color(0xFFECDCFF),
    onSecondaryContainer = Color(0xFF2B0057),
    tertiary          = UrGoldDark,
    onTertiary        = Color.White,
    tertiaryContainer = Color(0xFFFFF0C0),
    onTertiaryContainer = Color(0xFF261A00),
    background        = UrBlueLightBg,
    onBackground      = Color(0xFF0D1B2A),
    surface           = UrSurfLight,
    onSurface         = Color(0xFF0D1B2A),
    surfaceVariant    = UrSurfVarLight,
    onSurfaceVariant  = Color(0xFF3D5A7A),
    outline           = Color(0xFF7090B0),
)

@Composable
fun URDictionaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

val AppTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium= TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)
