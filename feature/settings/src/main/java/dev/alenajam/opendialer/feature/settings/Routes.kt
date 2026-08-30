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
data class SettingsSubpageRoute(val index: Int)

@Serializable
data class SettingsSubpageDestinationRoute(
    val subpageIndex: Int,
    val destinationIndex: Int,
    val payload: String? = null
)
