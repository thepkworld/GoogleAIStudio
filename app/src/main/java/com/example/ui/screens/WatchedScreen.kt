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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterListOff
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.RatingScale
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.MediaFilter
import com.example.ui.VaultStats
import com.example.ui.WatchedRatingFilter
import com.example.ui.WatchedSort
import com.example.ui.WatchedTimeFilter
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.formatWatchedDate
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WatchedScreen(
    entries: List<VaultEntry>,
    stats: VaultStats,
    searchQuery: String,
    selectedFilter: MediaFilter,
    selectedTimeFilter: WatchedTimeFilter,
    selectedRatingFilter: WatchedRatingFilter,
    selectedGenre: String,
    availableGenres: List<String>,
    selectedSort: WatchedSort,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (MediaFilter) -> Unit,
    onTimeFilterChange: (WatchedTimeFilter) -> Unit,
    onRatingFilterChange: (WatchedRatingFilter) -> Unit,
    onGenreChange: (String) -> Unit,
    onSortChange: (WatchedSort) -> Unit,
    onResetFilters: () -> Unit,
    onEntryClick: (String) -> Unit,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = searchQuery.isNotBlank() ||
        selectedFilter != MediaFilter.ALL ||
        selectedTimeFilter != WatchedTimeFilter.ALL_TIME ||
        selectedRatingFilter != WatchedRatingFilter.ALL ||
        selectedGenre != "All" ||
        selectedSort != WatchedSort.RECENT

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Library Totals & Monthly Analytics Card
        item(key = "watched_summary") {
            Surface(
                color = CinemaSlate,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, CinemaEmerald.copy(alpha = 0.30f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_summary_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "WATCHED LIBRARY",
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaEmerald
                            )
                            Text(
                                text = "Your Cinema Archive",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CinemaAlabaster
                            )
                        }
                        if (stats.ratedCount > 0) {
                            Surface(
                                color = CinemaGold.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", stats.averageRating)} · ${RatingScale.labelFor(stats.averageRating)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CinemaGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = CinemaBorderSubtle)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LibraryStatBox(
                            label = "TOTAL WATCHED",
                            value = "${stats.totalWatched}",
                            caption = "${stats.watchedMovies} Movies · ${stats.watchedSeries} Series",
                            accentColor = CinemaEmerald,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_total_watched")
                        )
                        LibraryStatBox(
                            label = "THIS MONTH (${stats.currentMonthLabel.uppercase()})",
                            value = "${stats.watchedThisMonth}",
                            caption = "${stats.watchedMoviesThisMonth} Movies · ${stats.watchedSeriesThisMonth} Series",
                            accentColor = CinemaGold,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_monthly_watched")
                        )
                    }
                }
            }
        }

        // Library Quick Search
        item(key = "library_search") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search watched titles, directors, reviews…",
                        color = CinemaSilver.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = CinemaEmerald)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear filter", tint = CinemaSilver)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaEmerald,
                    unfocusedBorderColor = CinemaBorderSubtle,
                    focusedContainerColor = CinemaSlate,
                    unfocusedContainerColor = CinemaSlate,
                    focusedTextColor = CinemaAlabaster,
                    unfocusedTextColor = CinemaAlabaster,
                    cursorColor = CinemaEmerald
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input")
            )
        }

        // All Filters (Media Type, Time Period, Rating Tier, Genre, Sort)
        item(key = "watched_controls") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Media Type + Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    if (hasActiveFilters) {
                        TextButton(onClick = onResetFilters) {
                            Icon(
                                imageVector = Icons.Rounded.FilterListOff,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = CinemaGold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Time Period Filters
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WatchedTimeFilter.entries, key = { it.name }) { tf ->
                        val selected = selectedTimeFilter == tf
                        Surface(
                            color = if (selected) CinemaGold.copy(alpha = 0.18f) else CinemaSlate,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) CinemaGold else CinemaBorderSubtle),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTimeFilterChange(tf) }
                                .testTag("library_time_${tf.name.lowercase()}")
                        ) {
                            Text(
                                text = tf.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CinemaGold else CinemaSilver,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Rating Tier Filters
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WatchedRatingFilter.entries, key = { it.name }) { rf ->
                        val selected = selectedRatingFilter == rf
                        Surface(
                            color = if (selected) CinemaEmerald.copy(alpha = 0.18f) else CinemaSlate,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) CinemaEmerald else CinemaBorderSubtle),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onRatingFilterChange(rf) }
                                .testTag("library_rating_${rf.name.lowercase()}")
                        ) {
                            Text(
                                text = rf.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CinemaEmerald else CinemaSilver,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Genre Filters (when user has watched genres)
                if (availableGenres.size > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableGenres, key = { "lib_genre_$it" }) { genre ->
                            val selected = selectedGenre == genre
                            Surface(
                                color = if (selected) CinemaSlateElevated else CinemaSlate,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (selected) CinemaGold else CinemaBorderSubtle),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onGenreChange(genre) }
                            ) {
                                Text(
                                    text = if (genre == "All") "Genre: All" else genre,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) CinemaGold else CinemaSilver,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // Sort Options
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
                                text = "Sort: ${sort.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CinemaEmerald else CinemaSilver,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Filtered Count Header
        item(key = "library_results_count") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasActiveFilters) "Filtered Results" else "All Watched",
                    style = MaterialTheme.typography.titleSmall,
                    color = CinemaAlabaster
                )
                Text(
                    text = "${entries.size} ${if (entries.size == 1) "TITLE" else "TITLES"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinemaEmerald
                )
            }
        }

        if (entries.isEmpty()) {
            item(key = "watched_empty") {
                EmptyVaultState(
                    icon = Icons.Rounded.CheckCircle,
                    title = if (hasActiveFilters) "No watched titles match these filters" else "Your Library is empty",
                    subtitle = if (hasActiveFilters) {
                        "Try clearing one or more filters to view your full watched archive."
                    } else {
                        "Mark any movie or series as Seen to record your 1–10 rating, headline, and watched date."
                    },
                    actionLabel = if (hasActiveFilters) "Reset Filters" else "Discover Movies & Series",
                    onActionClick = if (hasActiveFilters) onResetFilters else onBrowseClick
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
private fun LibraryStatBox(
    label: String,
    value: String,
    caption: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaObsidian.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CinemaSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = CinemaAlabaster.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WatchedItemCard(
    entry: VaultEntry,
    onClick: () -> Unit
) {
    val intScore = if (entry.userRating > 0.0) entry.userRating.roundToInt().coerceIn(1, 10) else 0
    val ratingWord = RatingScale.labelFor(intScore)
    val ratingTint = when {
        intScore >= 9 -> CinemaGold
        intScore >= 7 -> CinemaEmerald
        intScore >= 5 -> CinemaAlabaster
        intScore >= 1 -> CinemaCrimson
        else -> CinemaSilver
    }

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
                posterUrl = entry.posterUrl,
                title = entry.title,
                mediaType = entry.mediaTypeEnum,
                userRating = entry.userRating,
                status = WatchStatus.WATCHED,
                modifier = Modifier
                    .width(82.dp)
                    .height(118.dp)
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
                    if (intScore > 0) {
                        Surface(
                            color = ratingTint.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ratingTint.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = ratingTint,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$intScore/10 · $ratingWord",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ratingTint,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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

                if (entry.effectiveWatchedAt > 0L) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = CinemaSilver,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Seen ${formatWatchedDate(entry.effectiveWatchedAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaSilver
                        )
                    }
                }

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
                            color = CinemaAlabaster.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
