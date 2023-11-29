package dev.alenajam.opendialer.features.dialer.calls.cache

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import javax.inject.Inject


class StartCache
@Inject constructor(private val cacheRepositoryImpl: CacheRepositoryImpl) : UseCase<Unit, Unit>() {
  override suspend fun run(params: Unit): Either<Failure, Unit> = cacheRepositoryImpl.start()
}