package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.Call
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissedCallTrackerTest {
    @Test
    fun ringingCallThatEndsUnansweredIsMissed() {
        val tracker = MissedCallTracker()

        tracker.onStateChanged(Call.STATE_RINGING)
        tracker.onStateChanged(Call.STATE_DISCONNECTED)

        assertTrue(tracker.shouldNotify())
    }

    @Test
    fun answeredCallIsNotMissedAfterItEnds() {
        val tracker = MissedCallTracker()

        tracker.onStateChanged(Call.STATE_RINGING)
        tracker.onStateChanged(Call.STATE_ACTIVE)
        tracker.onStateChanged(Call.STATE_DISCONNECTED)

        assertFalse(tracker.shouldNotify())
    }

    @Test
    fun locallyDeclinedCallDoesNotPostMissedNotification() {
        val tracker = MissedCallTracker()

        tracker.onStateChanged(Call.STATE_RINGING)
        tracker.markLocallyDeclined()
        tracker.onStateChanged(Call.STATE_DISCONNECTED)

        assertFalse(tracker.shouldNotify())
    }

    @Test
    fun outgoingCallDoesNotPostMissedNotification() {
        val tracker = MissedCallTracker()

        tracker.onStateChanged(Call.STATE_DIALING)
        tracker.onStateChanged(Call.STATE_DISCONNECTED)

        assertFalse(tracker.shouldNotify())
    }

    @Test
    fun callThatWasActiveAndThenHeldDoesNotPostMissedNotification() {
        val tracker = MissedCallTracker()

        tracker.onStateChanged(Call.STATE_RINGING)
        tracker.onStateChanged(Call.STATE_ACTIVE)
        tracker.onStateChanged(Call.STATE_HOLDING)
        tracker.onStateChanged(Call.STATE_DISCONNECTED)

        assertFalse(tracker.shouldNotify())
    }
}
