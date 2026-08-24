package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.CallAudioState
import android.telecom.CallEndpoint
import org.junit.Assert.assertEquals
import org.junit.Test

class TelecomAdapterTest {
    @Test
    fun defaultRouteLabelsCoverUserVisibleRoutes() {
        assertEquals("Speaker", TelecomAdapter.defaultRouteLabel(CallAudioState.ROUTE_SPEAKER))
        assertEquals("Bluetooth", TelecomAdapter.defaultRouteLabel(CallAudioState.ROUTE_BLUETOOTH))
        assertEquals("Wired headset", TelecomAdapter.defaultRouteLabel(CallAudioState.ROUTE_WIRED_HEADSET))
        assertEquals("Phone", TelecomAdapter.defaultRouteLabel(CallAudioState.ROUTE_EARPIECE))
    }

    @Test
    fun endpointTypesMapToLegacyRoutes() {
        assertEquals(CallAudioState.ROUTE_SPEAKER, TelecomAdapter.toLegacyRoute(CallEndpoint.TYPE_SPEAKER))
        assertEquals(CallAudioState.ROUTE_BLUETOOTH, TelecomAdapter.toLegacyRoute(CallEndpoint.TYPE_BLUETOOTH))
        assertEquals(CallAudioState.ROUTE_WIRED_HEADSET, TelecomAdapter.toLegacyRoute(CallEndpoint.TYPE_WIRED_HEADSET))
        assertEquals(CallAudioState.ROUTE_EARPIECE, TelecomAdapter.toLegacyRoute(CallEndpoint.TYPE_EARPIECE))
    }
}
