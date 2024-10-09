package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.forwardingPainter
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact

@Composable
internal fun ContactsSearchScreen(
    viewModel: SearchContactsViewModel = viewModel(),
    navController: NavController,
) {
    val result = viewModel.result.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current
    val requestCallPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.handleCallRuntimePermissionGranted()
        }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box {
            SearchList(
                result = result.value,
                hasPermission = hasPermission.value,
                handleRuntimePermissionGranted = { viewModel.handleRuntimePermissionGranted(query = query) }
            )

            Dialpad(
                query = query,
                onQueryChange = {
                    query = it
                    viewModel.searchContactsByDialpad(it)
                },
                onCall = {
                    viewModel.makeCall(
                        activity = context.getActivity() as Activity,
                        number = query
                    ).let {
                        if (!it) {
                            requestCallPermissions.launch(PermissionUtils.makeCallPermissions)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BoxScope.SearchList(
    result: SearchContactsViewModel.Result?,
    hasPermission: Boolean,
    handleRuntimePermissionGranted: () -> Unit
) {
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
                handleRuntimePermissionGranted()
            }
        }

    if (!hasPermission) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterVertically
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.placeholder_search_permissions))
            Button(
                onClick = { requestPermissions.launch(input = PermissionUtils.searchPermissions) }
            ) {
                Text(text = stringResource(R.string.turn_on))
            }
        }
        return
    }

    LazyColumn {
        result?.contacts?.let { contacts ->
            items(contacts) { contact ->
                ResultRow(contact, onClick = { })
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
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            ) {
                val placeholder = forwardingPainter(
                    painter = rememberVectorPainter(Icons.Filled.AccountCircle),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun BoxScope.Dialpad(
    query: String,
    onQueryChange: (query: String) -> Unit,
    onCall: () -> Unit
) {
    var selection by remember { mutableStateOf(TextRange.Zero) }

    fun handleButtonClick(button: String) {
        onQueryChange(query.replaceRange(selection.start, selection.end, button))
        selection = TextRange(selection.start + 1)
    }

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
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
                        focusedContainerColor = Color.Transparent
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

            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { handleButtonClick("1") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "1",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "",
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("2") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "2",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "abc",
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("3") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "3",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "def",
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { handleButtonClick("4") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "4",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "ghi",
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("5") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "5",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "jkl",
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("6") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "6",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "mno",
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { handleButtonClick("7") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "7",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "pqrs",
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("8") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "8",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "tuv",
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("9") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "9",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "wxyz",
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { handleButtonClick("*") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Text(
                        text = "*",
                        fontSize = 25.sp
                    )
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("0") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "0",
                            fontSize = 25.sp
                        )

                        Text(
                            text = "+",
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { handleButtonClick("#") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(),
                ) {
                    Text(
                        text = "#",
                        fontSize = 25.sp
                    )
                }
            }

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