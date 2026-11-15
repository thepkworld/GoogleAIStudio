package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CatalogTitle
import com.example.data.model.DataSourceOrigin
import com.example.data.model.ExternalRatingsAndAwards
import com.example.data.model.MediaType
import com.example.data.model.RatingScale
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.components.CinemaBackdropImage
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.RatingPill
import com.example.ui.components.formatWatchedDate
import com.example.ui.theme.CinemaTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TitleDetailScreen(
    titleId: String,
    catalogTitle: CatalogTitle?,
    vaultEntry: VaultEntry?,
    isLoadingDetail: Boolean,
    detailError: String? = null,
    isMutating: Boolean,
    onRetry: () -> Unit = {},
    onBack: () -> Unit,
    onRecommendationClick: (String) -> Unit,
    onPersonClick: (Int) -> Unit,
    onCollectionClick: (Int) -> Unit,
    onSeasonClick: (seriesId: Int, seasonNumber: Int) -> Unit,
    onSetStatus: (WatchStatus) -> Unit,
    onTogglePriority: () -> Unit,
    onUpdateEpisodes: (Long) -> Unit,
    onUpdateRewatch: (Long) -> Unit,
    onDeleteEntry: () -> Unit
) {
    val colors = CinemaTheme.colors
    val context = LocalContext.current
    val displayTitle = catalogTitle?.title?.ifBlank { vaultEntry?.title } ?: vaultEntry?.title ?: "Untitled"
    val displayYear = catalogTitle?.releaseYear ?: vaultEntry?.releaseYear?.toInt() ?: 2024
    val displayMediaType = catalogTitle?.mediaType ?: vaultEntry?.mediaTypeEnum ?: MediaType.MOVIE
    val displayDirector = catalogTitle?.directorOrCreator?.ifBlank { vaultEntry?.directorOrCreator }
        ?: vaultEntry?.directorOrCreator ?: "Unknown"
    val displayRuntime = catalogTitle?.runtimeOrSeasons?.ifBlank { vaultEntry?.runtimeOrSeasons }
        ?: vaultEntry?.runtimeOrSeasons ?: ""
    val displaySynopsis = catalogTitle?.synopsis?.ifBlank { vaultEntry?.synopsis }
        ?: vaultEntry?.synopsis ?: ""
    val displayGenres = catalogTitle?.genres?.ifEmpty { vaultEntry?.genres.orEmpty() }
        ?: vaultEntry?.genres.orEmpty()
    val displayPosterKey = vaultEntry?.posterKey ?: catalogTitle?.posterKey ?: "cinema"
    val displayPosterUrl = catalogTitle?.posterUrl?.ifBlank { vaultEntry?.posterUrl }
        ?: vaultEntry?.posterUrl.orEmpty()
    val displayBackdropUrl = catalogTitle?.backdropUrl?.ifBlank { vaultEntry?.backdropUrl }
        ?: vaultEntry?.backdropUrl.orEmpty()
    val totalEpisodes = catalogTitle?.totalEpisodes?.takeIf { it > 0 }
        ?: vaultEntry?.totalEpisodes?.toInt() ?: 0
    val isReleased = catalogTitle?.isReleased ?: vaultEntry?.isReleased ?: true

    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.obsidian)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item(key = "detail_hero") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CinemaBackdropImage(
                        imageUrl = displayBackdropUrl.ifBlank { displayPosterUrl },
                        fallbackKey = displayPosterKey,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(275.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(275.dp)
                            .background(
                                Brush.verticalGradient(
                                    0.0f to colors.scrimDark.copy(alpha = 0.42f),
                                    0.55f to colors.obsidian.copy(alpha = 0.72f),
                                    1.0f to colors.obsidian
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = colors.scrimDark.copy(alpha = 0.72f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("detail_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.onScrimLight
                                )
                            }
                        }

                        if (vaultEntry != null) {
                            Surface(
                                color = colors.scrimDark.copy(alpha = 0.72f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, colors.crimson.copy(alpha = 0.4f))
                            ) {
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.testTag("detail_delete_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove from Vault",
                                        tint = colors.crimson
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 165.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CinemaPosterArtwork(
                            posterKey = displayPosterKey,
                            posterUrl = displayPosterUrl,
                            title = displayTitle,
                            mediaType = displayMediaType,
                            userRating = vaultEntry?.userRating ?: 0.0,
                            status = vaultEntry?.statusEnum,
                            modifier = Modifier
                                .width(114.dp)
                                .height(166.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = displayTitle,
                                style = MaterialTheme.typography.displayMedium,
                                color = colors.alabaster
                            )

                            if (catalogTitle?.tagline?.isNotBlank() == true) {
                                Text(
                                    text = "\"${catalogTitle.tagline}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = colors.gold
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (catalogTitle != null && catalogTitle.criticScore > 0.0) {
                                    RatingPill(score = catalogTitle.criticScore, labelPrefix = "TMDB")
                                }
                                Text(
                                    text = "$displayYear",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.silver
                                )
                                if (catalogTitle?.ageRating?.isNotBlank() == true) {
                                    Surface(
                                        color = colors.slateElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = catalogTitle.ageRating,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.alabaster,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoadingDetail) {
                item(key = "detail_loading_bar") {
                    LinearProgressIndicator(
                        color = colors.gold,
                        trackColor = colors.slateElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            if (detailError != null && !isLoadingDetail && catalogTitle == null) {
                item(key = "detail_error_state") {
                    DetailErrorCard(
                        message = detailError,
                        onRetry = onRetry,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Status & Vault Controls
            item(key = "detail_status_controls") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isWatchlist = vaultEntry?.statusEnum == WatchStatus.WANT_TO_WATCH
                        val isWatched = vaultEntry?.statusEnum == WatchStatus.WATCHED

                        Button(
                            onClick = { onSetStatus(WatchStatus.WANT_TO_WATCH) },
                            enabled = !isMutating,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWatchlist) colors.gold else colors.slate,
                                contentColor = if (isWatchlist) colors.onGold else colors.alabaster
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isWatchlist) colors.gold else colors.borderSubtle
                            ),
                            modifier = Modifier
                                .weight(1.25f)
                                .height(50.dp)
                                .testTag("detail_want_to_watch_button")
                        ) {
                            Icon(Icons.Rounded.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWatchlist) "In Bucketlist" else "Add to Bucketlist",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { onSetStatus(WatchStatus.WATCHED) },
                            enabled = !isMutating,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWatched) colors.emerald else colors.slate,
                                contentColor = if (isWatched) {
                                    if (colors.isDark) colors.obsidian else colors.onGold
                                } else {
                                    colors.alabaster
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isWatched) colors.emerald else colors.borderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("detail_watched_button")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWatched) "Watched" else "Mark Seen",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!isReleased) {
                        val relLabel = catalogTitle?.releaseDate?.ifBlank { vaultEntry?.releaseDate.orEmpty() }
                            ?.ifBlank { displayYear.toString() } ?: displayYear.toString()
                        Surface(
                            color = colors.gold.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Today,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Upcoming release ($relLabel) · Can be added to Bucketlist",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.gold
                                )
                            }
                        }
                    }

                    if (vaultEntry != null && vaultEntry.statusEnum == WatchStatus.WATCHED) {
                        val intScore = if (vaultEntry.userRating > 0.0) vaultEntry.userRating.roundToInt().coerceIn(1, 10) else 0
                        Surface(
                            color = colors.slate,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colors.emerald.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "YOUR WATCH LOG",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.emerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (intScore > 0) {
                                                "$intScore/10 · ${RatingScale.labelFor(intScore)}"
                                            } else {
                                                "Watched · Tap Edit to rate 1–10"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.alabaster
                                        )
                                    }
                                    TextButton(onClick = { onSetStatus(WatchStatus.WATCHED) }) {
                                        Text("Edit Log", color = colors.gold, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (vaultEntry.effectiveWatchedAt > 0L) {
                                    Text(
                                        text = "Watched on ${formatWatchedDate(vaultEntry.effectiveWatchedAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.silver
                                    )
                                }
                                if (vaultEntry.reviewTitle.isNotBlank()) {
                                    Text(
                                        text = "\"${vaultEntry.reviewTitle}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        color = colors.gold
                                    )
                                }
                            }
                        }
                    }

                    if (vaultEntry != null) {
                        Surface(
                            color = colors.slate,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (vaultEntry.statusEnum == WatchStatus.WANT_TO_WATCH) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.PushPin, contentDescription = null, tint = colors.gold, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Pin as Up Next", style = MaterialTheme.typography.titleSmall, color = colors.alabaster)
                                                Text("Prioritize at the top of your watchlist", style = MaterialTheme.typography.bodySmall, color = colors.silver)
                                            }
                                        }
                                        Switch(
                                            checked = vaultEntry.priority,
                                            onCheckedChange = { onTogglePriority() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = colors.onGold,
                                                checkedTrackColor = colors.gold,
                                                uncheckedThumbColor = colors.silver,
                                                uncheckedTrackColor = colors.slateElevated
                                            )
                                        )
                                    }
                                }

                                if (displayMediaType == MediaType.SERIES && totalEpisodes > 0) {
                                    val watchedEp = vaultEntry.episodesWatched
                                    val progress = (watchedEp.toFloat() / totalEpisodes.toFloat()).coerceIn(0f, 1f)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Episode Progress", style = MaterialTheme.typography.titleSmall, color = colors.alabaster)
                                            Text("$watchedEp / $totalEpisodes EP", style = MaterialTheme.typography.labelMedium, color = colors.gold)
                                        }
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            color = colors.gold,
                                            trackColor = colors.slateElevated,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(7.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedButton(
                                                onClick = { onUpdateEpisodes((watchedEp - 1).coerceAtLeast(0)) },
                                                enabled = watchedEp > 0,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease episodes", modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("-1 Ep")
                                            }
                                            Button(
                                                onClick = { onUpdateEpisodes((watchedEp + 1).coerceAtMost(totalEpisodes.toLong())) },
                                                enabled = watchedEp < totalEpisodes,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.gold,
                                                    contentColor = colors.onGold
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Rounded.Add, contentDescription = "Increase episodes", modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("+1 Ep")
                                            }
                                        }
                                    }
                                }

                                if (vaultEntry.statusEnum == WatchStatus.WATCHED) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Repeat, contentDescription = null, tint = colors.emerald, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Rewatches", style = MaterialTheme.typography.titleSmall, color = colors.alabaster)
                                                Text("Logged ${vaultEntry.rewatchCount} rewatch(es)", style = MaterialTheme.typography.bodySmall, color = colors.silver)
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onUpdateRewatch((vaultEntry.rewatchCount - 1).coerceAtLeast(0)) },
                                                enabled = vaultEntry.rewatchCount > 0
                                            ) {
                                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease rewatch", tint = colors.silver)
                                            }
                                            Text("${vaultEntry.rewatchCount}", style = MaterialTheme.typography.titleMedium, color = colors.emerald)
                                            IconButton(onClick = { onUpdateRewatch(vaultEntry.rewatchCount + 1) }) {
                                                Icon(Icons.Rounded.Add, contentDescription = "Log rewatch", tint = colors.emerald)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Full TMDB Details
            item(key = "detail_info") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (displayGenres.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayGenres.forEach { genre ->
                                Surface(
                                    color = colors.slateElevated,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, colors.borderSubtle)
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.alabaster,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Dedicated Ratings & Awards Section (OMDb IMDb / Rotten Tomatoes / Metacritic + Awards)
                    val ratingsAndAwards = catalogTitle?.ratingsAndAwards
                    val fallbackAwardsText = catalogTitle?.awardsHighlight
                        ?.takeIf { it.isNotBlank() && !it.startsWith("Status:", ignoreCase = true) && !it.startsWith("Series Status:", ignoreCase = true) }
                        .orEmpty()

                    if (isLoadingDetail && ratingsAndAwards == null) {
                        RatingsAndAwardsSkeletonCard()
                    } else if (ratingsAndAwards != null && (ratingsAndAwards.hasRatings || ratingsAndAwards.hasAwards || fallbackAwardsText.isNotBlank())) {
                        RatingsAndAwardsSection(
                            ratingsAndAwards = ratingsAndAwards,
                            fallbackAwards = fallbackAwardsText,
                            dataSource = catalogTitle?.dataSource ?: DataSourceOrigin.TMDB_WITH_OMDB
                        )
                    } else if (fallbackAwardsText.isNotBlank()) {
                        RatingsAndAwardsSection(
                            ratingsAndAwards = ExternalRatingsAndAwards(
                                awards = fallbackAwardsText,
                                tmdbScore = catalogTitle?.criticScore ?: 0.0
                            ),
                            fallbackAwards = fallbackAwardsText,
                            dataSource = catalogTitle?.dataSource ?: DataSourceOrigin.LOCAL_CACHE
                        )
                    }

                    if (displaySynopsis.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Synopsis", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            Text(displaySynopsis, style = MaterialTheme.typography.bodyLarge, color = colors.silver)
                        }
                    } else if (isLoadingDetail) {
                        SynopsisSkeletonBlock()
                    }

                    // Where to Watch Providers
                    if (catalogTitle != null && catalogTitle.watchProviders.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LiveTv, contentDescription = null, tint = colors.gold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Where to Watch", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(catalogTitle.watchProviders, key = { "${it.id}_${it.tier}" }) { provider ->
                                    Surface(
                                        color = colors.slate,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, colors.borderSubtle)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (provider.logoUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(provider.logoUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = provider.name,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Column {
                                                Text(provider.name, style = MaterialTheme.typography.labelLarge, color = colors.alabaster)
                                                Text(provider.tier, style = MaterialTheme.typography.labelSmall, color = colors.emerald)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Franchise Collection Banner
                    val collection = catalogTitle?.collection
                    if (collection != null) {
                        Surface(
                            color = colors.slate,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onCollectionClick(collection.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CollectionsBookmark,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("PART OF COLLECTION", style = MaterialTheme.typography.labelSmall, color = colors.gold)
                                    Text(collection.name, style = MaterialTheme.typography.titleMedium, color = colors.alabaster)
                                }
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Open collection", tint = colors.gold)
                            }
                        }
                    }

                    // Seasons Explorer (TV Series)
                    val tmdbSeriesId = catalogTitle?.tmdbId
                    if (displayMediaType == MediaType.SERIES &&
                        tmdbSeriesId != null &&
                        catalogTitle.seasons.isNotEmpty()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Seasons & Episodes", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(catalogTitle.seasons, key = { "season_${it.seasonNumber}" }) { season ->
                                    Surface(
                                        color = colors.slate,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, colors.borderSubtle),
                                        modifier = Modifier
                                            .width(154.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { onSeasonClick(tmdbSeriesId, season.seasonNumber) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            CinemaPosterArtwork(
                                                posterKey = displayPosterKey,
                                                posterUrl = season.posterUrl.ifBlank { displayPosterUrl },
                                                title = season.name,
                                                mediaType = MediaType.SERIES,
                                                showBottomBadge = false,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                            )
                                            Text(
                                                text = season.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = colors.alabaster,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${season.episodeCount} Episodes",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.gold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Production Details
                    Surface(
                        color = colors.slate,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Production Details", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            DetailFactRow(
                                label = if (displayMediaType == MediaType.SERIES) "Creator / Showrunner" else "Director",
                                value = displayDirector
                            )
                            if (displayRuntime.isNotBlank()) {
                                DetailFactRow(
                                    label = if (displayMediaType == MediaType.SERIES) "Format" else "Runtime",
                                    value = displayRuntime
                                )
                            }
                            if (catalogTitle != null) {
                                if (catalogTitle.releaseDate.isNotBlank()) DetailFactRow("Release Date", catalogTitle.releaseDate)
                                if (catalogTitle.studioOrNetwork.isNotBlank()) DetailFactRow("Studio / Network", catalogTitle.studioOrNetwork)
                                if (catalogTitle.country.isNotBlank()) DetailFactRow("Country", catalogTitle.country)
                                if (catalogTitle.language.isNotBlank()) DetailFactRow("Language", catalogTitle.language)
                                if (catalogTitle.voteCount > 0) DetailFactRow("TMDB Votes", "${catalogTitle.voteCount} ratings")
                                if (catalogTitle.budget > 0) DetailFactRow("Budget", "$${NumberFormat.getInstance(Locale.US).format(catalogTitle.budget)}")
                                if (catalogTitle.revenue > 0) DetailFactRow("Box Office", "$${NumberFormat.getInstance(Locale.US).format(catalogTitle.revenue)}")
                            }
                        }
                    }

                    // Trailers & Videos
                    if (catalogTitle != null && catalogTitle.videos.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Trailers & Featurettes", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(catalogTitle.videos, key = { it.id }) { video ->
                                    val url = video.youtubeUrl
                                    Surface(
                                        color = colors.slate,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .width(210.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable(enabled = url != null) {
                                                url?.let {
                                                    runCatching {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                                                    }
                                                }
                                            }
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.PlayCircle, contentDescription = "Play ${video.name}", tint = colors.gold, modifier = Modifier.size(30.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(video.name, style = MaterialTheme.typography.titleSmall, color = colors.alabaster, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("${video.type} · ${video.site}", style = MaterialTheme.typography.labelSmall, color = colors.silver)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cast Section
                    if (catalogTitle != null && catalogTitle.cast.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Top Billed Cast", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(catalogTitle.cast, key = { "${it.id}_${it.actor}_${it.role}" }) { member ->
                                    Surface(
                                        color = colors.slate,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, colors.borderSubtle),
                                        modifier = Modifier
                                            .width(156.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable(enabled = member.id > 0) { onPersonClick(member.id) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (member.profileUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(member.profileUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = member.actor,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(colors.slateElevated)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(colors.gold.copy(alpha = 0.16f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = colors.gold, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Text(member.actor, style = MaterialTheme.typography.titleSmall, color = colors.alabaster, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(member.role, style = MaterialTheme.typography.bodySmall, color = colors.silver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Backdrop Gallery
                    if (catalogTitle != null && catalogTitle.backdrops.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Stills & Backdrops", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(catalogTitle.backdrops, key = { it }) { url ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                                        contentDescription = "Backdrop still",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(230.dp)
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(colors.slateElevated)
                                    )
                                }
                            }
                        }
                    }

                    // Keywords
                    if (catalogTitle != null && catalogTitle.keywords.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Themes & Keywords", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                catalogTitle.keywords.forEach { kw ->
                                    Surface(
                                        color = colors.slate,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, colors.borderSubtle)
                                    ) {
                                        Text(
                                            text = "#$kw",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.gold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TMDB Community Reviews
                    if (catalogTitle != null && catalogTitle.tmdbReviews.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("TMDB Community Reviews", style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
                            catalogTitle.tmdbReviews.take(4).forEach { review ->
                                var expanded by remember(review.id) { mutableStateOf(false) }
                                Surface(
                                    color = colors.slate,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, colors.borderSubtle),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { expanded = !expanded }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(review.author, style = MaterialTheme.typography.titleSmall, color = colors.alabaster)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (review.rating != null && review.rating > 0.0) {
                                                    RatingPill(score = review.rating)
                                                }
                                                if (review.createdAt.isNotBlank()) {
                                                    Text(review.createdAt, style = MaterialTheme.typography.labelSmall, color = colors.silver)
                                                }
                                            }
                                        }
                                        Text(
                                            text = review.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.silver,
                                            maxLines = if (expanded) 20 else 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (expanded) "Show less" else "Tap to read full review",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.gold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recommended Next
                    if (catalogTitle != null && catalogTitle.recommendations.isNotEmpty()) {
                        HorizontalTitleRow(
                            heading = "Recommended Next",
                            items = catalogTitle.recommendations,
                            keyPrefix = "rec",
                            onTitleClick = onRecommendationClick
                        )
                    }

                    // Similar Titles
                    if (catalogTitle != null && catalogTitle.similarTitles.isNotEmpty()) {
                        HorizontalTitleRow(
                            heading = "Similar Titles",
                            items = catalogTitle.similarTitles,
                            keyPrefix = "sim",
                            onTitleClick = onRecommendationClick
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && vaultEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.slate,
            titleContentColor = colors.alabaster,
            textContentColor = colors.silver,
            title = { Text("Remove from Vault?") },
            text = { Text("\"${vaultEntry.title}\" and your saved rating/review will be removed from your Vault.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteEntry()
                    }
                ) {
                    Text("Remove", color = colors.crimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = colors.silver)
                }
            }
        )
    }
}

@Composable
private fun HorizontalTitleRow(
    heading: String,
    items: List<CatalogTitle>,
    keyPrefix: String,
    onTitleClick: (String) -> Unit
) {
    val colors = CinemaTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(heading, style = MaterialTheme.typography.headlineSmall, color = colors.alabaster)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { "${keyPrefix}_${it.id}" }) { item ->
                Column(
                    modifier = Modifier
                        .width(128.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTitleClick(item.id) }
                ) {
                    CinemaPosterArtwork(
                        posterKey = item.posterKey,
                        posterUrl = item.posterUrl,
                        title = item.title,
                        mediaType = item.mediaType,
                        userRating = item.criticScore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.70f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.alabaster,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.releaseYear}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.silver
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailFactRow(
    label: String,
    value: String
) {
    val colors = CinemaTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.silver)
        Spacer(modifier = Modifier.width(16.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.alabaster, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RatingsAndAwardsSection(
    ratingsAndAwards: ExternalRatingsAndAwards,
    fallbackAwards: String,
    dataSource: DataSourceOrigin,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val awardsText = if (ratingsAndAwards.hasAwards) ratingsAndAwards.awards else fallbackAwards

    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.32f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("detail_ratings_awards_section")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ratings & Awards",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.alabaster
                )
                Surface(
                    color = colors.slateElevated,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Text(
                        text = dataSource.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.silver,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RatingMetricTile(
                    sourceLabel = "IMDb",
                    scoreValue = ratingsAndAwards.imdbRating.ifBlank { "—" },
                    subLabel = ratingsAndAwards.imdbVotes.takeIf { it.isNotBlank() }?.let { "$it votes" } ?: "Audience",
                    accentColor = colors.gold,
                    iconContent = {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "IMDb Rating",
                            tint = colors.gold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                RatingMetricTile(
                    sourceLabel = "Rotten Tomatoes",
                    scoreValue = ratingsAndAwards.rottenTomatoes.ifBlank { "—" },
                    subLabel = "Tomatometer",
                    accentColor = colors.crimson,
                    iconContent = {
                        RottenTomatoesIcon(modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.weight(1f)
                )

                val metaNumeric = Regex("(\\d+)").find(ratingsAndAwards.metacritic)?.groupValues?.getOrNull(1)?.toIntOrNull()
                val metaColor = when {
                    metaNumeric == null -> colors.silver
                    metaNumeric >= 61 -> colors.emerald
                    metaNumeric >= 40 -> colors.gold
                    else -> colors.crimson
                }
                RatingMetricTile(
                    sourceLabel = "Metacritic",
                    scoreValue = ratingsAndAwards.metacritic.ifBlank { "—" },
                    subLabel = "Metascore",
                    accentColor = metaColor,
                    iconContent = {
                        Surface(
                            color = metaColor.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, metaColor.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Assessment,
                                contentDescription = "Metacritic Score",
                                tint = metaColor,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(16.dp)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (awardsText.isNotBlank()) {
                Surface(
                    color = colors.gold.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_awards_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.gold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = "Awards",
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "AWARDS & HONORS",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.gold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = awardsText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.alabaster,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingMetricTile(
    sourceLabel: String,
    scoreValue: String,
    subLabel: String,
    accentColor: Color,
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slateElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            iconContent()
            Text(
                text = scoreValue,
                style = MaterialTheme.typography.titleMedium,
                color = if (scoreValue == "—") colors.silver else colors.alabaster,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = sourceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.silver.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RottenTomatoesIcon(modifier: Modifier = Modifier) {
    val colors = CinemaTheme.colors
    val crimsonColor = colors.crimson
    val emeraldColor = colors.emerald
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = crimsonColor,
                radius = minOf(w, h) * 0.38f,
                center = Offset(w * 0.5f, h * 0.56f)
            )
            drawCircle(
                color = emeraldColor,
                radius = minOf(w, h) * 0.14f,
                center = Offset(w * 0.5f, h * 0.20f)
            )
        }
    }
}

@Composable
private fun RatingsAndAwardsSkeletonCard(modifier: Modifier = Modifier) {
    val colors = CinemaTheme.colors
    val pulse = rememberInfiniteTransition(label = "ratings_skeleton_pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .testTag("detail_ratings_skeleton")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.slateElevated.copy(alpha = alpha))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.slateElevated.copy(alpha = alpha))
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.slateElevated.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun SynopsisSkeletonBlock(modifier: Modifier = Modifier) {
    val colors = CinemaTheme.colors
    val pulse = rememberInfiniteTransition(label = "synopsis_skeleton_pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "synopsis_alpha"
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.slateElevated.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.slateElevated.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.slateElevated.copy(alpha = alpha))
        )
    }
}

@Composable
private fun DetailErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.crimson.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("detail_error_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = colors.crimson,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Unable to load title details",
                style = MaterialTheme.typography.titleMedium,
                color = colors.alabaster,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.silver,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.gold,
                    contentColor = colors.onGold
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry", fontWeight = FontWeight.Bold)
            }
        }
    }
}
