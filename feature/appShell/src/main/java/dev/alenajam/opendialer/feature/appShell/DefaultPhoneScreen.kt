package dev.alenajam.opendialer.feature.appShell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DefaultPhoneScreen(onSetAsDefault: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DefaultPhoneIllustration()
        Spacer(Modifier.height(64.dp))
        Text(
            text = stringResource(R.string.default_phone_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
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
        Spacer(Modifier.height(28.dp))
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

@Composable
private fun DefaultPhoneIllustration() {
    val outline = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val primary = MaterialTheme.colorScheme.primary

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawOval(
                color = outline,
                topLeft = Offset(size.width * .08f, size.height * .18f),
                size = Size(size.width * .30f, size.height * .48f),
                style = Stroke(width = 4.dp.toPx()),
            )
            drawCircle(Color(0xFF58B978), radius = 22.dp.toPx(), center = Offset(size.width * .10f, size.height * .47f))
            drawRect(Color(0xFFF2635B), topLeft = Offset(size.width * .53f, size.height * .12f), size = Size(34.dp.toPx(), 34.dp.toPx()))
            drawArc(Color(0xFFFFCA3A), 72f, 180f, true, Offset(-8.dp.toPx(), size.height * .62f), Size(88.dp.toPx(), 88.dp.toPx()))
            drawOval(Color(0xFF30313A), Offset(size.width * .35f, size.height * .82f), Size(size.width * .34f, 20.dp.toPx()))

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
        androidx.compose.material3.Surface(
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
