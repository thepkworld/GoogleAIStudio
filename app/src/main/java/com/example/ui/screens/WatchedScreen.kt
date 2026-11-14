package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.MediaFilter
import com.example.ui.VaultStats
import com.example.ui.WatchedSort
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.RatingPill
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated
import java.util.Locale

@Composable
fun WatchedScreen(
    entries: List<VaultEntry>,
    stats: VaultStats,
    selectedFilter: MediaFilter,
    selectedSort: WatchedSort,
    onFilterChange: (MediaFilter) -> Unit,
    onSortChange: (WatchedSort) -> Unit,
    onEntryClick: (String) -> Unit,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Watched Analytics Header Card
        item(key = "watched_summary") {
            Surface(
                color = CinemaSlate,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CinemaEmerald.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "WATCHED LOG",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaEmerald
                        )
                        Text(
                            text = "${stats.totalWatched} Completed",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CinemaAlabaster
                        )
                        Text(
                            text = "${stats.watchedMovies} Movies · ${stats.watchedSeries} Series",
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaSilver
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (stats.ratedCount > 0) {
                                String.format(Locale.US, "%.1f", stats.averageRating)
                            } else {
                                "—"
                            },
                            style = MaterialTheme.typography.displayMedium,
                            color = CinemaGold
                        )
                        Text(
                            text = "AVG SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaSilver
                        )
                    }
                }
            }
        }

        // Filter & Sort Controls
        item(key = "watched_controls") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MediaFilter.entries.forEach { filter ->
                        val selected = selectedFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaEmerald,
                                selectedLabelColor = CinemaObsidian,
                                containerColor = CinemaSlate,
                                labelColor = CinemaSilver
                            )
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WatchedSort.entries, key = { it.name }) { sort ->
                        val selected = selectedSort == sort
                        Surface(
                            color = if (selected) CinemaSlateElevated else CinemaSlate,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (selected) CinemaEmerald else CinemaBorderSubtle
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSortChange(sort) }
                        ) {
                            Text(
                                text = sort.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CinemaEmerald else CinemaSilver,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            item(key = "watched_empty") {
                EmptyVaultState(
                    icon = Icons.Rounded.CheckCircle,
                    title = "No watched titles yet",
                    subtitle = "Mark any movie or series as Watched to start tracking your ratings, rewatches, and reviews.",
                    actionLabel = "Explore Movies & Series",
                    onActionClick = onBrowseClick
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                WatchedItemCard(
                    entry = entry,
                    onClick = { onEntryClick(entry.id) }
                )
            }
        }
    }
}

@Composable
private fun WatchedItemCard(
    entry: VaultEntry,
    onClick: () -> Unit
) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("watched_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaPosterArtwork(
                posterKey = entry.posterKey,
                title = entry.title,
                mediaType = entry.mediaTypeEnum,
                userRating = entry.userRating,
                status = WatchStatus.WATCHED,
                modifier = Modifier
                    .width(82.dp)
                    .height(116.dp)
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
                    if (entry.userRating > 0.0) {
                        RatingPill(score = entry.userRating, labelPrefix = "YOU")
                    } else {
                        Surface(
                            color = CinemaSlateElevated,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TAP TO RATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaSilver,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (entry.rewatchCount > 0) {
                        Surface(
                            color = CinemaEmerald.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Repeat,
                                    contentDescription = null,
                                    tint = CinemaEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${entry.rewatchCount}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaEmerald
                                )
                            }
                        }
                    }
                }

                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaAlabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${entry.releaseYear} · ${entry.directorOrCreator}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (entry.hasReview) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.RateReview,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = entry.reviewTitle.ifBlank { entry.reviewBody },
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaAlabaster.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = "Tap to write a full review & favorite quote",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaSilver.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
