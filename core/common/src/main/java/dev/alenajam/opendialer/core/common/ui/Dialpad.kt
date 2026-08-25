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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Dialpad(
    onDigitClick: (digit: Char) -> Unit,
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
                onClick = onDigitClick
            )

            DigitButton(
                digit = '2',
                subtitle = "abc",
                onClick = onDigitClick
            )

            DigitButton(
                digit = '3',
                subtitle = "def",
                onClick = onDigitClick
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
                onClick = onDigitClick
            )

            DigitButton(
                digit = '5',
                subtitle = "jkl",
                onClick = onDigitClick
            )

            DigitButton(
                digit = '6',
                subtitle = "mno",
                onClick = onDigitClick
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
                onClick = onDigitClick
            )

            DigitButton(
                digit = '8',
                subtitle = "tuv",
                onClick = onDigitClick
            )

            DigitButton(
                digit = '9',
                subtitle = "wxyz",
                onClick = onDigitClick
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
                onClick = onDigitClick
            )

            DigitButton(
                digit = '0',
                subtitle = "+",
                onClick = onDigitClick,
                onLongClick = onZeroLongClick
            )

            DigitButton(
                digit = '#',
                onClick = onDigitClick
            )
        }
    }
}

@Composable
private fun RowScope.DigitButton(
    digit: Char,
    subtitle: String? = null,
    onClick: (digit: Char) -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(ButtonDefaults.shape)
            .combinedClickable(
                onClick = { onClick(digit) },
                onLongClick = onLongClick
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
