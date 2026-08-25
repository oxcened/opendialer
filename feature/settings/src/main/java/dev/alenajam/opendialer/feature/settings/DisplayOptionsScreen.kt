package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper.ThemeMode
import dev.alenajam.opendialer.core.common.copy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayOptionsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(SharedPreferenceHelper.getThemeMode(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.display_options)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding.copy(top = padding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp))
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.entries.forEachIndexed { index, themeMode ->
                    ThemeOption(
                        label = themeMode.label(),
                        selected = selectedTheme == themeMode,
                        onClick = {
                            selectedTheme = themeMode
                            SharedPreferenceHelper.setThemeMode(context, themeMode)
                        },
                        shape = RoundedCornerShape(
                            topStart = if (index == 0) 20.dp else 2.dp,
                            topEnd = if (index == 0) 20.dp else 2.dp,
                            bottomStart = if (index == ThemeMode.entries.lastIndex) 20.dp else 2.dp,
                            bottomEnd = if (index == ThemeMode.entries.lastIndex) 20.dp else 2.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> stringResource(R.string.light)
    ThemeMode.DARK -> stringResource(R.string.dark)
    ThemeMode.SYSTEM -> stringResource(R.string.set_battery_saver)
}
