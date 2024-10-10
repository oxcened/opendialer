package dev.alenajam.opendialer.feature.callDetail

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.data.calls.CallOption
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.data.calls.CallsRepositoryImpl
import javax.inject.Inject


class GetDetailOptions
@Inject constructor(private val callsRepositoryImpl: CallsRepositoryImpl) :
  UseCase<DialerCall, List<CallOption>>() {
  override suspend fun run(params: DialerCall): Either<Failure, List<CallOption>> =
    callsRepositoryImpl.getDetailOptions(params)
}