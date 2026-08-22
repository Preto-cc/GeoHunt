package com.example.geohunt.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen80,
    secondary = EarthBrown80,
    tertiary = TreasureGold80,
    background = DarkBackground,
    surface = DarkBackground,
    surfaceVariant = SurfaceVariantDark,
    primaryContainer = SuccessGreenDark,
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen40,
    secondary = EarthBrown40,
    tertiary = TreasureGold40,
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = SurfaceVariantLight,
    primaryContainer = SuccessGreenLight,
    onPrimaryContainer = ForestGreen40,
    onPrimary = Color.White
)

@Composable
fun GeoHuntTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}