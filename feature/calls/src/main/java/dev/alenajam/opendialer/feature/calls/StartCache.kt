package dev.alenajam.opendialer.feature.calls

import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.core.interactor.UseCase
import dev.alenajam.opendialer.data.callsCache.CacheRepositoryImpl
import javax.inject.Inject

class StartCache
@Inject constructor(private val cacheRepositoryImpl: CacheRepositoryImpl) : UseCase<Unit, Unit>() {
  override suspend fun run(params: Unit): Either<Failure, Unit> = cacheRepositoryImpl.start()
}