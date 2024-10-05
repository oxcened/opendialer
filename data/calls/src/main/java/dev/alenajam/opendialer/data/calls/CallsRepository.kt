package dev.alenajam.opendialer.data.calls

import android.content.ContentResolver
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import kotlinx.coroutines.flow.Flow

interface CallsRepository {
  fun getCalls(): Flow<List<DialerCallEntity>>
  suspend fun getCallByIds(contentResolver: ContentResolver, ids: List<Int>): Either<Failure, List<DialerCallEntity>>
  suspend fun getDetailOptions(call: DialerCall): Either<Failure, List<CallOption>>
  suspend fun deleteCalls(calls: List<DetailCall>): Either<Failure, Unit>
  suspend fun blockCaller(number: String): Either<Failure, Unit>
  suspend fun unblockCaller(number: String): Either<Failure, Unit>
}