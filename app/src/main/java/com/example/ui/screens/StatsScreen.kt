package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.RatingScale
import com.example.ui.ActivityBucket
import com.example.ui.DetailedStats
import com.example.ui.GenreStat
import com.example.ui.components.EmptyVaultState
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaAsh
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
import kotlin.math.roundToInt

private enum class ActivityChartMode(val label: String) {
    MONTHLY("Monthly"),
    ANNUAL("Annual")
}

private enum class GenreViewMode(val label: String) {
    MOST_WATCHED("Most Watched"),
    TOP_RATED("Top Rated")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    stats: DetailedStats,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activityMode by remember { mutableStateOf(ActivityChartMode.MONTHLY) }
    var genreMode by remember { mutableStateOf(GenreViewMode.MOST_WATCHED) }

    if (stats.totalWatched == 0 && stats.totalBucketlist == 0) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("stats_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 96.dp)
        ) {
            item(key = "stats_empty") {
                EmptyVaultState(
                    icon = Icons.Rounded.Insights,
                    title = "Your Cinema Stats Await",
                    subtitle = "Log watched movies, rate series 1–10, or build your Bucketlist to unlock deep analytics, watch hours, and genre breakdowns.",
                    actionLabel = "Explore Movies & Series",
                    onActionClick = onBrowseClick
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overview Hero + Completion Progress Rings
        item(key = "stats_hero") {
            StatsHeroOverviewCard(stats = stats)
        }

        // 2. Watch Time & Engagement Breakdown
        item(key = "stats_watch_time") {
            WatchTimeBreakdownCard(stats = stats)
        }

        // 3. Monthly / Annual Watching Activity Chart
        item(key = "stats_activity") {
            WatchingActivityCard(
                stats = stats,
                mode = activityMode,
                onModeChange = { activityMode = it }
            )
        }

        // 4. 1–10 Rating Distribution
        item(key = "stats_ratings") {
            RatingDistributionCard(stats = stats)
        }

        // 5. Genre Breakdown & Top-Rated Genres
        if (stats.genreBreakdown.isNotEmpty()) {
            item(key = "stats_genres") {
                GenreAnalyticsCard(
                    mostWatched = stats.genreBreakdown,
                    topRated = stats.topRatedGenres,
                    mode = genreMode,
                    onModeChange = { genreMode = it }
                )
            }
        }

        // 6. Release Year / Decade Breakdown
        if (stats.decadeBreakdown.isNotEmpty() && stats.totalWatched > 0) {
            item(key = "stats_decades") {
                HorizontalBarSectionCard(
                    title = "Release Eras & Decades",
                    subtitle = "Distribution across cinema eras",
                    icon = Icons.Rounded.History,
                    accentColor = CinemaIndigo,
                    items = stats.decadeBreakdown,
                    tag = "stats_decades_card"
                )
            }
        }

        // 7. Language & OTT Platform / Studio Breakdown
        if (stats.languageBreakdown.isNotEmpty() || stats.platformBreakdown.isNotEmpty()) {
            item(key = "stats_languages_platforms") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (stats.languageBreakdown.isNotEmpty()) {
                        HorizontalBarSectionCard(
                            title = "Languages Watched",
                            subtitle = "Original & spoken audio diversity",
                            icon = Icons.Rounded.Language,
                            accentColor = CinemaEmerald,
                            items = stats.languageBreakdown,
                            tag = "stats_languages_card"
                        )
                    }
                    if (stats.platformBreakdown.isNotEmpty()) {
                        HorizontalBarSectionCard(
                            title = "OTT & Studio Footprint",
                            subtitle = "Networks, streamers & studios",
                            icon = Icons.Rounded.LiveTv,
                            accentColor = CinemaGold,
                            items = stats.platformBreakdown,
                            tag = "stats_platforms_card"
                        )
                    }
                }
            }
        }

        // 8. Top Directors & Creators
        if (stats.topCreators.isNotEmpty()) {
            item(key = "stats_creators") {
                TopCreatorsCard(creators = stats.topCreators)
            }
        }
    }
}

@Composable
private fun StatsHeroOverviewCard(stats: DetailedStats) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.30f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to CinemaGold.copy(alpha = 0.09f),
                        1f to Color.Transparent
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "CINEMA INTELLIGENCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Viewing Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CinemaAlabaster
                    )
                }
                if (stats.averageRating > 0.0) {
                    Surface(
                        color = CinemaGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.40f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", stats.averageRating)} AVG",
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Key Stat Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "WATCHED",
                    value = "${stats.totalWatched}",
                    caption = "${stats.watchedMovies}M · ${stats.watchedSeries}S",
                    accentColor = CinemaEmerald,
                    icon = Icons.Rounded.CheckCircle,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_total_watched")
                )
                MetricTile(
                    label = "BUCKETLIST",
                    value = "${stats.totalBucketlist}",
                    caption = "${stats.priorityBucketlist} Up Next",
                    accentColor = CinemaGold,
                    icon = Icons.Rounded.Bookmark,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_total_bucketlist")
                )
                MetricTile(
                    label = "THIS MONTH",
                    value = "${stats.watchedThisMonth}",
                    caption = stats.currentMonthLabel,
                    accentColor = CinemaIndigo,
                    icon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = CinemaBorderSubtle)

            // Completion Rate Progress Rings
            Text(
                text = "COMPLETION RATES",
                style = MaterialTheme.typography.labelSmall,
                color = CinemaSilver,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompletionRingItem(
                    label = "Movies",
                    progress = stats.moviesCompletionRate,
                    detail = "${stats.watchedMovies}/${stats.watchedMovies + stats.bucketlistMovies}",
                    color = CinemaGold,
                    modifier = Modifier.weight(1f)
                )
                CompletionRingItem(
                    label = "Series",
                    progress = stats.seriesCompletionRate,
                    detail = "${stats.watchedSeries}/${stats.watchedSeries + stats.bucketlistSeries}",
                    color = CinemaEmerald,
                    modifier = Modifier.weight(1f)
                )
                CompletionRingItem(
                    label = "Episodes",
                    progress = stats.episodeCompletionRate,
                    detail = if (stats.totalEpisodesTarget > 0) {
                        "${stats.totalEpisodesWatched}/${stats.totalEpisodesTarget}"
                    } else {
                        "${stats.totalEpisodesWatched} ep"
                    },
                    color = CinemaIndigo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    caption: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaObsidian.copy(alpha = 0.65f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = CinemaAlabaster.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompletionRingItem(
    label: String,
    progress: Float,
    detail: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "ring_$label"
    )
    val percentText = "${(animatedProgress * 100f).roundToInt()}%"

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(76.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                drawArc(
                    color = CinemaSlateElevated,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                if (animatedProgress > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            Text(
                text = percentText,
                style = MaterialTheme.typography.titleSmall,
                color = CinemaAlabaster,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CinemaAlabaster,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = CinemaSilver
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatchTimeBreakdownCard(stats: DetailedStats) {
    val totalHrs = stats.totalHoursWatched.coerceAtLeast(0.0)
    val movieShare = if (totalHrs > 0.0) (stats.movieHoursWatched / totalHrs).toFloat().coerceIn(0f, 1f) else 0.5f

    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_watch_time_card")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Time Spent Watching",
                        style = MaterialTheme.typography.titleLarge,
                        color = CinemaAlabaster
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.1f hrs", totalHrs),
                    style = MaterialTheme.typography.headlineSmall,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stacked split bar: Movies vs Series hours
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(CinemaSlateElevated)
            ) {
                if (totalHrs > 0.0) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (movieShare > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(movieShare.coerceAtLeast(0.01f))
                                    .background(CinemaGold)
                            )
                        }
                        if (1f - movieShare > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((1f - movieShare).coerceAtLeast(0.01f))
                                    .background(CinemaEmerald)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CinemaGold)
                    )
                    Text(
                        text = "Movies: ${String.format(Locale.US, "%.1f", stats.movieHoursWatched)} hrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaAlabaster
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CinemaEmerald)
                    )
                    Text(
                        text = "Series: ${String.format(Locale.US, "%.1f", stats.seriesHoursWatched)} hrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaAlabaster
                    )
                }
            }

            HorizontalDivider(color = CinemaBorderSubtle)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatChip(
                    icon = Icons.Rounded.Tv,
                    label = "${stats.totalEpisodesWatched} Episodes",
                    accentColor = CinemaIndigo
                )
                MiniStatChip(
                    icon = Icons.Rounded.Repeat,
                    label = "${stats.totalRewatches} Rewatches",
                    accentColor = CinemaEmerald
                )
                MiniStatChip(
                    icon = Icons.Rounded.RateReview,
                    label = "${stats.totalReviews} Reviews",
                    accentColor = CinemaGold
                )
                MiniStatChip(
                    icon = Icons.Rounded.Movie,
                    label = "${stats.watchedThisYear} This Year",
                    accentColor = CinemaAlabaster
                )
            }
        }
    }
}

@Composable
private fun MiniStatChip(
    icon: ImageVector,
    label: String,
    accentColor: Color
) {
    Surface(
        color = CinemaObsidian.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = CinemaAlabaster
            )
        }
    }
}

@Composable
private fun WatchingActivityCard(
    stats: DetailedStats,
    mode: ActivityChartMode,
    onModeChange: (ActivityChartMode) -> Unit
) {
    val buckets: List<ActivityBucket> = if (mode == ActivityChartMode.MONTHLY) {
        stats.monthlyActivity
    } else {
        stats.annualActivity
    }
    val maxTotal = (buckets.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)

    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_activity_card")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Watching Activity",
                        style = MaterialTheme.typography.titleLarge,
                        color = CinemaAlabaster
                    )
                    Text(
                        text = if (mode == ActivityChartMode.MONTHLY) {
                            "Last 6 months log"
                        } else {
                            "Last 5 years log"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaSilver
                    )
                }

                SegmentedPillToggle(
                    options = ActivityChartMode.entries,
                    selected = mode,
                    labelFor = { it.label },
                    onSelect = onModeChange
                )
            }

            // Stacked Vertical Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                buckets.forEach { bucket ->
                    val movieFraction = bucket.moviesCount.toFloat() / maxTotal.toFloat()
                    val seriesFraction = bucket.seriesCount.toFloat() / maxTotal.toFloat()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (bucket.total > 0) {
                            Text(
                                text = "${bucket.total}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.56f)
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Subtle track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CinemaSlateElevated.copy(alpha = 0.45f))
                            )
                            if (bucket.total > 0) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight((movieFraction + seriesFraction).coerceIn(0.08f, 1f))
                                        .clip(RoundedCornerShape(8.dp)),
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (bucket.seriesCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(bucket.seriesCount.toFloat())
                                                .background(CinemaEmerald)
                                        )
                                    }
                                    if (bucket.moviesCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(bucket.moviesCount.toFloat())
                                                .background(CinemaGold)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bucket.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaSilver,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = CinemaGold, label = "Movies")
                LegendItem(color = CinemaEmerald, label = "Series")
            }
        }
    }
}

@Composable
private fun RatingDistributionCard(stats: DetailedStats) {
    val maxBucket = (stats.ratingDistribution1To10.maxOrNull() ?: 0).coerceAtLeast(1)

    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_rating_dist_card")
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "1–10 Rating Distribution",
                            style = MaterialTheme.typography.titleLarge,
                            color = CinemaAlabaster
                        )
                    }
                    Text(
                        text = if (stats.ratedCount > 0) {
                            "${stats.ratedCount} rated · ${stats.masterpiecesCount} Masterpiece / Excellent (9–10)"
                        } else {
                            "Rate watched titles 1–10 to build your taste curve"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaSilver
                    )
                }
            }

            // 10-column Histogram
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                stats.ratingDistribution1To10.forEachIndexed { idx, count ->
                    val score = idx + 1
                    val barColor = when {
                        score >= 9 -> CinemaGold
                        score >= 7 -> CinemaEmerald
                        score >= 5 -> CinemaAlabaster
                        else -> CinemaCrimson
                    }
                    val heightFraction = if (count > 0) {
                        (count.toFloat() / maxBucket.toFloat()).coerceIn(0.10f, 1f)
                    } else {
                        0f
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (count > 0) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = barColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.76f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CinemaSlateElevated.copy(alpha = 0.40f))
                            )
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.76f)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(barColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (count > 0) barColor else CinemaAsh,
                            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            if (stats.averageRating > 0.0) {
                Surface(
                    color = CinemaObsidian.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.22f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Average Score Verdict",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaSilver
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", stats.averageRating)}/10 · ${RatingScale.labelFor(stats.averageRating)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreAnalyticsCard(
    mostWatched: List<GenreStat>,
    topRated: List<GenreStat>,
    mode: GenreViewMode,
    onModeChange: (GenreViewMode) -> Unit
) {
    val activeList = if (mode == GenreViewMode.MOST_WATCHED) mostWatched else topRated.ifEmpty { mostWatched }
    val maxCount = (mostWatched.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_genre_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Genre Insights",
                        style = MaterialTheme.typography.titleLarge,
                        color = CinemaAlabaster
                    )
                    Text(
                        text = if (mode == GenreViewMode.MOST_WATCHED) {
                            "Most watched genres"
                        } else {
                            "Highest average rating by genre"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaSilver
                    )
                }

                SegmentedPillToggle(
                    options = GenreViewMode.entries,
                    selected = mode,
                    labelFor = { it.label },
                    onSelect = onModeChange
                )
            }

            activeList.forEach { genre ->
                val fillFraction = if (mode == GenreViewMode.MOST_WATCHED) {
                    (genre.count.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
                } else {
                    (genre.averageRating.toFloat() / 10f).coerceIn(0.08f, 1f)
                }
                val barColor = if (mode == GenreViewMode.MOST_WATCHED) CinemaEmerald else CinemaGold

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = genre.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = CinemaAlabaster
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (genre.averageRating > 0.0) {
                                Text(
                                    text = "★ ${String.format(Locale.US, "%.1f", genre.averageRating)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CinemaGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${genre.count} ${if (genre.count == 1) "title" else "titles"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaSilver
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(CinemaSlateElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fillFraction)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalBarSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    items: List<Pair<String, Int>>,
    tag: String
) {
    val maxCount = (items.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = CinemaAlabaster
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaSilver
                    )
                }
            }

            items.forEach { (label, count) ->
                val fraction = if (count > 0) {
                    (count.toFloat() / maxCount.toFloat()).coerceIn(0.06f, 1f)
                } else {
                    0f
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaAlabaster,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(104.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(9.dp)
                            .clip(CircleShape)
                            .background(CinemaSlateElevated)
                    ) {
                        if (fraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                        }
                    }
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (count > 0) accentColor else CinemaAsh,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopCreatorsCard(creators: List<Pair<String, Int>>) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_creators_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Most Watched Directors & Creators",
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaAlabaster
                )
            }

            creators.forEachIndexed { index, (name, count) ->
                Surface(
                    color = CinemaObsidian.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CinemaBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = CinemaGold.copy(alpha = 0.16f),
                                shape = CircleShape,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CinemaGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = CinemaSilver,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall,
                                color = CinemaAlabaster,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "$count ${if (count == 1) "title" else "titles"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> SegmentedPillToggle(
    options: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit
) {
    Surface(
        color = CinemaObsidian.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle)
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            options.forEach { option ->
                val isSelected = option == selected
                Surface(
                    color = if (isSelected) CinemaGold else Color.Transparent,
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { onSelect(option) }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelFor(option),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CinemaObsidian else CinemaSilver,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CinemaSilver
        )
    }
}
