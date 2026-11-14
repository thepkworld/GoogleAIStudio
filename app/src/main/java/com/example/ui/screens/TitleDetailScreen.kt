package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.RatingPill
import com.example.ui.components.posterDrawableForKey
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TitleDetailScreen(
    titleId: String,
    catalogTitle: CatalogTitle?,
    vaultEntry: VaultEntry?,
    isMutating: Boolean,
    onBack: () -> Unit,
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
        containsSpoilers: Boolean
    ) -> Unit,
    onDeleteEntry: () -> Unit
) {
    val displayTitle = vaultEntry?.title ?: catalogTitle?.title ?: "Untitled"
    val displayYear = vaultEntry?.releaseYear?.toInt() ?: catalogTitle?.releaseYear ?: 2024
    val displayMediaType = vaultEntry?.mediaTypeEnum ?: catalogTitle?.mediaType ?: MediaType.MOVIE
    val displayDirector = vaultEntry?.directorOrCreator?.ifBlank { catalogTitle?.directorOrCreator }
        ?: catalogTitle?.directorOrCreator ?: "Unknown"
    val displayRuntime = vaultEntry?.runtimeOrSeasons?.ifBlank { catalogTitle?.runtimeOrSeasons }
        ?: catalogTitle?.runtimeOrSeasons ?: ""
    val displaySynopsis = vaultEntry?.synopsis?.ifBlank { catalogTitle?.synopsis }
        ?: catalogTitle?.synopsis ?: ""
    val displayGenres = vaultEntry?.genres?.ifEmpty { catalogTitle?.genres.orEmpty() }
        ?: catalogTitle?.genres.orEmpty()
    val displayPosterKey = vaultEntry?.posterKey ?: catalogTitle?.posterKey ?: "cinema"
    val totalEpisodes = vaultEntry?.totalEpisodes?.toInt() ?: catalogTitle?.totalEpisodes ?: 0

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Editable review & rating state synced to current vault entry
    var draftRating by remember(vaultEntry?.id, vaultEntry?.userRating) {
        mutableDoubleStateOf(vaultEntry?.userRating ?: 0.0)
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
            // Full-bleed backdrop + overlapping poster
            item(key = "detail_hero") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = posterDrawableForKey(displayPosterKey)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
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
                                    0.0f to CinemaObsidian.copy(alpha = 0.45f),
                                    0.55f to CinemaObsidian.copy(alpha = 0.72f),
                                    1.0f to CinemaObsidian
                                )
                            )
                    )

                    // Top Navigation Bar over backdrop
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

                    // Poster + Title Lockup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 165.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CinemaPosterArtwork(
                            posterKey = displayPosterKey,
                            title = displayTitle,
                            mediaType = displayMediaType,
                            userRating = vaultEntry?.userRating ?: 0.0,
                            status = vaultEntry?.statusEnum,
                            modifier = Modifier
                                .width(114.dp)
                                .height(164.dp)
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
                                if (catalogTitle != null) {
                                    RatingPill(score = catalogTitle.criticScore, labelPrefix = "CRITIC")
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
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWatchlist) "In Watchlist" else "Want to Watch",
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
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWatched) "Watched" else "Mark Watched",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Priority / Series Episode Tracker / Rewatch Controls
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
                                            Icon(
                                                imageVector = Icons.Rounded.PushPin,
                                                contentDescription = null,
                                                tint = CinemaGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Pin as Up Next",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = CinemaAlabaster
                                                )
                                                Text(
                                                    text = "Prioritize at the top of your watchlist",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = CinemaSilver
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = vaultEntry.priority,
                                            onCheckedChange = { onTogglePriority() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = CinemaObsidian,
                                                checkedTrackColor = CinemaGold
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
                                            Text(
                                                text = "Episode Progress",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = CinemaAlabaster
                                            )
                                            Text(
                                                text = "$watchedEp / $totalEpisodes EP",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = CinemaGold
                                            )
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
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = CinemaGold,
                                                    contentColor = CinemaObsidian
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
                                            Icon(
                                                imageVector = Icons.Rounded.Repeat,
                                                contentDescription = null,
                                                tint = CinemaEmerald,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Rewatches",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = CinemaAlabaster
                                                )
                                                Text(
                                                    text = "Logged ${vaultEntry.rewatchCount} rewatch(es)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = CinemaSilver
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onUpdateRewatch((vaultEntry.rewatchCount - 1).coerceAtLeast(0)) },
                                                enabled = vaultEntry.rewatchCount > 0
                                            ) {
                                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease rewatch", tint = CinemaSilver)
                                            }
                                            Text(
                                                text = "${vaultEntry.rewatchCount}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = CinemaEmerald
                                            )
                                            IconButton(
                                                onClick = { onUpdateRewatch(vaultEntry.rewatchCount + 1) }
                                            ) {
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

            // Full Details, Info, Genres & Synopsis
            item(key = "detail_info") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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

                    // Synopsis
                    if (displaySynopsis.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Synopsis",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CinemaAlabaster
                            )
                            Text(
                                text = displaySynopsis,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CinemaSilver
                            )
                        }
                    }

                    // Awards Highlight
                    if (catalogTitle?.awardsHighlight?.isNotBlank() == true) {
                        Surface(
                            color = CinemaGold.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = catalogTitle.awardsHighlight,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CinemaGold
                                )
                            }
                        }
                    }

                    // Production & Technical Facts Card
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
                            Text(
                                text = "Production Details",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CinemaAlabaster
                            )
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
                                DetailFactRow("Studio / Network", catalogTitle.studioOrNetwork)
                                DetailFactRow("Country", catalogTitle.country)
                                DetailFactRow("Language", catalogTitle.language)
                            }
                        }
                    }

                    // Cast Section
                    if (catalogTitle != null && catalogTitle.cast.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Top Billed Cast",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CinemaAlabaster
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(catalogTitle.cast, key = { it.actor }) { member ->
                                    Surface(
                                        color = CinemaSlate,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CinemaBorderSubtle),
                                        modifier = Modifier.width(156.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(CinemaGold.copy(alpha = 0.16f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Person,
                                                    contentDescription = null,
                                                    tint = CinemaGold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Text(
                                                text = member.actor,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = CinemaAlabaster,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = member.role,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CinemaSilver,
                                                maxLines = 1,
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
                                Icon(
                                    imageVector = Icons.Rounded.RateReview,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your Rating & Review",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = CinemaAlabaster
                                )
                            }

                            Text(
                                text = if (draftRating > 0.0) {
                                    "${String.format(Locale.US, "%.1f", draftRating)} / 10"
                                } else {
                                    "Unrated"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Interactive 0.5-step 10-point score slider + quick star presets
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Slider(
                                value = draftRating.toFloat(),
                                onValueChange = { raw ->
                                    val snapped = (raw * 2f).roundToInt() / 2.0
                                    draftRating = snapped.coerceIn(0.0, 10.0)
                                },
                                valueRange = 0f..10f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = CinemaGold,
                                    activeTrackColor = CinemaGold,
                                    inactiveTrackColor = CinemaSlateElevated
                                ),
                                modifier = Modifier.testTag("detail_rating_slider")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(0.0, 5.0, 7.5, 8.5, 9.5, 10.0).forEach { preset ->
                                    val active = kotlin.math.abs(draftRating - preset) < 0.05
                                    Surface(
                                        color = if (active) CinemaGold else CinemaSlateElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { draftRating = preset }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (preset > 0.0) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Star,
                                                    contentDescription = null,
                                                    tint = if (active) CinemaObsidian else CinemaGold,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(Modifier.width(3.dp))
                                            }
                                            Text(
                                                text = if (preset == 0.0) "Clear" else String.format(Locale.US, "%.1f", preset),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (active) CinemaObsidian else CinemaSilver
                                            )
                                        }
                                    }
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
                                .testTag("review_headline_input")
                        )

                        OutlinedTextField(
                            value = draftReviewBody,
                            onValueChange = { if (it.length <= 4000) draftReviewBody = it },
                            label = { Text("Full Review & Thoughts") },
                            placeholder = { Text("Write your thoughts on pacing, cinematography, performances, finale…") },
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
                                Icon(
                                    imageVector = Icons.Rounded.FormatQuote,
                                    contentDescription = null,
                                    tint = CinemaGold
                                )
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
                                Text(
                                    text = "Mark as containing spoilers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CinemaAlabaster
                                )
                            }
                            Switch(
                                checked = draftSpoilers,
                                onCheckedChange = { draftSpoilers = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CinemaObsidian,
                                    checkedTrackColor = CinemaCrimson
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val targetStatus = vaultEntry?.statusEnum ?: WatchStatus.WATCHED
                                onSaveReview(
                                    targetStatus,
                                    draftRating,
                                    draftReviewTitle,
                                    draftReviewBody,
                                    draftQuote,
                                    draftSpoilers
                                )
                            },
                            enabled = !isMutating,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaGold,
                                contentColor = CinemaObsidian
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_review_button")
                        ) {
                            Text(
                                text = if (vaultEntry == null) "Save to Watched & Publish Review" else "Save Rating & Review",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
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
private fun DetailFactRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CinemaSilver
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = CinemaAlabaster,
            fontWeight = FontWeight.Medium
        )
    }
}
