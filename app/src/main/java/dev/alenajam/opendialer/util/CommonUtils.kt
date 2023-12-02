package dev.alenajam.opendialer.util

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.features.dialer.calls.DialerCall
import dev.alenajam.opendialer.features.dialer.contacts.DialerContact
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator

inline fun getValueAnimator(
  forward: Boolean = true,
  duration: Long? = null,
  interpolator: TimeInterpolator? = null,
  crossinline updateListener: (progress: Float) -> Unit
): ValueAnimator {
  val a = if (forward) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
  a.addUpdateListener { updateListener(it.animatedValue as Float) }
  duration?.let { a.duration = it }
  interpolator?.let { a.interpolator = it }
  return a
}

fun Context.getContactImagePlaceholder(call: DialerCall, generator: ColorGenerator): TextDrawable {
  var name = call.contactInfo.name

  if (call.isAnonymous()) {
    name = getString(R.string.anonymous)
  } else if (name.isNullOrBlank()) {
    name = call.contactInfo.number ?: ""
  }

  val filteredName = name.replace("[^a-zA-Z0-9]".toRegex(), "")
  var firstCharStr = ""

  if (filteredName.isNotEmpty()) {
    val firstChar = filteredName[0]
    firstCharStr = firstChar.toString()
  }

  return TextDrawable.builder()
    .beginConfig()
    .endConfig()
    .buildRound(firstCharStr, generator.getColor(call.id))
}

fun Context.getContactImagePlaceholder(
  contact: DialerContact,
  generator: ColorGenerator
): TextDrawable {
  val filteredName = contact.name.replace("[^a-zA-Z0-9]".toRegex(), "")
  var firstCharStr = ""

  if (filteredName.isNotEmpty()) {
    val firstChar = filteredName[0]
    firstCharStr = firstChar.toString()
  }

  return TextDrawable.builder()
    .beginConfig()
    .endConfig()
    .buildRound(firstCharStr, generator.getColor(contact.id))
}

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

fun equalNumbers(number1: String?, number2: String?): Boolean {
  return PhoneNumberUtils.compare(number1, number2)
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