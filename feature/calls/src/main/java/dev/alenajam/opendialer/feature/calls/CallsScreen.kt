package dev.alenajam.opendialer.feature.calls

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallMissed
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.core.common.ui.CallAccountPicker
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.data.calls.CallType
import dev.alenajam.opendialer.data.calls.ContactInfo
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.data.contacts.DialerContact
import org.ocpsoft.prettytime.PrettyTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

private enum class CallFilter(val labelRes: Int) {
    ALL(R.string.filter_all),
    MISSED(R.string.filter_missed),
    CONTACTS(R.string.filter_contacts),
}

@Composable
fun CallsScreen(
    viewModel: CallsViewModel = hiltViewModel(),
    onOpenHistory: (callIds: List<Int>) -> Unit,
    onOpenContacts: () -> Unit = {},
    onAddFavorite: () -> Unit = {},
    onEditNumberBeforeCall: (String) -> Unit = {}
) {
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.recentsPermissions.all { result[it] == true }) {
                viewModel.handleRuntimePermissionGranted()
            }
        }

    var pendingCallNumber by remember { mutableStateOf<String?>(null) }
    var callAccounts by remember { mutableStateOf<List<CallAccount>?>(null) }
    val requestCallPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (PermissionUtils.makeCallPermissions.all { result[it] == true }) {
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

    val calls = viewModel.calls.collectAsStateWithLifecycle()
    val favorites = viewModel.favorites.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var openRowId by remember { mutableStateOf<Int?>(null) }
    var favoritesExpanded by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(CallFilter.ALL) }

    val icons = LocalAppIcons.current
    val filteredCalls = remember(calls.value, selectedFilter) {
        when (selectedFilter) {
            CallFilter.MISSED -> calls.value.filter { it.type == CallType.MISSED || it.type == CallType.REJECTED }
            CallFilter.CONTACTS -> calls.value.filter { it.isContactSaved() }
            else -> calls.value
        }
    }

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

    val callsByDate = remember(filteredCalls) {
        filteredCalls.groupBy { call ->
            call.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.startCache()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.stopCache()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            if (!hasPermission.value) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.placeholder_call_log),
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = { requestPermissions.launch(input = PermissionUtils.recentsPermissions) }
                    ) {
                        Text(text = stringResource(R.string.turn_on))
                    }
                }
                return@Surface
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    FavoritesSection(
                        favorites = favorites.value,
                        expanded = favoritesExpanded,
                        onToggleExpand = { favoritesExpanded = !favoritesExpanded },
                        onAddClick = onAddFavorite,
                        onViewContactsClick = onOpenContacts,
                        onFavoriteClick = { placeCall(it.number) },
                        onRemoveFavorite = { viewModel.unstarContact(it.id) }
                    )
                }

                callsByDate.forEach { (date, callsForDate) ->
                    item(key = "header-$date") {
                        CallDateHeader(date)
                    }
                    itemsIndexed(callsForDate, key = { _, call -> call.id }) { index, call ->
                        val isOpen = openRowId == call.id

                        CallRow(call = call,
                            isOpen = isOpen,
                            roundTop = index == 0,
                            roundBottom = index == callsForDate.lastIndex,
                            icons = icons,
                            onClick = { openRowId = if (isOpen) null else call.id },
                            makeCall = { placeCall(call.contactInfo.number!!) },
                            sendMessage = { viewModel.sendMessage(call.contactInfo.number!!) },
                            addContact = { viewModel.addToContact(call.contactInfo.number!!) },
                            openContact = { viewModel.openContact(call) },
                            openHistory = { onOpenHistory(call.childCalls.map { it.id }) },
                            copyNumber = { viewModel.copyNumber(call.contactInfo.number!!) },
                            editNumberBeforeCall = { onEditNumberBeforeCall(call.contactInfo.number!!) },
                            blockNumber = { viewModel.blockNumber(call.contactInfo.number!!) },
                            deleteCall = { viewModel.deleteCall(call) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: CallFilter,
    onFilterSelected: (CallFilter) -> Unit
) {
    val filters = CallFilter.entries
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
            label = { Text(stringResource(filter.labelRes)) }
            )
        }
    }
}

@Composable
private fun FavoritesSection(
    favorites: List<DialerContact>,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddClick: () -> Unit,
    onViewContactsClick: () -> Unit,
    onFavoriteClick: (DialerContact) -> Unit,
    onRemoveFavorite: (DialerContact) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onToggleExpand)
        ) {
            Text(
                text = stringResource(R.string.favorites),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.view_contacts),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .clickable(onClick = onViewContactsClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                favorites.forEach { favorite ->
                    FavoriteItem(
                        favorite = favorite,
                        onClick = { onFavoriteClick(favorite) },
                        onRemove = { onRemoveFavorite(favorite) }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp)
                ) {
                    Surface(
                        onClick = onAddClick,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.PersonAddAlt, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.add),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteItem(
    favorite: DialerContact,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            ContactAvatar(
                name = favorite.name,
                photoUri = favorite.image,
                colorKey = favorite.number,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { showMenu = true }
                    )
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(20.dp),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.remove)) },
                    onClick = {
                        onRemove()
                        showMenu = false
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = favorite.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CallDateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> stringResource(R.string.call_log_date_today)
        today.minusDays(1) -> stringResource(R.string.call_log_date_yesterday)
        else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    Text(
        text = label,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CallRow(
    call: DialerCall,
    isOpen: Boolean,
    roundTop: Boolean,
    roundBottom: Boolean,
    icons: dev.alenajam.opendialer.core.common.ui.AppIcons,
    onClick: () -> Unit,
    makeCall: () -> Unit,
    sendMessage: () -> Unit,
    addContact: () -> Unit,
    openContact: () -> Unit,
    openHistory: () -> Unit,
    copyNumber: () -> Unit,
    editNumberBeforeCall: () -> Unit,
    blockNumber: () -> Unit,
    deleteCall: () -> Unit,
) {
    var contextMenuExpanded by remember { mutableStateOf(false) }
    var showBlockConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val resources = LocalContext.current.resources
    val relativeTime = PrettyTime().format(call.date)
    val phoneType = call.contactInfo.type
        ?.takeIf { call.isContactSaved() }
        ?.let {
            ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                resources,
                it,
                call.contactInfo.label,
            ).toString()
        }
        .orEmpty()
    val cardShape = RoundedCornerShape(
        topStart = if (roundTop) 20.dp else 2.dp,
        topEnd = if (roundTop) 20.dp else 2.dp,
        bottomStart = if (roundBottom) 20.dp else 2.dp,
        bottomEnd = if (roundBottom) 20.dp else 2.dp,
    )

    Box {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 1.dp)
                .clip(cardShape)
                .combinedClickable(onClick = onClick, onLongClick = { contextMenuExpanded = true }),
            shape = cardShape,
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
                    name = call.contactInfo.name.takeUnless { call.isVoicemailNumber },
                    photoUri = call.contactInfo.photoUri.takeUnless { call.isVoicemailNumber },
                    colorKey = call.contactInfo.number.orEmpty(),
                    fallbackIcon = if (call.isVoicemailNumber) {
                        Icons.Outlined.Voicemail
                    } else {
                        Icons.Outlined.Person
                    },
                    contentDescription = if (call.isVoicemailNumber) {
                        stringResource(R.string.filter_voicemail)
                    } else if (call.isContactSaved()) {
                        stringResource(R.string.open_contact)
                    } else {
                        null
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = call.isContactSaved() && !call.isVoicemailNumber,
                            onClick = openContact
                        )
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (call.isVoicemailNumber) stringResource(R.string.filter_voicemail)
                        else if (call.isAnonymous()) stringResource(id = R.string.anonymous)
                        else if (!call.contactInfo.name.isNullOrBlank()) call.contactInfo.name!!
                        else call.contactInfo.number!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = when (call.type) {
                                CallType.INCOMING, CallType.ANSWERED_EXTERNALLY -> icons.callReceived
                                CallType.OUTGOING -> icons.callMade
                                CallType.MISSED, CallType.REJECTED -> icons.callMissed
                                CallType.VOICEMAIL -> icons.voicemail
                                CallType.BLOCKED -> icons.block
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = if (phoneType.isBlank()) {
                                relativeTime
                            } else {
                                stringResource(R.string.call_log_type_time, phoneType, relativeTime)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!call.isAnonymous()) {
                    IconButton(onClick = makeCall) {
                        Icon(
                            imageVector = icons.phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isOpen) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    if (!call.isAnonymous()) {
                        if (!call.isContactSaved()) {
                            CallRowButton(
                                label = stringResource(R.string.add_to_a_contact),
                                icon = icons.personAdd,
                                roundTop = true,
                                onClick = addContact
                            )
                        }

                        CallRowButton(
                            label = stringResource(R.string.send_message),
                            icon = icons.message,
                            roundTop = call.isContactSaved(),
                            onClick = sendMessage,
                        )
                    }

                    CallRowButton(
                        label = stringResource(R.string.history),
                        icon = icons.history,
                        roundTop = call.isAnonymous(),
                        roundBottom = true,
                        onClick = openHistory,
                    )
                }
            }
        }
        }

        DropdownMenu(
            expanded = contextMenuExpanded,
            onDismissRequest = { contextMenuExpanded = false }
        ) {
            if (!call.isAnonymous()) {
                DropdownMenuItem(text = { Text(stringResource(R.string.copy_number)) }, onClick = {
                    contextMenuExpanded = false
                    copyNumber()
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.edit_number_before_call)) }, onClick = {
                    contextMenuExpanded = false
                    editNumberBeforeCall()
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.blockThisCaller)) }, onClick = {
                    contextMenuExpanded = false
                    showBlockConfirmation = true
                })
            }
            DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, onClick = {
                contextMenuExpanded = false
                showDeleteConfirmation = true
            })
        }
    }

    if (showBlockConfirmation) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmation = false },
            title = { Text(stringResource(R.string.block_confirmation_title, call.contactInfo.number.orEmpty())) },
            text = { Text(stringResource(R.string.block_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = { showBlockConfirmation = false; blockNumber() }) { Text(stringResource(R.string.blockThisCaller)) }
            },
            dismissButton = { TextButton(onClick = { showBlockConfirmation = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_call_title)) },
            text = { Text(stringResource(R.string.delete_call_message)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirmation = false; deleteCall() }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun CallRowButton(
    label: String,
    icon: ImageVector,
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private val incomingCallMock = DialerCall(
    id = 1,
    number = "3331234567",
    date = Date(),
    type = CallType.INCOMING,
    options = listOf(),
    childCalls = listOf(),
    contactInfo = ContactInfo(
        number = "3331234567"
    )
)
private val outgoingCallMock = incomingCallMock.copy(type = CallType.OUTGOING)
private val anonymousCallMock = incomingCallMock.copy(
    number = null, contactInfo = ContactInfo(number = null)
)

@Preview(showBackground = true)
@Composable
private fun IncomingCallPreview() {
    CallRow(call = incomingCallMock,
        isOpen = false,
        roundTop = true,
        roundBottom = true,
        icons = dev.alenajam.opendialer.core.common.ui.DefaultAppIcons,
        onClick = {},
        makeCall = {},
        addContact = {},
        openContact = {},
        sendMessage = {},
        openHistory = {},
        copyNumber = {},
        editNumberBeforeCall = {},
        blockNumber = {},
        deleteCall = {})
}

@Preview(showBackground = true)
@Composable
private fun OutgoingCallPreview() {
    CallRow(call = outgoingCallMock,
        isOpen = false,
        roundTop = true,
        roundBottom = true,
        icons = dev.alenajam.opendialer.core.common.ui.DefaultAppIcons,
        onClick = {},
        makeCall = {},
        addContact = {},
        openContact = {},
        sendMessage = {},
        openHistory = {},
        copyNumber = {},
        editNumberBeforeCall = {},
        blockNumber = {},
        deleteCall = {})
}

@Preview(showBackground = true)
@Composable
private fun AnonymousCallPreview() {
    CallRow(call = anonymousCallMock,
        isOpen = false,
        roundTop = true,
        roundBottom = true,
        icons = dev.alenajam.opendialer.core.common.ui.DefaultAppIcons,
        onClick = {},
        makeCall = {},
        addContact = {},
        openContact = {},
        sendMessage = {},
        openHistory = {},
        copyNumber = {},
        editNumberBeforeCall = {},
        blockNumber = {},
        deleteCall = {})
}
