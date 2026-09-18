package dev.alenajam.opendialer.feature.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.ui.contactAvatarColorKey
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.data.contacts.DialerContactSummary

private val ContactListPaper = androidx.compose.ui.graphics.Color(0xFFF9F7FC)
private val ContactListInk = androidx.compose.ui.graphics.Color(0xFF202020)

data class ContactRowTrailingContent(
    val content: @Composable (DialerContactSummary, (Int, String?) -> Unit) -> Unit,
)

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    searchQuery: String = "",
    @Suppress("UNUSED_PARAMETER") onOpenHistory: (callIds: List<Int>) -> Unit = {},
    contactRowTrailingContent: ContactRowTrailingContent? = null,
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
    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ContactListPaper,
    ) {
        if (!hasPermission.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.CenterVertically
                ),
            ) {
                RetroContactText(
                    text = stringResource(R.string.placeholder_contacts),
                    size = 16.sp,
                    textAlign = TextAlign.Center,
                )
                RetroContactCommand(
                    label = stringResource(R.string.turn_on),
                    onClick = { requestPermissions.launch(input = PermissionUtils.contactsPermissions) },
                )
            }
            return@Surface
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 2.dp,
                    top = 12.dp,
                    end = 2.dp,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            if (searchQuery.isBlank()) {
                item(key = "new-contact") {
                    RetroContactCommand(
                        label = stringResource(R.string.new_contact),
                        onClick = { CommonUtils.createContact(context, null) },
                    )
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
                            onOpenContact = { viewModel.openContact(item.contact.id) },
                            trailingContent = contactRowTrailingContent?.let { trailingContent ->
                                { trailingContent.content(item.contact, onOpenSettingsSubpage) }
                            },
                        )
                    }
                }
            }
            }
            ContactFastScroller(
                listState = listState,
                contentDescription = stringResource(R.string.fast_scroll_contacts),
                modifier = Modifier.align(Alignment.CenterEnd).padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
internal fun ContactFastScroller(
    listState: LazyListState,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val layoutInfo = listState.layoutInfo
    val visibleItemCount = layoutInfo.visibleItemsInfo.size
    val totalItemCount = layoutInfo.totalItemsCount
    if (totalItemCount <= 12 || totalItemCount <= visibleItemCount * 2) return

    val position = (listState.firstVisibleItemIndex.toFloat() /
        (totalItemCount - visibleItemCount).coerceAtLeast(1)).coerceIn(0f, 1f)
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = modifier
            .width(40.dp)
            .fillMaxHeight()
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        scope.launch { listState.requestScrollToItem((offset.y / size.height * (totalItemCount - visibleItemCount).coerceAtLeast(0)).roundToInt()) }
                    },
                    onDrag = { change, _ ->
                        scope.launch { listState.requestScrollToItem((change.position.y / size.height * (totalItemCount - visibleItemCount).coerceAtLeast(0)).roundToInt()) }
                    },
                )
            },
    ) {
        val thumbHeight = (maxHeight * (visibleItemCount.toFloat() / totalItemCount)).coerceIn(48.dp, maxHeight)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = 8.dp)
                .width(6.dp)
                .height(thumbHeight)
                .offset(y = (maxHeight - thumbHeight).coerceAtLeast(0.dp) * position)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
        )
    }
}

@Composable
private fun ProfileContactCard(
    contact: DialerContactSummary,
    onOpenProfile: () -> Unit,
    onShareProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenProfile)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(
            name = contact.name,
            photoUri = contact.image,
            colorKey = contactAvatarColorKey(contact.name),
            modifier = Modifier.size(42.dp),
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
            RetroContactText(stringResource(R.string.your_info), 13.sp, color = ContactListInk.copy(alpha = 0.75f))
            RetroContactText(contact.name, 16.sp, maxLines = 1)
        }
        AppIcon(
            icon = LocalAppIcons.current.share,
            contentDescription = stringResource(R.string.share_contact),
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onShareProfile),
            tint = ContactListInk,
        )
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        if (isFavorites) {
            AppIcon(
                icon = LocalAppIcons.current.favorite,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ContactListInk,
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        }
        RetroContactText(label, 13.sp, color = ContactListInk.copy(alpha = 0.75f))
    }
}

@Composable
private fun ContactRow(
    contact: DialerContactSummary,
    onOpenContact: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenContact)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(
            name = contact.name,
            photoUri = contact.image,
            colorKey = contactAvatarColorKey(contact.name),
            modifier = Modifier.size(42.dp),
        )
        RetroContactText(
            text = contact.name,
            size = 16.sp,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
            maxLines = 1,
        )
        trailingContent?.let { content -> content() }
    }
}

@Composable
private fun RetroContactCommand(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RetroContactText(label, 18.sp)
        }
    }
}

@Composable
private fun RetroContactText(
    text: String,
    size: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    color: androidx.compose.ui.graphics.Color = ContactListInk,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = size,
        lineHeight = size * 1.15f,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
    )
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
        onOpenContact = {},
    )
}
