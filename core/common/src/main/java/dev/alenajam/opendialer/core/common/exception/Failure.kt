package dev.alenajam.opendialer.core.common.exception

sealed class Failure(var source: String? = null) {
  class GenericFailure(val throwable: Throwable? = null, source: String? = null) : Failure(source)
  object NoData : Failure()
  object NotPermitted : Failure()
  object LocalFailure : Failure()
}