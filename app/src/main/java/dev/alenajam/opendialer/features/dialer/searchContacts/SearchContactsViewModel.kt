package dev.alenajam.opendialer.features.dialer.searchContacts

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.alenajam.opendialer.core.platform.BaseViewModel
import javax.inject.Inject

class SearchContactsViewModel
@Inject constructor(
  private val app: dev.alenajam.opendialer.App,
  private val searchContactsUseCase: SearchContacts,
  private val searchContactsDialpadUseCase: SearchContactsDialpad
) : BaseViewModel() {
  override val TAG = SearchContactsViewModel::class.simpleName

  val result: MutableLiveData<Result> = MutableLiveData()

  fun searchContacts(query: String) =
    searchContactsUseCase(viewModelScope, SearchContactsParams(app.contentResolver, query)) {
      it.fold(::handleFailure) { res -> handleResult(query, res) }
    }

  fun searchContactsDialpad(query: String) = searchContactsDialpadUseCase(
    viewModelScope,
    SearchContactsDialpadParams(app.contentResolver, query)
  ) {
    it.fold(::handleFailure) { res -> handleResult(query, res) }
  }

  private fun handleResult(query: String, contacts: List<DialerSearchContactEntity>) {
    result.postValue(Result(query, DialerSearchContact.mapList(contacts)))
  }

  fun makeCall(activity: Activity, number: String) = dev.alenajam.opendialer.util.CommonUtils.makeCall(activity, number)
  fun sendMessage(activity: Activity, number: String) = dev.alenajam.opendialer.util.CommonUtils.makeSms(activity, number)
  fun createContact(activity: Activity, number: String) =
    dev.alenajam.opendialer.util.CommonUtils.createContact(activity, number)

  fun addToContact(activity: Activity, number: String) =
    dev.alenajam.opendialer.util.CommonUtils.addContactAsExisting(activity, number)

  class Result(
    val query: String,
    val contacts: List<DialerSearchContact>
  )
}