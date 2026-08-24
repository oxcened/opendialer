package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.LocalCustomColorsScheme
import dev.alenajam.opendialer.core.common.R as CommonR

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun IncomingCallControls(
    onHangup: () -> Unit,
    onAnswer: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quickResponses = remember { context.resources.getStringArray(CommonR.array.array_quick_responses) }
    var showQuickResponses by remember { mutableStateOf(false) }
    var showCustomMessage by remember { mutableStateOf(false) }
    var customMessage by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 32.dp, top = 32.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Surface(
                onClick = { showQuickResponses = true },
                shape = RoundedCornerShape(28.dp),
                color = Color.White
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Message, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Message", style = MaterialTheme.typography.titleMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onHangup,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CallEnd,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Text(text = "Decline")
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onAnswer,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = LocalCustomColorsScheme.current.success
                        ),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Call,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Text(text = "Answer")
                }
            }
        }
    }

    if (showQuickResponses) {
        ModalBottomSheet(onDismissRequest = { showQuickResponses = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                quickResponses.forEach { response ->
                    TextButton(
                        onClick = {
                            showQuickResponses = false
                            onMessage(response)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(response, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp))
                    }
                }
                TextButton(
                    onClick = {
                        showQuickResponses = false
                        showCustomMessage = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Write your own…", modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp))
                }
            }
        }
    }

    if (showCustomMessage) {
        AlertDialog(
            onDismissRequest = { showCustomMessage = false },
            title = { Text("Reply with a message") },
            text = { TextField(value = customMessage, onValueChange = { customMessage = it }) },
            confirmButton = {
                TextButton(
                    enabled = customMessage.isNotBlank(),
                    onClick = {
                        showCustomMessage = false
                        onMessage(customMessage)
                    }
                ) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { showCustomMessage = false }) { Text("Cancel") } }
        )
    }
}
