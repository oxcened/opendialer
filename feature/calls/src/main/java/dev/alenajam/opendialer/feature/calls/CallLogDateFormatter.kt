package dev.alenajam.opendialer.feature.calls

import android.text.format.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

internal fun formatCallLogDateHeader(date: LocalDate, today: LocalDate, locale: Locale): String {
    val skeleton = if (date.year == today.year) "EEE, d MMM" else "EEE, d MMM yyyy"
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}

internal fun formatCallLogTime(date: Date, locale: Locale): String =
    java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, locale).format(date)
