package com.example.jokes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColors = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private fun TextUnit.scale(factor: Float): TextUnit = (value * factor).sp

@Composable
private fun scaledTypography(factor: Float): Typography {
    val base = Typography()
    return Typography(
        displayLarge   = base.displayLarge.copy(fontSize = base.displayLarge.fontSize.scale(factor)),
        displayMedium  = base.displayMedium.copy(fontSize = base.displayMedium.fontSize.scale(factor)),
        displaySmall   = base.displaySmall.copy(fontSize = base.displaySmall.fontSize.scale(factor)),
        headlineLarge  = base.headlineLarge.copy(fontSize = base.headlineLarge.fontSize.scale(factor)),
        headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize.scale(factor)),
        headlineSmall  = base.headlineSmall.copy(fontSize = base.headlineSmall.fontSize.scale(factor)),
        titleLarge     = base.titleLarge.copy(fontSize = base.titleLarge.fontSize.scale(factor)),
        titleMedium    = base.titleMedium.copy(fontSize = base.titleMedium.fontSize.scale(factor)),
        titleSmall     = base.titleSmall.copy(fontSize = base.titleSmall.fontSize.scale(factor)),
        bodyLarge      = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize.scale(factor)),
        bodyMedium     = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize.scale(factor)),
        bodySmall      = base.bodySmall.copy(fontSize = base.bodySmall.fontSize.scale(factor)),
        labelLarge     = base.labelLarge.copy(fontSize = base.labelLarge.fontSize.scale(factor)),
        labelMedium    = base.labelMedium.copy(fontSize = base.labelMedium.fontSize.scale(factor)),
        labelSmall     = base.labelSmall.copy(fontSize = base.labelSmall.fontSize.scale(factor))
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography(fontScale),
        content = content
    )
}