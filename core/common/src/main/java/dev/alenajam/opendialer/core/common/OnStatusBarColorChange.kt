package dev.alenajam.opendialer.core.common

import androidx.annotation.ColorInt

interface OnStatusBarColorChange {
  fun onColorChange(@ColorInt color: Int)
}