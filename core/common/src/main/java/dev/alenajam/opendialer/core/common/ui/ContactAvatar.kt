package dev.alenajam.opendialer.core.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun ContactAvatar(
    name: String?,
    photoUri: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    colorKey: String = contactAvatarColorKey(name),
    fallbackIcon: IconSource = IconSource.Vector(Icons.Outlined.Person),
    fallbackIconModifier: Modifier = Modifier.size(24.dp),
    initialTextStyle: TextStyle = MaterialTheme.typography.titleLarge
) {
    val contactName = name.orEmpty().trim()
    val colors = contactAvatarColors(colorKey)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(Color(colors.background))
    ) {
        if (contactName.isBlank()) {
            AppIcon(
                icon = fallbackIcon,
                contentDescription = contentDescription,
                tint = Color(colors.foreground),
                modifier = fallbackIconModifier
            )
        } else {
            Text(
                text = contactName.take(1).uppercase(Locale.ROOT),
                style = initialTextStyle,
                color = Color(colors.foreground)
            )
        }

        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
