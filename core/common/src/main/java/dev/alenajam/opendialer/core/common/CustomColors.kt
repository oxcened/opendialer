package dev.alenajam.opendialer.core.common

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColors(
    val success: Color = Color.Unspecified
)

val LightSuccessColor = Color(0xFF4CAF50)

val DarkSuccessColor = Color(0xFF2E7D32)

val LightCustomColors = CustomColors(
    success = LightSuccessColor
)

val DarkCustomColors = CustomColors(
    success = DarkSuccessColor
)

val LocalCustomColorsScheme = staticCompositionLocalOf<CustomColors> {
    error("LocalCustomColorsScheme not provided")
}