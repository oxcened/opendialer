package dev.alenajam.opendialer.model

interface BackPressedListener {
  /** Should return false to override default behaviour */
  fun onBackPressed(): Boolean
}