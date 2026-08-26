package dev.alenajam.opendialer.feature.contacts

import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.core.common.ui.CallAccountPicker
import dev.alenajam.opendialer.data.contacts.DialerContact

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    searchQuery: String = "",
    onOpenHistory: (callIds: List<Int>) -> Unit = {},
) {
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
                viewModel.handleRuntimePermissionGranted()
            }
        }

    val contacts = viewModel.contacts.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var openRowKey by remember { mutableStateOf<String?>(null) }
    var pendingCallNumber by remember { mutableStateOf<String?>(null) }
    var callAccounts by remember { mutableStateOf<List<CallAccount>?>(null) }
    val requestCallPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (PermissionUtils.makeCallPermissions.all { result[it] == true }) {
            viewModel.handleCallRuntimePermissionGranted()
            pendingCallNumber?.let { number ->
                when (val placementResult = viewModel.makeCall(number)) {
                    is CallPlacementResult.AccountSelectionRequired -> {
                        callAccounts = placementResult.accounts
                    }
                    else -> pendingCallNumber = null
                }
            }
        } else {
            pendingCallNumber = null
        }
    }

    fun placeCall(number: String) {
        when (val result = viewModel.makeCall(number)) {
            CallPlacementResult.PermissionRequired -> {
                pendingCallNumber = number
                requestCallPermissions.launch(PermissionUtils.makeCallPermissions)
            }
            is CallPlacementResult.AccountSelectionRequired -> {
                pendingCallNumber = number
                callAccounts = result.accounts
            }
            else -> Unit
        }
    }
    val filteredContacts = if (searchQuery.isBlank()) {
        contacts.value
    } else {
        val trimmedQuery = searchQuery.trim()
        val normalizedQuery = PhoneNumberUtils.normalizeNumber(trimmedQuery)
        contacts.value.filter { contact ->
            contact.name.contains(trimmedQuery, ignoreCase = true) ||
                contact.number.contains(trimmedQuery, ignoreCase = true) ||
                normalizedQuery.isNotEmpty() && PhoneNumberUtils.normalizeNumber(contact.number)
                    .contains(normalizedQuery)
        }
    }
    val groupBySection = searchQuery.isBlank()
    val allContactsLabel = stringResource(R.string.all_contacts)
    val contactListItems = remember(filteredContacts, groupBySection, allContactsLabel) {
        buildContactListItems(
            contacts = filteredContacts,
            groupBySection = groupBySection,
            allContactsLabel = allContactsLabel,
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        callAccounts?.let { accounts ->
            CallAccountPicker(
                accounts = accounts,
                onAccountSelected = { account ->
                    val number = pendingCallNumber ?: return@CallAccountPicker
                    callAccounts = null
                    when (val result = viewModel.makeCall(number, account)) {
                        is CallPlacementResult.AccountSelectionRequired -> callAccounts = result.accounts
                        else -> Unit
                    }
                },
                onDismiss = {
                    callAccounts = null
                    pendingCallNumber = null
                },
            )
        }
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

        LazyColumn {
            if (searchQuery.isBlank()) {
                item(key = "new-contact") {
                    Button(
                        onClick = { CommonUtils.createContact(context, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.PersonAddAlt1, contentDescription = null)
                        Text(
                            text = stringResource(R.string.new_contact),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
            items(
                items = contactListItems,
                key = { item ->
                    when (item) {
                        is ContactsListEntry.Header -> "header-${item.label}"
                        is ContactsListEntry.Contact ->
                            "contact-${item.sectionLabel}-${item.contact.dataId}"
                    }
                },
            ) { item ->
                when (item) {
                    is ContactsListEntry.Header -> ContactSectionHeader(item.label)
                    is ContactsListEntry.Contact -> {
                        // A favorite is intentionally shown both here and in its alphabetical section.
                        // Include the section so each rendered copy owns its expansion state.
                        val rowKey = "${item.sectionLabel}-${item.contact.dataId}"
                        val isOpen = openRowKey == rowKey
                        ContactRow(
                            contact = item.contact,
                            isOpen = isOpen,
                            roundTop = item.isFirstInSection,
                            roundBottom = item.isLastInSection,
                            onClick = { openRowKey = if (isOpen) null else rowKey },
                            onOpenContact = { viewModel.openContact(item.contact.id) },
                            onCall = { placeCall(item.contact.number) },
                            onMessage = { viewModel.sendMessage(item.contact.number) },
                            onHistory = {
                                viewModel.getHistoryIds(item.contact.number)
                                    .takeIf { it.isNotEmpty() }
                                    ?.let(onOpenHistory)
                            },
                        )
                    }
                }
            }
        }
    }
}

private sealed class ContactsListEntry {
    data class Header(val label: String) : ContactsListEntry()

    data class Contact(
        val contact: DialerContact,
        val sectionLabel: String,
        val isFirstInSection: Boolean,
        val isLastInSection: Boolean,
    ) : ContactsListEntry()
}

private fun buildContactListItems(
    contacts: List<DialerContact>,
    groupBySection: Boolean,
    allContactsLabel: String,
): List<ContactsListEntry> = buildList {
    fun addSection(label: String, sectionContacts: List<DialerContact>) {
        if (sectionContacts.isEmpty()) return
        add(ContactsListEntry.Header(label))
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

    addSection("Favorites", sortedContacts.filter { it.starred })
    sortedContacts
        .groupBy { it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
        .toSortedMap()
        .forEach { (initial, sectionContacts) -> addSection(initial, sectionContacts) }
}

@Composable
private fun ContactSectionHeader(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (label == "Favorites") {
            Icon(
                imageVector = Icons.Default.Star,
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
    contact: DialerContact,
    isOpen: Boolean,
    roundTop: Boolean,
    roundBottom: Boolean,
    onClick: () -> Unit,
    onOpenContact: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onHistory: () -> Unit,
) {
    val resources = LocalContext.current.resources
    val phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
        resources,
        contact.phoneType,
        contact.phoneLabel,
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 1.dp),
        shape = RoundedCornerShape(
            topStart = if (roundTop) 20.dp else 2.dp,
            topEnd = if (roundTop) 20.dp else 2.dp,
            bottomStart = if (roundBottom) 20.dp else 2.dp,
            bottomEnd = if (roundBottom) 20.dp else 2.dp,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            ) {
                ContactAvatar(
                    name = contact.name,
                    photoUri = contact.image,
                    colorKey = contact.number,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenContact)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            R.string.contact_phone_subtitle,
                            phoneType,
                            contact.number,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onCall) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = stringResource(R.string.call_contact),
                    )
                }
            }

            AnimatedVisibility(visible = isOpen) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    ContactActionRow(
                        icon = Icons.AutoMirrored.Outlined.Message,
                        label = stringResource(R.string.message_contact),
                        roundTop = true,
                        onClick = onMessage,
                    )
                    ContactActionRow(
                        icon = Icons.Outlined.History,
                        label = stringResource(R.string.contact_history),
                        roundBottom = true,
                        onClick = onHistory,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactActionRow(
    icon: ImageVector,
    label: String,
    roundTop: Boolean = false,
    roundBottom: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(
            topStart = if (roundTop) 12.dp else 2.dp,
            topEnd = if (roundTop) 12.dp else 2.dp,
            bottomStart = if (roundBottom) 12.dp else 2.dp,
            bottomEnd = if (roundBottom) 12.dp else 2.dp,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

val contactMock = DialerContact(
    dataId = 1,
    id = 1,
    name = "John Doe",
    starred = false,
    image = null,
    number = "+39 333 123 4567",
    phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
    phoneLabel = null,
)

@Preview(showBackground = true)
@Composable
private fun ContactRowPreview() {
    ContactRow(
        contact = contactMock,
        isOpen = true,
        roundTop = true,
        roundBottom = true,
        onClick = {},
        onOpenContact = {},
        onCall = {},
        onMessage = {},
        onHistory = {},
    )
}
