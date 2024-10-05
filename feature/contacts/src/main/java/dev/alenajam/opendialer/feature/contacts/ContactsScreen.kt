package dev.alenajam.opendialer.feature.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.forwardingPainter
import dev.alenajam.opendialer.data.contacts.DialerContact

@Composable
internal fun ContactsScreen(
  viewModel: ContactsViewModel = viewModel(),
) {
  val requestPermissions =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
      if (PermissionUtils.contactsPermissions.all { result[it] == true }) {
        viewModel.handleRuntimePermissionGranted()
      }
    }

  val calls = viewModel.contacts.collectAsStateWithLifecycle()
  val hasPermission = viewModel.hasRuntimePermission.collectAsStateWithLifecycle()

  Surface(modifier = Modifier.fillMaxSize()) {
    if (!hasPermission.value) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
      ) {
        Text(text = stringResource(R.string.placeholder_contacts))
        OutlinedButton(
          onClick = { requestPermissions.launch(input=PermissionUtils.contactsPermissions) }
        ) {
          Text(text = stringResource(R.string.turn_on))
        }
      }
      return@Surface
    }

    LazyColumn {
      items(calls.value) { contact ->
        ContactRow(
          contact = contact,
          onClick = { viewModel.openContact(contact.id) }
        )
      }
    }
  }
}

@Composable
private fun ContactRow(
  contact: DialerContact,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
  ) {
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

      Text(
        text = contact.name,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

val contactMock = DialerContact(
  id = 1,
  name = "John Doe",
  starred = false,
  image = null
)

@Preview(showBackground = true)
@Composable
private fun ContactRowPreview() {
  ContactRow(
    contact = contactMock,
    onClick = {},
  )
}
