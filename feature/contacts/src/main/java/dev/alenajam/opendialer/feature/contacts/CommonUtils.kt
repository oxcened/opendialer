package dev.alenajam.opendialer.feature.contacts

import android.content.Context
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import dev.alenajam.opendialer.data.contacts.DialerContact

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