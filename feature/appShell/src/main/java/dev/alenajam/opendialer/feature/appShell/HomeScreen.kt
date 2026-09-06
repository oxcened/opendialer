package dev.alenajam.opendialer.feature.appShell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.feature.calls.CallsScreen
import dev.alenajam.opendialer.feature.contacts.ContactsScreen
import dev.alenajam.opendialer.feature.contacts.ContactRowTrailingContent
import dev.alenajam.opendialer.feature.contactsSearch.ContactsTextSearchResults
import dev.alenajam.opendialer.feature.voicemail.VoicemailScreen

enum class HomeTab {
    CALLS,
    CONTACTS,
    VOICEMAIL,
    CUSTOM,
}

data class HomeNavigationItem(
    val label: @Composable () -> Unit,
    val icon: @Composable (selected: Boolean) -> Unit,
    val content: @Composable (onOpenSettings: () -> Unit, onOpenSettingsSubpage: (Int, String?) -> Unit, onSetBackAction: (Boolean, () -> Unit) -> Unit) -> Unit,
)

data class HomeScreenConfiguration(
    val showVoicemailInNavigation: Boolean = true,
    val showVoicemailInOverflow: Boolean = false,
    val customNavigationItem: HomeNavigationItem? = null,
    val contactRowTrailingContent: ContactRowTrailingContent? = null,
    /** Gives custom screens such as a game-style profile a clean, full-viewport presentation. */
    val hideSearchAndDialpadOnCustomTab: Boolean = false,
    val customActionBar: (@Composable (onSelect: () -> Unit, onMenu: () -> Unit, onBack: () -> Unit, backEnabled: Boolean) -> Unit)? = null,
    val customContextMenu: (@Composable (currentTab: HomeTab, onCalls: () -> Unit, onContacts: () -> Unit, onCustom: () -> Unit, onDismiss: () -> Unit) -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onOpenDialpad: (String) -> Unit,
    onOpenHistory: (ids: List<Int>) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onAddFavorite: () -> Unit = {},
    onOpenSettingsSubpage: (Int, String?) -> Unit = { _, _ -> },
    onOpenVoicemail: () -> Unit = {},
    configuration: HomeScreenConfiguration = HomeScreenConfiguration(),
) {
    var currentTab by rememberSaveable { mutableStateOf(HomeTab.CALLS) }
    var searchQuery by remember { mutableStateOf("") }
    var customContextMenuOpen by remember { mutableStateOf(false) }
    var customBackEnabled by remember { mutableStateOf(false) }
    var customBackAction by remember { mutableStateOf<() -> Unit>({}) }
    val isSearchActive = searchQuery.isNotEmpty()

    BackHandler(enabled = isSearchActive) {
        searchQuery = ""
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            if (!configuration.hideSearchAndDialpadOnCustomTab || currentTab != HomeTab.CUSTOM) SearchBar(
                inputField = @Composable {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text(stringResource(R.string.search_contacts)) },
                        leadingIcon = {
                            IconButton(onClick = { if (isSearchActive) searchQuery = "" }) {
                                AppIcon(
                                    if (isSearchActive) {
                                        LocalAppIcons.current.arrowLeft
                                    } else {
                                        LocalAppIcons.current.search
                                    },
                                    contentDescription = if (isSearchActive) {
                                        stringResource(R.string.clear_search)
                                    } else {
                                        null
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            if (isSearchActive) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    AppIcon(
                                        LocalAppIcons.current.close,
                                        contentDescription = stringResource(R.string.clear_search),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Box {
                                    var expanded by remember { mutableStateOf(false) }
                                    IconButton(onClick = { expanded = true }) {
                                        AppIcon(LocalAppIcons.current.more, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        if (configuration.showVoicemailInOverflow) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.voicemail)) },
                                                onClick = {
                                                    onOpenVoicemail()
                                                    expanded = false
                                                },
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.screen_settings_title)) },
                                            onClick = onOpenSettings,
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.screen_about_title)) },
                                            onClick = onOpenAbout,
                                        )
                                    }
                                }
                            }
                        },
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            ) {}
        },
        bottomBar = {
            val icons = LocalAppIcons.current
            if (configuration.customActionBar != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    configuration.customActionBar.invoke(
                        { onOpenDialpad("") },
                        { if (customBackEnabled) customBackAction() else customContextMenuOpen = true },
                        { customBackAction() },
                        customBackEnabled,
                    )
                }
            } else NavigationBar {
                NavigationBarItem(
                    selected = currentTab == HomeTab.CALLS,
                    icon = {
                        AppIcon(
                            if (currentTab == HomeTab.CALLS) icons.recentsSelected else icons.recents,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.recents)) },
                    onClick = { currentTab = HomeTab.CALLS },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.CONTACTS,
                    icon = {
                        AppIcon(
                            if (currentTab == HomeTab.CONTACTS) icons.contactsSelected else icons.contacts,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.contacts)) },
                    onClick = { currentTab = HomeTab.CONTACTS },
                )
                if (configuration.showVoicemailInNavigation) {
                    NavigationBarItem(
                        selected = currentTab == HomeTab.VOICEMAIL,
                        icon = {
                            AppIcon(
                                if (currentTab == HomeTab.VOICEMAIL) icons.voicemailSelected else icons.voicemail,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(stringResource(R.string.voicemail)) },
                        onClick = { currentTab = HomeTab.VOICEMAIL },
                    )
                }
                configuration.customNavigationItem?.let { item ->
                    NavigationBarItem(
                        selected = currentTab == HomeTab.CUSTOM,
                        icon = { item.icon(currentTab == HomeTab.CUSTOM) },
                        label = item.label,
                        onClick = { currentTab = HomeTab.CUSTOM },
                    )
                }
            }
        },
        floatingActionButton = {
            if (!configuration.hideSearchAndDialpadOnCustomTab || currentTab != HomeTab.CUSTOM) {
                FloatingActionButton(onClick = { onOpenDialpad("") }) {
                    AppIcon(
                        LocalAppIcons.current.dialpad,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Surface {
            if (searchQuery.isNotBlank()) {
                ContactsTextSearchResults(query = searchQuery, onOpenHistory = onOpenHistory)
            } else {
                when (currentTab) {
                    HomeTab.CALLS -> CallsScreen(
                        onOpenHistory = onOpenHistory,
                        onOpenContacts = { currentTab = HomeTab.CONTACTS },
                        onAddFavorite = onAddFavorite,
                        onEditNumberBeforeCall = onOpenDialpad,
                    )
                    HomeTab.CONTACTS -> ContactsScreen(
                        onOpenHistory = onOpenHistory,
                        contactRowTrailingContent = configuration.contactRowTrailingContent,
                        onOpenSettingsSubpage = onOpenSettingsSubpage,
                    )
                    HomeTab.VOICEMAIL -> VoicemailScreen()
                    HomeTab.CUSTOM -> configuration.customNavigationItem?.content(
                        onOpenSettings,
                        onOpenSettingsSubpage,
                        { enabled, action ->
                            customBackEnabled = enabled
                            customBackAction = action
                        },
                    )
                }
            }
            }
        }
    }
    if (customContextMenuOpen) {
        configuration.customContextMenu?.invoke(
            currentTab,
            { customContextMenuOpen = false; currentTab = HomeTab.CALLS },
            { customContextMenuOpen = false; currentTab = HomeTab.CONTACTS },
            { customContextMenuOpen = false; currentTab = HomeTab.CUSTOM },
            { customContextMenuOpen = false },
        )
    }
    }
}
