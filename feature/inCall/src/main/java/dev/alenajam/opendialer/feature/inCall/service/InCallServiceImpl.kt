package dev.alenajam.opendialer.feature.inCall.service

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.CallEndpoint
import android.telecom.InCallService
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InCallServiceImpl : InCallService() {
    @Inject
    lateinit var callHandler: CallsHandler

    @Inject
    lateinit var telecomAdapter: TelecomAdapter

    @Inject
    lateinit var proximitySensor: ProximitySensor

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callHandler.addCall(call, this)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callHandler.removeCall(call)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        telecomAdapter.onLegacyAudioStateChanged(audioState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            callHandler.updateCallAudioState(audioState)
        }
    }

    @RequiresApi(34)
    override fun onCallEndpointChanged(callEndpoint: CallEndpoint) {
        super.onCallEndpointChanged(callEndpoint)
        telecomAdapter.onCallEndpointChanged(callEndpoint)
        callHandler.updateCallEndpoint(TelecomAdapter.toLegacyRoute(callEndpoint.endpointType))
    }

    @RequiresApi(34)
    override fun onAvailableCallEndpointsChanged(availableEndpoints: List<CallEndpoint>) {
        super.onAvailableCallEndpointsChanged(availableEndpoints)
        telecomAdapter.onAvailableCallEndpointsChanged(availableEndpoints)
    }

    @RequiresApi(34)
    override fun onMuteStateChanged(isMuted: Boolean) {
        super.onMuteStateChanged(isMuted)
        telecomAdapter.onMuteStateChanged(isMuted)
        callHandler.updateMuteState(isMuted)
    }

    override fun onCanAddCallChanged(canAddCall: Boolean) {
        super.onCanAddCallChanged(canAddCall)
        callHandler.updateCanAddCall(canAddCall)
    }

    override fun onBringToForeground(showDialpad: Boolean) {
        super.onBringToForeground(showDialpad)
        callHandler.attemptStartActivity()
    }

    override fun onBind(intent: Intent): IBinder? {
        callHandler.setup(
            this,
            applicationContext,
            proximitySensor
        )
        telecomAdapter.attach(this)
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        callHandler.tearDown()
        telecomAdapter.detach(this)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
