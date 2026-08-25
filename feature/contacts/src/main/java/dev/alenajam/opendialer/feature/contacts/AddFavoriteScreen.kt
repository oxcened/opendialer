package dev.alenajam.opendialer.feature.contacts

import android.telephony.PhoneNumberUtils
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.contacts.DialerContact
import dev.alenajam.opendialer.feature.contacts.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavoriteScreen(
    onNavigateBack: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    ContactPickerScreen(
        onNavigateBack = onNavigateBack,
        onContactSelected = { contact ->
            viewModel.toggleFavorite(contact.id, true)
            onNavigateBack()
        },
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerScreen(
    onNavigateBack: () -> Unit,
    onContactSelected: (DialerContact) -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
                viewModel.handleRuntimePermissionGranted()
            }
        }

    val items: List<ContactListItem> = remember(contacts, isSearching, searchQuery) {
        val query = searchQuery.trim()
        if (isSearching && query.isNotEmpty()) {
            val normalizedQuery = PhoneNumberUtils.normalizeNumber(query)
            return@remember contacts
                .filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                        contact.number.contains(query, ignoreCase = true) ||
                        normalizedQuery.isNotEmpty() && PhoneNumberUtils.normalizeNumber(contact.number)
                            .contains(normalizedQuery)
                }
                .sortedBy { it.name }
                .map { ContactListItem.ContactItem(it) }
        }

        buildList {
            val favorites = contacts.filter { it.starred }.sortedBy { it.name }
            if (favorites.isNotEmpty()) {
                add(ContactListItem.Header(label = "", isFavorites = true))
                addAll(favorites.map { ContactListItem.ContactItem(it) })
            }

            contacts
                .groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
                .toSortedMap()
                .forEach { (char, contactsForChar) ->
                    add(ContactListItem.Header(char.toString()))
                    addAll(contactsForChar.sortedBy { it.name }.map { ContactListItem.ContactItem(it) })
                }
        }
    }

    LaunchedEffect(isSearching) {
        if (isSearching) searchFocusRequester.requestFocus() else focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            if (isSearching) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.search_contacts)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (searchQuery.isNotEmpty()) {
                                        searchQuery = ""
                                    } else {
                                        isSearching = false
                                    }
                                }) {
                                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close_search))
                                }
                            },
                            modifier = Modifier.focusRequester(searchFocusRequester)
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {}
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.choose_contact)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search))
                        }
                    }
                )
            }
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
                                SectionHeader(text = item.label, isFavorites = item.isFavorites)
                            }
                            is ContactListItem.ContactItem -> {
                                FavoritePickerRow(
                                    contact = item.contact,
                                    onClick = {
                                        onContactSelected(item.contact)
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
    data class Header(val label: String, val isFavorites: Boolean = false) : ContactListItem()
    data class ContactItem(val contact: DialerContact) : ContactListItem()
}

@Composable
private fun SectionHeader(text: String, isFavorites: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (isFavorites) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = if (isFavorites) stringResource(R.string.favorites) else text,
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
            text = stringResource(R.string.favorites_permission_prompt),
            textAlign = TextAlign.Center
        )
        Button(onClick = requestPermissions) {
            Text(text = stringResource(R.string.turn_on))
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
