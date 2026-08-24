package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.CallEndpoint
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioRouteSelectorTest {
    @Test
    fun speakerToggleSelectsSpeakerWhenCurrentlyPrivate() {
        assertEquals(
            CallEndpoint.TYPE_SPEAKER,
            AudioRouteSelector.speakerTarget(
                CallEndpoint.TYPE_EARPIECE,
                listOf(CallEndpoint.TYPE_EARPIECE, CallEndpoint.TYPE_SPEAKER)
            )
        )
    }

    @Test
    fun speakerToggleLeavesSpeakerForWiredHeadsetBeforeEarpiece() {
        assertEquals(
            CallEndpoint.TYPE_WIRED_HEADSET,
            AudioRouteSelector.speakerTarget(
                CallEndpoint.TYPE_SPEAKER,
                listOf(
                    CallEndpoint.TYPE_SPEAKER,
                    CallEndpoint.TYPE_EARPIECE,
                    CallEndpoint.TYPE_WIRED_HEADSET
                )
            )
        )
    }

    @Test
    fun bluetoothToggleSelectsBluetoothAndReturnsToEarpiece() {
        assertEquals(
            CallEndpoint.TYPE_BLUETOOTH,
            AudioRouteSelector.bluetoothTarget(
                CallEndpoint.TYPE_EARPIECE,
                listOf(CallEndpoint.TYPE_EARPIECE, CallEndpoint.TYPE_BLUETOOTH)
            )
        )
        assertEquals(
            CallEndpoint.TYPE_EARPIECE,
            AudioRouteSelector.bluetoothTarget(
                CallEndpoint.TYPE_BLUETOOTH,
                listOf(CallEndpoint.TYPE_EARPIECE, CallEndpoint.TYPE_BLUETOOTH)
            )
        )
    }

    @Test
    fun routeToggleFallsBackToSpeakerWithoutPrivateEndpoint() {
        assertEquals(
            CallEndpoint.TYPE_SPEAKER,
            AudioRouteSelector.speakerTarget(
                CallEndpoint.TYPE_SPEAKER,
                listOf(CallEndpoint.TYPE_SPEAKER)
            )
        )
    }
}
