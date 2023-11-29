package dev.alenajam.opendialer.data.callsCache

class ContactInfoRequest(
  number: String?,
  countryIso: String?,
  val callLogInfo: ContactInfo
) : NumberWithCountryIso(number, countryIso)