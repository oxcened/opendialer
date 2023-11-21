package dev.alenajam.opendialer.features.dialer.calls.cache

class ContactInfoRequest(
  number: String?,
  countryIso: String?,
  val callLogInfo: ContactInfo
) : NumberWithCountryIso(number, countryIso)