package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Activity
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.ui.IconSource
import dev.alenajam.opendialer.core.common.ui.contactAvatarColorKey
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.core.common.LocalCustomColorsScheme
import dev.alenajam.opendialer.core.common.ui.CallAccountPicker
import dev.alenajam.opendialer.core.common.ui.Dialpad
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact

@Composable
fun ContactsSearchScreen(
    viewModel: SearchContactsViewModel = hiltViewModel(),
    onOpenHistory: (callIds: List<Int>) -> Unit = {},
    onDialpadCallStarted: () -> Unit = {},
) {
    val icons = LocalAppIcons.current
    val result = viewModel.result.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf(viewModel.prefilledNumber) }
    var selection by remember { mutableStateOf(TextRange(query.length)) }
    var openRowKey by remember { mutableStateOf<String?>(null) }
    var pendingCallNumber by remember { mutableStateOf<String?>(null) }
    var callAccounts by remember { mutableStateOf<List<CallAccount>?>(null) }
    val context = LocalContext.current
    val requestCallPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.makeCallPermissions.all { result[it] == true }) {
                viewModel.handleCallRuntimePermissionGranted()
                pendingCallNumber?.let { number ->
                    when (val placementResult = viewModel.makeCall(number)) {
                        is CallPlacementResult.AccountSelectionRequired -> callAccounts = placementResult.accounts
                        else -> pendingCallNumber = null
                    }
                }
            } else {
                pendingCallNumber = null
            }
        }

    fun makeCall(number: String): Boolean {
        return when (val result = viewModel.makeCall(number)) {
            CallPlacementResult.Placed -> true
            CallPlacementResult.PermissionRequired -> {
                pendingCallNumber = number
                requestCallPermissions.launch(PermissionUtils.makeCallPermissions)
                false
            }
            is CallPlacementResult.AccountSelectionRequired -> {
                pendingCallNumber = number
                callAccounts = result.accounts
                false
            }
            CallPlacementResult.Unavailable -> false
        }
    }

    callAccounts?.let { accounts ->
        CallAccountPicker(
            accounts = accounts,
            onAccountSelected = { account ->
                val number = pendingCallNumber ?: return@CallAccountPicker
                callAccounts = null
                when (viewModel.makeCall(number, account)) {
                    CallPlacementResult.Placed -> onDialpadCallStarted()
                    is CallPlacementResult.AccountSelectionRequired -> Unit
                    else -> Unit
                }
            },
            onDismiss = {
                callAccounts = null
                pendingCallNumber = null
            },
        )
    }

    Scaffold(
        bottomBar = {
            Footer(
                query = query,
                selection = selection,
                icons = icons,
                onQueryChange = { newQuery, newSelection ->
                    query = newQuery
                    selection = newSelection
                    viewModel.searchContactsByDialpad(newQuery)
                },
                onCall = {
                    if (query.isEmpty()) {
                        viewModel.getLastOutgoingNumber()?.let { lastNumber ->
                            query = lastNumber
                            selection = TextRange(lastNumber.length)
                            viewModel.searchContactsByDialpad(lastNumber)
                        }
                    } else {
                        if (makeCall(query)) onDialpadCallStarted()
                    }
                }
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

            Column(modifier = Modifier.padding(top = 8.dp)) {
                SearchList(
                    result = result.value,
                    openRowKey = openRowKey,
                    icons = icons,
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
                    icons = icons,
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
fun ContactsTextSearchResults(
    query: String,
    viewModel: SearchContactsViewModel = hiltViewModel(),
    onOpenHistory: (callIds: List<Int>) -> Unit = {},
) {
    val icons = LocalAppIcons.current
    val result = viewModel.result.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var openRowKey by remember { mutableStateOf<String?>(null) }
    var pendingCallNumber by remember { mutableStateOf<String?>(null) }
    var callAccounts by remember { mutableStateOf<List<CallAccount>?>(null) }
    val context = LocalContext.current
    val requestCallPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (PermissionUtils.makeCallPermissions.all { permissions[it] == true }) {
                viewModel.handleCallRuntimePermissionGranted()
                pendingCallNumber?.let { number ->
                    when (val placementResult = viewModel.makeCall(number)) {
                        CallPlacementResult.Placed -> pendingCallNumber = null
                        is CallPlacementResult.AccountSelectionRequired -> callAccounts = placementResult.accounts
                        else -> pendingCallNumber = null
                    }
                }
            } else {
                pendingCallNumber = null
            }
        }

    fun makeCall(number: String) {
        when (val placementResult = viewModel.makeCall(number)) {
            CallPlacementResult.PermissionRequired -> {
                pendingCallNumber = number
                requestCallPermissions.launch(PermissionUtils.makeCallPermissions)
            }
            is CallPlacementResult.AccountSelectionRequired -> {
                pendingCallNumber = number
                callAccounts = placementResult.accounts
            }
            else -> Unit
        }
    }

    LaunchedEffect(query, hasPermission.value) {
        if (hasPermission.value) viewModel.searchContacts(query)
    }

    callAccounts?.let { accounts ->
        CallAccountPicker(
            accounts = accounts,
            onAccountSelected = { account ->
                pendingCallNumber?.let { number -> viewModel.makeCall(number, account) }
                callAccounts = null
                pendingCallNumber = null
            },
            onDismiss = {
                callAccounts = null
                pendingCallNumber = null
            },
        )
    }

    if (!hasPermission.value) {
        PermissionPrompt(
            onPermissionGranted = { viewModel.handleTextSearchPermissionGranted(query) }
        )
        return
    }

    SearchList(
        result = result.value,
        openRowKey = openRowKey,
        icons = icons,
        onRowClick = { key -> openRowKey = if (openRowKey == key) null else key },
        onCall = { makeCall(it.number) },
        onMessage = { viewModel.sendMessage(context.getActivity() as Activity, it.number) },
        onAddContact = { viewModel.addToContact(context.getActivity() as Activity, it.number) },
        onOpenContact = { viewModel.openContact(context.getActivity() as Activity, it.contactId) },
        onHistory = {
            viewModel.getHistoryIds(it.number)
                .takeIf { ids -> ids.isNotEmpty() }
                ?.let(onOpenHistory)
        },
    )
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
internal fun SearchList(
    result: SearchContactsViewModel.Result?,
    openRowKey: String?,
    icons: dev.alenajam.opendialer.core.common.ui.AppIcons = LocalAppIcons.current,
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
            itemsIndexed(contacts, key = { _, contact -> contact.dataId }) { index, contact ->
                val rowKey = contact.dataId.toString()
                val isOpen = openRowKey == rowKey
                ResultRow(
                    contact = contact,
                    query = result.query,
                    isDialpadSearch = result.isDialpadSearch,
                    isOpen = isOpen,
                    isFirst = index == 0,
                    isLast = index == contacts.lastIndex,
                    icons = icons,
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
    query: String,
    isDialpadSearch: Boolean,
    isOpen: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    icons: dev.alenajam.opendialer.core.common.ui.AppIcons,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onAddContact: () -> Unit,
    onOpenContact: () -> Unit,
    onHistory: () -> Unit
) {
    val context = LocalContext.current
    val phoneType = if (
        contact.phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM &&
        !contact.label.isNullOrBlank()
    ) {
        contact.label.orEmpty()
    } else {
        stringResource(ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(contact.phoneType))
    }
    val title = if (contact.name.isNotBlank()) contact.name else contact.number
    val highlightedTitle = highlightSearchMatch(
        context = context,
        text = title,
        query = query,
        isDialpadSearch = isDialpadSearch,
        isPhoneNumber = contact.name.isBlank(),
    )
    val highlightedNumber = highlightSearchMatch(
        context = context,
        text = contact.number,
        query = query,
        isDialpadSearch = isDialpadSearch,
        isPhoneNumber = true,
    )
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 20.dp else 2.dp,
            topEnd = if (isFirst) 20.dp else 2.dp,
            bottomStart = if (isLast) 20.dp else 2.dp,
            bottomEnd = if (isLast) 20.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
                ContactAvatar(
                    name = contact.name.takeIf { it.isNotBlank() },
                    photoUri = contact.image,
                    colorKey = contactAvatarColorKey(contact.name, contact.number),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable(
                            enabled = contact.contactId > 0,
                            onClick = onOpenContact
                        )
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = highlightedTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = phoneType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = highlightedNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                IconButton(onClick = onCall) {
                    AppIcon(
                        icon = icons.phone,
                        contentDescription = stringResource(R.string.call_contact),
                        modifier = Modifier.size(24.dp)
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
                            icon = icons.accountCircle,
                            label = stringResource(R.string.open_contact),
                            roundTop = true,
                            onClick = onOpenContact
                        )
                    } else {
                        ResultActionRow(
                            icon = icons.personAdd,
                            label = stringResource(R.string.add_to_a_contact),
                            roundTop = true,
                            onClick = onAddContact
                        )
                    }

                    ResultActionRow(
                        icon = icons.message,
                        label = stringResource(R.string.send_message),
                        onClick = onMessage
                    )

                    ResultActionRow(
                        icon = icons.history,
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
    icon: IconSource,
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            AppIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ActionsList(
    query: String,
    icons: dev.alenajam.opendialer.core.common.ui.AppIcons,
    onCreateNewContact: () -> Unit,
    onAddToContact: () -> Unit,
    onSendMessage: () -> Unit
) {
    if (query.isBlank()) return

    Column {
        ActionRow(
            icon = icons.personAdd,
            label = stringResource(R.string.create_new_contact),
            onClick = onCreateNewContact
        )

        ActionRow(
            icon = icons.personAdd,
            label = stringResource(R.string.add_to_a_contact),
            onClick = onAddToContact
        )

        ActionRow(
            icon = icons.message,
            label = stringResource(R.string.send_message),
            onClick = onSendMessage
        )
    }
}

@Composable
private fun ActionRow(
    icon: IconSource,
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
            AppIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
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
    selection: TextRange,
    icons: dev.alenajam.opendialer.core.common.ui.AppIcons,
    onQueryChange: (query: String, selection: TextRange) -> Unit,
    onCall: () -> Unit
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tonePlayer = remember(context) { DialpadTonePlayer(context) }

    DisposableEffect(tonePlayer) {
        onDispose(tonePlayer::release)
    }

    fun handleButtonClick(digit: Char) {
        val newQuery = query.replaceRange(selection.start, selection.end, digit.toString())
        onQueryChange(newQuery, TextRange(selection.start + 1))
    }

    fun insertDialModifier(modifier: Char) {
        val newQuery = query.replaceRange(selection.start, selection.end, modifier.toString())
        onQueryChange(newQuery, TextRange(selection.start + 1))
        overflowExpanded = false
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
                Box {
                    IconButton(
                        onClick = { overflowExpanded = true },
                        enabled = query.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.dialpad_more_options)
                        )
                    }

                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_2_second_pause)) },
                            onClick = { insertDialModifier(',') }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_wait)) },
                            onClick = { insertDialModifier(';') }
                        )
                    }
                }

                TextField(
                    modifier = Modifier.weight(1f),
                    value = TextFieldValue(text = query, selection = selection),
                    onValueChange = { onQueryChange(it.text, it.selection) },
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

                val isBackspaceEnabled = query.isNotEmpty()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .alpha(if (isBackspaceEnabled) 1f else 0.38f)
                        .combinedClickable(
                            enabled = isBackspaceEnabled,
                            onClick = {
                                if (selection.end > selection.start) {
                                    onQueryChange(
                                        query.replaceRange(selection.start, selection.end, ""),
                                        TextRange(selection.start)
                                    )
                                } else if (selection.start > 0) {
                                    onQueryChange(
                                        query.replaceRange(
                                            selection.start - 1,
                                            selection.end,
                                            ""
                                        ),
                                        TextRange(selection.start - 1)
                                    )
                                }
                            },
                            onLongClick = {
                                onQueryChange("", TextRange.Zero)
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = stringResource(R.string.backspace)
                    )
                }
            }

            Dialpad(
                onDigitClick = ::handleButtonClick,
                onDigitPress = tonePlayer::start,
                onDigitRelease = tonePlayer::stop,
                onZeroLongClick = { handleButtonClick('+') }
            )

            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalCustomColorsScheme.current.success,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(60.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        icon = icons.phone,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(text = stringResource(R.string.dialpad_button_call_label))
                }
            }
        }
    }
}
