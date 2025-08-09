package dev.alenajam.opendialer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.feature.calls.CallsScreen
import dev.alenajam.opendialer.feature.contacts.ContactsScreen

private enum class Route {
    CALLS,
    CONTACTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onOpenDialpad: () -> Unit,
    onOpenHistory: (ids: List<Int>) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var currentRoute by remember { mutableStateOf(Route.CALLS) }

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
                        placeholder = { Text(text = stringResource(id = R.string.coming_soon)) },
                        leadingIcon = {
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = null
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.TopStart)
                                ) {
                                    var expanded by remember { mutableStateOf(false) }

                                    IconButton(onClick = { expanded = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = null
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text(text = stringResource(R.string.home_menu_settings_label)) },
                                            onClick = onOpenSettings,
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {}
        },
        bottomBar = {
            NavigationBar {
                val isSelected = { item: Route -> item == currentRoute }

                NavigationBarItem(
                    selected = isSelected(Route.CALLS),
                    icon = {
                        Icon(
                            imageVector = if (isSelected(Route.CALLS)) Icons.Filled.AccessTimeFilled else Icons.Outlined.AccessTime,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(R.string.recents)) },
                    onClick = { currentRoute = Route.CALLS },
                )

                NavigationBarItem(
                    selected = isSelected(Route.CONTACTS),
                    icon = {
                        Icon(
                            imageVector = if (isSelected(Route.CONTACTS)) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(R.string.contacts)) },
                    onClick = { currentRoute = Route.CONTACTS },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenDialpad) {
                Icon(imageVector = Icons.Outlined.Dialpad, contentDescription = null)
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (currentRoute) {
                Route.CALLS -> CallsScreen(
                    onOpenHistory = onOpenHistory
                )

                Route.CONTACTS -> ContactsScreen()
            }
        }
    }
}