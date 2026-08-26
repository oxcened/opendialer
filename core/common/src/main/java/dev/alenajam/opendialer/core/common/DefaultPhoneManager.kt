package dev.alenajam.opendialer.core.common

import android.content.Intent

interface DefaultPhoneManager {
    fun isDefaultDialer(): Boolean
    fun createRequestDefaultDialerIntent(): Intent?
}
