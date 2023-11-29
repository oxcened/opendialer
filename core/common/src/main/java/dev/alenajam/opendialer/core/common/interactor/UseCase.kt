package dev.alenajam.opendialer.core.interactor

import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class UseCase<in Params, out T> {
  abstract suspend fun run(params: Params): Either<Failure, T>

  operator fun invoke(
    scope: CoroutineScope,
    params: Params,
    onResult: (Either<Failure, T>) -> Unit
  ) = scope.launch {
    val result = withContext(Dispatchers.IO) { run(params) }
    onResult(result)
  }
}