package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SampColorScheme = darkColorScheme(
  primary = SampAmber,
  onPrimary = SampOnAmber,
  primaryContainer = SampAmberContainer,
  onPrimaryContainer = SampAmber,
  secondary = SampCyan,
  onSecondary = SampOnAmber,
  secondaryContainer = SampCyanContainer,
  onSecondaryContainer = SampCyan,
  tertiary = SampGreen,
  background = SampBackground,
  onBackground = SampTextPrimary,
  surface = SampSurface,
  onSurface = SampTextPrimary,
  surfaceVariant = SampSurfaceVariant,
  onSurfaceVariant = SampTextSecondary,
  outline = SampBorder,
  error = SampRed
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = SampColorScheme,
    typography = Typography,
    content = content
  )
}
