package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogTitle
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.MediaFilter
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.RatingPill
import com.example.ui.components.posterDrawableForKey
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated

@Composable
fun BrowseScreen(
    featuredTitle: CatalogTitle,
    catalogTitles: List<CatalogTitle>,
    allGenres: List<String>,
    searchQuery: String,
    selectedMediaFilter: MediaFilter,
    selectedGenre: String,
    vaultById: Map<String, VaultEntry>,
    onSearchQueryChange: (String) -> Unit,
    onMediaFilterChange: (MediaFilter) -> Unit,
    onGenreChange: (String) -> Unit,
    onTitleClick: (String) -> Unit,
    onQuickWatchlist: (CatalogTitle) -> Unit,
    onQuickWatched: (CatalogTitle) -> Unit,
    onAddCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val acclaimedTitles = catalogTitles.filter { it.criticScore >= 8.8 }.take(6)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Featured Hero Banner
        item(key = "hero_banner") {
            FeaturedCinemaHero(
                title = featuredTitle,
                vaultEntry = vaultById[featuredTitle.id],
                onTitleClick = { onTitleClick(featuredTitle.id) },
                onQuickWatchlist = { onQuickWatchlist(featuredTitle) }
            )
        }

        // Search & Add Custom CTA Row
        item(key = "search_controls") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search movies, series, directors, cast…",
                            color = CinemaSilver.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = CinemaGold
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear search",
                                    tint = CinemaSilver
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = CinemaBorderSubtle,
                        focusedContainerColor = CinemaSlate,
                        unfocusedContainerColor = CinemaSlate,
                        focusedTextColor = CinemaAlabaster,
                        unfocusedTextColor = CinemaAlabaster,
                        cursorColor = CinemaGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("browse_search_input")
                )

                // Media Type Filter Row + Custom Title Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MediaFilter.entries.forEach { filter ->
                            val selected = selectedMediaFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { onMediaFilterChange(filter) },
                                label = {
                                    Text(
                                        text = filter.label,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaGold,
                                    selectedLabelColor = CinemaObsidian,
                                    containerColor = CinemaSlate,
                                    labelColor = CinemaSilver
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = CinemaBorderSubtle,
                                    selectedBorderColor = CinemaGold
                                ),
                                modifier = Modifier.testTag("browse_filter_${filter.name.lowercase()}")
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onAddCustomClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.55f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("add_custom_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaGold
                        )
                    }
                }
            }
        }

        // Genre Carousel
        item(key = "genre_chips") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allGenres, key = { it }) { genre ->
                    val selected = selectedGenre == genre
                    Surface(
                        color = if (selected) CinemaGold.copy(alpha = 0.18f) else CinemaSlate,
                        shape = CircleShape,
                        border = BorderStroke(
                            1.dp,
                            if (selected) CinemaGold else CinemaBorderSubtle
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .clickable { onGenreChange(genre) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) CinemaGold else CinemaSilver
                            )
                        }
                    }
                }
            }
        }

        // Critically Acclaimed Spotlight Carousel (when not searching)
        if (searchQuery.isBlank() && selectedGenre == "All" && acclaimedTitles.isNotEmpty()) {
            item(key = "acclaimed_section") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Critically Acclaimed",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CinemaAlabaster
                        )
                        Text(
                            text = "${acclaimedTitles.size} PICKS",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGold
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(acclaimedTitles, key = { "spotlight_${it.id}" }) { item ->
                            val entry = vaultById[item.id]
                            SpotlightPosterCard(
                                title = item,
                                vaultEntry = entry,
                                onClick = { onTitleClick(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // Full Catalog List Header
        item(key = "catalog_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedGenre == "All") "Explore Library" else "$selectedGenre Titles",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CinemaAlabaster
                )
                Text(
                    text = "${catalogTitles.size} TITLES",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinemaSilver
                )
            }
        }

        if (catalogTitles.isEmpty()) {
            item(key = "empty_browse") {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyVaultState(
                        icon = Icons.Rounded.MovieFilter,
                        title = "No matching titles",
                        subtitle = "Try clearing your search or add any movie or series manually to your Vault.",
                        actionLabel = "Add Custom Title",
                        onActionClick = onAddCustomClick
                    )
                }
            }
        } else {
            items(catalogTitles, key = { it.id }) { item ->
                val entry = vaultById[item.id]
                CatalogListCard(
                    item = item,
                    vaultEntry = entry,
                    onClick = { onTitleClick(item.id) },
                    onQuickWatchlist = { onQuickWatchlist(item) },
                    onQuickWatched = { onQuickWatched(item) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturedCinemaHero(
    title: CatalogTitle,
    vaultEntry: VaultEntry?,
    onTitleClick: () -> Unit,
    onQuickWatchlist: () -> Unit
) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onTitleClick)
            .testTag("featured_hero_card")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = posterDrawableForKey(title.posterKey)),
                contentDescription = "${title.title} backdrop",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .background(
                        Brush.verticalGradient(
                            0.0f to CinemaObsidian.copy(alpha = 0.25f),
                            0.45f to CinemaObsidian.copy(alpha = 0.72f),
                            1.0f to CinemaObsidian.copy(alpha = 0.96f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CinemaGold,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FEATURED PREMIERE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaObsidian,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    RatingPill(score = title.criticScore)
                    Text(
                        text = "${title.releaseYear} · ${title.runtimeOrSeasons}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaSilver
                    )
                }

                Text(
                    text = title.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = CinemaAlabaster,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = title.synopsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaSilver,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onTitleClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaGold,
                            contentColor = CinemaObsidian
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "View Details",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    val inWatchlist = vaultEntry?.statusEnum == WatchStatus.WANT_TO_WATCH
                    val isWatched = vaultEntry?.statusEnum == WatchStatus.WATCHED
                    OutlinedButton(
                        onClick = onQuickWatchlist,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isWatched) CinemaEmerald else CinemaGold.copy(alpha = 0.65f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isWatched -> Icons.Rounded.CheckCircle
                                inWatchlist -> Icons.Rounded.Bookmark
                                else -> Icons.Rounded.BookmarkBorder
                            },
                            contentDescription = null,
                            tint = if (isWatched) CinemaEmerald else CinemaGold,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isWatched -> "Watched"
                                inWatchlist -> "In Watchlist"
                                else -> "Want to Watch"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isWatched) CinemaEmerald else CinemaAlabaster
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotlightPosterCard(
    title: CatalogTitle,
    vaultEntry: VaultEntry?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(146.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        CinemaPosterArtwork(
            posterKey = title.posterKey,
            title = title.title,
            mediaType = title.mediaType,
            userRating = vaultEntry?.userRating?.takeIf { it > 0 } ?: title.criticScore,
            status = vaultEntry?.statusEnum,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title.title,
            style = MaterialTheme.typography.titleSmall,
            color = CinemaAlabaster,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${title.releaseYear} · ${title.genres.firstOrNull().orEmpty()}",
            style = MaterialTheme.typography.bodySmall,
            color = CinemaSilver,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CatalogListCard(
    item: CatalogTitle,
    vaultEntry: VaultEntry?,
    onClick: () -> Unit,
    onQuickWatchlist: () -> Unit,
    onQuickWatched: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("catalog_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaPosterArtwork(
                posterKey = item.posterKey,
                title = item.title,
                mediaType = item.mediaType,
                showBottomBadge = false,
                status = vaultEntry?.statusEnum,
                modifier = Modifier
                    .width(84.dp)
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
                    RatingPill(score = item.criticScore)
                    Text(
                        text = "${item.releaseYear} · ${item.ageRating}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaSilver
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaAlabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${item.directorOrCreator} · ${item.runtimeOrSeasons}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.genres.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val isWatchlist = vaultEntry?.statusEnum == WatchStatus.WANT_TO_WATCH
                    val isWatched = vaultEntry?.statusEnum == WatchStatus.WATCHED

                    Surface(
                        color = if (isWatchlist) CinemaGold.copy(alpha = 0.2f) else CinemaSlateElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isWatchlist) CinemaGold else CinemaBorderSubtle
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onQuickWatchlist)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isWatchlist) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Add to Want to Watch",
                                tint = if (isWatchlist) CinemaGold else CinemaSilver,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isWatchlist) "Queued" else "Watchlist",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isWatchlist) CinemaGold else CinemaAlabaster
                            )
                        }
                    }

                    Surface(
                        color = if (isWatched) CinemaEmerald.copy(alpha = 0.2f) else CinemaSlateElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isWatched) CinemaEmerald else CinemaBorderSubtle
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onQuickWatched)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Mark Watched",
                                tint = if (isWatched) CinemaEmerald else CinemaSilver,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isWatched) "Watched" else "Mark Seen",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isWatched) CinemaEmerald else CinemaAlabaster
                            )
                        }
                    }
                }
            }
        }
    }
}
