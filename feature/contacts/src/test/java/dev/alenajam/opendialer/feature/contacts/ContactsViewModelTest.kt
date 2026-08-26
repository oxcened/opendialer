package dev.alenajam.opendialer.feature.contacts

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
import dev.alenajam.opendialer.data.contacts.ContactsRepository
import dev.alenajam.opendialer.data.contacts.DialerContactEntity
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
class ContactsViewModelTest {
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
    fun `load contacts when permission is granted`() = runTest(dispatcher) {
        val contact = contactEntity(id = 1, name = "John Doe")
        val contactsRepository = FakeContactsRepository(listOf(contact))
        val callsRepository = FakeCallsRepository(emptyList())
        val viewModel = ContactsViewModel(
            contactsRepository = contactsRepository,
            callsRepository = callsRepository,
            app = PermissionGrantedApplication(),
            callPlacementRepository = FakeCallPlacementRepository(),
        )

        advanceUntilIdle()

        assertEquals(1, viewModel.contacts.value.size)
        assertEquals("John Doe", viewModel.contacts.value[0].name)
    }

    @Test
    fun `toggle favorite calls repository`() = runTest(dispatcher) {
        val contactsRepository = FakeContactsRepository(emptyList())
        val viewModel = ContactsViewModel(
            contactsRepository = contactsRepository,
            callsRepository = FakeCallsRepository(emptyList()),
            app = PermissionGrantedApplication(),
            callPlacementRepository = FakeCallPlacementRepository(),
        )

        viewModel.toggleFavorite(1, true)
        advanceUntilIdle()

        assertEquals(1, contactsRepository.toggledContactId)
        assertEquals(true, contactsRepository.toggledIsFavorite)
    }

    private fun contactEntity(id: Int, name: String) = DialerContactEntity(
        dataId = id.toLong(),
        id = id,
        name = name,
        starred = 0,
        photoUri = null,
        number = "123456789",
        phoneType = 2,
        phoneLabel = null,
    )
}

private class PermissionGrantedApplication : Application() {
    override fun checkSelfPermission(permission: String): Int = PackageManager.PERMISSION_GRANTED
}

private class FakeContactsRepository(contacts: List<DialerContactEntity>) : ContactsRepository {
    private val contactsFlow = MutableStateFlow(contacts)
    var toggledContactId: Int? = null
    var toggledIsFavorite: Boolean? = null

    override fun getContacts(): Flow<List<DialerContactEntity>> = contactsFlow

    override suspend fun toggleFavorite(contactId: Int, isFavorite: Boolean) {
        toggledContactId = contactId
        toggledIsFavorite = isFavorite
    }
}

private class FakeCallsRepository(calls: List<DialerCallEntity>) : CallsRepository {
    private val callsFlow = MutableStateFlow(calls)

    override fun getCalls(): Flow<List<DialerCallEntity>> = callsFlow

    override suspend fun getCallByIds(
        contentResolver: android.content.ContentResolver,
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
