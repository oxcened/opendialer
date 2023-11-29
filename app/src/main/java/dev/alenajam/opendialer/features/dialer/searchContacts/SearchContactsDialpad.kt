package dev.alenajam.opendialer.features.dialer.searchContacts

import android.content.ContentResolver
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.features.dialer.DialerRepositoryImpl
import javax.inject.Inject


class SearchContactsDialpadParams(val contentResolver: ContentResolver, val query: String)

class SearchContactsDialpad
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
  UseCase<SearchContactsDialpadParams, List<DialerSearchContactEntity>>() {
  override suspend fun run(params: SearchContactsDialpadParams): Either<Failure, List<DialerSearchContactEntity>> =
    dialerRepositoryImpl.searchContactsDialpad(params.contentResolver, params.query)
}