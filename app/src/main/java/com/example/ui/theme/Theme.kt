package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InvioraColorScheme = lightColorScheme(
  primary = GoldPrimary,
  onPrimary = Color.White,
  primaryContainer = IvorySurfaceLight,
  onPrimaryContainer = CharcoalPrimary,
  secondary = GoldLight,
  onSecondary = Color.White,
  background = IvoryBg,
  onBackground = CharcoalPrimary,
  surface = IvorySurface,
  onSurface = CharcoalPrimary,
  surfaceVariant = IvorySurfaceLight,
  onSurfaceVariant = CharcoalSecondary,
  outline = IvoryBorder,
  outlineVariant = IvoryBorderStrong
)

@Composable
fun InvioraTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = InvioraColorScheme,
    typography = Typography,
    content = content
  )
}

// Keep alias for tests if needed
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  InvioraTheme(content = content)
}

