package dev.alenajam.opendialer.feature.callDetail

import kotlinx.serialization.Serializable

@Serializable data class CallDetailRoute(val callIds: List<Int>)