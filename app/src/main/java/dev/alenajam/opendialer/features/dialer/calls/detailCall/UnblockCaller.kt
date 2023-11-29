package dev.alenajam.opendialer.features.dialer.calls.detailCall

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.features.dialer.DialerRepositoryImpl
import javax.inject.Inject


class UnblockCaller
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<String, Unit>() {
  override suspend fun run(params: String): Either<Failure, Unit> =
    dialerRepositoryImpl.unblockCaller(params)
}