package dev.alenajam.opendialer.core.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.alenajam.opendialer.core.common.R
import dev.alenajam.opendialer.core.common.telecom.CallAccount

@Composable
fun CallAccountPicker(
    accounts: List<CallAccount>,
    onAccountSelected: (CallAccount) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_sim_card)) },
        text = {
            Column {
                accounts.forEach { account ->
                    TextButton(onClick = { onAccountSelected(account) }) {
                        Text(account.number?.let { "${account.label} · $it" } ?: account.label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
