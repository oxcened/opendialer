package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.copy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    subpages: List<SettingsSubpage> = emptyList(),
    onOpenSubpage: (Int) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding.copy(top = innerPadding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp))
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            subpages.forEachIndexed { index, page ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    onClick = { onOpenSubpage(index) },
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 20.dp else 2.dp,
                        topEnd = if (index == 0) 20.dp else 2.dp,
                        bottomStart = if (index == subpages.lastIndex) 20.dp else 2.dp,
                        bottomEnd = if (index == subpages.lastIndex) 20.dp else 2.dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(page.title, style = MaterialTheme.typography.titleMedium)
                        page.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
    }
}
