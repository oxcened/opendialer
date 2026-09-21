package dev.alenajam.opendialer.feature.settings

import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

@Serializable
data object AboutRoute

@Serializable
data object QuickResponsesRoute

@Serializable
data object DisplayOptionsRoute

@Serializable
data class SettingsSubpageRoute(val pageId: String, val payload: String? = null)

@Serializable
data class SettingsSubpageDestinationRoute(
    val pageId: String,
    val destinationId: String,
    val payload: String? = null
)
