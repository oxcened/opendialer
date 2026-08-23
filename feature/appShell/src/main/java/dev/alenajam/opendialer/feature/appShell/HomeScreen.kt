package dev.alenajam.opendialer.feature.appShell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.feature.calls.CallsScreen
import dev.alenajam.opendialer.feature.contacts.ContactsScreen

private enum class HomeTab {
    CALLS,
    CONTACTS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onOpenDialpad: () -> Unit,
    onOpenHistory: (ids: List<Int>) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var currentTab by remember { mutableStateOf(HomeTab.CALLS) }

    Scaffold(
        topBar = {
            SearchBar(
                inputField = @Composable {
                    SearchBarDefaults.InputField(
                        query = "",
                        onQueryChange = {},
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        enabled = false,
                        placeholder = { Text(stringResource(R.string.coming_soon)) },
                        leadingIcon = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.Search, contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            Box {
                                var expanded by remember { mutableStateOf(false) }
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.home_menu_settings_label)) },
                                        onClick = onOpenSettings,
                                    )
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
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == HomeTab.CALLS,
                    icon = {
                        Icon(
                            if (currentTab == HomeTab.CALLS) Icons.Filled.AccessTimeFilled
                            else Icons.Outlined.AccessTime,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(R.string.recents)) },
                    onClick = { currentTab = HomeTab.CALLS },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.CONTACTS,
                    icon = {
                        Icon(
                            if (currentTab == HomeTab.CONTACTS) Icons.Filled.People
                            else Icons.Outlined.People,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(R.string.contacts)) },
                    onClick = { currentTab = HomeTab.CONTACTS },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenDialpad) {
                Icon(Icons.Outlined.Dialpad, contentDescription = null)
            }
        },
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                HomeTab.CALLS -> CallsScreen(onOpenHistory = onOpenHistory)
                HomeTab.CONTACTS -> ContactsScreen()
            }
        }
    }
}
