package dev.alenajam.opendialer.feature.settings

import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

@Serializable
data object AboutRoute

@Serializable
data class SettingsSubpageRoute(val index: Int)
