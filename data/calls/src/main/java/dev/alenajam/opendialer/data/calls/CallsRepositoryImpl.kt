package dev.alenajam.opendialer.data.calls

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.BlockedNumberContract
import dev.alenajam.opendialer.core.common.DefaultPhoneManager
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallsRepositoryImpl @Inject constructor(
    private val app: Application,
    private val voicemailNumberProvider: VoicemailNumberProvider,
    private val defaultPhoneManager: DefaultPhoneManager,
) : CallsRepository {
    override fun getCalls(): Flow<List<DialerCallEntity>> =
        callbackFlow {
            val callsUri = CallsData.getUri(app)
            fun getCallsData(): List<DialerCallEntity>? =
                CallsData.getCursor(app.contentResolver, callsUri)?.use {
                    CallsData.getData(
                        cursor = it,
                        voicemailNumbers = voicemailNumberProvider.getNumbers(),
                        contactPhoneTypes = getContactPhoneTypes(app.contentResolver),
                    )
                }
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    getCallsData()?.let(::trySend)
                }
            }

            app.contentResolver.registerContentObserver(callsUri, true, observer)

            getCallsData()?.let(::trySend)

            awaitClose {
                app.contentResolver.unregisterContentObserver(observer)
            }
        }

    override suspend fun getCallByIds(
        ids: List<Int>
    ): Either<Failure, List<DialerCallEntity>> {
        val selectedCalls = CallDetailData.getCursor(app.contentResolver, ids)?.use { cursor ->
            CallDetailData.getData(
                cursor = cursor,
                voicemailNumbers = voicemailNumberProvider.getNumbers(),
                contactPhoneTypes = getContactPhoneTypes(app.contentResolver),
            )
        } ?: return Either.Left(Failure.NoData)

        val numbers = selectedCalls.mapNotNull { it.number }.distinct()
        val data = CallDetailData.getCursorForNumbers(app.contentResolver, numbers)?.use { cursor ->
            CallDetailData.getData(
                cursor = cursor,
                voicemailNumbers = voicemailNumberProvider.getNumbers(),
                contactPhoneTypes = getContactPhoneTypes(app.contentResolver),
            )
        } ?: selectedCalls

        return if (data.isEmpty()) {
            Either.Left(Failure.NoData)
        } else {
            Either.Right(data)
        }
    }

    override suspend fun getDetailOptions(call: DialerCall): Either<Failure, List<CallOption>> {
        return with(app) {
            val options = mutableListOf<CallOption>()

            if (!call.isAnonymous()) {
                options.addAll(
                    listOf(
                        CallOption(CallOption.ID_COPY_NUMBER, 0),
                        CallOption(
                            CallOption.ID_EDIT_BEFORE_CALL,
                            0
                        )
                    )
                )
            }

            options.add(
                CallOption(
                    CallOption.ID_DELETE,
                    0
                )
            )

            if (!call.isAnonymous()) {
                val hasDefault = defaultPhoneManager.isDefaultDialer()
                val canUserBlockNumbers = BlockedNumberContract.canCurrentUserBlockNumbers(this)
                if (hasDefault && canUserBlockNumbers) {
                    val isBlocked =
                        BlockedNumbersData.isBlocked(this, call.contactInfo.number)
                    val blockOption = CallOption(
                        if (isBlocked) CallOption.ID_UNBLOCK_CALLER else CallOption.ID_BLOCK_CALLER,
                        0
                    )
                    options.add(blockOption)
                }
            }

            Either.Right(options)
        }
    }

    override suspend fun deleteCalls(calls: List<DetailCall>): Either<Failure, Unit> {
        if (!PermissionUtils.hasRecentsPermission(app)) {
            return Either.Left(Failure.NotPermitted)
        }

        var deleted = 0
        calls.forEach {
            deleted += CallsData.delete(app.contentResolver, it.id)
        }

        return if (deleted > 0) Either.Right(Unit) else Either.Left(Failure.LocalFailure)
    }

    override suspend fun blockCaller(number: String): Either<Failure, Unit> {
        val uri = BlockedNumbersData.insert(app.contentResolver, number)
        return if (uri == null) Either.Left(Failure.LocalFailure) else Either.Right(Unit)
    }

    override suspend fun unblockCaller(number: String): Either<Failure, Unit> {
        val blocked = BlockedNumbersData.unblock(app, number)
        return if (blocked < 1) Either.Left(Failure.LocalFailure) else Either.Right(Unit)
    }
}
