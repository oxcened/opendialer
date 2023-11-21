package dev.alenajam.opendialer.features.dialer.calls.detailCall

import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.core.interactor.UseCase
import dev.alenajam.opendialer.features.dialer.DialerRepositoryImpl
import dev.alenajam.opendialer.features.dialer.calls.DialerCall
import javax.inject.Inject


class GetDetailOptions
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<DialerCall, List<dev.alenajam.opendialer.model.CallOption>>() {
  override suspend fun run(params: DialerCall): Either<Failure, List<dev.alenajam.opendialer.model.CallOption>> =
    dialerRepositoryImpl.getDetailOptions(params)
}