package dev.alenajam.opendialer.feature.appShell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DefaultPhoneScreen(onSetAsDefault: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompactHeight = maxHeight < 720.dp
            val verticalPadding = if (isCompactHeight) 24.dp else 48.dp
            val illustrationSize = if (isCompactHeight) 180.dp else 280.dp
            val illustrationSpacing = if (isCompactHeight) 24.dp else 64.dp
            val titleSpacing = if (isCompactHeight) 12.dp else 20.dp
            val buttonSpacing = if (isCompactHeight) 20.dp else 28.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                DefaultPhoneIllustration(illustrationSize = illustrationSize)
                Spacer(Modifier.height(illustrationSpacing))
                Text(
                    text = stringResource(R.string.default_phone_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(titleSpacing))
                Text(
                    text = stringResource(R.string.default_phone_description),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(buttonSpacing))
                Button(
                    onClick = onSetAsDefault,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        text = stringResource(R.string.default_phone_button),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultPhoneIllustration(illustrationSize: androidx.compose.ui.unit.Dp) {
    val outline = MaterialTheme.colorScheme.outline
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val primary = MaterialTheme.colorScheme.primary
    val success = MaterialTheme.colorScheme.tertiary
    val error = MaterialTheme.colorScheme.error
    val accent = MaterialTheme.colorScheme.secondary
    val shadow = MaterialTheme.colorScheme.surfaceContainerHighest

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.size(illustrationSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawOval(
                color = outline,
                topLeft = Offset(size.width * .08f, size.height * .18f),
                size = Size(size.width * .30f, size.height * .48f),
                style = Stroke(width = 4.dp.toPx()),
            )
            drawCircle(success, radius = 22.dp.toPx(), center = Offset(size.width * .10f, size.height * .47f))
            drawRect(error, topLeft = Offset(size.width * .53f, size.height * .12f), size = Size(34.dp.toPx(), 34.dp.toPx()))
            drawArc(accent, 72f, 180f, true, Offset(-8.dp.toPx(), size.height * .62f), Size(88.dp.toPx(), 88.dp.toPx()))
            drawOval(shadow, Offset(size.width * .35f, size.height * .82f), Size(size.width * .34f, 20.dp.toPx()))

            val triangle = Path().apply {
                moveTo(size.width * .82f, size.height * .42f)
                lineTo(size.width * .98f, size.height * .42f)
                lineTo(size.width * .82f, size.height * .58f)
                close()
            }
            drawPath(triangle, color = outline, style = Stroke(width = 4.dp.toPx()))
            drawRect(
                color = outline,
                topLeft = Offset(size.width * .79f, size.height * .69f),
                size = Size(size.width * .18f, size.height * .08f),
                style = Stroke(width = 4.dp.toPx()),
            )
        }
        Surface(
            modifier = Modifier.size(132.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = primaryContainer,
        ) {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                modifier = Modifier.padding(34.dp),
                tint = primary,
            )
        }
    }
}
