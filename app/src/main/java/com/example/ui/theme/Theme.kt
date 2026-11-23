package com.example.ui.theme

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
  primary = GoldPrimary,
  onPrimary = Color(0xFF261900),
  primaryContainer = GoldContainer,
  onPrimaryContainer = OnGoldContainer,
  secondary = GoldSecondary,
  onSecondary = Color(0xFF382900),
  secondaryContainer = PlumDarkSurfaceVariant,
  onSecondaryContainer = TextPrimaryDark,
  tertiary = Color(0xFFE5B3A5),
  onTertiary = Color(0xFF442118),
  background = PlumDarkBackground,
  onBackground = TextPrimaryDark,
  surface = PlumDarkSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = PlumDarkSurfaceVariant,
  onSurfaceVariant = TextSecondaryDark,
  outline = PlumDarkOutline
)

private val LightColorScheme = lightColorScheme(
  primary = GoldPrimaryLight,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFDF9B),
  onPrimaryContainer = Color(0xFF2E1C00),
  secondary = Color(0xFF705B2E),
  onSecondary = Color.White,
  secondaryContainer = PlumLightSurfaceVariant,
  onSecondaryContainer = TextPrimaryLight,
  tertiary = Color(0xFF8B5041),
  onTertiary = Color.White,
  background = PlumLightBackground,
  onBackground = TextPrimaryLight,
  surface = PlumLightSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = PlumLightSurfaceVariant,
  onSurfaceVariant = TextSecondaryLight,
  outline = PlumLightOutline
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to stunning luxury dark aesthetic
  dynamicColor: Boolean = false, // Keep bespoke salon theme palette
  content: @Composable () -> Unit,
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
