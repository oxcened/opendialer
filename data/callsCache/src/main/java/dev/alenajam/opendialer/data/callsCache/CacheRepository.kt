package dev.alenajam.opendialer.data.callsCache

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either

interface CacheRepository {
    fun start()
    fun stop()
    fun requestUpdateContactInfo(
        number: String?,
        countryIso: String?,
        callLogInfo: ContactInfo
    ): Either<Failure, Unit>

    fun invalidate()
}
