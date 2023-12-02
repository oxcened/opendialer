package dev.alenajam.opendialer.data.calls

import android.content.ContentResolver
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import kotlinx.coroutines.flow.Flow

interface DialerRepository {
  fun getCalls(contentResolver: ContentResolver): Flow<List<DialerCallEntity>>
  suspend fun getDetailOptions(call: DialerCall): Either<Failure, List<CallOption>>
  suspend fun deleteCalls(calls: List<DetailCall>): Either<Failure, Unit>
  suspend fun blockCaller(number: String): Either<Failure, Unit>
  suspend fun unblockCaller(number: String): Either<Failure, Unit>
}