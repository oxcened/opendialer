package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper
import dev.alenajam.opendialer.core.common.copy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickResponsesScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var responses by remember { mutableStateOf(SharedPreferenceHelper.getQuickResponses(context)) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.customize_quick_responses)) },
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
            responses.forEachIndexed { index, response ->
                QuickResponseRow(
                    response = response,
                    onClick = { editingIndex = index },
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 20.dp else 2.dp,
                        topEnd = if (index == 0) 20.dp else 2.dp,
                        bottomStart = if (index == responses.lastIndex) 20.dp else 2.dp,
                        bottomEnd = if (index == responses.lastIndex) 20.dp else 2.dp
                    )
                )
            }
        }
    }

    editingIndex?.let { index ->
        EditQuickResponseDialog(
            initialResponse = responses[index],
            onDismiss = { editingIndex = null },
            onSave = { editedResponse ->
                val updatedResponses = responses.toMutableList().apply {
                    this[index] = editedResponse
                }
                responses = updatedResponses
                SharedPreferenceHelper.saveQuickResponses(context, updatedResponses)
                editingIndex = null
            }
        )
    }
}

@Composable
private fun QuickResponseRow(
    response: String,
    onClick: () -> Unit,
    shape: RoundedCornerShape
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                response,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_quick_response))
        }
    }
}

@Composable
private fun EditQuickResponseDialog(
    initialResponse: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var response by remember(initialResponse) { mutableStateOf(initialResponse) }
    val trimmedResponse = response.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_quick_response)) },
        text = {
            OutlinedTextField(
                value = response,
                onValueChange = { response = it },
                label = { Text(stringResource(R.string.quick_response)) },
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(trimmedResponse) },
                enabled = trimmedResponse.isNotEmpty()
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
