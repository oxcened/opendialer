package dev.alenajam.opendialer.features.dialer

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccount
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.functional.Event
import dev.alenajam.opendialer.core.common.platform.BaseViewModel
import dev.alenajam.opendialer.features.dialer.calls.DialerCall
import dev.alenajam.opendialer.features.dialer.calls.cache.CacheRepositoryImpl
import dev.alenajam.opendialer.features.dialer.calls.cache.ContactInfo
import dev.alenajam.opendialer.features.dialer.calls.cache.StartCache
import dev.alenajam.opendialer.features.dialer.calls.detailCall.BlockCaller
import dev.alenajam.opendialer.features.dialer.calls.detailCall.DeleteCalls
import dev.alenajam.opendialer.features.dialer.calls.detailCall.GetDetailOptions
import dev.alenajam.opendialer.features.dialer.calls.detailCall.UnblockCaller
import dev.alenajam.opendialer.features.dialer.contacts.DialerContact
import dev.alenajam.opendialer.helper.ContactsHelper
import dev.alenajam.opendialer.util.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DialerViewModel
@Inject constructor(
  dialerRepository: DialerRepositoryImpl,
  private val app: Application,
  private val getDetailOptions: GetDetailOptions,
  private val deleteCallsUseCase: DeleteCalls,
  private val blockCallerUseCase: BlockCaller,
  private val unblockCallerUseCase: UnblockCaller,
  private val cacheRepository: CacheRepositoryImpl,
  private val startCacheUseCase: StartCache
) : BaseViewModel() {
  override val TAG: String? = DialerViewModel::class.simpleName

  @ExperimentalCoroutinesApi
  val calls: LiveData<List<DialerCall>> = dialerRepository
    .getCalls(app.contentResolver)
    .map { DialerCall.mapList(it) }
    .flowOn(Dispatchers.IO)
    .asLiveData()


  @ExperimentalCoroutinesApi
  val contacts: LiveData<List<DialerContact>> = dialerRepository
    .getContacts(app.contentResolver)
    .map { DialerContact.mapList(it) }
    .flowOn(Dispatchers.IO)
    .asLiveData()

  val detailOptions: MutableLiveData<List<dev.alenajam.opendialer.model.CallOption>> =
    MutableLiveData()
  val deletedDetailCalls: MutableLiveData<Event<Unit>> = MutableLiveData()
  val blockedCaller: MutableLiveData<Event<Unit>> = MutableLiveData()
  val unblockedCaller: MutableLiveData<Event<Unit>> = MutableLiveData()

  fun getDetailOptions(call: DialerCall) =
    getDetailOptions(viewModelScope, call) { it.fold(::handleFailure, ::handleDetailOptions) }

  fun sendMessage(activity: Activity, call: DialerCall) =
    CommonUtils.makeSms(activity, call.contactInfo.number)

  fun makeCall(activity: Activity, number: String) = CommonUtils.makeCall(activity, number)
  fun callDetail(navController: NavController, call: DialerCall) = Unit
    // navController.navigate(MainFragmentDirections.actionHomeFragmentToCallDetailFragment(call))

  fun createContact(activity: Activity, call: DialerCall) =
    CommonUtils.createContact(activity, call.contactInfo.number)

  fun addToContact(activity: Activity, call: DialerCall) =
    CommonUtils.addContactAsExisting(activity, call.contactInfo.number)

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

  fun updateContactInfo(number: String?, countryIso: String?, callLogInfo: ContactInfo) {
    cacheRepository.requestUpdateContactInfo(viewModelScope, number, countryIso, callLogInfo)
  }

  fun startCache() {
    startCacheUseCase(viewModelScope, Unit) { it.fold(::handleFailure) {} }
  }

  fun stopCache() {
    cacheRepository.stop()
  }

  fun invalidateCache() {
    cacheRepository.invalidate()
  }

  private fun handleDetailOptions(options: List<dev.alenajam.opendialer.model.CallOption>) =
    detailOptions.postValue(options)

  private fun handleDeletedDetailCalls(unit: Unit) = deletedDetailCalls.postValue(Event(Unit))
  private fun handleBlockCaller(unit: Unit) = blockedCaller.postValue(Event(Unit))
  private fun handleUnblockCaller(unit: Unit) = unblockedCaller.postValue(Event(Unit))
}