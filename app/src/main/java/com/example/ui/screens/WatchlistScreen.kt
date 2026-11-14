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
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.MediaFilter
import com.example.ui.VaultStats
import com.example.ui.WatchlistSort
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated

@Composable
fun WatchlistScreen(
    entries: List<VaultEntry>,
    stats: VaultStats,
    selectedFilter: MediaFilter,
    selectedSort: WatchlistSort,
    onFilterChange: (MediaFilter) -> Unit,
    onSortChange: (WatchlistSort) -> Unit,
    onEntryClick: (String) -> Unit,
    onTogglePriority: (VaultEntry) -> Unit,
    onMarkWatched: (VaultEntry) -> Unit,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Banner
        item(key = "watchlist_summary") {
            Surface(
                color = CinemaSlate,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.25f)),
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
                            text = "WANT TO WATCH",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGold
                        )
                        Text(
                            text = "${stats.totalWatchlist} Titles Queued",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CinemaAlabaster
                        )
                        Text(
                            text = "${stats.priorityWatchlist} pinned Up Next",
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaSilver
                        )
                    }
                    Surface(
                        color = CinemaGold.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkAdd,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier
                                .padding(14.dp)
                                .size(28.dp)
                        )
                    }
                }
            }
        }

        // Filter & Sort Controls
        item(key = "watchlist_controls") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MediaFilter.entries.forEach { filter ->
                        val selected = selectedFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGold,
                                selectedLabelColor = CinemaObsidian,
                                containerColor = CinemaSlate,
                                labelColor = CinemaSilver
                            )
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WatchlistSort.entries, key = { it.name }) { sort ->
                        val selected = selectedSort == sort
                        Surface(
                            color = if (selected) CinemaSlateElevated else CinemaSlate,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (selected) CinemaGold else CinemaBorderSubtle
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSortChange(sort) }
                        ) {
                            Text(
                                text = sort.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CinemaGold else CinemaSilver,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            item(key = "watchlist_empty") {
                EmptyVaultState(
                    icon = Icons.Rounded.BookmarkAdd,
                    title = "Your Want to Watch list is empty",
                    subtitle = "Browse acclaimed films and series or add your own custom title to plan your next watch.",
                    actionLabel = "Browse Movies & Series",
                    onActionClick = onBrowseClick
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                WatchlistItemCard(
                    entry = entry,
                    onClick = { onEntryClick(entry.id) },
                    onTogglePriority = { onTogglePriority(entry) },
                    onMarkWatched = { onMarkWatched(entry) }
                )
            }
        }
    }
}

@Composable
private fun WatchlistItemCard(
    entry: VaultEntry,
    onClick: () -> Unit,
    onTogglePriority: () -> Unit,
    onMarkWatched: () -> Unit
) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (entry.priority) CinemaGold.copy(alpha = 0.5f) else CinemaBorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("watchlist_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaPosterArtwork(
                posterKey = entry.posterKey,
                posterUrl = entry.posterUrl,
                title = entry.title,
                mediaType = entry.mediaTypeEnum,
                showBottomBadge = false,
                status = WatchStatus.WANT_TO_WATCH,
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (entry.priority) {
                        Surface(
                            color = CinemaGold.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PushPin,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UP NEXT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "${entry.releaseYear}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaSilver
                        )
                    }

                    IconButton(
                        onClick = onTogglePriority,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.priority) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = if (entry.priority) "Unpin Up Next" else "Pin Up Next",
                            tint = if (entry.priority) CinemaGold else CinemaSilver,
                            modifier = Modifier.size(20.dp)
                        )
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
                    text = listOfNotNull(
                        entry.directorOrCreator.takeIf { it.isNotBlank() },
                        entry.runtimeOrSeasons.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (entry.mediaTypeEnum == MediaType.SERIES && entry.totalEpisodes > 0) {
                    val progress = (entry.episodesWatched.toFloat() / entry.totalEpisodes.toFloat()).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "${entry.episodesWatched}/${entry.totalEpisodes} Episodes",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            color = CinemaGold,
                            trackColor = CinemaSlateElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                Button(
                    onClick = onMarkWatched,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaEmerald.copy(alpha = 0.16f),
                        contentColor = CinemaEmerald
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Watched",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
