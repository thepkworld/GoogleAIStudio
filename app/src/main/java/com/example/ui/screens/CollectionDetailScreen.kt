package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CollectionDetail
import com.example.data.model.VaultEntry
import com.example.ui.components.CinemaBackdropImage
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.RatingPill
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate

@Composable
fun CollectionDetailScreen(
    collectionDetail: CollectionDetail?,
    isLoading: Boolean,
    vaultById: Map<String, VaultEntry>,
    onBack: () -> Unit,
    onTitleClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaObsidian)
    ) {
        if (isLoading && collectionDetail == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CinemaGold)
            }
        } else if (collectionDetail != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "collection_hero") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CinemaBackdropImage(
                            imageUrl = collectionDetail.backdropUrl.ifBlank { collectionDetail.posterUrl },
                            fallbackKey = "cinema",
                            contentDescription = collectionDetail.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to CinemaObsidian.copy(alpha = 0.4f),
                                        0.6f to CinemaObsidian.copy(alpha = 0.78f),
                                        1.0f to CinemaObsidian
                                    )
                                )
                        )

                        Surface(
                            color = CinemaObsidian.copy(alpha = 0.72f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                                .padding(12.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("collection_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = CinemaAlabaster
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = CinemaGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "TMDB COLLECTION · ${collectionDetail.parts.size} FILMS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = collectionDetail.name,
                                style = MaterialTheme.typography.displayMedium,
                                color = CinemaAlabaster
                            )
                        }
                    }
                }

                if (collectionDetail.overview.isNotBlank()) {
                    item(key = "collection_overview") {
                        Text(
                            text = collectionDetail.overview,
                            style = MaterialTheme.typography.bodyLarge,
                            color = CinemaSilver,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                itemsIndexed(collectionDetail.parts, key = { _, part -> "part_${part.id}" }) { index, part ->
                    val entry = vaultById[part.id]
                    Surface(
                        color = CinemaSlate,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CinemaBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onTitleClick(part.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CinemaPosterArtwork(
                                posterKey = part.posterKey,
                                posterUrl = part.posterUrl,
                                title = part.title,
                                mediaType = part.mediaType,
                                showBottomBadge = false,
                                status = entry?.statusEnum,
                                modifier = Modifier
                                    .width(76.dp)
                                    .aspectRatio(0.70f)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CinemaGold
                                    )
                                    if (part.criticScore > 0.0) {
                                        RatingPill(score = part.criticScore)
                                    }
                                    Text(
                                        text = "${part.releaseYear}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CinemaSilver
                                    )
                                }
                                Text(
                                    text = part.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CinemaAlabaster,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = part.synopsis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CinemaSilver,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
