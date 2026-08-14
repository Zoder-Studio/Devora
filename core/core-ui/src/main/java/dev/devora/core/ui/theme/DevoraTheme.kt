package dev.devora.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DevoraDarkColorScheme = darkColorScheme(
    primary = CatppuccinMocha.Mauve,
    secondary = CatppuccinMocha.Blue,
    tertiary = CatppuccinMocha.Green,
    error = CatppuccinMocha.Red,
    background = CatppuccinMocha.Base,
    surface = CatppuccinMocha.Surface0,
    onBackground = CatppuccinMocha.Text,
    onSurface = CatppuccinMocha.Text
)

private val DevoraLightColorScheme = lightColorScheme(
    primary = CatppuccinMocha.Mauve,
    secondary = CatppuccinMocha.Blue,
    tertiary = CatppuccinMocha.Green,
    error = CatppuccinMocha.Red
)

@Composable
fun DevoraTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DevoraDarkColorScheme else DevoraLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DevoraTypography,
        content = content
    )
}