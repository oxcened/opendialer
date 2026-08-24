package dev.alenajam.opendialer.feature.inCall.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.telecom.Call;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class CallDisplaySelectorTest {
    @Test
    public void ringingCallTakesPriorityOverActiveCall() {
        CallDisplaySelector.Selection<String> result = CallDisplaySelector.select(Arrays.asList(
                candidate("active", Call.STATE_ACTIVE, false, 0),
                candidate("ringing", Call.STATE_RINGING, false, 1)
        ));

        assertEquals("ringing", result.getPrimary());
        assertEquals("active", result.getSecondary());
    }

    @Test
    public void sequenceMakesSelectionDeterministicWithinSameState() {
        CallDisplaySelector.Selection<String> result = CallDisplaySelector.select(Arrays.asList(
                candidate("later", Call.STATE_HOLDING, false, 20),
                candidate("earlier", Call.STATE_HOLDING, false, 10)
        ));

        assertEquals("earlier", result.getPrimary());
        assertEquals("later", result.getSecondary());
    }

    @Test
    public void conferenceChildrenAreExcludedFromTopLevelDisplay() {
        CallDisplaySelector.Selection<String> result = CallDisplaySelector.select(Arrays.asList(
                candidate("child", Call.STATE_ACTIVE, true, 0),
                candidate("parent", Call.STATE_HOLDING, false, 1)
        ));

        assertEquals("parent", result.getPrimary());
        assertNull(result.getSecondary());
    }

    @Test
    public void onlyConferenceChildrenProducesNoSelectionDuringHandoff() {
        CallDisplaySelector.Selection<String> result = CallDisplaySelector.select(Collections.singletonList(
                candidate("child", Call.STATE_ACTIVE, true, 0)
        ));

        assertNull(result.getPrimary());
        assertNull(result.getSecondary());
    }

    @Test
    public void disconnectedCallsAreNeverSelected() {
        CallDisplaySelector.Selection<String> result = CallDisplaySelector.select(Collections.singletonList(
                candidate("gone", Call.STATE_DISCONNECTED, false, 0)
        ));

        assertNull(result.getPrimary());
        assertNull(result.getSecondary());
    }

    private static CallDisplaySelector.Candidate<String> candidate(
            String value,
            int state,
            boolean conferenced,
            long sequence
    ) {
        return new CallDisplaySelector.Candidate<>(value, state, conferenced, sequence);
    }
}
