package dev.alenajam.opendialer.feature.callDetail

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.data.calls.DetailCall
import dev.alenajam.opendialer.data.calls.DialerRepositoryImpl
import javax.inject.Inject


class DeleteCalls
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<List<DetailCall>, Unit>() {
  override suspend fun run(params: List<DetailCall>): Either<Failure, Unit> =
    dialerRepositoryImpl.deleteCalls(params)
}