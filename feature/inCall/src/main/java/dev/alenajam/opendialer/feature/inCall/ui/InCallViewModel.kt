
package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.service.CallDisplayState
import dev.alenajam.opendialer.feature.inCall.service.CallsHandler
import dev.alenajam.opendialer.feature.inCall.service.InCallCommands
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall
import dev.alenajam.opendialer.feature.inCall.service.OngoingCallHelper
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class InCallViewModel
@Inject constructor(
    callHandler: CallsHandler,
    private val inCallCommands: InCallCommands,
    private val app: Application
) : ViewModel() {
    private val displayState: LiveData<CallDisplayState> = callHandler.displayState
    val primaryCall = displayState.map { it.primary }
    val secondaryCall = displayState.map { it.secondary }
    val calls: LiveData<Map<Call, OngoingCall>> = callHandler.calls
    private val audioState: LiveData<CallAudioState> = callHandler.audioState
    val canAddCall: LiveData<Boolean> = callHandler.canAddCall
    private var statusTimer: Timer? = null
    val stateLabel = primaryCall.switchMap { getStateLiveData(it) }
    val isHolding = primaryCall.map { it?.state == Call.STATE_HOLDING }
    val canHold = primaryCall.map { it?.canBeHeld() == true }
    val canMerge = primaryCall.map { it?.canBeMerged() == true }
    val canManageConference = primaryCall.map { it?.isConference == true }
    val isSpeaker = audioState.map { it.route == CallAudioState.ROUTE_SPEAKER }
    val isMuted = audioState.map { it.isMuted }
    val callerName = primaryCall.map { it?.let { call -> call.callerName ?: call.callerNumber }.orEmpty() }
    val callerNumber = primaryCall.map { it?.callerNumber.orEmpty() }
    val callerNumberLabel = primaryCall.map { it?.callerNumberLabel.orEmpty() }
    val callerImageUri = primaryCall.map { it?.callerImageUri }
    val isIncoming = primaryCall.map { it?.state == Call.STATE_RINGING }
    val hasSecondaryCall = secondaryCall.map { it != null }
    val secondaryCallerName = secondaryCall.map { it?.let { call -> call.callerName ?: call.callerNumber } }

    override fun onCleared() {
        super.onCleared()
        statusTimer?.cancel()
        statusTimer = null
    }

    fun getStateLiveData(call: OngoingCall?): LiveData<String> {
        statusTimer?.cancel()
        statusTimer = null
        val initialValue = when (call?.state) {
            Call.STATE_RINGING -> app.getString(R.string.call_ringing_title)
            Call.STATE_CONNECTING -> app.getString(R.string.call_connecting_title)
            Call.STATE_HOLDING -> app.getString(R.string.call_holding_title)
            Call.STATE_DIALING -> app.getString(R.string.call_dialing_title)
            Call.STATE_DISCONNECTING -> app.getString(R.string.call_disconnecting_title)
            Call.STATE_DISCONNECTED -> app.getString(R.string.call_disconnected_title)
            Call.STATE_ACTIVE -> "00:00"
            else -> ""
        }
        val liveData = MutableLiveData(initialValue)
        if (call?.state == Call.STATE_ACTIVE) {
            statusTimer = fixedRateTimer(period = 1000) {
                val differenceTime =
                    CommonUtils.getCurrentTime() - call.startTime + call.totalTime
                liveData.postValue(CommonUtils.getDurationTimeString(differenceTime))
            }
        }
        return liveData
    }

    fun hangup(message: String? = null) = primaryCall.value?.hangup(message)
    fun answer() = primaryCall.value?.answer()
    fun turnSpeaker() = inCallCommands.toggleSpeaker()
    fun turnBluetooth() = inCallCommands.toggleBluetooth()
    fun turnMute() = inCallCommands.toggleMute()
    fun playDtmf(digit: Char) = primaryCall.value?.playDtmf(digit)
    fun hold() = primaryCall.value?.hold()
    fun switch() = secondaryCall.value?.hold(false)

    fun addCall(activity: Activity) = activity.startActivity(
        Intent(Intent.ACTION_DIAL).putExtra(
            MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL,
            true
        )
    )

    fun merge() = primaryCall.value?.let { OngoingCallHelper.merge(it) }

    fun swap() {
        val secondary = secondaryCall.value
        if (secondary == null) return
        secondary.hold(false)
    }

    fun split(call: OngoingCall) = call.split()

    fun hangup(call: OngoingCall) = call.hangup()
}
