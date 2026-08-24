package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.forwardingPainter
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.core.common.ui.Dialpad
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact

@Composable
fun ContactsSearchScreen(
    viewModel: SearchContactsViewModel = hiltViewModel(),
    onOpenHistory: (callIds: List<Int>) -> Unit = {},
) {
    val result = viewModel.result.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf(viewModel.prefilledNumber) }
    var openRowKey by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val requestCallPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.handleCallRuntimePermissionGranted()
        }

    fun makeCall(number: String) {
        viewModel.makeCall(
            activity = context.getActivity() as Activity,
            number = number
        ).let {
            if (!it) {
                requestCallPermissions.launch(PermissionUtils.makeCallPermissions)
            }
        }
    }

    Scaffold(
        bottomBar = {
            Footer(
                query = query,
                onQueryChange = {
                    query = it
                    viewModel.searchContactsByDialpad(it)
                },
                onCall = { makeCall(query) }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (!hasPermission.value) {
                PermissionPrompt(
                    onPermissionGranted = { viewModel.handleRuntimePermissionGranted(query = query) }
                )
                return@Surface
            }

            Column {
                SearchList(
                    result = result.value,
                    openRowKey = openRowKey,
                    onRowClick = { key -> openRowKey = if (openRowKey == key) null else key },
                    onCall = { makeCall(it.number) },
                    onMessage = { viewModel.sendMessage(context.getActivity() as Activity, it.number) },
                    onAddContact = { viewModel.addToContact(context.getActivity() as Activity, it.number) },
                    onOpenContact = { viewModel.openContact(context.getActivity() as Activity, it.contactId) },
                    onHistory = {
                        viewModel.getHistoryIds(it.number)
                            .takeIf { ids -> ids.isNotEmpty() }
                            ?.let(onOpenHistory)
                    }
                )

                ActionsList(
                    query = query,
                    onCreateNewContact = {
                        viewModel.createContact(
                            activity = context.getActivity() as Activity,
                            number = query
                        )
                    },
                    onAddToContact = {
                        viewModel.addToContact(
                            activity = context.getActivity() as Activity,
                            number = query
                        )
                    },
                    onSendMessage = {
                        viewModel.sendMessage(
                            activity = context.getActivity() as Activity,
                            number = query
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionPrompt(
    onPermissionGranted: () -> Unit
) {
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
                onPermissionGranted()
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterVertically
        ),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(text = stringResource(R.string.placeholder_search_permissions))
        Button(
            onClick = { requestPermissions.launch(input = PermissionUtils.searchPermissions) }
        ) {
            Text(text = stringResource(R.string.turn_on))
        }
    }
}

@Composable
private fun SearchList(
    result: SearchContactsViewModel.Result?,
    openRowKey: String?,
    onRowClick: (key: String) -> Unit,
    onCall: (contact: DialerSearchContact) -> Unit,
    onMessage: (contact: DialerSearchContact) -> Unit,
    onAddContact: (contact: DialerSearchContact) -> Unit,
    onOpenContact: (contact: DialerSearchContact) -> Unit,
    onHistory: (contact: DialerSearchContact) -> Unit
) {
    LazyColumn {
        result?.contacts?.let { contacts ->
            if (result.query.isBlank() && contacts.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.suggested),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
            items(contacts) { contact ->
                val rowKey = "${contact.contactId}-${contact.number}"
                val isOpen = openRowKey == rowKey
                ResultRow(
                    contact = contact,
                    isOpen = isOpen,
                    onClick = { onRowClick(rowKey) },
                    onCall = { onCall(contact) },
                    onMessage = { onMessage(contact) },
                    onAddContact = { onAddContact(contact) },
                    onOpenContact = { onOpenContact(contact) },
                    onHistory = { onHistory(contact) }
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    contact: DialerSearchContact,
    isOpen: Boolean,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onAddContact: () -> Unit,
    onOpenContact: () -> Unit,
    onHistory: () -> Unit
) {
    Surface(onClick = onClick) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
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
                        .clickable(
                            enabled = contact.contactId > 0,
                            onClick = onOpenContact
                        ),
                    placeholder = placeholder,
                    error = placeholder,
                    fallback = placeholder
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (contact.name.isNotBlank()) contact.name else contact.number,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = contact.number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onCall) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = stringResource(R.string.call_contact)
                    )
                }
            }

            AnimatedVisibility(visible = isOpen) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    if (contact.contactId > 0) {
                        ResultActionRow(
                            icon = Icons.Default.AccountCircle,
                            label = stringResource(R.string.open_contact),
                            roundTop = true,
                            onClick = onOpenContact
                        )
                    } else {
                        ResultActionRow(
                            icon = Icons.Outlined.PersonAddAlt,
                            label = stringResource(R.string.add_to_a_contact),
                            roundTop = true,
                            onClick = onAddContact
                        )
                    }

                    ResultActionRow(
                        icon = Icons.AutoMirrored.Outlined.Message,
                        label = stringResource(R.string.send_message),
                        onClick = onMessage
                    )

                    ResultActionRow(
                        icon = Icons.Outlined.History,
                        label = stringResource(R.string.contact_history),
                        roundBottom = true,
                        onClick = onHistory
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultActionRow(
    icon: ImageVector,
    label: String,
    roundTop: Boolean = false,
    roundBottom: Boolean = false,
    onClick: () -> Unit
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ActionsList(
    query: String,
    onCreateNewContact: () -> Unit,
    onAddToContact: () -> Unit,
    onSendMessage: () -> Unit
) {
    if (query.isBlank()) return

    Column {
        ActionRow(
            icon = Icons.Outlined.PersonAddAlt,
            label = stringResource(R.string.create_new_contact),
            onClick = onCreateNewContact
        )

        ActionRow(
            icon = Icons.Outlined.PersonAddAlt,
            label = stringResource(R.string.add_to_a_contact),
            onClick = onAddToContact
        )

        ActionRow(
            icon = Icons.AutoMirrored.Outlined.Message,
            label = stringResource(R.string.send_message),
            onClick = onSendMessage
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )

            Text(
                text = label
            )
        }
    }
}

@Composable
private fun Footer(
    query: String,
    onQueryChange: (query: String) -> Unit,
    onCall: () -> Unit
) {
    var selection by remember { mutableStateOf(TextRange.Zero) }

    fun handleButtonClick(digit: Char) {
        onQueryChange(query.replaceRange(selection.start, selection.end, digit.toString()))
        selection = TextRange(selection.start + 1)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {},
                    enabled = false
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = ""
                    )
                }

                TextField(
                    modifier = Modifier.weight(1f),
                    value = TextFieldValue(text = query, selection = selection),
                    onValueChange = { selection = it.selection },
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        if (selection.end > selection.start) {
                            onQueryChange(query.replaceRange(selection.start, selection.end, ""))
                            selection = TextRange(selection.start)
                        } else if (selection.start > 0) {
                            onQueryChange(
                                query.replaceRange(
                                    selection.start - 1,
                                    selection.end,
                                    ""
                                )
                            )
                            selection = TextRange(selection.start - 1)
                        }
                    },
                    enabled = query.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = ""
                    )
                }
            }

            Dialpad(
                onDigitClick = ::handleButtonClick
            )

            Button(
                onClick = onCall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(60.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null
                    )
                    Text(text = stringResource(R.string.dialpad_button_call_label))
                }
            }
        }
    }
}
