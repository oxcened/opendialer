package dev.alenajam.opendialer.feature.callDetail

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.data.calls.DialerRepositoryImpl
import javax.inject.Inject


class UnblockCaller
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<String, Unit>() {
  override suspend fun run(params: String): Either<Failure, Unit> =
    dialerRepositoryImpl.unblockCaller(params)
}