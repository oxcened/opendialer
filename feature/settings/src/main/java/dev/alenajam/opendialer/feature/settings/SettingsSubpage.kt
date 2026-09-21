package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SettingsSubpageDestination(
    val id: String,
    val title: String,
    val content: @Composable (payload: String?, onNavigateBack: () -> Unit) -> Unit
)

class SettingsSubpageNavigator(
    private val navigateToDestination: (String, String?) -> Unit,
    private val onNavigateBack: () -> Unit
) {
    fun navigateTo(destinationId: String, payload: String? = null) =
        navigateToDestination(destinationId, payload)

    fun navigateBack() = onNavigateBack()
}

data class SettingsSubpage(
    val id: String,
    val title: String,
    val description: String? = null,
    val subtitle: String? = null,
    val topBarTitle: (@Composable () -> Unit)? = null,
    val showTopBar: Boolean = true,
    val content: @Composable ColumnScope.(payload: String?) -> Unit,
    val actions: @Composable RowScope.() -> Unit = {},
    val isScrollable: Boolean = true,
    val topContentPadding: Dp = 16.dp,
    val contentHorizontalPadding: Dp = 16.dp,
    val visibleInSettings: Boolean = true,
    val destinations: List<SettingsSubpageDestination> = emptyList()
)
