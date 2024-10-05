package dev.alenajam.opendialer.feature.contacts

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.contacts.ContactsRepositoryImpl
import dev.alenajam.opendialer.data.contacts.DialerContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ContactsViewModel
@Inject constructor(
  private val contactsRepository: ContactsRepositoryImpl,
  private val app: Application,
) : ViewModel() {
  private val _contacts = MutableStateFlow<List<DialerContact>>(emptyList())
  val contacts: StateFlow<List<DialerContact>> = _contacts
  private val _hasRuntimePermission = MutableStateFlow(false)
  val hasRuntimePermission: StateFlow<Boolean> = _hasRuntimePermission

  init {
    _hasRuntimePermission.value = PermissionUtils.hasContactsPermission(app)
    getContacts()
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

  fun openContact(contactId: Int) {
    CommonUtils.showContactDetail(app, contactId)
  }
}