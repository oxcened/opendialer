package dev.alenajam.opendialer.core.interactor

import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class UseCaseFlow<in Params, T> {
  private var job: Job? = null

  abstract fun run(params: Params): Flow<T>

  operator fun invoke(
    scope: CoroutineScope,
    params: Params,
    onResult: (Either<Failure, T>) -> Unit,
    onCompletion: (suspend (flowCollector: FlowCollector<T>) -> Unit)? = null
  ) {
    if (job?.isActive == true) {
      return
    }

    job = scope.launch {
      withContext(Dispatchers.IO) {
        run(params)
          .catch {
            onResult(Either.Left(Failure.GenericFailure(it)))
          }
          .onCompletion {
            onCompletion?.invoke(this)
          }
          .collect {
            onResult(Either.Right(it))
          }
      }
    }
  }
}