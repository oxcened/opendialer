package dev.alenajam.opendialer.model

interface ToolbarListener {
  fun showToolbar(animate: Boolean)
  fun hideToolbar(animate: Boolean)
}