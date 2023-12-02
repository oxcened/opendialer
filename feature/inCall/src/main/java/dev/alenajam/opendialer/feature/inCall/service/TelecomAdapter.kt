package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.CallAudioState

object TelecomAdapter {
  var callService: InCallServiceImpl? = null

  private fun setAudioRoute(route: Int) = callService?.setAudioRoute(route)

  fun turnSpeaker() {
    callService?.let {
      setAudioRoute(
        if (it.callAudioState.route == CallAudioState.ROUTE_SPEAKER)
          CallAudioState.ROUTE_WIRED_OR_EARPIECE
        else
          CallAudioState.ROUTE_SPEAKER
      )
    }
  }

  fun turnBluetooth() {
    callService?.let {
      setAudioRoute(
        if (it.callAudioState.route == CallAudioState.ROUTE_BLUETOOTH)
          CallAudioState.ROUTE_WIRED_OR_EARPIECE
        else
          CallAudioState.ROUTE_BLUETOOTH
      )
    }
  }

  fun turnMute() = callService?.let { it.setMuted(!it.callAudioState.isMuted) }

  fun canAddCall() = callService?.canAddCall()

  fun tearDown() {
    callService = null
  }
}