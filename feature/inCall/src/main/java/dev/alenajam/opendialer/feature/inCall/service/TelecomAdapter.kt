package dev.alenajam.opendialer.feature.inCall.service

import android.annotation.SuppressLint
import android.os.Build
import android.os.OutcomeReceiver
import android.telecom.CallAudioState
import android.telecom.CallEndpoint
import android.telecom.CallEndpointException
import androidx.annotation.RequiresApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelecomAdapter @Inject constructor() : InCallCommands {
    private var callService: InCallServiceImpl? = null
    private var currentEndpoint: CallEndpoint? = null
    private var availableEndpoints: List<CallEndpoint> = emptyList()
    private var isMuted = false

    fun attach(callService: InCallServiceImpl) {
        this.callService = callService
    }

    fun onLegacyAudioStateChanged(state: CallAudioState) {
        isMuted = state.isMuted
    }

    fun onCallEndpointChanged(endpoint: CallEndpoint) {
        currentEndpoint = endpoint
    }

    fun onAvailableCallEndpointsChanged(endpoints: List<CallEndpoint>) {
        availableEndpoints = endpoints.toList()
    }

    fun onMuteStateChanged(muted: Boolean) {
        isMuted = muted
    }

    override fun toggleSpeaker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nextType = if (currentEndpoint?.endpointType == CallEndpoint.TYPE_SPEAKER) {
                preferredPrivateEndpointType()
            } else {
                CallEndpoint.TYPE_SPEAKER
            }
            requestEndpoint(nextType)
        } else {
            toggleLegacyRoute(CallAudioState.ROUTE_SPEAKER)
        }
    }

    override fun toggleBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nextType = if (currentEndpoint?.endpointType == CallEndpoint.TYPE_BLUETOOTH) {
                preferredPrivateEndpointType()
            } else {
                CallEndpoint.TYPE_BLUETOOTH
            }
            requestEndpoint(nextType)
        } else {
            toggleLegacyRoute(CallAudioState.ROUTE_BLUETOOTH)
        }
    }

    override fun toggleMute() {
        callService?.setMuted(!isMuted)
    }

    @Suppress("DEPRECATION")
    private fun toggleLegacyRoute(route: Int) {
        callService?.let { service ->
            val currentRoute = service.callAudioState?.route ?: return
            service.setAudioRoute(
                if (currentRoute == route) CallAudioState.ROUTE_WIRED_OR_EARPIECE else route
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun preferredPrivateEndpointType(): Int =
        when {
            availableEndpoints.any { it.endpointType == CallEndpoint.TYPE_WIRED_HEADSET } ->
                CallEndpoint.TYPE_WIRED_HEADSET
            availableEndpoints.any { it.endpointType == CallEndpoint.TYPE_EARPIECE } ->
                CallEndpoint.TYPE_EARPIECE
            else -> CallEndpoint.TYPE_SPEAKER
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun requestEndpoint(type: Int) {
        val service = callService ?: return
        val endpoint = availableEndpoints.firstOrNull { it.endpointType == type } ?: return
        service.requestCallEndpointChange(
            endpoint,
            service.mainExecutor,
            object : OutcomeReceiver<Void?, CallEndpointException> {
                override fun onResult(result: Void?) = Unit
                override fun onError(error: CallEndpointException) = Unit
            }
        )
    }

    fun detach(callService: InCallServiceImpl) {
        if (this.callService === callService) {
            this.callService = null
            currentEndpoint = null
            availableEndpoints = emptyList()
        }
    }

    companion object {
        @JvmStatic
        @SuppressLint("InlinedApi")
        fun toLegacyRoute(endpointType: Int): Int = when (endpointType) {
            CallEndpoint.TYPE_BLUETOOTH -> CallAudioState.ROUTE_BLUETOOTH
            CallEndpoint.TYPE_SPEAKER -> CallAudioState.ROUTE_SPEAKER
            CallEndpoint.TYPE_WIRED_HEADSET -> CallAudioState.ROUTE_WIRED_HEADSET
            CallEndpoint.TYPE_STREAMING -> CallAudioState.ROUTE_STREAMING
            else -> CallAudioState.ROUTE_EARPIECE
        }
    }
}
