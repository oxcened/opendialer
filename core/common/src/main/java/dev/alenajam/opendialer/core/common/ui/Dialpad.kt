package dev.alenajam.opendialer.core.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Dialpad(
    onDigitClick: (digit: Char) -> Unit,
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
                onClick = onDigitClick
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
    onClick: (digit: Char) -> Unit
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
                text = digit.toString(),
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
