package dev.alenajam.opendialer.feature.inCall.ui

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.platform.BaseViewModel
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.service.CallsHandler
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall
import dev.alenajam.opendialer.feature.inCall.service.OngoingCallHelper
import dev.alenajam.opendialer.feature.inCall.service.TelecomAdapter
import javax.inject.Inject

@HiltViewModel
class InCallViewModel
@Inject constructor(callHandler: CallsHandler) : BaseViewModel() {
  override val TAG = InCallViewModel::class.simpleName

  val primaryCall: MutableLiveData<OngoingCall> = callHandler.primaryCall
  val secondaryCall: MutableLiveData<OngoingCall> = callHandler.secondaryCall
  val calls: MutableLiveData<Map<Call, OngoingCall>> = callHandler.calls
  val audioState: MutableLiveData<CallAudioState> = callHandler.audioState
  val canAddCall: MutableLiveData<Boolean> = callHandler.canAddCall

  fun hangup(call: OngoingCall, message: String? = null) = call.hangup(message)
  fun answer(call: OngoingCall) = call.answer()
  fun turnSpeaker() = TelecomAdapter.turnSpeaker()
  fun turnBluetooth() = TelecomAdapter.turnBluetooth()
  fun turnMute() = TelecomAdapter.turnMute()
  fun playDtmf(call: OngoingCall, digit: Char) = call.playDtmf(digit)
  fun hold(call: OngoingCall) = call.hold()
  fun switch() = secondaryCall.value?.hold(false)

  fun addCall(fragment: Fragment) = fragment.startActivity(
    Intent(Intent.ACTION_DIAL).putExtra(
      "add_call",
      true
    )
  )

  fun manageConference(fragment: Fragment) = fragment.findNavController()
    .navigate(R.id.action_inCallFragment_to_inCallManageConferenceFragment)

  fun merge(call: OngoingCall) = OngoingCallHelper.merge(call)
}