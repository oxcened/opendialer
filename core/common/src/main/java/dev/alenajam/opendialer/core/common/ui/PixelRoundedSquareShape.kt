package dev.alenajam.opendialer.core.common.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/** A square with small 8-bit steps in place of smooth rounded corners. */
object PixelRoundedSquareShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            moveTo(width * 0.125f, 0f)
            lineTo(width * 0.875f, 0f)
            lineTo(width * 0.875f, height * 0.0625f)
            lineTo(width * 0.9375f, height * 0.0625f)
            lineTo(width * 0.9375f, height * 0.125f)
            lineTo(width, height * 0.125f)
            lineTo(width, height * 0.875f)
            lineTo(width * 0.9375f, height * 0.875f)
            lineTo(width * 0.9375f, height * 0.9375f)
            lineTo(width * 0.875f, height * 0.9375f)
            lineTo(width * 0.875f, height)
            lineTo(width * 0.125f, height)
            lineTo(width * 0.125f, height * 0.9375f)
            lineTo(width * 0.0625f, height * 0.9375f)
            lineTo(width * 0.0625f, height * 0.875f)
            lineTo(0f, height * 0.875f)
            lineTo(0f, height * 0.125f)
            lineTo(width * 0.0625f, height * 0.125f)
            lineTo(width * 0.0625f, height * 0.0625f)
            lineTo(width * 0.125f, height * 0.0625f)
            close()
        }
        return Outline.Generic(path)
    }
}
