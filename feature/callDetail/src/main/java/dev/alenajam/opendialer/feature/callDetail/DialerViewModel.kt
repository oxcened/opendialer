package dev.alenajam.opendialer.feature.callDetail

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccount
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.ContactsHelper
import dev.alenajam.opendialer.core.common.functional.Event
import dev.alenajam.opendialer.core.common.platform.BaseViewModel
import dev.alenajam.opendialer.data.calls.CallOption
import dev.alenajam.opendialer.data.calls.DialerCall
import javax.inject.Inject

@HiltViewModel
class DialerViewModel
@Inject constructor(
  private val app: Application,
  private val getDetailOptions: GetDetailOptions,
  private val deleteCallsUseCase: DeleteCalls,
  private val blockCallerUseCase: BlockCaller,
  private val unblockCallerUseCase: UnblockCaller
) : BaseViewModel() {
  override val TAG: String? = DialerViewModel::class.simpleName

  val detailOptions: MutableLiveData<List<CallOption>> =
    MutableLiveData()
  val deletedDetailCalls: MutableLiveData<Event<Unit>> = MutableLiveData()
  val blockedCaller: MutableLiveData<Event<Unit>> = MutableLiveData()
  val unblockedCaller: MutableLiveData<Event<Unit>> = MutableLiveData()

  fun getDetailOptions(call: DialerCall) =
    getDetailOptions(viewModelScope, call) { it.fold(::handleFailure, ::handleDetailOptions) }

  fun makeCall(activity: Activity, number: String) = CommonUtils.makeCall(activity, number)

  fun copyNumber(call: DialerCall) = CommonUtils.copyToClipobard(app, call.contactInfo.number)
  fun openContact(activity: Activity, call: DialerCall) {
    ContactsHelper.getContactByPhoneNumber(activity, call.contactInfo.number)?.let {
      CommonUtils.showContactDetail(activity, it.id)
    }
  }

  fun editNumberBeforeCall(activity: Activity, call: DialerCall) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.fromParts(PhoneAccount.SCHEME_TEL, call.number, null)
    }
    activity.startActivity(intent)
  }

  fun deleteCalls(call: DialerCall) = deleteCallsUseCase(viewModelScope, call.childCalls) {
    it.fold(
      ::handleFailure,
      ::handleDeletedDetailCalls
    )
  }

  fun blockCaller(call: DialerCall) = call.contactInfo.number?.let {
    blockCallerUseCase(
      viewModelScope,
      it
    ) { res -> res.fold(::handleFailure, ::handleBlockCaller) }
  }

  fun unblockCaller(call: DialerCall) = call.contactInfo.number?.let {
    unblockCallerUseCase(
      viewModelScope,
      it
    ) { res -> res.fold(::handleFailure, ::handleUnblockCaller) }
  }

  private fun handleDetailOptions(options: List<CallOption>) =
    detailOptions.postValue(options)

  private fun handleDeletedDetailCalls(unit: Unit) = deletedDetailCalls.postValue(Event(Unit))
  private fun handleBlockCaller(unit: Unit) = blockedCaller.postValue(Event(Unit))
  private fun handleUnblockCaller(unit: Unit) = unblockedCaller.postValue(Event(Unit))
}