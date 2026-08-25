package dev.alenajam.opendialer.data.callsCache

internal class ContactInfoRequestDeduplicator {
    private val updatedNumbers = mutableSetOf<NumberWithCountryIso>()

    fun markIfNew(number: String?, countryIso: String?): Boolean =
        updatedNumbers.add(NumberWithCountryIso(number, countryIso))

    fun remove(number: String?, countryIso: String?) {
        updatedNumbers.remove(NumberWithCountryIso(number, countryIso))
    }

    fun clear() {
        updatedNumbers.clear()
    }
}
