package dev.alenajam.opendialer.feature.inCall.ui

import android.telecom.Call
import org.junit.Assert.assertEquals
import org.junit.Test

class CallStatusTest {
    @Test
    fun mapsEveryDisplayedTelecomState() {
        assertEquals(CallStatus.RINGING, CallStatus.fromTelecomState(Call.STATE_RINGING))
        assertEquals(CallStatus.CONNECTING, CallStatus.fromTelecomState(Call.STATE_CONNECTING))
        assertEquals(CallStatus.DIALING, CallStatus.fromTelecomState(Call.STATE_DIALING))
        assertEquals(CallStatus.ACTIVE, CallStatus.fromTelecomState(Call.STATE_ACTIVE))
        assertEquals(CallStatus.HOLDING, CallStatus.fromTelecomState(Call.STATE_HOLDING))
        assertEquals(CallStatus.DISCONNECTING, CallStatus.fromTelecomState(Call.STATE_DISCONNECTING))
        assertEquals(CallStatus.DISCONNECTED, CallStatus.fromTelecomState(Call.STATE_DISCONNECTED))
    }

    @Test
    fun unknownOrAbsentStateIsIdle() {
        assertEquals(CallStatus.IDLE, CallStatus.fromTelecomState(null))
        assertEquals(CallStatus.IDLE, CallStatus.fromTelecomState(Call.STATE_NEW))
    }
}
