package dev.alenajam.opendialer.features.dialer.calls.cache

open class NumberWithCountryIso(
  val number: String?,
  val countryIso: String?
) {
  override fun equals(other: Any?): Boolean {
    if (other == null) return false

    if (other !is NumberWithCountryIso) return false

    return number == other.number && countryIso == other.countryIso
  }

  override fun hashCode(): Int {
    val numberHashCode = number?.hashCode() ?: 0
    val countryIsoHashCode = countryIso?.hashCode() ?: 0
    return numberHashCode.xor(countryIsoHashCode)
  }
}