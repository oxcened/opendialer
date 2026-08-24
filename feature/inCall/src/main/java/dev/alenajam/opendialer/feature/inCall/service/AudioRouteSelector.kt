package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.CallEndpoint

internal object AudioRouteSelector {
    fun speakerTarget(currentType: Int?, availableTypes: Collection<Int>): Int =
        if (currentType == CallEndpoint.TYPE_SPEAKER) privateTarget(availableTypes)
        else CallEndpoint.TYPE_SPEAKER

    fun bluetoothTarget(currentType: Int?, availableTypes: Collection<Int>): Int =
        if (currentType == CallEndpoint.TYPE_BLUETOOTH) privateTarget(availableTypes)
        else CallEndpoint.TYPE_BLUETOOTH

    private fun privateTarget(availableTypes: Collection<Int>): Int = when {
        CallEndpoint.TYPE_WIRED_HEADSET in availableTypes -> CallEndpoint.TYPE_WIRED_HEADSET
        CallEndpoint.TYPE_EARPIECE in availableTypes -> CallEndpoint.TYPE_EARPIECE
        else -> CallEndpoint.TYPE_SPEAKER
    }
}
