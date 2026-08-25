package dev.alenajam.opendialer.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.provider.BlockedNumberContract
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.copy

private data class SettingsListItem(
    val title: String,
    val description: String?,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenQuickResponses: () -> Unit = {},
    onOpenDisplayOptions: () -> Unit = {},
    subpages: List<SettingsSubpage> = emptyList(),
    onOpenSubpage: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val canManageBlockedNumbers = BlockedNumberContract.canCurrentUserBlockNumbers(context)
    val displayOptionsTitle = stringResource(R.string.display_options)
    val displayOptionsDescription = stringResource(R.string.display_options_description)
    val quickResponsesTitle = stringResource(R.string.customize_quick_responses)
    val quickResponsesDescription = stringResource(R.string.customize_quick_responses_description)
    val manageBlockedNumbersTitle = stringResource(R.string.manageBlockedNumbers)
    val manageBlockedNumbersDescription = stringResource(R.string.manage_blocked_numbers_description)
    val extensionTitle = stringResource(R.string.app_name)
    val dialerItems = buildList {
        add(
            SettingsListItem(
                title = displayOptionsTitle,
                description = displayOptionsDescription,
                onClick = onOpenDisplayOptions
            )
        )
        add(
            SettingsListItem(
                title = quickResponsesTitle,
                description = quickResponsesDescription,
                onClick = onOpenQuickResponses
            )
        )
        if (canManageBlockedNumbers) {
            add(
                SettingsListItem(
                    title = manageBlockedNumbersTitle,
                    description = manageBlockedNumbersDescription,
                    onClick = {
                        try {
                            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE)
                                as? TelecomManager
                            val intent = telecomManager?.createManageBlockedNumbersIntent()
                                ?: throw ActivityNotFoundException()
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                R.string.manage_blocked_numbers_unavailable,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            )
        }
    }
    val extensionItems = subpages.mapIndexed { index, page ->
        SettingsListItem(page.title, page.description) { onOpenSubpage(index) }
    }
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
            SettingsSection(
                title = stringResource(R.string.dialer),
                items = dialerItems
            )
            if (extensionItems.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SettingsSection(title = extensionTitle, items = extensionItems)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, items: List<SettingsListItem>) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
    items.forEachIndexed { index, item ->
        SettingsCard(
            title = item.title,
            description = item.description,
            onClick = item.onClick,
            shape = RoundedCornerShape(
                topStart = if (index == 0) 20.dp else 2.dp,
                topEnd = if (index == 0) 20.dp else 2.dp,
                bottomStart = if (index == items.lastIndex) 20.dp else 2.dp,
                bottomEnd = if (index == items.lastIndex) 20.dp else 2.dp
            )
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    description: String?,
    onClick: () -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp)
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
