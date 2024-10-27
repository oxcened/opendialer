package dev.alenajam.opendialer.feature.contactsSearch

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.forwardingPainter
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact

@Composable
fun ContactsSearchScreen(
    viewModel: SearchContactsViewModel = hiltViewModel(),
) {
    val result = viewModel.result.collectAsStateWithLifecycle()
    val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
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
            Dialpad(
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
            SearchList(
                result = result.value,
                hasPermission = hasPermission.value,
                onRuntimePermissionGranted = { viewModel.handleRuntimePermissionGranted(query = query) },
                onResultClick = { makeCall(it.number) }
            )
        }
    }
}

@Composable
private fun SearchList(
    result: SearchContactsViewModel.Result?,
    hasPermission: Boolean,
    onRuntimePermissionGranted: () -> Unit,
    onResultClick: (contact: DialerSearchContact) -> Unit
) {
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
                onRuntimePermissionGranted()
            }
        }

    if (!hasPermission) {
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
        return
    }

    LazyColumn {
        result?.contacts?.let { contacts ->
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
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
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
private fun Dialpad(
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
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 24.dp)
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
                DigitButton(
                    digit = "1",
                    subtitle = "",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "2",
                    subtitle = "abc",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "3",
                    subtitle = "def",
                    onClick = ::handleButtonClick
                )
            }

            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DigitButton(
                    digit = "4",
                    subtitle = "ghi",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "5",
                    subtitle = "jkl",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "6",
                    subtitle = "mno",
                    onClick = ::handleButtonClick
                )
            }

            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DigitButton(
                    digit = "7",
                    subtitle = "pqrs",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "8",
                    subtitle = "tuv",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "9",
                    subtitle = "wxyz",
                    onClick = ::handleButtonClick
                )
            }

            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DigitButton(
                    digit = "*",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "0",
                    subtitle = "+",
                    onClick = ::handleButtonClick
                )

                DigitButton(
                    digit = "#",
                    onClick = ::handleButtonClick
                )
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

@Composable
private fun RowScope.DigitButton(
    digit: String,
    subtitle: String? = null,
    onClick: (digit: String) -> Unit
) {
    FilledTonalButton(
        onClick = { onClick(digit) },
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.filledTonalButtonColors().copy(
            containerColor = Color.White,
        )
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit,
                fontSize = 25.sp
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                )
            }
        }
    }
}