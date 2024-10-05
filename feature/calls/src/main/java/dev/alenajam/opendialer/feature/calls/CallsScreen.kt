package dev.alenajam.opendialer.feature.calls

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallMissed
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.forwardingPainter
import dev.alenajam.opendialer.data.calls.CallType
import dev.alenajam.opendialer.data.calls.ContactInfo
import dev.alenajam.opendialer.data.calls.DialerCall
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date

@Composable
internal fun CallsScreen(
  viewModel: CallsViewModel = viewModel(), navController: NavController,
) {
  val requestPermissions =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
      if (PermissionUtils.recentsPermissions.all { result[it] == true }) {
        viewModel.handleCallsPermissionGranted()
      }
    }

  val calls = viewModel.calls.collectAsStateWithLifecycle()
  val hasPermissions = viewModel.hasCallsPermission.collectAsStateWithLifecycle()
  var openRowId by remember { mutableStateOf<Int?>(null) }

  Surface(modifier = Modifier.fillMaxSize()) {
    if (!hasPermissions.value) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
      ) {
        Text(text = stringResource(R.string.placeholder_call_log))
        OutlinedButton(
          onClick = { requestPermissions.launch(input=PermissionUtils.recentsPermissions) }
        ) {
          Text(text = stringResource(R.string.turn_on))
        }
      }
      return@Surface
    }

    LazyColumn {
      items(calls.value) { call ->
        val isOpen = openRowId == call.id
        CallRow(call = call,
          isOpen = isOpen,
          onClick = { openRowId = if (isOpen) null else call.id },
          makeCall = { viewModel.makeCall(call.contactInfo.number!!) },
          sendMessage = { viewModel.sendMessage(call.contactInfo.number!!) },
          addContact = { viewModel.addToContact(call.contactInfo.number!!) },
          openHistory = { viewModel.callDetail(navController, call) })
      }
    }
  }
}

@Composable
private fun CallRow(
  call: DialerCall,
  isOpen: Boolean,
  onClick: () -> Unit,
  makeCall: () -> Unit,
  sendMessage: () -> Unit,
  addContact: () -> Unit,
  openHistory: () -> Unit,
) {
  Surface(
    onClick = onClick, tonalElevation = if (isOpen) 8.dp else 0.dp
  ) {
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
          model = call.contactInfo.photoUri,
          contentDescription = null,
          modifier = Modifier.size(50.dp),
          placeholder = placeholder,
          error = placeholder,
          fallback = placeholder
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (call.isAnonymous()) stringResource(id = R.string.anonymous)
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
                CallType.INCOMING, CallType.ANSWERED_EXTERNALLY -> Icons.Outlined.CallReceived
                CallType.OUTGOING -> Icons.Outlined.CallMade
                CallType.MISSED, CallType.REJECTED -> Icons.Outlined.CallMissed
                CallType.VOICEMAIL -> Icons.Outlined.Voicemail
                CallType.BLOCKED -> Icons.Outlined.Block
              },
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )

            Text(
              text = PrettyTime().format(call.date),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (!call.isAnonymous()) {
          IconButton(onClick = makeCall) {
            Icon(
              imageVector = Icons.Outlined.Phone,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      AnimatedVisibility(visible = isOpen) {
        Divider()

        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!call.isAnonymous()) {
            if (!call.isContactSaved()) {
              CallRowButton(
                label = "Add contact", icon = Icons.Outlined.PersonAddAlt, onClick = addContact
              )
            }

            CallRowButton(
              label = "Message", icon = Icons.Outlined.Message, onClick = sendMessage
            )
          }

          CallRowButton(
            label = "History", icon = Icons.Outlined.History, onClick = openHistory
          )
        }
      }
    }
  }
}

@Composable
private fun RowScope.CallRowButton(
  label: String, icon: ImageVector, onClick: () -> Unit
) {
  Surface(
    onClick = onClick, modifier = Modifier.weight(1f), color = Color.Transparent
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(vertical = 16.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
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
    onClick = {},
    makeCall = {},
    addContact = {},
    sendMessage = {},
    openHistory = {})
}

@Preview(showBackground = true)
@Composable
private fun OutgoingCallPreview() {
  CallRow(call = outgoingCallMock,
    isOpen = false,
    onClick = {},
    makeCall = {},
    addContact = {},
    sendMessage = {},
    openHistory = {})
}

@Preview(showBackground = true)
@Composable
private fun AnonymousCallPreview() {
  CallRow(call = anonymousCallMock,
    isOpen = false,
    onClick = {},
    makeCall = {},
    addContact = {},
    sendMessage = {},
    openHistory = {})
}