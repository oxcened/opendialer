package dev.alenajam.opendialer.feature.appShell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    val content: @Composable (onOpenSettingsSubpage: (Int) -> Unit) -> Unit,
)

data class HomeScreenConfiguration(
    val showVoicemailInNavigation: Boolean = true,
    val showVoicemailInOverflow: Boolean = false,
    val customNavigationItem: HomeNavigationItem? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onOpenDialpad: (String) -> Unit,
    onOpenHistory: (ids: List<Int>) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onAddFavorite: () -> Unit = {},
    onOpenSettingsSubpage: (Int) -> Unit = {},
    onOpenVoicemail: () -> Unit = {},
    configuration: HomeScreenConfiguration = HomeScreenConfiguration(),
) {
    var currentTab by rememberSaveable { mutableStateOf(HomeTab.CALLS) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SearchBar(
                inputField = @Composable {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text(stringResource(R.string.search_contacts)) },
                        leadingIcon = {
                            IconButton(onClick = {}) {
                                AppIcon(
                                    LocalAppIcons.current.search,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
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
                                        Icon(Icons.Default.MoreVert, contentDescription = null)
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
            NavigationBar {
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
            FloatingActionButton(onClick = { onOpenDialpad("") }) {
                AppIcon(
                    LocalAppIcons.current.dialpad,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
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
                    HomeTab.CONTACTS -> ContactsScreen(onOpenHistory = onOpenHistory)
                    HomeTab.VOICEMAIL -> VoicemailScreen()
                    HomeTab.CUSTOM -> configuration.customNavigationItem?.content(onOpenSettingsSubpage)
                }
            }
        }
    }
}
