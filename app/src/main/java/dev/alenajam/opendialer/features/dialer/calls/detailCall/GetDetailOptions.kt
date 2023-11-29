package dev.alenajam.opendialer.features.dialer.calls.detailCall

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.features.dialer.DialerRepositoryImpl
import dev.alenajam.opendialer.features.dialer.calls.DialerCall
import dev.alenajam.opendialer.model.CallOption
import javax.inject.Inject


class GetDetailOptions
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<DialerCall, List<CallOption>>() {
  override suspend fun run(params: DialerCall): Either<Failure, List<CallOption>> =
    dialerRepositoryImpl.getDetailOptions(params)
}