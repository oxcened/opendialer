package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SettingsSubpageDestination(
    val title: String,
    val content: @Composable (payload: String?, onNavigateBack: () -> Unit) -> Unit
)

class SettingsSubpageNavigator internal constructor(
    private val navigateToDestination: (Int, String?) -> Unit
) {
    fun navigateTo(destinationIndex: Int, payload: String? = null) = 
        navigateToDestination(destinationIndex, payload)
}

data class SettingsSubpage(
    val title: String,
    val description: String? = null,
    val subtitle: String? = null,
    val content: @Composable ColumnScope.() -> Unit,
    val actions: @Composable RowScope.() -> Unit = {},
    val isScrollable: Boolean = true,
    val topContentPadding: Dp = 16.dp,
    val visibleInSettings: Boolean = true,
    val destinations: List<SettingsSubpageDestination> = emptyList()
)
