package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaIndigo
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated
import java.util.Locale

@DrawableRes
fun posterDrawableForKey(posterKey: String): Int {
    return when (posterKey.lowercase()) {
        "scifi" -> R.drawable.img_poster_scifi
        "noir" -> R.drawable.img_poster_noir
        "drama" -> R.drawable.img_poster_drama
        else -> R.drawable.img_hero_cinema
    }
}

fun accentTintForKey(posterKey: String, titleSeed: String): Color {
    val palette = listOf(CinemaGold, CinemaCrimson, CinemaIndigo, CinemaEmerald)
    val index = kotlin.math.abs((posterKey + titleSeed).hashCode()) % palette.size
    return palette[index]
}

@Composable
fun CinemaBackdropImage(
    imageUrl: String,
    fallbackKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val fallbackRes = posterDrawableForKey(fallbackKey)
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = fallbackRes),
            error = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun CinemaPosterArtwork(
    posterKey: String,
    title: String,
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    posterUrl: String = "",
    cornerRadius: Dp = 14.dp,
    showBottomBadge: Boolean = true,
    showMediaTypeBadge: Boolean = true,
    userRating: Double = 0.0,
    status: WatchStatus? = null
) {
    val tint = accentTintForKey(posterKey, title)
    val fallbackRes = posterDrawableForKey(posterKey)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(CinemaSlateElevated)
    ) {
        if (posterUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(posterUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = fallbackRes),
                error = painterResource(id = fallbackRes),
                contentDescription = "$title poster artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = fallbackRes),
                contentDescription = "$title poster artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Subtle atmospheric gradient scrim for badge legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to if (posterUrl.isBlank()) tint.copy(alpha = 0.14f) else CinemaObsidian.copy(alpha = 0.18f),
                        0.6f to Color.Transparent,
                        1.0f to CinemaObsidian.copy(alpha = if (posterUrl.isBlank()) 0.88f else 0.65f)
                    )
                )
        )

        // Top row: Media type badge & status dot
        if (showMediaTypeBadge || status != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = if (showMediaTypeBadge) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showMediaTypeBadge) {
                    Surface(
                        color = CinemaObsidian.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.16f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (mediaType == MediaType.SERIES) Icons.Rounded.Tv else Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (mediaType == MediaType.SERIES) "SERIES" else "FILM",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaAlabaster
                            )
                        }
                    }
                }

            if (status != null) {
                Surface(
                    shape = CircleShape,
                    color = if (status == WatchStatus.WATCHED) CinemaEmerald else CinemaGold,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (status == WatchStatus.WATCHED) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.Bookmark
                            },
                            contentDescription = status.label,
                            tint = CinemaObsidian,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        }

        if (showBottomBadge && userRating > 0.0) {
            Surface(
                color = CinemaObsidian.copy(alpha = 0.84f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, CinemaGold.copy(alpha = 0.55f)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", userRating),
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaGold
                    )
                }
            }
        }
    }
}

@Composable
fun RatingPill(
    score: Double,
    labelPrefix: String = "",
    accentColor: Color = CinemaGold,
    modifier: Modifier = Modifier
) {
    Surface(
        color = accentColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = buildString {
                    if (labelPrefix.isNotEmpty()) {
                        append(labelPrefix)
                        append(" ")
                    }
                    append(String.format(Locale.US, "%.1f", score))
                },
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusPill(
    status: WatchStatus,
    modifier: Modifier = Modifier
) {
    val isWatched = status == WatchStatus.WATCHED
    val color = if (isWatched) CinemaEmerald else CinemaGold
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.38f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isWatched) Icons.Rounded.CheckCircle else Icons.Rounded.Bookmark,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyVaultState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaSlate.copy(alpha = 0.72f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(CinemaGold.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = CinemaAlabaster,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaSilver,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = CinemaObsidian
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
