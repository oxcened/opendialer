package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.Call

/** Tracks whether a call that reached the ringing state ended unanswered. */
internal class MissedCallTracker {
    private var wasRinging = false
    private var wasActive = false
    private var wasLocallyDeclined = false

    fun onStateChanged(state: Int) {
        when (state) {
            Call.STATE_RINGING -> wasRinging = true
            Call.STATE_ACTIVE -> wasActive = true
        }
    }

    fun markLocallyDeclined() {
        wasLocallyDeclined = true
    }

    fun shouldNotify(): Boolean = wasRinging && !wasActive && !wasLocallyDeclined
}
