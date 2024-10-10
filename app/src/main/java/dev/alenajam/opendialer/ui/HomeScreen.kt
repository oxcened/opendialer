package dev.alenajam.opendialer.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.feature.calls.CallsScreen
import dev.alenajam.opendialer.feature.contacts.ContactsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onOpenDialpad: () -> Unit,
    onOpenHistory: (ids: List<Int>) -> Unit
) {
    var selectedNavigationItem by remember { mutableStateOf("CALLS") }
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
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null
                            )
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
                val isSelected = { item: String -> item == selectedNavigationItem }
                NavigationBarItem(
                    selected = isSelected("CALLS"),
                    icon = {
                        Icon(
                            imageVector = if (isSelected("CALLS")) Icons.Filled.AccessTimeFilled else Icons.Outlined.AccessTime,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(R.string.recents)) },
                    onClick = { selectedNavigationItem = "CALLS" },
                )

                NavigationBarItem(
                    selected = isSelected("CONTACTS"),
                    icon = {
                        Icon(
                            imageVector = if (isSelected("CONTACTS")) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(R.string.contacts)) },
                    onClick = { selectedNavigationItem = "CONTACTS" },
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
            when (selectedNavigationItem) {
                "CALLS" -> CallsScreen(
                    onOpenHistory = onOpenHistory
                )

                "CONTACTS" -> ContactsScreen()
            }
        }
    }
}