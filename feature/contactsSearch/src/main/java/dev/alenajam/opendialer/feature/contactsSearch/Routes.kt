package dev.alenajam.opendialer.feature.contactsSearch

import kotlinx.serialization.Serializable

@Serializable
data class ContactsSearchRoute(val prefilledNumber: String = "")