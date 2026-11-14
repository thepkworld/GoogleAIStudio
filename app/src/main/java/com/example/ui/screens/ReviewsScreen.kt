package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.VaultEntry
import com.example.ui.MediaFilter
import com.example.ui.VaultStats
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.RatingPill
import com.example.ui.components.StatusPill
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewsScreen(
    reviewedEntries: List<VaultEntry>,
    stats: VaultStats,
    selectedFilter: MediaFilter,
    onFilterChange: (MediaFilter) -> Unit,
    onEntryClick: (String) -> Unit,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rating Distribution & Taste Profile Card
        item(key = "reviews_analytics") {
            Surface(
                color = CinemaSlate,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CRITIC JOURNAL",
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaGold
                            )
                            Text(
                                text = "${stats.totalReviews} Reviews · ${stats.ratedCount} Rated",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CinemaAlabaster
                            )
                        }
                        if (stats.ratedCount > 0) {
                            RatingPill(score = stats.averageRating, labelPrefix = "AVG")
                        }
                    }

                    // Custom Canvas Rating Histogram
                    RatingHistogramChart(buckets = stats.ratingBuckets)

                    if (stats.topGenres.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "TOP WATCHED GENRES",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaSilver
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                stats.topGenres.forEach { (genre, count) ->
                                    Surface(
                                        color = CinemaSlateElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, CinemaBorderSubtle)
                                    ) {
                                        Text(
                                            text = "$genre ($count)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = CinemaAlabaster,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips
        item(key = "reviews_filter") {
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
        }

        if (reviewedEntries.isEmpty()) {
            item(key = "reviews_empty") {
                EmptyVaultState(
                    icon = Icons.Rounded.RateReview,
                    title = "No ratings or reviews yet",
                    subtitle = "Open any movie or series and score it out of 10 or write your full thoughts and favorite quotes.",
                    actionLabel = "Pick a Title to Review",
                    onActionClick = onBrowseClick
                )
            }
        } else {
            items(reviewedEntries, key = { it.id }) { entry ->
                ReviewJournalCard(
                    entry = entry,
                    onClick = { onEntryClick(entry.id) }
                )
            }
        }
    }
}

@Composable
private fun RatingHistogramChart(buckets: List<Int>) {
    val labels = listOf("1–2", "3–4", "5–6", "7–8", "9–10")
    val maxCount = (buckets.maxOrNull() ?: 1).coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
        ) {
            val count = buckets.size
            val gap = 14.dp.toPx()
            val barWidth = (size.width - gap * (count - 1)) / count
            val maxBarHeight = size.height - 6.dp.toPx()

            buckets.forEachIndexed { index, value ->
                val ratio = value.toFloat() / maxCount.toFloat()
                val barHeight = if (value == 0) 6.dp.toPx() else (maxBarHeight * ratio).coerceAtLeast(14.dp.toPx())
                val x = index * (barWidth + gap)
                val y = size.height - barHeight

                drawRoundRect(
                    color = CinemaSlateElevated,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                val brush = if (value > 0) {
                    Brush.verticalGradient(listOf(CinemaGold, CinemaGold.copy(alpha = 0.45f)))
                } else {
                    Brush.verticalGradient(listOf(CinemaBorderSubtle, CinemaBorderSubtle))
                }
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { idx, label ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${buckets.getOrElse(idx) { 0 }}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if ((buckets.getOrElse(idx) { 0 }) > 0) CinemaGold else CinemaSilver
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaSilver.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewJournalCard(
    entry: VaultEntry,
    onClick: () -> Unit
) {
    var revealSpoiler by remember(entry.id) { mutableStateOf(!entry.containsSpoilers) }

    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CinemaPosterArtwork(
                    posterKey = entry.posterKey,
                    posterUrl = entry.posterUrl,
                    title = entry.title,
                    mediaType = entry.mediaTypeEnum,
                    showBottomBadge = false,
                    modifier = Modifier
                        .width(58.dp)
                        .height(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (entry.userRating > 0.0) {
                            RatingPill(score = entry.userRating, labelPrefix = "SCORE")
                        }
                        StatusPill(status = entry.statusEnum)
                    }
                }
            }

            if (entry.reviewTitle.isNotBlank()) {
                Text(
                    text = entry.reviewTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
            }

            if (entry.containsSpoilers && !revealSpoiler) {
                Surface(
                    color = CinemaCrimson.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CinemaCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { revealSpoiler = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = CinemaCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Spoiler Alert · Tap to reveal review",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaCrimson
                        )
                    }
                }
            } else {
                if (entry.reviewBody.isNotBlank()) {
                    Text(
                        text = entry.reviewBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaAlabaster.copy(alpha = 0.92f)
                    )
                }

                if (entry.favoriteQuote.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CinemaSlateElevated)
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatQuote,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\"${entry.favoriteQuote}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = CinemaSilver
                        )
                    }
                }

                if (entry.containsSpoilers && revealSpoiler) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { revealSpoiler = false }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                            tint = CinemaSilver,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hide spoilers",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaSilver
                        )
                    }
                }
            }
        }
    }
}
