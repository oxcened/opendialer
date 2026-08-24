package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.CallAudioState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelecomAdapter @Inject constructor() : InCallCommands {
    private var callService: InCallServiceImpl? = null

    fun attach(callService: InCallServiceImpl) {
        this.callService = callService
    }

    private fun setAudioRoute(route: Int) = callService?.setAudioRoute(route)

    override fun toggleSpeaker() {
        callService?.let {
            setAudioRoute(
                if (it.callAudioState.route == CallAudioState.ROUTE_SPEAKER)
                    CallAudioState.ROUTE_WIRED_OR_EARPIECE
                else
                    CallAudioState.ROUTE_SPEAKER
            )
        }
    }

    override fun toggleBluetooth() {
        callService?.let {
            setAudioRoute(
                if (it.callAudioState.route == CallAudioState.ROUTE_BLUETOOTH)
                    CallAudioState.ROUTE_WIRED_OR_EARPIECE
                else
                    CallAudioState.ROUTE_BLUETOOTH
            )
        }
    }

    override fun toggleMute() {
        callService?.let { it.setMuted(!it.callAudioState.isMuted) }
    }

    fun detach(callService: InCallServiceImpl) {
        if (this.callService === callService) {
            this.callService = null
        }
    }
}
