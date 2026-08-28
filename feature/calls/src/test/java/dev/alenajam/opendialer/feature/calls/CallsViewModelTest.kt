package dev.alenajam.opendialer.feature.calls

import android.app.Application
import android.content.pm.PackageManager
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementRepository
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.data.calls.CallOption
import dev.alenajam.opendialer.data.calls.CallsRepository
import dev.alenajam.opendialer.data.calls.DetailCall
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.data.calls.DialerCallEntity
import dev.alenajam.opendialer.data.callsCache.CacheRepository
import dev.alenajam.opendialer.data.callsCache.ContactInfo
import dev.alenajam.opendialer.data.contacts.ContactsRepository
import dev.alenajam.opendialer.data.contacts.DialerContactEntity
import dev.alenajam.opendialer.data.contacts.DialerContactSummaryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `contact changes refresh cached info without a screen row effect`() = runTest(dispatcher) {
        val callsRepository = FakeCallsRepository(listOf(callEntity()))
        val contactsRepository = FakeContactsRepository()
        val cacheRepository = FakeCacheRepository()
        val viewModel = CallsViewModel(
            callsRepository = callsRepository,
            contactsRepository = contactsRepository,
            app = PermissionGrantedApplication(),
            cacheRepository = cacheRepository,
            callPlacementRepository = FakeCallPlacementRepository(),
            callLogPreferences = FakeCallLogPreferences(),
            updateChecker = FakeUpdateChecker(),
        )
        advanceUntilIdle()
        val invalidationsBeforeContactChange = cacheRepository.invalidations

        viewModel.startCache()
        advanceUntilIdle()
        assertEquals(1, cacheRepository.updateRequests)
        val updateRequestsBeforeContactChange = cacheRepository.updateRequests

        contactsRepository.emit(listOf(contactEntity(name = "Ada Lovelace")))
        advanceUntilIdle()

        assertEquals(invalidationsBeforeContactChange + 1, cacheRepository.invalidations)
        assertEquals(updateRequestsBeforeContactChange + 1, cacheRepository.updateRequests)
    }

    private fun callEntity() = DialerCallEntity(
        id = 1,
        number = "6505551212",
        name = null,
        date = 0,
        duration = 0,
        type = 1,
        isNew = 0,
        photoUri = null,
        countryIso = "US",
        label = null,
        lookupUri = null,
    )

    private fun contactEntity(name: String) = DialerContactEntity(
        dataId = 1,
        id = 1,
        name = name,
        starred = 0,
        photoUri = null,
        number = "6505551212",
        phoneType = 2,
        phoneLabel = null,
    )
}

private class PermissionGrantedApplication : Application() {
    override fun checkSelfPermission(permission: String): Int = PackageManager.PERMISSION_GRANTED
}

private class FakeCallsRepository(calls: List<DialerCallEntity>) : CallsRepository {
    private val calls = MutableStateFlow(calls)

    override fun getCalls(): Flow<List<DialerCallEntity>> = calls

    override suspend fun getCallByIds(
        ids: List<Int>,
    ): Either<Failure, List<DialerCallEntity>> = Either.Left(Failure.NoData)

    override suspend fun getDetailOptions(call: DialerCall): Either<Failure, List<CallOption>> =
        Either.Left(Failure.NoData)

    override suspend fun deleteCalls(calls: List<DetailCall>): Either<Failure, Unit> =
        Either.Right(Unit)

    override suspend fun blockCaller(number: String): Either<Failure, Unit> = Either.Right(Unit)

    override suspend fun unblockCaller(number: String): Either<Failure, Unit> = Either.Right(Unit)
}

private class FakeCallPlacementRepository : CallPlacementRepository {
    override fun placeCall(number: String, account: CallAccount?): CallPlacementResult =
        CallPlacementResult.Placed

    override fun placeVoicemailCall(account: CallAccount?): CallPlacementResult =
        CallPlacementResult.Placed
}

private class FakeCallLogPreferences : CallLogPreferences {
    private var favoritesExpanded = true

    override fun isFavoritesExpanded(): Boolean = favoritesExpanded

    override fun setFavoritesExpanded(expanded: Boolean) {
        favoritesExpanded = expanded
    }

    override fun getDismissedUpdateVersion(): String? = null

    override fun setDismissedUpdateVersion(version: String) = Unit
}

private class FakeUpdateChecker : UpdateChecker {
    override suspend fun getAvailableUpdate(): AppUpdate? = null
}

private class FakeContactsRepository : ContactsRepository {
    private val favoriteContacts = MutableStateFlow<List<DialerContactEntity>>(emptyList())

    override fun getContacts(): Flow<List<DialerContactSummaryEntity>> = MutableStateFlow(emptyList())

    override fun getProfileContact(): Flow<DialerContactSummaryEntity?> = MutableStateFlow(null)

    override fun getFavoriteContacts(): Flow<List<DialerContactEntity>> = favoriteContacts

    override suspend fun toggleFavorite(contactId: Int, isFavorite: Boolean) = Unit

    override suspend fun getContactNumbers(contactId: Int): List<String> = emptyList()

    override suspend fun contactExists(contactId: Int?, contactKeys: List<String>): Boolean = false

    fun emit(contacts: List<DialerContactEntity>) {
        favoriteContacts.value = contacts
    }
}

private class FakeCacheRepository : CacheRepository {
    var updateRequests = 0
    var invalidations = 0

    override fun start() = Unit

    override fun stop() = Unit

    override fun requestUpdateContactInfo(
        number: String?,
        countryIso: String?,
        callLogInfo: ContactInfo,
    ): Either<Failure, Unit> {
        updateRequests++
        return Either.Right(Unit)
    }

    override fun invalidate() {
        invalidations++
    }
}
