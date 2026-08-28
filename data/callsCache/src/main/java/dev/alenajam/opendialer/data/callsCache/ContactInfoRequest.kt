package dev.alenajam.opendialer.data.callsCache

data class ContactInfoRequest(
    val number: String?,
    val countryIso: String?,
    val callLogInfo: ContactInfo
)
