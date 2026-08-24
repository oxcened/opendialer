package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.copy

val LocalSettingsSubpageNavigator = staticCompositionLocalOf<SettingsSubpageNavigator?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSubpageScreen(
    page: SettingsSubpage,
    onNavigateBack: () -> Unit,
    onNavigateToDestination: (Int) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(page.title) }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
        })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding.copy(top = padding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp))
                .fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            CompositionLocalProvider(
                LocalSettingsSubpageNavigator provides SettingsSubpageNavigator(onNavigateToDestination)
            ) { page.content(this@Column) }
        }
    }
}
