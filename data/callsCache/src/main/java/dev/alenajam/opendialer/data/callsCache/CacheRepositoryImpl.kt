package dev.alenajam.opendialer.data.callsCache

import android.app.Application
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheRepositoryImpl
@Inject constructor(private val app: Application) : CacheRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var channel: Channel<ContactInfoRequest>? = null
    private var worker: Job? = null
    private val requestDeduplicator = ContactInfoRequestDeduplicator()

    @Synchronized
    override fun start() {
        if (worker?.isActive == true) return

        val requests = Channel<ContactInfoRequest>(Channel.BUFFERED)
        channel = requests
        worker = scope.launch {
            for (request in requests) {
                attemptUpdateContactInfo(request)
            }
        }
    }

    @Synchronized
    override fun stop() {
        channel?.cancel()
        channel = null
        worker?.cancel()
        worker = null
        requestDeduplicator.clear()
    }

    private fun attemptUpdateContactInfo(request: ContactInfoRequest) {
        if (request.number === null) {
            return
        }

        /** Fetch new contact info */
        val info = CacheData.getContactInfoByNumber(
            app,
            request.number,
            request.countryIso
        )

        if (info === ContactInfo.EMPTY) {
            return
        }

        /** Update call log */
        ContactInfoHelper(app).updateCallLogContactInfo(
            request.number,
            request.countryIso,
            info,
            request.callLogInfo
        )
    }

    override fun requestUpdateContactInfo(
        number: String?,
        countryIso: String?,
        callLogInfo: ContactInfo
    ): Either<Failure, Unit> {
        if (number == null) return Either.Right(Unit)

        val requests = channel ?: return Either.Left(Failure.LocalFailure)
        if (!requestDeduplicator.markIfNew(number, countryIso)) {
            return Either.Right(Unit)
        }

        val request = ContactInfoRequest(
            number,
            countryIso,
            callLogInfo
        )
        return if (requests.trySend(request).isSuccess) {
            Either.Right(Unit)
        } else {
            requestDeduplicator.remove(number, countryIso)
            Either.Left(Failure.LocalFailure)
        }
    }

    override fun invalidate() {
        requestDeduplicator.clear()
    }
}
