package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.Call
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallActionPolicyTest {
    @Test
    fun onlyRingingCallsCanBeAnsweredOrRejected() {
        assertTrue(CallActionPolicy.shouldAnswer(Call.STATE_RINGING))
        assertTrue(CallActionPolicy.shouldReject(Call.STATE_RINGING))
        assertFalse(CallActionPolicy.shouldAnswer(Call.STATE_ACTIVE))
        assertFalse(CallActionPolicy.shouldReject(Call.STATE_HOLDING))
    }

    @Test
    fun holdDefaultsToHoldingEligibleNonHeldCall() {
        assertEquals(
            CallActionPolicy.HoldAction.HOLD,
            CallActionPolicy.holdAction(Call.STATE_ACTIVE, canHold = true, requestedHold = null)
        )
    }

    @Test
    fun heldCallDefaultsToUnholdAndExplicitFalseUnholds() {
        assertEquals(
            CallActionPolicy.HoldAction.UNHOLD,
            CallActionPolicy.holdAction(Call.STATE_HOLDING, canHold = true, requestedHold = null)
        )
        assertEquals(
            CallActionPolicy.HoldAction.UNHOLD,
            CallActionPolicy.holdAction(Call.STATE_HOLDING, canHold = false, requestedHold = false)
        )
    }

    @Test
    fun holdIsNotRequestedWhenCapabilityIsMissingOrExplicitlyDisabled() {
        assertEquals(
            CallActionPolicy.HoldAction.NONE,
            CallActionPolicy.holdAction(Call.STATE_ACTIVE, canHold = false, requestedHold = null)
        )
        assertEquals(
            CallActionPolicy.HoldAction.NONE,
            CallActionPolicy.holdAction(Call.STATE_ACTIVE, canHold = true, requestedHold = false)
        )
    }

    @Test
    fun dtmfIsLimitedToActiveCalls() {
        assertTrue(CallActionPolicy.canPlayDtmf(Call.STATE_ACTIVE))
        assertFalse(CallActionPolicy.canPlayDtmf(Call.STATE_DIALING))
        assertFalse(CallActionPolicy.canPlayDtmf(Call.STATE_HOLDING))
    }
}
