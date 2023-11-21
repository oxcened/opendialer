package dev.alenajam.opendialer.features.dialer.calls.cache

import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.core.interactor.UseCase
import javax.inject.Inject


class StartCache
@Inject constructor(private val cacheRepositoryImpl: CacheRepositoryImpl) : UseCase<Unit, Unit>() {
  override suspend fun run(params: Unit): Either<Failure, Unit> = cacheRepositoryImpl.start()
}