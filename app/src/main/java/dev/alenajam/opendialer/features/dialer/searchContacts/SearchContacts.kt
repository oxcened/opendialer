package dev.alenajam.opendialer.features.dialer.searchContacts

import android.content.ContentResolver
import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.core.interactor.UseCase
import dev.alenajam.opendialer.features.dialer.DialerRepositoryImpl
import javax.inject.Inject


class SearchContactsParams(val contentResolver: ContentResolver, val query: String)

class SearchContacts
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<SearchContactsParams, List<DialerSearchContactEntity>>() {
  override suspend fun run(params: SearchContactsParams): Either<Failure, List<DialerSearchContactEntity>> =
    dialerRepositoryImpl.searchContacts(params.contentResolver, params.query)
}