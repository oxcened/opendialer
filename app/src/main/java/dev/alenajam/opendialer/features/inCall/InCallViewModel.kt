package dev.alenajam.opendialer.features.inCall

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.core.platform.BaseViewModel
import dev.alenajam.opendialer.util.TelecomAdapter
import javax.inject.Inject

class InCallViewModel
@Inject constructor(callHandler: dev.alenajam.opendialer.util.CallsHandler) : BaseViewModel() {
  override val TAG = InCallViewModel::class.simpleName

  val primaryCall: MutableLiveData<dev.alenajam.opendialer.model.OngoingCall> = callHandler.primaryCall
  val secondaryCall: MutableLiveData<dev.alenajam.opendialer.model.OngoingCall> = callHandler.secondaryCall
  val calls: MutableLiveData<Map<Call, dev.alenajam.opendialer.model.OngoingCall>> = callHandler.calls
  val audioState: MutableLiveData<CallAudioState> = callHandler.audioState
  val canAddCall: MutableLiveData<Boolean> = callHandler.canAddCall

  fun hangup(call: dev.alenajam.opendialer.model.OngoingCall, message: String? = null) = call.hangup(message)
  fun answer(call: dev.alenajam.opendialer.model.OngoingCall) = call.answer()
  fun turnSpeaker() = TelecomAdapter.turnSpeaker()
  fun turnBluetooth() = TelecomAdapter.turnBluetooth()
  fun turnMute() = TelecomAdapter.turnMute()
  fun playDtmf(call: dev.alenajam.opendialer.model.OngoingCall, digit: Char) = call.playDtmf(digit)
  fun hold(call: dev.alenajam.opendialer.model.OngoingCall) = call.hold()
  fun switch() = secondaryCall.value?.hold(false)

  fun addCall(fragment: Fragment) = fragment.startActivity(
    Intent(Intent.ACTION_DIAL).putExtra(
      dev.alenajam.opendialer.features.main.MainActivity.EXTRA_KEY_ADD_CALL,
      true
    )
  )

  fun manageConference(fragment: Fragment) = fragment.findNavController()
    .navigate(R.id.action_inCallFragment_to_inCallManageConferenceFragment)

  fun merge(call: dev.alenajam.opendialer.model.OngoingCall) = dev.alenajam.opendialer.helper.OngoingCallHelper.merge(call)
}