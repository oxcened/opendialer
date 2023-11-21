package dev.alenajam.opendialer.model

import androidx.annotation.ColorInt

interface OnStatusBarColorChange {
  fun onColorChange(@ColorInt color: Int)
}