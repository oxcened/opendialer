package dev.alenajam.opendialer.data.callsCache

import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import kotlinx.coroutines.CoroutineScope

interface CacheRepository {
  suspend fun start(): Either<Failure, Unit>
  fun stop()
  fun requestUpdateContactInfo(
    coroutineScope: CoroutineScope,
    number: String?,
    countryIso: String?,
    callLogInfo: ContactInfo
  ): Either<Failure, Unit>

  fun invalidate()
}