package dev.alenajam.opendialer.feature.callDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallMissed
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.forwardingPainter
import dev.alenajam.opendialer.data.calls.CallType
import dev.alenajam.opendialer.data.calls.ContactInfo
import dev.alenajam.opendialer.data.calls.DetailCall
import dev.alenajam.opendialer.data.calls.DialerCall
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date

@Composable
fun CallDetailScreen(
    viewModel: DialerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val call = viewModel.call.observeAsState()
    val isAnon = call.value?.isAnonymous() == true
    val childCalls = call.value?.childCalls ?: emptyList()

    Scaffold(
        topBar = {
            TopBar(
                call = call.value,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            BottomBar(
                isAnon = isAnon,
                makeCall = { viewModel.makeCall(call.value!!.number!!) },
                sendMessage = viewModel::sendMessage,
                copyNumber = { viewModel.copyNumber(call.value!!) },
                dialNumber = { viewModel.editNumberBeforeCall(call.value!!) },
                deleteCalls = { viewModel.deleteCalls(call.value!!) }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxHeight()
                .padding(innerPadding)
        ) {
            LazyColumn {
                items(childCalls) { call ->
                    CallRow(call = call)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    call: DialerCall?,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            if (call != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val placeholder = forwardingPainter(
                        painter = rememberVectorPainter(Icons.Filled.AccountCircle),
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    AsyncImage(
                        model = call.contactInfo.photoUri,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        placeholder = placeholder,
                        error = placeholder,
                        fallback = placeholder
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (call.isAnonymous()) stringResource(id = R.string.anonymous)
                        else if (!call.contactInfo.name.isNullOrBlank()) call.contactInfo.name!!
                        else call.contactInfo.number!!,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
            }
        }
    )
}

@Composable
private fun BottomBar(
    isAnon: Boolean,
    makeCall: () -> Unit,
    sendMessage: () -> Unit,
    copyNumber: () -> Unit,
    dialNumber: () -> Unit,
    deleteCalls: () -> Unit,
) {
    BottomAppBar(
        actions = {
            if (!isAnon) {
                IconButton(onClick = sendMessage) {
                    Icon(Icons.Outlined.Message, contentDescription = "Localized description")
                }
                IconButton(onClick = copyNumber) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Localized description")
                }
                IconButton(onClick = dialNumber) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Localized description")
                }
            }
            IconButton(onClick = deleteCalls) {
                Icon(Icons.Outlined.Delete, contentDescription = "Localized description")
            }
        },
        floatingActionButton = {
            if (!isAnon) {
                FloatingActionButton(
                    onClick = makeCall,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(Icons.Outlined.Call, "Localized description")
                }
            }
        }
    )
}

@Composable
private fun CallRow(
    call: DetailCall
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
    ) {
        Icon(
            imageVector = when (call.type) {
                CallType.INCOMING, CallType.ANSWERED_EXTERNALLY -> Icons.Outlined.CallReceived
                CallType.OUTGOING -> Icons.Outlined.CallMade
                CallType.MISSED, CallType.REJECTED -> Icons.Outlined.CallMissed
                CallType.VOICEMAIL -> Icons.Outlined.Voicemail
                CallType.BLOCKED -> Icons.Outlined.Block
            }, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (call.type) {
                    CallType.OUTGOING -> stringResource(id = R.string.outgoing_call)
                    CallType.INCOMING, CallType.ANSWERED_EXTERNALLY -> stringResource(id = R.string.incoming_call)
                    CallType.MISSED -> stringResource(id = R.string.missed_call)
                    CallType.VOICEMAIL -> stringResource(id = R.string.voicemail_call)
                    CallType.REJECTED -> stringResource(id = R.string.rejected_call)
                    CallType.BLOCKED -> stringResource(id = R.string.blocked_call)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = PrettyTime().format(call.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = CommonUtils.getDurationTimeStringMinimal(call.duration * 1000),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val incomingDetailCallMock = DetailCall(
    id = 1,
    date = Date(),
    type = CallType.INCOMING,
    duration = 500L,
)

private val callMock = DialerCall(
    id = 1,
    number = "333123456",
    date = Date(),
    type = CallType.OUTGOING,
    options = emptyList(),
    childCalls = listOf(
        incomingDetailCallMock
    ),
    contactInfo = ContactInfo(
        name = "John Doe",
        number = "3331234567",
        photoUri = null
    )
)

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(call = callMock) {}
}

@Preview(showBackground = true)
@Composable
private fun BottomBarPreview() {
    BottomBar(
        isAnon = false,
        makeCall = {},
        sendMessage = {},
        copyNumber = {},
        dialNumber = {},
        deleteCalls = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun CallRowPreview() {
    CallRow(call = incomingDetailCallMock)
}