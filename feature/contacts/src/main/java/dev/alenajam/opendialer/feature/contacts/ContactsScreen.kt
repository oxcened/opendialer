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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.forwardingPainter
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
    var openRowKey by remember { mutableStateOf<String?>(null) }
    var pendingCallNumber by remember { mutableStateOf<String?>(null) }
    val requestCallPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (PermissionUtils.makeCallPermissions.all { result[it] == true }) {
            viewModel.handleCallRuntimePermissionGranted()
            pendingCallNumber?.let(viewModel::makeCall)
        }
        pendingCallNumber = null
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
                OutlinedButton(
                    onClick = { requestPermissions.launch(input = PermissionUtils.contactsPermissions) }
                ) {
                    Text(text = stringResource(R.string.turn_on))
                }
            }
            return@Surface
        }

        LazyColumn {
            itemsIndexed(
                filteredContacts,
                key = { _, contact -> "${contact.id}-${contact.number}" },
            ) { index, contact ->
                val rowKey = "${contact.id}-${contact.number}"
                val isOpen = openRowKey == rowKey
                ContactRow(
                    contact = contact,
                    isOpen = isOpen,
                    roundTop = index == 0,
                    roundBottom = index == filteredContacts.lastIndex,
                    onClick = { openRowKey = if (isOpen) null else rowKey },
                    onOpenContact = { viewModel.openContact(contact.id) },
                    onCall = {
                        if (!viewModel.makeCall(contact.number)) {
                            pendingCallNumber = contact.number
                            requestCallPermissions.launch(PermissionUtils.makeCallPermissions)
                        }
                    },
                    onMessage = { viewModel.sendMessage(contact.number) },
                    onHistory = {
                        viewModel.getHistoryIds(contact.number)
                            .takeIf { it.isNotEmpty() }
                            ?.let(onOpenHistory)
                    },
                )
            }
        }
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
        color = Color.White,
        shadowElevation = 0.5.dp,
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            ) {
                val placeholder = forwardingPainter(
                    painter = rememberVectorPainter(Icons.Filled.AccountCircle),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                )
                AsyncImage(
                    model = contact.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable(onClick = onOpenContact),
                    placeholder = placeholder,
                    error = placeholder,
                    fallback = placeholder
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
