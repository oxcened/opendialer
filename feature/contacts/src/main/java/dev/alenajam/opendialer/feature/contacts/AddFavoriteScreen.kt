package dev.alenajam.opendialer.feature.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.contacts.DialerContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavoriteScreen(
    onNavigateBack: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasRuntimePermission.collectAsStateWithLifecycle()

    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
                viewModel.handleRuntimePermissionGranted()
            }
        }

    val items = remember(contacts) {
        buildList {
            val favorites = contacts.filter { it.starred }.sortedBy { it.name }
            if (favorites.isNotEmpty()) {
                add(ContactListItem.Header("Favorites"))
                addAll(favorites.map { ContactListItem.ContactItem(it) })
            }

            contacts.filter { !it.starred }
                .groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
                .toSortedMap()
                .forEach { (char, contactsForChar) ->
                    add(ContactListItem.Header(char.toString()))
                    addAll(contactsForChar.sortedBy { it.name }.map { ContactListItem.ContactItem(it) })
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a contact") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasPermission) {
                PermissionPrompt(
                    requestPermissions = { requestPermissions.launch(PermissionUtils.contactsPermissions) }
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { item ->
                        when (item) {
                            is ContactListItem.Header -> {
                                SectionHeader(text = item.label)
                            }
                            is ContactListItem.ContactItem -> {
                                FavoritePickerRow(
                                    contact = item.contact,
                                    onClick = {
                                        viewModel.toggleFavorite(item.contact.id, true)
                                        onNavigateBack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class ContactListItem {
    data class Header(val label: String) : ContactListItem()
    data class ContactItem(val contact: DialerContact) : ContactListItem()
}

@Composable
private fun SectionHeader(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (text == "Favorites") {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionPrompt(
    requestPermissions: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterVertically
        ),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "To manage favorites, turn on the contacts permissions",
            textAlign = TextAlign.Center
        )
        Button(onClick = requestPermissions) {
            Text(text = "Turn on")
        }
    }
}

@Composable
private fun FavoritePickerRow(
    contact: DialerContact,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ContactAvatar(
                name = contact.name,
                photoUri = contact.image,
                colorKey = contact.number,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = contact.number,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
