package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

data class SettingsSubpageDestination(
    val title: String,
    val content: @Composable (onNavigateBack: () -> Unit) -> Unit
)

class SettingsSubpageNavigator internal constructor(
    private val navigateToDestination: (Int) -> Unit
) {
    fun navigateTo(destinationIndex: Int) = navigateToDestination(destinationIndex)
}

data class SettingsSubpage(
    val title: String,
    val description: String? = null,
    val content: @Composable ColumnScope.() -> Unit,
    val isScrollable: Boolean = true,
    val destinations: List<SettingsSubpageDestination> = emptyList()
)
