package dev.alenajam.opendialer.data.contactsSearch

import android.app.Application
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import javax.inject.Inject

class DialerRepositoryImpl
@Inject constructor(private val app: Application) : DialerRepository {
    override suspend fun searchContacts(
        query: String
    ): Either<Failure, List<DialerSearchContactEntity>> {
        SearchContactsData.getCursor(app.contentResolver, query)?.use {
            val data = SearchContactsData.getData(it)
            return Either.Right(data)
        }

        return Either.Left(Failure.NoData)
    }

    override suspend fun searchContactsDialpad(
        query: String
    ): Either<Failure, List<DialerSearchContactEntity>> {
        SearchContactsDialpadData.getCursor(app.contentResolver)?.use {
            val data = SearchContactsDialpadData.getData(app, it, query)
            return Either.Right(data)
        }

        return Either.Left(Failure.NoData)
    }
}
