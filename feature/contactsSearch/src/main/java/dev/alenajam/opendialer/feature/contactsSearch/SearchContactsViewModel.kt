package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Application
import android.app.Activity
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.aosp.SmartDialNameMatcher
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementRepository
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.data.calls.CallsRepository
import dev.alenajam.opendialer.data.calls.DialerCallEntity
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContactEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchContactsViewModel
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val app: Application,
    private val searchContactsUseCase: SearchContacts,
    private val searchContactsDialpadUseCase: SearchContactsDialpad,
    private val callsRepository: CallsRepository,
    private val callPlacementRepository: CallPlacementRepository,
) : ViewModel() {
    private val _result = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = _result
    private val _hasRuntimePermission = MutableStateFlow(false)
    val hasRuntimePermission: StateFlow<Boolean> = _hasRuntimePermission
    private val _hasCallRuntimePermission = MutableStateFlow(false)
    val hasCallRuntimePermission: StateFlow<Boolean> = _hasCallRuntimePermission
    private val _calls = MutableStateFlow<List<DialerCallEntity>>(emptyList())
    private val contactsSearch = savedStateHandle.toRoute<ContactsSearchRoute>()
    val prefilledNumber = contactsSearch.prefilledNumber
    private var callsJob: Job? = null

    init {
        _hasRuntimePermission.value = PermissionUtils.hasSearchPermission(app)
        _hasCallRuntimePermission.value = PermissionUtils.hasMakeCallPermission(app)
        searchContactsByDialpad(prefilledNumber)
        getCalls()
    }

    private fun getCalls() {
        if (!PermissionUtils.hasRecentsPermission(app)) return
        callsJob?.cancel()
        callsJob = viewModelScope.launch {
            callsRepository.getCalls().collect { _calls.value = it }
        }
    }

    fun handleRuntimePermissionGranted(query: String) {
        _hasRuntimePermission.value = true
        searchContactsByDialpad(query)
        getCalls()
    }

    fun handleTextSearchPermissionGranted(query: String) {
        _hasRuntimePermission.value = true
        searchContacts(query)
        getCalls()
    }

    fun handleCallRuntimePermissionGranted() {
        _hasCallRuntimePermission.value = true
    }

    fun searchContactsByDialpad(query: String) {
        if (!hasRuntimePermission.value) return
        searchContactsDialpadUseCase(
            viewModelScope,
            SearchContactsDialpadParams(app.contentResolver, query)
        ) {
            it.fold({}) { res ->
                handleResult(
                    SmartDialNameMatcher.normalizeNumber(app, query),
                    res
                )
            }
        }
    }

    fun searchContacts(query: String) {
        if (!hasRuntimePermission.value) return
        searchContactsUseCase(
            viewModelScope,
            SearchContactsParams(app.contentResolver, query)
        ) {
            it.fold({}) { contacts -> handleResult(query, contacts) }
        }
    }

    private fun handleResult(query: String, contacts: List<DialerSearchContactEntity>) {
        _result.value = Result(query, DialerSearchContact.mapList(contacts))
    }

    fun makeCall(number: String): CallPlacementResult = callPlacementRepository.placeCall(number)

    fun makeCall(number: String, account: CallAccount): CallPlacementResult =
        callPlacementRepository.placeCall(number, account)

    fun sendMessage(activity: Activity, number: String) = CommonUtils.makeSms(activity, number)
    fun createContact(activity: Activity, number: String) =
        CommonUtils.createContact(activity, number)

    fun addToContact(activity: Activity, number: String) =
        CommonUtils.addContactAsExisting(activity, number)

    fun openContact(activity: Activity, contactId: Int) {
        CommonUtils.showContactDetail(activity, contactId)
    }

    fun getHistoryIds(number: String): List<Int> = _calls.value
        .filter { PhoneNumberUtils.compare(it.number, number) }
        .map { it.id }

    class Result(
        val query: String,
        val contacts: List<DialerSearchContact>
    )
}
