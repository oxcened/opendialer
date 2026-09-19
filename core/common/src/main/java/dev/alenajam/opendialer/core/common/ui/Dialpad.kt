package dev.alenajam.opendialer.core.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Dialpad(
    onDigitClick: (digit: Char) -> Unit,
    onDigitPress: (digit: Char) -> Unit = {},
    onDigitRelease: () -> Unit = {},
    onZeroLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DigitButton(
                digit = '1',
                subtitle = "",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )

            DigitButton(
                digit = '2',
                subtitle = "abc",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )

            DigitButton(
                digit = '3',
                subtitle = "def",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DigitButton(
                digit = '4',
                subtitle = "ghi",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )

            DigitButton(
                digit = '5',
                subtitle = "jkl",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )

            DigitButton(
                digit = '6',
                subtitle = "mno",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DigitButton(
                digit = '7',
                subtitle = "pqrs",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )

            DigitButton(
                digit = '8',
                subtitle = "tuv",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )

            DigitButton(
                digit = '9',
                subtitle = "wxyz",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DigitButton(
                digit = '*',
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )

            DigitButton(
                digit = '0',
                subtitle = "+",
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
                onLongClick = onZeroLongClick
            )

            DigitButton(
                digit = '#',
                onClick = onDigitClick,
                onPress = onDigitPress,
                onRelease = onDigitRelease,
            )
        }
    }
}

@Composable
private fun RowScope.DigitButton(
    digit: Char,
    subtitle: String? = null,
    onClick: (digit: Char) -> Unit,
    onPress: (digit: Char) -> Unit,
    onRelease: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var handledOnPress by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            handledOnPress = true
            onPress(digit)
            if (onLongClick == null) onClick(digit)
        } else {
            onRelease()
        }
    }

    Surface(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(ButtonDefaults.shape)
            .combinedClickable(
                interactionSource = interactionSource,
                onClick = {
                    if (!handledOnPress || onLongClick != null) onClick(digit)
                    handledOnPress = false
                },
                onLongClick = onLongClick?.let { handleLongClick ->
                    {
                        handledOnPress = false
                        handleLongClick()
                    }
                }
            ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = ButtonDefaults.shape
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit.toString(),
                fontSize = 28.sp,
                modifier = Modifier.offset(y = 4.dp)
            )

            if (subtitle != null) {
                Text(
                    text = subtitle.uppercase(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
            }
        }
    }
}
