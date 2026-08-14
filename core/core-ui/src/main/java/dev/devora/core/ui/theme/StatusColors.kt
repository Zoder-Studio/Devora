package dev.devora.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Devora needs a "success" color role for build/workflow status
 * (green), which Material3's default ColorScheme does not provide.
 * Reuses the existing Catppuccin Mocha palette instead of inventing
 * a new one.
 */
object DevoraStatusColors {
    val success: Color @Composable get() = CatppuccinMocha.Green
    val warning: Color @Composable get() = CatppuccinMocha.Yellow
    val danger: Color @Composable get() = MaterialTheme.colorScheme.error
    val info: Color @Composable get() = CatppuccinMocha.Blue
    val neutral: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}