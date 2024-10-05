package dev.alenajam.opendialer.feature.calls

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.ContactsHelper
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.navigateToCallDetail
import dev.alenajam.opendialer.data.calls.CallsRepositoryImpl
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.data.callsCache.CacheRepositoryImpl
import dev.alenajam.opendialer.data.contacts.ContactsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CallsViewModel
@Inject constructor(
  private val callsRepository: CallsRepositoryImpl,
  private val contactsRepository: ContactsRepositoryImpl,
  private val app: Application,
  private val cacheRepository: CacheRepositoryImpl,
  private val startCacheUseCase: StartCache
) : ViewModel() {
  private val _calls = MutableStateFlow<List<DialerCall>>(emptyList())
  val calls: StateFlow<List<DialerCall>> = _calls
  private val _hasRuntimePermission = MutableStateFlow(false)
  val hasRuntimePermission: StateFlow<Boolean> = _hasRuntimePermission
  private val _lastInvalidateCache = MutableStateFlow<LocalDateTime?>(null)
  val lastInvalidateCache: StateFlow<LocalDateTime?> = _lastInvalidateCache
  private var shouldInvalidateCache = false
  private var hasContactsRuntimePermission = false

  init {
    _hasRuntimePermission.value = PermissionUtils.hasRecentsPermission(app)
    hasContactsRuntimePermission = PermissionUtils.hasContactsPermission(app)
    getCalls()
    getContacts()
  }

  fun getCalls() {
    if (!hasRuntimePermission.value) return

    viewModelScope.launch {
      callsRepository.getCalls().collect { calls ->
        _calls.value = DialerCall.mapList(calls)
      }
    }
  }

  fun getContacts() {
    if (!hasContactsRuntimePermission) return

    viewModelScope.launch {
      contactsRepository.getContacts().collect { shouldInvalidateCache = true }
    }
  }

  fun handleRuntimePermissionGranted() {
    _hasRuntimePermission.value = true
    getCalls()
  }

  fun sendMessage(activity: Activity, call: DialerCall) =
    CommonUtils.makeSms(activity, call.contactInfo.number)
  fun sendMessage(number: String) =
    CommonUtils.makeSms(app, number)

  fun makeCall(activity: Activity, number: String) = CommonUtils.makeCall(activity, number)
  fun makeCall(number: String) = CommonUtils.makeCall(app, number)

  fun callDetail(navController: NavController, call: DialerCall) {
    navController.navigateToCallDetail(call.childCalls.map { it.id })
  }

  fun createContact(activity: Activity, call: DialerCall) =
    CommonUtils.createContact(activity, call.contactInfo.number)

  fun addToContact(activity: Activity, call: DialerCall) =
    CommonUtils.addContactAsExisting(activity, call.contactInfo.number)
  fun addToContact(number: String) =
    CommonUtils.addContactAsExisting(app, number)

  fun openContact(activity: Activity, call: DialerCall) {
    ContactsHelper.getContactByPhoneNumber(activity, call.contactInfo.number)?.let {
      CommonUtils.showContactDetail(activity, it.id)
    }
  }

  fun updateContactInfo(call: DialerCall) {
    if (call.isAnonymous() || !hasContactsRuntimePermission) {
      return
    }

    val callLogInfo = call.contactInfo

    cacheRepository.requestUpdateContactInfo(
      viewModelScope,
      call.number,
      call.countryIso,
      callLogInfo = dev.alenajam.opendialer.data.callsCache.ContactInfo(
        name = callLogInfo.name,
        number = callLogInfo.number,
        photoUri = callLogInfo.photoUri,
        type = callLogInfo.type,
        label = callLogInfo.label,
        lookupUri = callLogInfo.lookupUri,
        normalizedNumber = callLogInfo.normalizedNumber,
        formattedNumber = callLogInfo.formattedNumber,
        geoDescription = callLogInfo.geoDescription,
        photoId = callLogInfo.photoId
      )
    )
  }

  fun startCache() {
    startCacheUseCase(viewModelScope, Unit) { /* TODO handle failure */ }
  }

  fun stopCache() {
    cacheRepository.stop()
  }

  fun invalidateCache() {
    cacheRepository.invalidate()
    _lastInvalidateCache.value = LocalDateTime.now()
  }

  fun attemptInvalidateCache() {
    if (shouldInvalidateCache) {
      invalidateCache()
      shouldInvalidateCache = false
    }
  }
}