package dev.alenajam.opendialer.feature.calls

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.ContactsHelper
import dev.alenajam.opendialer.core.common.navigateToCallDetail
import dev.alenajam.opendialer.data.calls.ContactInfo
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.data.calls.DialerRepositoryImpl
import dev.alenajam.opendialer.data.callsCache.CacheRepositoryImpl
import dev.alenajam.opendialer.data.contacts.DialerContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DialerViewModel
@Inject constructor(
  dialerRepository: DialerRepositoryImpl,
  contactsRepository: dev.alenajam.opendialer.data.contacts.DialerRepositoryImpl,
  private val app: Application,
  private val cacheRepository: CacheRepositoryImpl,
  private val startCacheUseCase: StartCache
) : ViewModel() {
  val calls: LiveData<List<DialerCall>> = dialerRepository
    .getCalls(app.contentResolver)
    .map { DialerCall.mapList(it) }
    .flowOn(Dispatchers.IO)
    .asLiveData()

  val contacts: LiveData<List<DialerContact>> = contactsRepository
    .getContacts(app.contentResolver)
    .map { DialerContact.mapList(it) }
    .flowOn(Dispatchers.IO)
    .asLiveData()

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

  fun updateContactInfo(number: String?, countryIso: String?, callLogInfo: ContactInfo) {
    cacheRepository.requestUpdateContactInfo(
      viewModelScope,
      number,
      countryIso,
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
  }
}