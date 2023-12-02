package dev.alenajam.opendialer.data.contactsSearch

import android.app.Application
import android.content.ContentResolver
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import javax.inject.Inject

class DialerRepositoryImpl
@Inject constructor(private val app: Application) : DialerRepository {
  override suspend fun searchContacts(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>> {
    SearchContactsData.getCursor(contentResolver, query)?.let {
      val data = SearchContactsData.getData(it)
      it.close()
      return Either.Right(data)
    }

    return Either.Left(Failure.NoData)
  }

  override suspend fun searchContactsDialpad(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>> {
    SearchContactsDialpadData.getCursor(contentResolver)?.let {
      val data = SearchContactsDialpadData.getData(app, it, query)
      it.close()
      return Either.Right(data)
    }

    return Either.Left(Failure.NoData)
  }
}