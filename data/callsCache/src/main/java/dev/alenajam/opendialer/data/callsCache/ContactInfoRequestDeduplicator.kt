package dev.alenajam.opendialer.data.callsCache

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

internal class ContactInfoRequestDeduplicator {
    private val updatedNumbers = Collections.newSetFromMap(ConcurrentHashMap<NumberWithCountryIso, Boolean>())

    fun markIfNew(number: String?, countryIso: String?): Boolean =
        updatedNumbers.add(NumberWithCountryIso(number, countryIso))

    fun remove(number: String?, countryIso: String?) {
        updatedNumbers.remove(NumberWithCountryIso(number, countryIso))
    }

    fun clear() {
        updatedNumbers.clear()
    }
}
