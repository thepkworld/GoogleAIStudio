package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType
import com.example.data.model.RatingScale
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.components.CinemaBackdropImage
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.RatingPill
import com.example.ui.components.TenStarRatingBar
import com.example.ui.components.WatchedDatePickerDialog
import com.example.ui.components.formatWatchedDate
import com.example.ui.components.isSameLocalDay
import com.example.ui.components.normalizeToLocalNoonMillis
import com.example.ui.components.todayLocalNoonMillis
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated
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
    isMutating: Boolean,
    onBack: () -> Unit,
    onRecommendationClick: (String) -> Unit,
    onPersonClick: (Int) -> Unit,
    onCollectionClick: (Int) -> Unit,
    onSeasonClick: (seriesId: Int, seasonNumber: Int) -> Unit,
    onSetStatus: (WatchStatus) -> Unit,
    onTogglePriority: () -> Unit,
    onUpdateEpisodes: (Long) -> Unit,
    onUpdateRewatch: (Long) -> Unit,
    onSaveReview: (
        status: WatchStatus,
        rating: Double,
        reviewTitle: String,
        reviewBody: String,
        favoriteQuote: String,
        containsSpoilers: Boolean,
        watchedAt: Long?
    ) -> Unit,
    onDeleteEntry: () -> Unit
) {
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

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDetailDatePicker by remember { mutableStateOf(false) }

    var draftStars by remember(vaultEntry?.id, vaultEntry?.userRating) {
        val initial = vaultEntry?.userRating ?: 0.0
        mutableIntStateOf(if (initial > 0.0) initial.roundToInt().coerceIn(1, 10) else 0)
    }
    var draftWatchedAt by remember(vaultEntry?.id, vaultEntry?.watchedAt) {
        val existing = vaultEntry?.effectiveWatchedAt ?: 0L
        mutableLongStateOf(
            if (existing > 0L) normalizeToLocalNoonMillis(existing) else todayLocalNoonMillis()
        )
    }
    var draftReviewTitle by remember(vaultEntry?.id, vaultEntry?.reviewTitle) {
        mutableStateOf(vaultEntry?.reviewTitle.orEmpty())
    }
    var draftReviewBody by remember(vaultEntry?.id, vaultEntry?.reviewBody) {
        mutableStateOf(vaultEntry?.reviewBody.orEmpty())
    }
    var draftQuote by remember(vaultEntry?.id, vaultEntry?.favoriteQuote) {
        mutableStateOf(vaultEntry?.favoriteQuote.orEmpty())
    }
    var draftSpoilers by remember(vaultEntry?.id, vaultEntry?.containsSpoilers) {
        mutableStateOf(vaultEntry?.containsSpoilers ?: false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaObsidian)
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
                                    0.0f to CinemaObsidian.copy(alpha = 0.42f),
                                    0.55f to CinemaObsidian.copy(alpha = 0.72f),
                                    1.0f to CinemaObsidian
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
                            color = CinemaObsidian.copy(alpha = 0.72f),
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
                                    tint = CinemaAlabaster
                                )
                            }
                        }

                        if (vaultEntry != null) {
                            Surface(
                                color = CinemaObsidian.copy(alpha = 0.72f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, CinemaCrimson.copy(alpha = 0.4f))
                            ) {
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.testTag("detail_delete_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove from Vault",
                                        tint = CinemaCrimson
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
                                color = CinemaAlabaster
                            )

                            if (catalogTitle?.tagline?.isNotBlank() == true) {
                                Text(
                                    text = "\"${catalogTitle.tagline}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = CinemaGold
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
                                    color = CinemaSilver
                                )
                                if (catalogTitle?.ageRating?.isNotBlank() == true) {
                                    Surface(
                                        color = CinemaSlateElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = catalogTitle.ageRating,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaAlabaster,
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
                        color = CinemaGold,
                        trackColor = CinemaSlateElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
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
                                containerColor = if (isWatchlist) CinemaGold else CinemaSlate,
                                contentColor = if (isWatchlist) CinemaObsidian else CinemaAlabaster
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isWatchlist) CinemaGold else CinemaBorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("detail_want_to_watch_button")
                        ) {
                            Icon(Icons.Rounded.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWatchlist) "In BucketList" else "BucketList",
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
                                containerColor = if (isWatched) CinemaEmerald else CinemaSlate,
                                contentColor = if (isWatched) CinemaObsidian else CinemaAlabaster
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isWatched) CinemaEmerald else CinemaBorderSubtle
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

                    if (vaultEntry != null) {
                        Surface(
                            color = CinemaSlate,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CinemaBorderSubtle),
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
                                            Icon(Icons.Rounded.PushPin, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Pin as Up Next", style = MaterialTheme.typography.titleSmall, color = CinemaAlabaster)
                                                Text("Prioritize at the top of your watchlist", style = MaterialTheme.typography.bodySmall, color = CinemaSilver)
                                            }
                                        }
                                        Switch(
                                            checked = vaultEntry.priority,
                                            onCheckedChange = { onTogglePriority() },
                                            colors = SwitchDefaults.colors(checkedThumbColor = CinemaObsidian, checkedTrackColor = CinemaGold)
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
                                            Text("Episode Progress", style = MaterialTheme.typography.titleSmall, color = CinemaAlabaster)
                                            Text("$watchedEp / $totalEpisodes EP", style = MaterialTheme.typography.labelMedium, color = CinemaGold)
                                        }
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            color = CinemaGold,
                                            trackColor = CinemaSlateElevated,
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
                                                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = CinemaObsidian),
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
                                            Icon(Icons.Rounded.Repeat, contentDescription = null, tint = CinemaEmerald, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Rewatches", style = MaterialTheme.typography.titleSmall, color = CinemaAlabaster)
                                                Text("Logged ${vaultEntry.rewatchCount} rewatch(es)", style = MaterialTheme.typography.bodySmall, color = CinemaSilver)
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onUpdateRewatch((vaultEntry.rewatchCount - 1).coerceAtLeast(0)) },
                                                enabled = vaultEntry.rewatchCount > 0
                                            ) {
                                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease rewatch", tint = CinemaSilver)
                                            }
                                            Text("${vaultEntry.rewatchCount}", style = MaterialTheme.typography.titleMedium, color = CinemaEmerald)
                                            IconButton(onClick = { onUpdateRewatch(vaultEntry.rewatchCount + 1) }) {
                                                Icon(Icons.Rounded.Add, contentDescription = "Log rewatch", tint = CinemaEmerald)
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
                                    color = CinemaSlateElevated,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, CinemaBorderSubtle)
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CinemaAlabaster,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (displaySynopsis.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Synopsis", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            Text(displaySynopsis, style = MaterialTheme.typography.bodyLarge, color = CinemaSilver)
                        }
                    }

                    // Where to Watch Providers
                    if (catalogTitle != null && catalogTitle.watchProviders.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LiveTv, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Where to Watch", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(catalogTitle.watchProviders, key = { "${it.id}_${it.tier}" }) { provider ->
                                    Surface(
                                        color = CinemaSlate,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CinemaBorderSubtle)
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
                                                Text(provider.name, style = MaterialTheme.typography.labelLarge, color = CinemaAlabaster)
                                                Text(provider.tier, style = MaterialTheme.typography.labelSmall, color = CinemaEmerald)
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
                            color = CinemaSlate,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.45f)),
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
                                    tint = CinemaGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("PART OF COLLECTION", style = MaterialTheme.typography.labelSmall, color = CinemaGold)
                                    Text(collection.name, style = MaterialTheme.typography.titleMedium, color = CinemaAlabaster)
                                }
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Open collection", tint = CinemaGold)
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
                            Text("Seasons & Episodes", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(catalogTitle.seasons, key = { "season_${it.seasonNumber}" }) { season ->
                                    Surface(
                                        color = CinemaSlate,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CinemaBorderSubtle),
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
                                                color = CinemaAlabaster,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${season.episodeCount} Episodes",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CinemaGold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (catalogTitle?.awardsHighlight?.isNotBlank() == true) {
                        Surface(
                            color = CinemaGold.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(catalogTitle.awardsHighlight, style = MaterialTheme.typography.titleSmall, color = CinemaGold)
                            }
                        }
                    }

                    // Production Details
                    Surface(
                        color = CinemaSlate,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CinemaBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Production Details", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
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
                            Text("Trailers & Featurettes", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(catalogTitle.videos, key = { it.id }) { video ->
                                    val url = video.youtubeUrl
                                    Surface(
                                        color = CinemaSlate,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.35f)),
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
                                            Icon(Icons.Rounded.PlayCircle, contentDescription = "Play ${video.name}", tint = CinemaGold, modifier = Modifier.size(30.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(video.name, style = MaterialTheme.typography.titleSmall, color = CinemaAlabaster, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("${video.type} · ${video.site}", style = MaterialTheme.typography.labelSmall, color = CinemaSilver)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cast Section (Clickable to PersonDetailScreen when TMDB person id > 0)
                    if (catalogTitle != null && catalogTitle.cast.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Top Billed Cast", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(catalogTitle.cast, key = { "${it.id}_${it.actor}_${it.role}" }) { member ->
                                    Surface(
                                        color = CinemaSlate,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CinemaBorderSubtle),
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
                                                        .background(CinemaSlateElevated)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(CinemaGold.copy(alpha = 0.16f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Text(member.actor, style = MaterialTheme.typography.titleSmall, color = CinemaAlabaster, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(member.role, style = MaterialTheme.typography.bodySmall, color = CinemaSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Backdrop Gallery
                    if (catalogTitle != null && catalogTitle.backdrops.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Stills & Backdrops", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
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
                                            .background(CinemaSlateElevated)
                                    )
                                }
                            }
                        }
                    }

                    // Keywords
                    if (catalogTitle != null && catalogTitle.keywords.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Themes & Keywords", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                catalogTitle.keywords.forEach { kw ->
                                    Surface(
                                        color = CinemaSlate,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, CinemaBorderSubtle)
                                    ) {
                                        Text(
                                            text = "#$kw",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaGold,
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
                            Text("TMDB Community Reviews", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            catalogTitle.tmdbReviews.take(4).forEach { review ->
                                var expanded by remember(review.id) { mutableStateOf(false) }
                                Surface(
                                    color = CinemaSlate,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, CinemaBorderSubtle),
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
                                            Text(review.author, style = MaterialTheme.typography.titleSmall, color = CinemaAlabaster)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (review.rating != null && review.rating > 0.0) {
                                                    RatingPill(score = review.rating)
                                                }
                                                if (review.createdAt.isNotBlank()) {
                                                    Text(review.createdAt, style = MaterialTheme.typography.labelSmall, color = CinemaSilver)
                                                }
                                            }
                                        }
                                        Text(
                                            text = review.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CinemaSilver,
                                            maxLines = if (expanded) 20 else 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (expanded) "Show less" else "Tap to read full review",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaGold
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

            // Personal Rating & Full Review Editor Section
            item(key = "detail_review_editor") {
                Surface(
                    color = CinemaSlate,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.RateReview, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Your Rating & Review", style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
                            }
                            if (draftStars > 0) {
                                TextButton(onClick = { draftStars = 0 }) {
                                    Text("Clear", color = CinemaSilver, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        TenStarRatingBar(
                            selectedStars = draftStars,
                            onStarSelected = { star -> draftStars = star }
                        )

                        // Watched Date Selector (Today + Calendar)
                        val isToday = isSameLocalDay(draftWatchedAt, todayLocalNoonMillis())
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Watched Date", style = MaterialTheme.typography.titleSmall, color = CinemaAlabaster)
                                Text(
                                    text = formatWatchedDate(draftWatchedAt),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CinemaGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { draftWatchedAt = todayLocalNoonMillis() },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isToday) CinemaGold else CinemaBorderSubtle),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isToday) CinemaGold.copy(alpha = 0.16f) else CinemaSlateElevated,
                                        contentColor = if (isToday) CinemaGold else CinemaAlabaster
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Icon(Icons.Rounded.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Today", style = MaterialTheme.typography.labelLarge)
                                }
                                OutlinedButton(
                                    onClick = { showDetailDatePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (!isToday) CinemaGold else CinemaBorderSubtle),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (!isToday) CinemaGold.copy(alpha = 0.16f) else CinemaSlateElevated,
                                        contentColor = if (!isToday) CinemaGold else CinemaAlabaster
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick watched date", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Calendar", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }

                        HorizontalDivider(color = CinemaBorderSubtle)

                        OutlinedTextField(
                            value = draftReviewTitle,
                            onValueChange = { if (it.length <= 160) draftReviewTitle = it },
                            label = { Text("Review Headline") },
                            placeholder = { Text("e.g., Towering sci-fi cinema") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaGold,
                                unfocusedBorderColor = CinemaBorderSubtle,
                                focusedLabelColor = CinemaGold,
                                unfocusedLabelColor = CinemaSilver,
                                focusedTextColor = CinemaAlabaster,
                                unfocusedTextColor = CinemaAlabaster,
                                cursorColor = CinemaGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("review_title_input")
                        )

                        OutlinedTextField(
                            value = draftReviewBody,
                            onValueChange = { if (it.length <= 4000) draftReviewBody = it },
                            label = { Text("Full Review & Notes") },
                            placeholder = { Text("Write about direction, score, pacing, performances…") },
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaGold,
                                unfocusedBorderColor = CinemaBorderSubtle,
                                focusedLabelColor = CinemaGold,
                                unfocusedLabelColor = CinemaSilver,
                                focusedTextColor = CinemaAlabaster,
                                unfocusedTextColor = CinemaAlabaster,
                                cursorColor = CinemaGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("review_body_input")
                        )

                        OutlinedTextField(
                            value = draftQuote,
                            onValueChange = { if (it.length <= 400) draftQuote = it },
                            label = { Text("Favorite Quote or Scene (Optional)") },
                            leadingIcon = {
                                Icon(Icons.Rounded.FormatQuote, contentDescription = null, tint = CinemaGold)
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CinemaGold,
                                unfocusedBorderColor = CinemaBorderSubtle,
                                focusedLabelColor = CinemaGold,
                                unfocusedLabelColor = CinemaSilver,
                                focusedTextColor = CinemaAlabaster,
                                unfocusedTextColor = CinemaAlabaster,
                                cursorColor = CinemaGold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = if (draftSpoilers) CinemaCrimson else CinemaSilver,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mark as containing spoilers", style = MaterialTheme.typography.bodyMedium, color = CinemaAlabaster)
                            }
                            Switch(
                                checked = draftSpoilers,
                                onCheckedChange = { draftSpoilers = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CinemaObsidian, checkedTrackColor = CinemaCrimson)
                            )
                        }

                        Button(
                            onClick = {
                                val targetStatus = vaultEntry?.statusEnum ?: WatchStatus.WATCHED
                                onSaveReview(
                                    targetStatus,
                                    draftStars.toDouble(),
                                    draftReviewTitle,
                                    draftReviewBody,
                                    draftQuote,
                                    draftSpoilers,
                                    if (targetStatus == WatchStatus.WATCHED) draftWatchedAt else null
                                )
                            },
                            enabled = !isMutating,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = CinemaObsidian),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_review_button")
                        ) {
                            Text(
                                text = if (vaultEntry == null) "Save to Library & Publish Review" else "Save Rating & Review",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDetailDatePicker) {
        WatchedDatePickerDialog(
            initialLocalMillis = draftWatchedAt,
            onDismiss = { showDetailDatePicker = false },
            onDateSelected = { pickedLocal ->
                draftWatchedAt = pickedLocal
                showDetailDatePicker = false
            }
        )
    }

    if (showDeleteDialog && vaultEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CinemaSlate,
            titleContentColor = CinemaAlabaster,
            textContentColor = CinemaSilver,
            title = { Text("Remove from Vault?") },
            text = { Text("\"${vaultEntry.title}\" and your saved rating/review will be removed from your Vault.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteEntry()
                    }
                ) {
                    Text("Remove", color = CinemaCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = CinemaSilver)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(heading, style = MaterialTheme.typography.headlineSmall, color = CinemaAlabaster)
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
                        color = CinemaAlabaster,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.releaseYear}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaSilver
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CinemaSilver)
        Spacer(modifier = Modifier.width(16.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = CinemaAlabaster, fontWeight = FontWeight.Medium)
    }
}
