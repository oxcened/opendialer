package dev.alenajam.opendialer.core.common

interface BackPressedListener {
  /** Should return false to override default behaviour */
  fun onBackPressed(): Boolean
}