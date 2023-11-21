package dev.alenajam.opendialer.features.dialer

import android.content.ContentResolver
import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.features.dialer.calls.DialerCall
import dev.alenajam.opendialer.features.dialer.calls.DialerCallEntity
import dev.alenajam.opendialer.features.dialer.calls.detailCall.DetailCall
import dev.alenajam.opendialer.features.dialer.contacts.DialerContactEntity
import dev.alenajam.opendialer.features.dialer.searchContacts.DialerSearchContactEntity
import kotlinx.coroutines.flow.Flow

interface DialerRepository {
  fun getCalls(contentResolver: ContentResolver): Flow<List<DialerCallEntity>>
  fun getContacts(contentResolver: ContentResolver): Flow<List<DialerContactEntity>>
  suspend fun searchContacts(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>>

  suspend fun searchContactsDialpad(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>>

  suspend fun getDetailOptions(call: DialerCall): Either<Failure, List<dev.alenajam.opendialer.model.CallOption>>
  suspend fun deleteCalls(calls: List<DetailCall>): Either<Failure, Unit>
  suspend fun blockCaller(number: String): Either<Failure, Unit>
  suspend fun unblockCaller(number: String): Either<Failure, Unit>
}