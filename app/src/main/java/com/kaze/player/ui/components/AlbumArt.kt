package com.kaze.player.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme

/**
 * @param size Pixel size for the art. Pass null to use the modifier's own size.
 * @param cornerRadius Corner radius in dp. Default 8.
 */
@Composable
fun AlbumArt(
    uri: String?,
    modifier: Modifier = Modifier,
    size: Int? = 56,
    cornerRadius: Int = 8
) {
    val sizeModifier = if (size != null) modifier.size(size.dp) else modifier
    val shape = RoundedCornerShape(cornerRadius.dp)

    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = sizeModifier.clip(shape)
        )
    } else {
        val iconSize = (size ?: 56) * 0.5
        Surface(
            modifier = sizeModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = "Album art",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}
