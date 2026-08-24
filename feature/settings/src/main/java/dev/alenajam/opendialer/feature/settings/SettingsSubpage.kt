package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

data class SettingsSubpage(
    val title: String,
    val description: String? = null,
    val content: @Composable ColumnScope.() -> Unit
)
