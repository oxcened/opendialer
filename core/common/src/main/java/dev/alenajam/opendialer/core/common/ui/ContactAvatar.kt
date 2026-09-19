package dev.alenajam.opendialer.core.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
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
    fallbackIcon: IconSource = LocalAppIcons.current.person,
    fallbackIconModifier: Modifier = Modifier.size(24.dp),
    avatarIcon: IconSource? = null,
    initialTextStyle: TextStyle = MaterialTheme.typography.titleLarge
) {
    val contactName = name.orEmpty().trim()
    val colors = contactAvatarColors(colorKey)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(PixelRoundedSquareShape)
            .background(Color(colors.background))
    ) {
        if (avatarIcon != null) {
            AppIcon(
                icon = avatarIcon,
                contentDescription = contentDescription,
                tint = Color(colors.foreground),
                modifier = fallbackIconModifier,
            )
        } else if (contactName.isBlank()) {
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

        if (avatarIcon == null && !photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

    }
}

private object PixelRoundedSquareShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val x = size.width / 12f
        val y = size.height / 12f
        val path = Path().apply {
            moveTo(x * 2, 0f)
            lineTo(x * 10, 0f)
            lineTo(x * 10, y)
            lineTo(x * 11, y)
            lineTo(x * 11, y * 2)
            lineTo(size.width, y * 2)
            lineTo(size.width, y * 10)
            lineTo(x * 11, y * 10)
            lineTo(x * 11, y * 11)
            lineTo(x * 10, y * 11)
            lineTo(x * 10, size.height)
            lineTo(x * 2, size.height)
            lineTo(x * 2, y * 11)
            lineTo(x, y * 11)
            lineTo(x, y * 10)
            lineTo(0f, y * 10)
            lineTo(0f, y * 2)
            lineTo(x, y * 2)
            lineTo(x, y)
            lineTo(x * 2, y)
            close()
        }
        return Outline.Generic(path)
    }
}
