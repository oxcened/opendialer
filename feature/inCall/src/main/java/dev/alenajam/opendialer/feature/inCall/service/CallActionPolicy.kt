package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.Call

internal object CallActionPolicy {
    fun shouldAnswer(state: Int): Boolean = state == Call.STATE_RINGING

    fun shouldReject(state: Int): Boolean = state == Call.STATE_RINGING

    fun holdAction(state: Int, canHold: Boolean, requestedHold: Boolean?): HoldAction = when (requestedHold) {
        true -> if (canHold) HoldAction.HOLD else HoldAction.NONE
        false -> if (state == Call.STATE_HOLDING) HoldAction.UNHOLD else HoldAction.NONE
        null -> when {
            state == Call.STATE_HOLDING -> HoldAction.UNHOLD
            canHold -> HoldAction.HOLD
            else -> HoldAction.NONE
        }
    }

    fun canPlayDtmf(state: Int): Boolean = state == Call.STATE_ACTIVE

    enum class HoldAction { HOLD, UNHOLD, NONE }
}
