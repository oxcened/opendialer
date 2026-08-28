package dev.alenajam.opendialer.core.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material3.Typography

@Immutable
data class AppThemeExtension(
    val backgroundPainter: @Composable () -> Painter? = { null },
    val callScreenBackgroundPainter: @Composable () -> Painter? = { null },
    val customSuccessColor: Color = Color.Unspecified,
    val typography: Typography? = null,
)

val LocalAppThemeExtension = staticCompositionLocalOf { AppThemeExtension() }
