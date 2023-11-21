package dev.alenajam.opendialer.features.dialer.calls.detailCall

import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.core.interactor.UseCase
import dev.alenajam.opendialer.features.dialer.DialerRepositoryImpl
import javax.inject.Inject


class BlockCaller
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<String, Unit>() {
  override suspend fun run(params: String): Either<Failure, Unit> =
    dialerRepositoryImpl.blockCaller(params)
}