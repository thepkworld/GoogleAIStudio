package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SeasonDetail
import com.example.ui.components.RatingPill
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonEpisodesSheet(
    seasonDetail: SeasonDetail?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CinemaSlate,
        contentColor = CinemaAlabaster
    ) {
        if (isLoading && seasonDetail == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CinemaGold)
            }
        } else if (seasonDetail != null) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "season_header") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = seasonDetail.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = CinemaAlabaster
                        )
                        if (seasonDetail.airDate.isNotBlank() || seasonDetail.episodes.isNotEmpty()) {
                            Text(
                                text = listOf(
                                    seasonDetail.airDate.takeIf { it.isNotBlank() },
                                    "${seasonDetail.episodes.size} Episodes"
                                ).filterNotNull().joinToString(" · "),
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaGold
                            )
                        }
                        if (seasonDetail.overview.isNotBlank()) {
                            Text(
                                text = seasonDetail.overview,
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaSilver,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                items(seasonDetail.episodes, key = { it.id }) { ep ->
                    Surface(
                        color = CinemaObsidian,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CinemaBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ep.stillUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(ep.stillUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = ep.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(106.dp)
                                            .height(62.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CinemaSlateElevated)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .width(106.dp)
                                            .height(62.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CinemaSlateElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Movie,
                                            contentDescription = null,
                                            tint = CinemaGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "E${ep.episodeNumber}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = CinemaGold
                                        )
                                        if (ep.voteAverage > 0.0) {
                                            RatingPill(score = ep.voteAverage)
                                        }
                                    }
                                    Text(
                                        text = ep.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CinemaAlabaster,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val meta = listOfNotNull(
                                        ep.airDate.takeIf { it.isNotBlank() },
                                        ep.runtimeMinutes.takeIf { it > 0 }?.let { "${it}m" }
                                    ).joinToString(" · ")
                                    if (meta.isNotBlank()) {
                                        Text(
                                            text = meta,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaSilver
                                        )
                                    }
                                }
                            }

                            if (ep.overview.isNotBlank()) {
                                Text(
                                    text = ep.overview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CinemaSilver
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
