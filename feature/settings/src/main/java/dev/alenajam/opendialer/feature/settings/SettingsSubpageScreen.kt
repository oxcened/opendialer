package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.copy
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons

val LocalSettingsSubpageNavigator = staticCompositionLocalOf<SettingsSubpageNavigator?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSubpageScreen(
    page: SettingsSubpage,
    payload: String?,
    onNavigateBack: () -> Unit,
    onNavigateToDestination: (Int, String?) -> Unit
) {
    CompositionLocalProvider(
        LocalSettingsSubpageNavigator provides SettingsSubpageNavigator(onNavigateToDestination, onNavigateBack)
    ) {
        Scaffold(topBar = {
            TopAppBar(title = {
                page.topBarTitle?.invoke() ?: run {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(page.title)
                        page.subtitle?.let { subtitle ->
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    AppIcon(LocalAppIcons.current.arrowLeft, contentDescription = null)
                }
            }, actions = page.actions)
        }) { padding ->
            val contentModifier = Modifier
                .padding(padding.copy(top = padding.calculateTopPadding() + page.topContentPadding, start = 16.dp, end = 16.dp))
                .fillMaxSize()
                .let { modifier ->
                    if (page.isScrollable) modifier.verticalScroll(rememberScrollState()) else modifier
                }
            Column(modifier = contentModifier) {
                page.content(this@Column, payload)
            }
        }
    }
}
