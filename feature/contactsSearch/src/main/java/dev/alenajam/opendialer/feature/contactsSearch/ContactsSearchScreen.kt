package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
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
) {
    val result = viewModel.result.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf(viewModel.prefilledNumber) }
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
                    onResultClick = { makeCall(it.number) }
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
    onResultClick: (contact: DialerSearchContact) -> Unit
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
                ResultRow(contact, onClick = { onResultClick(contact) })
            }
        }
    }
}

@Composable
private fun ResultRow(
    contact: DialerSearchContact,
    onClick: () -> Unit
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
                    modifier = Modifier.size(50.dp),
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
            }
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
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
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
