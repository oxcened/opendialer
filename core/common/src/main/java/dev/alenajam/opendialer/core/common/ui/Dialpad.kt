package dev.alenajam.opendialer.core.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalDialpadFontFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Default }

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

    PixelButton(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit.toString(),
                fontFamily = LocalDialpadFontFamily.current,
                fontSize = 24.sp,
                modifier = Modifier.offset(y = 4.dp)
            )

            if (subtitle != null) {
                Text(
                    text = subtitle.uppercase(),
                    fontFamily = LocalDialpadFontFamily.current,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
            }
        }
    }
}

/** A shared GSC-style button with stepped corners and a crisp inset/shadow edge. */
@Composable
fun PixelButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit,
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val faceColor = if (enabled) containerColor else containerColor.copy(alpha = 0.38f)
    val shadowColor = if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .drawBehind {
                val step = 4.dp.toPx()
                fun pixelPath(offset: Float): Path = Path().apply {
                    moveTo(offset + step, offset)
                    lineTo(size.width - offset - step, offset)
                    lineTo(size.width - offset - step, offset + step)
                    lineTo(size.width - offset, offset + step)
                    lineTo(size.width - offset, size.height - offset - step)
                    lineTo(size.width - offset - step, size.height - offset - step)
                    lineTo(size.width - offset - step, size.height - offset)
                    lineTo(offset + step, size.height - offset)
                    lineTo(offset + step, size.height - offset - step)
                    lineTo(offset, size.height - offset - step)
                    lineTo(offset, offset + step)
                    lineTo(offset + step, offset + step)
                    close()
                }

                drawPath(pixelPath(0f), shadowColor)
                drawPath(pixelPath(if (isPressed) 3.dp.toPx() else 2.dp.toPx()), faceColor)
            }
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
        content = {
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                content = content,
            )
        },
    )
}
