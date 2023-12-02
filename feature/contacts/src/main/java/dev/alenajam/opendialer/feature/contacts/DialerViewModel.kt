package dev.alenajam.opendialer.feature.contacts

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.platform.BaseViewModel
import dev.alenajam.opendialer.data.contacts.DialerContact
import dev.alenajam.opendialer.data.contacts.DialerRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DialerViewModel
@Inject constructor(
  dialerRepository: DialerRepositoryImpl,
  app: Application,
) : BaseViewModel() {
  override val TAG: String? = DialerViewModel::class.simpleName

  val contacts: LiveData<List<DialerContact>> = dialerRepository
    .getContacts(app.contentResolver)
    .map { DialerContact.mapList(it) }
    .flowOn(Dispatchers.IO)
    .asLiveData()
}