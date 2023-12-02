package dev.alenajam.opendialer.util

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt

fun showInputMethod(view: View) {
  val manager: InputMethodManager =
    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  manager?.let { manager.showSoftInput(view, 0) }
}

fun hideInputMethod(view: View) {
  val manager: InputMethodManager =
    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  manager?.let { manager.hideSoftInputFromWindow(view.windowToken, 0) }
}

private fun setStatusBarLightMode(window: Window, light: Boolean) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    if (light) {
      window.insetsController?.setSystemBarsAppearance(
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
      )
    } else {
      window.insetsController?.setSystemBarsAppearance(
        0,
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
      )
    }
  } else {
    @Suppress("DEPRECATION")
    if (light) {
      window.decorView.systemUiVisibility =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
          View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    } else {
      window.decorView.systemUiVisibility = 0
    }
  }
}

@ColorInt
fun getContrastColor(@ColorInt color: Int): Int {
  // Counting the perceptive luminance - human eye favors green color...
  val a =
    1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
  return if (a < 0.5) Color.BLACK else Color.WHITE
}

fun updateStatusBarLightMode(window: Window, @ColorInt bgColor: Int) {
  val contrast = getContrastColor(bgColor)
  setStatusBarLightMode(window, contrast == Color.BLACK)
}