package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.aosp.SmartDialNameMatcher
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContactEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SearchContactsViewModel
@Inject constructor(
    private val app: Application,
    private val searchContactsUseCase: SearchContacts,
    private val searchContactsDialpadUseCase: SearchContactsDialpad
) : ViewModel() {
    private val _result = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = _result
    private val _hasRuntimePermission = MutableStateFlow(false)
    val hasRuntimePermission: StateFlow<Boolean> = _hasRuntimePermission
    private val _hasCallRuntimePermission = MutableStateFlow(false)
    private val hasCallRuntimePermission: StateFlow<Boolean> = _hasCallRuntimePermission

    init {
        _hasRuntimePermission.value = PermissionUtils.hasSearchPermission(app)
        _hasCallRuntimePermission.value = PermissionUtils.hasMakeCallPermission(app)
    }

    fun handleRuntimePermissionGranted(query: String) {
        _hasRuntimePermission.value = true
        searchContactsByDialpad(query)
    }

    fun handleCallRuntimePermissionGranted() {
        _hasCallRuntimePermission.value = true
    }

    fun searchContacts(query: String) {
        if (!hasRuntimePermission.value) return
        searchContactsUseCase(viewModelScope, SearchContactsParams(app.contentResolver, query)) {
            it.fold({}) { res -> handleResult(query, res) }
        }
    }

    fun searchContactsByDialpad(query: String) {
        if (!hasRuntimePermission.value) return
        searchContactsDialpadUseCase(
            viewModelScope,
            SearchContactsDialpadParams(app.contentResolver, query)
        ) {
            it.fold({}) { res -> handleResult(SmartDialNameMatcher.normalizeNumber(app, query), res) }
        }
    }

    private fun handleResult(query: String, contacts: List<DialerSearchContactEntity>) {
        _result.value = Result(query, DialerSearchContact.mapList(contacts))
    }

    fun makeCall(activity: Activity, number: String): Boolean {
        if (!hasCallRuntimePermission.value) return false
        CommonUtils.makeCall(activity, number)
        return true
    }
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