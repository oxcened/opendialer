package dev.alenajam.opendialer.feature.contacts

import android.app.Application
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.calls.CallsRepositoryImpl
import dev.alenajam.opendialer.data.calls.DialerCallEntity
import dev.alenajam.opendialer.data.contacts.ContactsRepositoryImpl
import dev.alenajam.opendialer.data.contacts.DialerContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel
@Inject constructor(
    private val contactsRepository: ContactsRepositoryImpl,
    private val callsRepository: CallsRepositoryImpl,
    private val app: Application,
) : ViewModel() {
    private val _contacts = MutableStateFlow<List<DialerContact>>(emptyList())
    val contacts: StateFlow<List<DialerContact>> = _contacts
    private val _hasRuntimePermission = MutableStateFlow(false)
    val hasRuntimePermission: StateFlow<Boolean> = _hasRuntimePermission
    private val _calls = MutableStateFlow<List<DialerCallEntity>>(emptyList())
    private var hasCallRuntimePermission = false

    init {
        _hasRuntimePermission.value = PermissionUtils.hasContactsPermission(app)
        hasCallRuntimePermission = PermissionUtils.hasMakeCallPermission(app)
        getContacts()
        getCalls()
    }

    fun getContacts() {
        if (!hasRuntimePermission.value) return

        viewModelScope.launch {
            contactsRepository.getContacts().collect { contacts ->
                _contacts.value = DialerContact.mapList(contacts)
            }
        }
    }

    fun handleRuntimePermissionGranted() {
        _hasRuntimePermission.value = true
        getContacts()
    }

    private fun getCalls() {
        if (!PermissionUtils.hasRecentsPermission(app)) return
        viewModelScope.launch {
            callsRepository.getCalls().collect { _calls.value = it }
        }
    }

    fun makeCall(number: String): Boolean {
        if (!hasCallRuntimePermission) return false
        CommonUtils.makeCall(app, number)
        return true
    }

    fun handleCallRuntimePermissionGranted() {
        hasCallRuntimePermission = true
    }

    fun sendMessage(number: String) = CommonUtils.makeSms(app, number)

    fun getHistoryIds(number: String): List<Int> = _calls.value
        .filter { PhoneNumberUtils.compare(it.number, number) }
        .map { it.id }

    fun openContact(contactId: Int) {
        CommonUtils.showContactDetail(app, contactId)
    }
}
