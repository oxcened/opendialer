package dev.alenajam.opendialer.feature.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.ui.contactAvatarColorKey
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.data.contacts.DialerContactSummary
import kotlinx.coroutines.launch

data class ContactRowOverflowAction(
    val settingsSubpageIndex: Int? = null,
    val onClick: suspend (DialerContactSummary) -> Unit,
    val content: @Composable () -> Unit,
)

data class ContactRowOverflowMenu(
    val trigger: @Composable (onClick: () -> Unit) -> Unit,
    val actions: List<ContactRowOverflowAction>,
)

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    searchQuery: String = "",
    @Suppress("UNUSED_PARAMETER") onOpenHistory: (callIds: List<Int>) -> Unit = {},
    contactRowOverflowMenu: ContactRowOverflowMenu? = null,
    onOpenSettingsSubpage: (Int, String?) -> Unit = { _, _ -> },
) {
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
                viewModel.handleRuntimePermissionGranted()
            }
        }

    val contacts = viewModel.contacts.collectAsStateWithLifecycle()
    val profileContact = viewModel.profileContact.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val filteredContacts = if (searchQuery.isBlank()) {
        contacts.value
    } else {
        val trimmedQuery = searchQuery.trim()
        contacts.value.filter { contact ->
            contact.name.contains(trimmedQuery, ignoreCase = true)
        }
    }
    val groupBySection = searchQuery.isBlank()
    val allContactsLabel = stringResource(R.string.all_contacts)
    val favoritesLabel = stringResource(R.string.favorites)
    val contactListItems = remember(filteredContacts, groupBySection, allContactsLabel, favoritesLabel) {
        buildContactListItems(
            contacts = filteredContacts,
            groupBySection = groupBySection,
            allContactsLabel = allContactsLabel,
            favoritesLabel = favoritesLabel,
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (!hasPermission.value) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.CenterVertically
                ),
            ) {
                Text(
                    text = stringResource(R.string.placeholder_contacts),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { requestPermissions.launch(input = PermissionUtils.contactsPermissions) }
                ) {
                    Text(text = stringResource(R.string.turn_on))
                }
            }
            return@Surface
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            if (searchQuery.isBlank()) {
                item(key = "new-contact") {
                    Button(
                        onClick = { CommonUtils.createContact(context, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        AppIcon(LocalAppIcons.current.personAddInContactsList, contentDescription = null)
                        Text(
                            text = stringResource(R.string.new_contact),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
                profileContact.value?.let { profile ->
                    item(key = "profile-contact") {
                        ProfileContactCard(
                            contact = profile,
                            onOpenProfile = viewModel::openProfileContact,
                            onShareProfile = { viewModel.shareProfileContact(profile.id) },
                        )
                    }
                }
            }
            items(
                items = contactListItems,
                key = { item ->
                    when (item) {
                        is ContactsListEntry.Header -> "header-${item.label}"
                        is ContactsListEntry.Contact -> "contact-${item.sectionLabel}-${item.contact.id}"
                    }
                },
            ) { item ->
                when (item) {
                    is ContactsListEntry.Header -> ContactSectionHeader(item.label, item.isFavorites)
                    is ContactsListEntry.Contact -> {
                        ContactRow(
                            contact = item.contact,
                            roundTop = item.isFirstInSection,
                            roundBottom = item.isLastInSection,
                            onOpenContact = { viewModel.openContact(item.contact.id) },
                            overflowMenu = contactRowOverflowMenu,
                            onOverflowAction = { action ->
                                coroutineScope.launch {
                                    action.onClick(item.contact)
                                    action.settingsSubpageIndex?.let { index ->
                                        onOpenSettingsSubpage(index, null)
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContactCard(
    contact: DialerContactSummary,
    onOpenProfile: () -> Unit,
    onShareProfile: () -> Unit,
) {
    Surface(
        onClick = onOpenProfile,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 0.5.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
        ) {
            ContactAvatar(
                name = contact.name,
                photoUri = contact.image,
                colorKey = contactAvatarColorKey(contact.name),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )
            Column {
                Text(
                    text = stringResource(R.string.your_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onShareProfile) {
                AppIcon(
                    icon = LocalAppIcons.current.share,
                    contentDescription = stringResource(R.string.share_contact),
                )
            }
        }
    }
}

private sealed class ContactsListEntry {
    data class Header(val label: String, val isFavorites: Boolean = false) : ContactsListEntry()

    data class Contact(
        val contact: DialerContactSummary,
        val sectionLabel: String,
        val isFirstInSection: Boolean,
        val isLastInSection: Boolean,
    ) : ContactsListEntry()
}

private fun buildContactListItems(
    contacts: List<DialerContactSummary>,
    groupBySection: Boolean,
    allContactsLabel: String,
    favoritesLabel: String,
): List<ContactsListEntry> = buildList {
    fun addSection(
        label: String,
        sectionContacts: List<DialerContactSummary>,
        isFavorites: Boolean = false,
    ) {
        if (sectionContacts.isEmpty()) return
        add(ContactsListEntry.Header(label, isFavorites))
        sectionContacts.forEachIndexed { index, contact ->
            add(
                ContactsListEntry.Contact(
                    contact = contact,
                    sectionLabel = label,
                    isFirstInSection = index == 0,
                    isLastInSection = index == sectionContacts.lastIndex,
                )
            )
        }
    }

    val sortedContacts = contacts.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    if (!groupBySection) {
        addSection(allContactsLabel, sortedContacts)
        return@buildList
    }

    addSection(favoritesLabel, sortedContacts.filter { it.starred }, isFavorites = true)
    sortedContacts
        .groupBy { it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
        .toSortedMap()
        .forEach { (initial, sectionContacts) -> addSection(initial, sectionContacts) }
}

@Composable
private fun ContactSectionHeader(label: String, isFavorites: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (isFavorites) {
            AppIcon(
                icon = LocalAppIcons.current.favorite,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContactRow(
    contact: DialerContactSummary,
    roundTop: Boolean,
    roundBottom: Boolean,
    onOpenContact: () -> Unit,
    overflowMenu: ContactRowOverflowMenu? = null,
    onOverflowAction: (ContactRowOverflowAction) -> Unit = {},
) {
    Surface(
        onClick = onOpenContact,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        shape = RoundedCornerShape(
            topStart = if (roundTop) 20.dp else 2.dp,
            topEnd = if (roundTop) 20.dp else 2.dp,
            bottomStart = if (roundBottom) 20.dp else 2.dp,
            bottomEnd = if (roundBottom) 20.dp else 2.dp,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 0.5.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
        ) {
            ContactAvatar(
                name = contact.name,
                photoUri = contact.image,
                colorKey = contactAvatarColorKey(contact.name),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )

            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            overflowMenu?.takeIf { it.actions.isNotEmpty() }?.let { menu ->
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                var overflowExpanded by remember { mutableStateOf(false) }
                Box {
                    menu.trigger { overflowExpanded = true }
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false },
                    ) {
                        menu.actions.forEach { action ->
                            DropdownMenuItem(
                                text = action.content,
                                onClick = {
                                    overflowExpanded = false
                                    onOverflowAction(action)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

val contactMock = DialerContactSummary(
    id = 1,
    name = "John Doe",
    starred = false,
    image = null,
)

@Preview(showBackground = true)
@Composable
private fun ContactRowPreview() {
    ContactRow(
        contact = contactMock,
        roundTop = true,
        roundBottom = true,
        onOpenContact = {},
    )
}
