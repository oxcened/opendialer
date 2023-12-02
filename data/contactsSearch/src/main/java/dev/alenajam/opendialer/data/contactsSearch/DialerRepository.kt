package dev.alenajam.opendialer.data.contactsSearch

import android.content.ContentResolver
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either

interface DialerRepository {
  suspend fun searchContacts(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>>

  suspend fun searchContactsDialpad(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>>
}