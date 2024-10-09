package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.service.CallsHandler
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall
import dev.alenajam.opendialer.feature.inCall.service.OngoingCallHelper
import dev.alenajam.opendialer.feature.inCall.service.TelecomAdapter
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class InCallViewModel
@Inject constructor(
    callHandler: CallsHandler,
    private val app: Application
) : ViewModel() {
    val primaryCall: MutableLiveData<OngoingCall> = callHandler.primaryCall
    val secondaryCall: MutableLiveData<OngoingCall> = callHandler.secondaryCall
    val calls: MutableLiveData<Map<Call, OngoingCall>> = callHandler.calls
    private val audioState: MutableLiveData<CallAudioState> = callHandler.audioState
    val canAddCall: MutableLiveData<Boolean> = callHandler.canAddCall
    private var statusTimer: Timer? = null
    val stateLabel = primaryCall.switchMap { getStateLiveData(it) }
    val isHolding = primaryCall.map { it.state == Call.STATE_HOLDING }
    val isSpeaker = audioState.map { it.route == CallAudioState.ROUTE_SPEAKER }
    val isMuted = audioState.map { it.isMuted }
    val callerName = primaryCall.map { it.callerName ?: it.callerNumber }
    val callerImageUri = primaryCall.map { it.callerImageUri }

    override fun onCleared() {
        super.onCleared()
        statusTimer?.cancel()
        statusTimer = null
    }

    fun getStateLiveData(call: OngoingCall): LiveData<String> {
        val initialValue = when (call.state) {
            Call.STATE_RINGING -> app.getString(R.string.call_ringing_title)
            Call.STATE_CONNECTING -> app.getString(R.string.call_connecting_title)
            Call.STATE_HOLDING -> app.getString(R.string.call_holding_title)
            Call.STATE_DIALING -> app.getString(R.string.call_dialing_title)
            Call.STATE_DISCONNECTING -> app.getString(R.string.call_disconnecting_title)
            Call.STATE_DISCONNECTED -> app.getString(R.string.call_disconnected_title)
            Call.STATE_ACTIVE -> "00:00:00"
            else -> ""
        }
        val liveData = MutableLiveData(initialValue)
        if (call.state == Call.STATE_ACTIVE) {
            statusTimer?.cancel()
            statusTimer = fixedRateTimer(period = 1000) {
                val differenceTime =
                    CommonUtils.getCurrentTime() - call.startTime + call.totalTime
                liveData.postValue(CommonUtils.getDurationTimeString(differenceTime))
            }
        }
        return liveData
    }

    fun hangup(message: String? = null) = primaryCall.value?.hangup(message)
    fun answer(call: OngoingCall) = call.answer()
    fun turnSpeaker() = TelecomAdapter.turnSpeaker()
    fun turnBluetooth() = TelecomAdapter.turnBluetooth()
    fun turnMute() = TelecomAdapter.turnMute()
    fun playDtmf(call: OngoingCall, digit: Char) = call.playDtmf(digit)
    fun hold() = primaryCall.value?.hold()
    fun switch() = secondaryCall.value?.hold(false)

    fun addCall(fragment: Fragment) = fragment.startActivity(
        Intent(Intent.ACTION_DIAL).putExtra(
            "add_call",
            true
        )
    )

    fun addCall(activity: Activity) = activity.startActivity(
        Intent(Intent.ACTION_DIAL).putExtra(
            "add_call",
            true
        )
    )

    fun manageConference(fragment: Fragment) = fragment.findNavController()
        .navigate(R.id.action_inCallFragment_to_inCallManageConferenceFragment)

    fun merge(call: OngoingCall) = OngoingCallHelper.merge(call)
}