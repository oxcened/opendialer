package dev.alenajam.opendialer.feature.calls

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.ContactsHelper
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementRepository
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.data.calls.CallsRepository
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.data.callsCache.CacheRepository
import dev.alenajam.opendialer.data.contacts.ContactsRepository
import dev.alenajam.opendialer.data.contacts.DialerContact
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallsViewModel
@Inject constructor(
    private val callsRepository: CallsRepository,
    private val contactsRepository: ContactsRepository,
    private val app: Application,
    private val cacheRepository: CacheRepository,
    private val callPlacementRepository: CallPlacementRepository,
    private val callLogPreferences: CallLogPreferences,
    private val updateChecker: UpdateChecker,
) : ViewModel() {
    private val _calls = MutableStateFlow<List<DialerCall>>(emptyList())
    val calls: StateFlow<List<DialerCall>> = _calls
    private val _favorites = MutableStateFlow<List<DialerContact>>(emptyList())
    val favorites: StateFlow<List<DialerContact>> = _favorites.asStateFlow()
    private val _favoritesExpanded = MutableStateFlow(callLogPreferences.isFavoritesExpanded())
    val favoritesExpanded: StateFlow<Boolean> = _favoritesExpanded.asStateFlow()
    private val _hasRuntimePermission = MutableStateFlow(false)
    val hasRuntimePermission: StateFlow<Boolean> = _hasRuntimePermission
    private val _availableUpdate = MutableStateFlow<AppUpdate?>(null)
    val availableUpdate: StateFlow<AppUpdate?> = _availableUpdate.asStateFlow()
    private var hasContactsRuntimePermission = false
    private var isCacheRunning = false
    private var callsJob: Job? = null
    private var contactsJob: Job? = null

    init {
        _hasRuntimePermission.value = PermissionUtils.hasRecentsPermission(app)
        hasContactsRuntimePermission = PermissionUtils.hasContactsPermission(app)
        getCalls()
        getContacts()
        checkForUpdate()
    }

    fun getCalls() {
        if (!hasRuntimePermission.value) return

        callsJob?.cancel()
        callsJob = viewModelScope.launch {
            callsRepository.getCalls().collect { calls ->
                val dialerCalls = DialerCall.mapList(calls)
                _calls.value = dialerCalls
                if (isCacheRunning) refreshContactInfo(dialerCalls)
            }
        }
    }

    fun getContacts() {
        if (!hasContactsRuntimePermission) return

        contactsJob?.cancel()
        contactsJob = viewModelScope.launch {
            contactsRepository.getFavoriteContacts().collect { contacts ->
                _favorites.value = DialerContact.mapList(contacts).filter { it.starred }
                cacheRepository.invalidate()
                if (isCacheRunning) refreshContactInfo()
            }
        }
    }

    fun handleRuntimePermissionGranted() {
        _hasRuntimePermission.value = true
        getCalls()
    }

    fun sendMessage(number: String) =
        CommonUtils.makeSms(app, number)

    fun makeCall(number: String): CallPlacementResult = callPlacementRepository.placeCall(number)

    fun makeCall(number: String, account: CallAccount): CallPlacementResult =
        callPlacementRepository.placeCall(number, account)
    fun copyNumber(number: String) = CommonUtils.copyToClipobard(app, number)

    fun blockNumber(number: String) {
        viewModelScope.launch { callsRepository.blockCaller(number) }
    }

    fun deleteCall(call: DialerCall) {
        viewModelScope.launch { callsRepository.deleteCalls(call.childCalls) }
    }

    fun addToContact(number: String) =
        CommonUtils.addContactAsExisting(app, number)

    fun openContact(call: DialerCall) {
        ContactsHelper.getContactByPhoneNumber(app, call.contactInfo.number)?.let {
            CommonUtils.showContactDetail(app, it.id)
        }
    }

    fun unstarContact(contactId: Int) {
        viewModelScope.launch {
            contactsRepository.toggleFavorite(contactId, false)
        }
    }

    fun toggleFavoritesExpanded() {
        _favoritesExpanded.value = !_favoritesExpanded.value
        callLogPreferences.setFavoritesExpanded(_favoritesExpanded.value)
    }

    fun dismissUpdate() {
        _availableUpdate.value?.let { update ->
            callLogPreferences.setDismissedUpdateVersion(update.version)
        }
        _availableUpdate.value = null
    }

    fun openUpdate() {
        val update = _availableUpdate.value ?: return
        runCatching {
            app.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    fun startCache() {
        cacheRepository.start()
        isCacheRunning = true
        refreshContactInfo()
    }

    fun stopCache() {
        isCacheRunning = false
        cacheRepository.stop()
    }

    private fun refreshContactInfo(calls: List<DialerCall> = _calls.value) {
        if (!hasContactsRuntimePermission) return

        calls.filterNot(DialerCall::isAnonymous).forEach { call ->
            val callLogInfo = call.contactInfo
            cacheRepository.requestUpdateContactInfo(
                number = call.number,
                countryIso = call.countryIso,
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
                    photoId = callLogInfo.photoId,
                ),
            )
        }
    }

    private fun checkForUpdate() {
        val cachedUpdate = callLogPreferences.getCachedAvailableUpdate()
        if (cachedUpdate != null && isNewerThanInstalledVersion(cachedUpdate)) {
            showUpdateUnlessDismissed(cachedUpdate)
        }
        if (!updateChecker.bypassCache &&
            System.currentTimeMillis() - callLogPreferences.getUpdateLastCheckMillis() < UPDATE_CHECK_INTERVAL_MILLIS
        ) {
            return
        }

        viewModelScope.launch {
            when (val result = updateChecker.checkForUpdate(callLogPreferences.getUpdateEtag())) {
                is UpdateCheckResult.Success -> {
                    callLogPreferences.setUpdateLastCheckMillis(System.currentTimeMillis())
                    callLogPreferences.setUpdateEtag(result.etag)
                    callLogPreferences.setCachedAvailableUpdate(result.update)
                    result.update?.let(::showUpdateUnlessDismissed)
                }
                UpdateCheckResult.NotModified -> {
                    callLogPreferences.setUpdateLastCheckMillis(System.currentTimeMillis())
                }
                UpdateCheckResult.Failed -> Unit
            }
        }
    }

    private fun isNewerThanInstalledVersion(update: AppUpdate): Boolean {
        val installedVersion = runCatching {
            app.packageManager.getPackageInfo(app.packageName, 0).versionName
        }.getOrNull() ?: return false
        val updateSemanticVersion = update.version.toSemanticVersionOrNull() ?: return false
        val installedSemanticVersion = installedVersion.toSemanticVersionOrNull(allowSuffix = true)
            ?: return false
        return updateSemanticVersion > installedSemanticVersion
    }

    private fun showUpdateUnlessDismissed(update: AppUpdate) {
        if (callLogPreferences.getDismissedUpdateVersion() != update.version) {
            _availableUpdate.value = update
        }
    }

    private companion object {
        const val UPDATE_CHECK_INTERVAL_MILLIS = 24 * 60 * 60 * 1_000L
    }
}
