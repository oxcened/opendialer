package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContactEntity
import javax.inject.Inject

@HiltViewModel
class SearchContactsViewModel
@Inject constructor(
  private val app: Application,
  private val searchContactsUseCase: SearchContacts,
  private val searchContactsDialpadUseCase: SearchContactsDialpad
) : ViewModel() {
  val result: MutableLiveData<Result> = MutableLiveData()

  fun searchContacts(query: String) =
    searchContactsUseCase(viewModelScope, SearchContactsParams(app.contentResolver, query)) {
      it.fold({}) { res -> handleResult(query, res) }
    }

  fun searchContactsDialpad(query: String) = searchContactsDialpadUseCase(
    viewModelScope,
    SearchContactsDialpadParams(app.contentResolver, query)
  ) {
    it.fold({}) { res -> handleResult(query, res) }
  }

  private fun handleResult(query: String, contacts: List<DialerSearchContactEntity>) {
    result.postValue(Result(query, DialerSearchContact.mapList(contacts)))
  }

  fun makeCall(activity: Activity, number: String) = CommonUtils.makeCall(activity, number)
  fun sendMessage(activity: Activity, number: String) = CommonUtils.makeSms(activity, number)
  fun createContact(activity: Activity, number: String) =
    CommonUtils.createContact(activity, number)

  fun addToContact(activity: Activity, number: String) =
    CommonUtils.addContactAsExisting(activity, number)

  class Result(
    val query: String,
    val contacts: List<DialerSearchContact>
  )
}