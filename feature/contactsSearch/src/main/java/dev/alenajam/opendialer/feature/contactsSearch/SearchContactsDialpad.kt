package dev.alenajam.opendialer.feature.contactsSearch

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import dev.alenajam.opendialer.core.common.interactor.UseCase
import dev.alenajam.opendialer.data.contactsSearch.DialerRepositoryImpl
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContactEntity
import javax.inject.Inject

data class SearchContactsDialpadParams(val query: String)

class SearchContactsDialpad
@Inject constructor(private val dialerRepositoryImpl: DialerRepositoryImpl) :
    UseCase<SearchContactsDialpadParams, List<DialerSearchContactEntity>>() {
    override suspend fun run(params: SearchContactsDialpadParams): Either<Failure, List<DialerSearchContactEntity>> =
        dialerRepositoryImpl.searchContactsDialpad(params.query)
}
