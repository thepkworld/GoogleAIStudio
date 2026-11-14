package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType
import com.example.data.model.OttPlatform
import com.example.data.model.PersonSummary
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.HomeUiState
import com.example.ui.components.CinemaBackdropImage
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.RatingPill
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated

@Composable
fun HomeScreen(
    homeState: HomeUiState,
    keyConfigured: Boolean,
    errorMessage: String?,
    vaultById: Map<String, VaultEntry>,
    onRefreshTmdb: () -> Unit,
    onOttPlatformSelect: (OttPlatform) -> Unit,
    onOpenSearch: () -> Unit,
    onTitleClick: (String) -> Unit,
    onPersonClick: (Int) -> Unit,
    onQuickBucketList: (CatalogTitle) -> Unit,
    onMarkSeenClick: (CatalogTitle) -> Unit,
    modifier: Modifier = Modifier
) {
    val hero = homeState.featuredTitle

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (!keyConfigured || errorMessage != null) {
            item(key = "home_tmdb_notice") {
                Surface(
                    color = CinemaSlate,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorMessage
                                ?: "Curated cinema active. Add TMDB_API_KEY in Secrets for live Bollywood, Hollywood & OTT releases.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaSilver,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onRefreshTmdb,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Retry TMDB sync",
                                tint = CinemaGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Search Bar Shortcut
        item(key = "home_search_shortcut") {
            Surface(
                color = CinemaSlate,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CinemaBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenSearch)
                    .testTag("home_search_shortcut")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Search any movie, series, actor, or franchise…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaSilver.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (homeState.isLoading) {
            item(key = "home_loading") {
                LinearProgressIndicator(
                    color = CinemaGold,
                    trackColor = CinemaSlateElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        // Featured Hero Spotlight
        item(key = "home_hero") {
            HomeHeroCard(
                title = hero,
                isLiveTmdb = homeState.isLiveTmdb,
                vaultEntry = vaultById[hero.id],
                onTitleClick = { onTitleClick(hero.id) },
                onBucketClick = { onQuickBucketList(hero) },
                onMarkSeenClick = { onMarkSeenClick(hero) }
            )
        }

        // 1. Bollywood Movies Section
        if (homeState.bollywoodMovies.isNotEmpty()) {
            item(key = "section_bollywood") {
                HomeMovieRail(
                    title = "Bollywood Movies",
                    subtitle = "Popular Hindi cinema & Indian hits",
                    icon = Icons.Rounded.Whatshot,
                    accentColor = CinemaGold,
                    items = homeState.bollywoodMovies,
                    vaultById = vaultById,
                    keyPrefix = "bolly",
                    onTitleClick = onTitleClick,
                    onQuickBucketList = onQuickBucketList,
                    onMarkSeenClick = onMarkSeenClick
                )
            }
        }

        // 2. Hollywood Movies Section
        if (homeState.hollywoodMovies.isNotEmpty()) {
            item(key = "section_hollywood") {
                HomeMovieRail(
                    title = "Hollywood Movies",
                    subtitle = "Top US studio & blockbuster releases",
                    icon = Icons.Rounded.MovieFilter,
                    accentColor = CinemaGold,
                    items = homeState.hollywoodMovies,
                    vaultById = vaultById,
                    keyPrefix = "holly",
                    onTitleClick = onTitleClick,
                    onQuickBucketList = onQuickBucketList,
                    onMarkSeenClick = onMarkSeenClick
                )
            }
        }

        // 3. OTT Wise New Movies Section
        item(key = "section_ott_new") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeSectionHeader(
                    title = "OTT New Movies",
                    subtitle = "Fresh streaming drops by platform",
                    icon = Icons.Rounded.LiveTv,
                    accentColor = CinemaEmerald,
                    countLabel = "${homeState.ottNewMovies.size} FILMS"
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(OttPlatform.entries, key = { it.name }) { platform ->
                        val selected = homeState.selectedOttPlatform == platform
                        FilterChip(
                            selected = selected,
                            onClick = { onOttPlatformSelect(platform) },
                            label = {
                                Text(
                                    text = platform.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaEmerald,
                                selectedLabelColor = CinemaObsidian,
                                containerColor = CinemaSlate,
                                labelColor = CinemaSilver
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = CinemaBorderSubtle,
                                selectedBorderColor = CinemaEmerald
                            ),
                            modifier = Modifier.testTag("ott_chip_${platform.name.lowercase()}")
                        )
                    }
                }

                if (homeState.isLoadingOtt) {
                    LinearProgressIndicator(
                        color = CinemaEmerald,
                        trackColor = CinemaSlateElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                if (homeState.ottNewMovies.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(homeState.ottNewMovies, key = { "ott_${it.id}" }) { item ->
                            HomeMovieCard(
                                title = item,
                                vaultEntry = vaultById[item.id],
                                onClick = { onTitleClick(item.id) },
                                onBucketClick = { onQuickBucketList(item) },
                                onMarkSeenClick = { onMarkSeenClick(item) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Classic Movies Section
        if (homeState.classicMovies.isNotEmpty()) {
            item(key = "section_classic") {
                HomeMovieRail(
                    title = "Classic Movies",
                    subtitle = "Timeless pre-2000 masterworks",
                    icon = Icons.Rounded.HistoryEdu,
                    accentColor = CinemaGold,
                    items = homeState.classicMovies,
                    vaultById = vaultById,
                    keyPrefix = "classic",
                    onTitleClick = onTitleClick,
                    onQuickBucketList = onQuickBucketList,
                    onMarkSeenClick = onMarkSeenClick
                )
            }
        }

        // 5. Famous Movies Section
        if (homeState.famousMovies.isNotEmpty()) {
            item(key = "section_famous") {
                HomeMovieRail(
                    title = "Famous Movies",
                    subtitle = "World's most beloved & iconic cinema",
                    icon = Icons.Rounded.AutoAwesome,
                    accentColor = CinemaGold,
                    items = homeState.famousMovies,
                    vaultById = vaultById,
                    keyPrefix = "famous",
                    onTitleClick = onTitleClick,
                    onQuickBucketList = onQuickBucketList,
                    onMarkSeenClick = onMarkSeenClick
                )
            }
        }

        // 6. Trending Stars & Creators
        if (homeState.trendingPeople.isNotEmpty()) {
            item(key = "home_trending_people") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeSectionHeader(
                        title = "Trending Stars & Creators",
                        subtitle = "Tap to explore biography & filmography",
                        icon = Icons.Rounded.Public,
                        accentColor = CinemaGold,
                        countLabel = "${homeState.trendingPeople.size} PEOPLE"
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(homeState.trendingPeople, key = { "home_person_${it.id}" }) { person ->
                            HomePersonCard(
                                person = person,
                                onClick = { onPersonClick(person.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    countLabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = CinemaAlabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = countLabel,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HomeMovieRail(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    items: List<CatalogTitle>,
    vaultById: Map<String, VaultEntry>,
    keyPrefix: String,
    onTitleClick: (String) -> Unit,
    onQuickBucketList: (CatalogTitle) -> Unit,
    onMarkSeenClick: (CatalogTitle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HomeSectionHeader(
            title = title,
            subtitle = subtitle,
            icon = icon,
            accentColor = accentColor,
            countLabel = "${items.size} FILMS"
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { "${keyPrefix}_${it.id}" }) { item ->
                HomeMovieCard(
                    title = item,
                    vaultEntry = vaultById[item.id],
                    onClick = { onTitleClick(item.id) },
                    onBucketClick = { onQuickBucketList(item) },
                    onMarkSeenClick = { onMarkSeenClick(item) }
                )
            }
        }
    }
}

@Composable
private fun HomeMovieCard(
    title: CatalogTitle,
    vaultEntry: VaultEntry?,
    onClick: () -> Unit,
    onBucketClick: () -> Unit,
    onMarkSeenClick: () -> Unit
) {
    val inBucket = vaultEntry?.statusEnum == WatchStatus.WANT_TO_WATCH
    val isSeen = vaultEntry?.statusEnum == WatchStatus.WATCHED

    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = Modifier
            .width(156.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CinemaPosterArtwork(
                posterKey = title.posterKey,
                posterUrl = title.posterUrl,
                title = title.title,
                mediaType = title.mediaType,
                userRating = vaultEntry?.userRating?.takeIf { it > 0.0 } ?: title.criticScore,
                status = vaultEntry?.statusEnum,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.70f)
            )

            Text(
                text = title.title,
                style = MaterialTheme.typography.titleSmall,
                color = CinemaAlabaster,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${title.releaseYear} · ${title.genres.firstOrNull() ?: title.language}",
                style = MaterialTheme.typography.bodySmall,
                color = CinemaSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = if (inBucket) CinemaGold.copy(alpha = 0.18f) else CinemaSlateElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (inBucket) CinemaGold else CinemaBorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBucketClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (inBucket) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Add to BucketList",
                            tint = if (inBucket) CinemaGold else CinemaSilver,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    color = if (isSeen) CinemaEmerald.copy(alpha = 0.2f) else CinemaSlateElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSeen) CinemaEmerald else CinemaBorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onMarkSeenClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Mark Seen",
                            tint = if (isSeen) CinemaEmerald else CinemaSilver,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSeen) "Seen" else "Mark Seen",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSeen) CinemaEmerald else CinemaAlabaster,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    title: CatalogTitle,
    isLiveTmdb: Boolean,
    vaultEntry: VaultEntry?,
    onTitleClick: () -> Unit,
    onBucketClick: () -> Unit,
    onMarkSeenClick: () -> Unit
) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onTitleClick)
            .testTag("home_hero_card")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            CinemaBackdropImage(
                imageUrl = title.backdropUrl.ifBlank { title.posterUrl },
                fallbackKey = title.posterKey,
                contentDescription = "${title.title} backdrop",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            0.0f to CinemaObsidian.copy(alpha = 0.20f),
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
                    Surface(color = CinemaGold, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = if (isLiveTmdb) "TONIGHT'S SPOTLIGHT" else "FEATURED PREMIERE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaObsidian,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    RatingPill(score = title.criticScore)
                    Text(
                        text = "${title.releaseYear} · ${if (title.mediaType == MediaType.SERIES) "Series" else "Movie"}",
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onTitleClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaGold,
                            contentColor = CinemaObsidian
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Details", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    val inBucket = vaultEntry?.statusEnum == WatchStatus.WANT_TO_WATCH
                    OutlinedButton(
                        onClick = onBucketClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.65f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = if (inBucket) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (inBucket) "In BucketList" else "BucketList",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaAlabaster
                        )
                    }

                    val isSeen = vaultEntry?.statusEnum == WatchStatus.WATCHED
                    OutlinedButton(
                        onClick = onMarkSeenClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CinemaEmerald.copy(alpha = 0.7f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = CinemaEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSeen) "Seen" else "Seen",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomePersonCard(
    person: PersonSummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(92.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (person.profileUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(person.profileUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = person.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CinemaSlateElevated)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CinemaSlateElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = person.name,
            style = MaterialTheme.typography.labelLarge,
            color = CinemaAlabaster,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = person.knownForDepartment,
            style = MaterialTheme.typography.labelSmall,
            color = CinemaGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
