package dev.alenajam.opendialer.data.contactsSearch

import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either

interface DialerRepository {
    suspend fun searchContacts(
        query: String
    ): Either<Failure, List<DialerSearchContactEntity>>

    suspend fun searchContactsDialpad(
        query: String
    ): Either<Failure, List<DialerSearchContactEntity>>
}
