package dev.alenajam.opendialer.core.common

import android.text.format.DateUtils
import java.util.Date

/** Formats a timestamp relative to now using Android's locale-aware resources. */
fun formatRelativeTime(date: Date, nowMillis: Long = System.currentTimeMillis()): String =
    DateUtils.getRelativeTimeSpanString(
        date.time,
        nowMillis,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
