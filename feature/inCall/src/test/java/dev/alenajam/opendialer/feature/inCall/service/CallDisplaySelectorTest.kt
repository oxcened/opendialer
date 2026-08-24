package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.Call
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallDisplaySelectorTest {
    @Test
    fun ringingCallTakesPriorityOverActiveCall() {
        val result = CallDisplaySelector.select(
            listOf(
                candidate("active", Call.STATE_ACTIVE, false, 0),
                candidate("ringing", Call.STATE_RINGING, false, 1)
            )
        )

        assertEquals("ringing", result.primary)
        assertEquals("active", result.secondary)
    }

    @Test
    fun dialingCallTakesPriorityOverActiveCall() {
        val result = CallDisplaySelector.select(
            listOf(
                candidate("active", Call.STATE_ACTIVE, false, 0),
                candidate("dialing", Call.STATE_DIALING, false, 1)
            )
        )

        assertEquals("dialing", result.primary)
        assertEquals("active", result.secondary)
    }

    @Test
    fun activeCallTakesPriorityOverHeldCall() {
        val result = CallDisplaySelector.select(
            listOf(
                candidate("held", Call.STATE_HOLDING, false, 0),
                candidate("active", Call.STATE_ACTIVE, false, 1)
            )
        )

        assertEquals("active", result.primary)
        assertEquals("held", result.secondary)
    }

    @Test
    fun selectedPrimaryIsNotRepeatedAsSecondary() {
        val result = CallDisplaySelector.select(
            listOf(candidate("active", Call.STATE_ACTIVE, false, 0))
        )

        assertEquals("active", result.primary)
        assertNull(result.secondary)
    }

    @Test
    fun sequenceMakesSelectionDeterministicWithinSameState() {
        val result = CallDisplaySelector.select(
            listOf(
                candidate("later", Call.STATE_HOLDING, false, 20),
                candidate("earlier", Call.STATE_HOLDING, false, 10)
            )
        )

        assertEquals("earlier", result.primary)
        assertEquals("later", result.secondary)
    }

    @Test
    fun conferenceChildrenAreExcludedFromTopLevelDisplay() {
        val result = CallDisplaySelector.select(
            listOf(
                candidate("child", Call.STATE_ACTIVE, true, 0),
                candidate("parent", Call.STATE_HOLDING, false, 1)
            )
        )

        assertEquals("parent", result.primary)
        assertNull(result.secondary)
    }

    @Test
    fun onlyConferenceChildrenProducesNoSelectionDuringHandoff() {
        val result = CallDisplaySelector.select(
            listOf(
                candidate("child", Call.STATE_ACTIVE, true, 0)
            )
        )

        assertNull(result.primary)
        assertNull(result.secondary)
    }

    @Test
    fun disconnectedCallsAreNeverSelected() {
        val result = CallDisplaySelector.select(
            listOf(
                candidate("gone", Call.STATE_DISCONNECTED, false, 0)
            )
        )

        assertNull(result.primary)
        assertNull(result.secondary)
    }

    private fun candidate(
        value: String,
        state: Int,
        conferenced: Boolean,
        sequence: Long
    ): CallDisplaySelector.Candidate<String> {
        return CallDisplaySelector.Candidate(value, state, conferenced, sequence)
    }
}
