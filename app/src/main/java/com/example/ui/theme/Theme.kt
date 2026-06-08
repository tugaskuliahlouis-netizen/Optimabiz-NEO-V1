package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeubrutalismColorScheme = lightColorScheme(
    primary = NeuYellow,
    secondary = NeuBlue,
    tertiary = NeuPink,
    background = NeuBg,
    surface = NeuCardBg,
    error = NeuPink,
    onPrimary = NeuBlack,
    onSecondary = NeuBlack,
    onTertiary = NeuBlack,
    onBackground = NeuBlack,
    onSurface = NeuBlack,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeubrutalismColorScheme,
        typography = Typography,
        content = content
    )
}
