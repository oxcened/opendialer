package dev.alenajam.opendialer.feature.callDetail

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.data.calls.CallsRepositoryImpl
import javax.inject.Inject


class UnblockCaller
@Inject constructor(private val callsRepositoryImpl: CallsRepositoryImpl) :
  UseCase<String, Unit>() {
  override suspend fun run(params: String): Either<Failure, Unit> =
    callsRepositoryImpl.unblockCaller(params)
}