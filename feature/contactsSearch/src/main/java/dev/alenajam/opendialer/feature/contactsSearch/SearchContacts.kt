package dev.alenajam.opendialer.feature.contactsSearch

import android.content.ContentResolver
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.data.contactsSearch.DialerRepositoryImpl
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContactEntity
import javax.inject.Inject

class SearchContactsParams(val contentResolver: ContentResolver, val query: String)

class SearchContacts
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<SearchContactsParams, List<DialerSearchContactEntity>>() {
  override suspend fun run(params: SearchContactsParams): Either<Failure, List<DialerSearchContactEntity>> =
    dialerRepositoryImpl.searchContacts(params.contentResolver, params.query)
}