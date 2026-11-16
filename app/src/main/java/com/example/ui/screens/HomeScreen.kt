package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material.icons.rounded.Upcoming
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.HomeUiState
import com.example.ui.components.CinemaBackdropImage
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.RatingPill
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaIndigo
import com.example.ui.theme.CinemaTheme

private data class GenreCollectionCard(
    val title: String,
    val subtitle: String,
    val queryGenre: String,
    val gradientStart: Color,
    val gradientEnd: Color
)

private val genreCollections = listOf(
    GenreCollectionCard("Sci-Fi", "Futures & galaxies", "Science Fiction", CinemaIndigo, Color(0xFF0A0B0F)),
    GenreCollectionCard("Action", "High-octane thrills", "Action", CinemaCrimson, Color(0xFF0A0B0F)),
    GenreCollectionCard("Crime & Noir", "Heists & mysteries", "Crime", CinemaGold, Color(0xFF0A0B0F)),
    GenreCollectionCard("Drama", "Prestige stories", "Drama", CinemaEmerald, Color(0xFF0A0B0F)),
    GenreCollectionCard("Comedy", "Feel-good & sharp", "Comedy", Color(0xFF4CC9F0), Color(0xFF0A0B0F)),
    GenreCollectionCard("Thriller", "Edge-of-seat tension", "Thriller", Color(0xFFF77F00), Color(0xFF0A0B0F)),
    GenreCollectionCard("Animation", "Worlds imagined", "Animation", Color(0xFFB5179E), Color(0xFF0A0B0F)),
    GenreCollectionCard("Romance", "Heart & chemistry", "Romance", Color(0xFFE05780), Color(0xFF0A0B0F))
)

@Composable
fun HomeScreen(
    homeState: HomeUiState,
    recentlyAdded: List<CatalogTitle>,
    keyConfigured: Boolean,
    errorMessage: String?,
    vaultById: Map<String, VaultEntry>,
    onRefreshTmdb: () -> Unit,
    onOttPlatformSelect: (OttPlatform) -> Unit,
    onOpenSearch: () -> Unit,
    onGenreCollectionClick: (String) -> Unit,
    onViewAllClick: (sectionTitle: String, items: List<CatalogTitle>) -> Unit,
    onTitleClick: (String) -> Unit,
    onPersonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val hero = homeState.featuredTitle

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        if (!keyConfigured || errorMessage != null) {
            item(key = "home_tmdb_notice") {
                Surface(
                    color = colors.slate,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.35f)),
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
                            tint = colors.gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorMessage
                                ?: "Curated cinema active. Configure TMDB or OMDb keys for live feeds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.silver,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onRefreshTmdb,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Retry live sync",
                                tint = colors.gold,
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
                color = colors.slate,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.borderSubtle),
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
                        tint = colors.gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Search movies, series, stars, or franchises…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.silver.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Skeleton Loader when loading initial live feeds
        if (homeState.isLoading) {
            item(key = "home_skeleton_loader") {
                HomeSkeletonLoader()
            }
        }

        // Featured Hero Spotlight (click card to open detail view — no card action buttons)
        item(key = "home_hero") {
            HomeHeroCard(
                title = hero,
                isLiveTmdb = homeState.isLiveTmdb,
                onTitleClick = { onTitleClick(hero.id) }
            )
        }

        // 1. Trending Now
        if (homeState.trendingNow.isNotEmpty()) {
            item(key = "section_trending_now") {
                HomeMovieRail(
                    title = "Trending Now",
                    subtitle = "Global hits capturing audiences today",
                    icon = Icons.Rounded.LocalFireDepartment,
                    accentColor = colors.gold,
                    items = homeState.trendingNow,
                    vaultById = vaultById,
                    keyPrefix = "trending",
                    onViewAll = { onViewAllClick("Trending Now", homeState.trendingNow) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 2. Latest Bollywood Movies
        if (homeState.bollywoodMovies.isNotEmpty()) {
            item(key = "section_bollywood") {
                HomeMovieRail(
                    title = "Latest Bollywood Movies",
                    subtitle = "Fresh Hindi cinema & Indian releases",
                    icon = Icons.Rounded.Whatshot,
                    accentColor = colors.gold,
                    items = homeState.bollywoodMovies,
                    vaultById = vaultById,
                    keyPrefix = "bolly",
                    onViewAll = { onViewAllClick("Latest Bollywood Movies", homeState.bollywoodMovies) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 3. Latest Hollywood Movies
        if (homeState.hollywoodMovies.isNotEmpty()) {
            item(key = "section_hollywood") {
                HomeMovieRail(
                    title = "Latest Hollywood Movies",
                    subtitle = "Recent US studio & theatrical releases",
                    icon = Icons.Rounded.Theaters,
                    accentColor = colors.gold,
                    items = homeState.hollywoodMovies,
                    vaultById = vaultById,
                    keyPrefix = "holly",
                    onViewAll = { onViewAllClick("Latest Hollywood Movies", homeState.hollywoodMovies) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 4. Upcoming Movies
        if (homeState.upcomingMovies.isNotEmpty()) {
            item(key = "section_upcoming") {
                HomeMovieRail(
                    title = "Upcoming Movies",
                    subtitle = "Coming soon to theatres — save to Bucketlist",
                    icon = Icons.Rounded.Upcoming,
                    accentColor = colors.indigo,
                    items = homeState.upcomingMovies,
                    vaultById = vaultById,
                    keyPrefix = "upcoming",
                    showUpcomingTag = true,
                    onViewAll = { onViewAllClick("Upcoming Movies", homeState.upcomingMovies) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 5. Popular on Netflix
        if (homeState.netflixPopular.isNotEmpty()) {
            item(key = "section_netflix") {
                HomeMovieRail(
                    title = "Popular on Netflix",
                    subtitle = "Most-watched films streaming on Netflix",
                    icon = Icons.Rounded.PlayCircle,
                    accentColor = colors.crimson,
                    items = homeState.netflixPopular,
                    vaultById = vaultById,
                    keyPrefix = "netflix",
                    onViewAll = { onViewAllClick("Popular on Netflix", homeState.netflixPopular) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 6. Popular on Amazon Prime
        if (homeState.primePopular.isNotEmpty()) {
            item(key = "section_prime") {
                HomeMovieRail(
                    title = "Popular on Amazon Prime",
                    subtitle = "Top streaming picks on Prime Video",
                    icon = Icons.Rounded.LiveTv,
                    accentColor = Color(0xFF0096C7),
                    items = homeState.primePopular,
                    vaultById = vaultById,
                    keyPrefix = "prime",
                    onViewAll = { onViewAllClick("Popular on Amazon Prime", homeState.primePopular) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 7. Latest OTT Release Movies (with platform switcher + View All)
        item(key = "section_ott_new") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeSectionHeader(
                    title = "Latest OTT Release Movies",
                    subtitle = "Fresh digital premieres across platforms",
                    icon = Icons.Rounded.NewReleases,
                    accentColor = colors.emerald,
                    onViewAll = if (homeState.ottNewMovies.isNotEmpty()) {
                        {
                            onViewAllClick(
                                "Latest OTT (${homeState.selectedOttPlatform.label})",
                                homeState.ottNewMovies
                            )
                        }
                    } else null
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
                                selectedContainerColor = colors.emerald,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                containerColor = colors.slate,
                                labelColor = colors.silver
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = colors.borderSubtle,
                                selectedBorderColor = colors.emerald
                            ),
                            modifier = Modifier.testTag("ott_chip_${platform.name.lowercase()}")
                        )
                    }
                }

                if (homeState.isLoadingOtt) {
                    LinearProgressIndicator(
                        color = colors.emerald,
                        trackColor = colors.slateElevated,
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
                                onClick = { onTitleClick(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // 8. Top Rated
        if (homeState.topRatedMovies.isNotEmpty()) {
            item(key = "section_top_rated") {
                HomeMovieRail(
                    title = "Top Rated",
                    subtitle = "Critically acclaimed highest-rated cinema",
                    icon = Icons.Rounded.Star,
                    accentColor = colors.gold,
                    items = homeState.topRatedMovies,
                    vaultById = vaultById,
                    keyPrefix = "toprated",
                    onViewAll = { onViewAllClick("Top Rated", homeState.topRatedMovies) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 9. Most Anticipated
        if (homeState.mostAnticipatedMovies.isNotEmpty()) {
            item(key = "section_anticipated") {
                HomeMovieRail(
                    title = "Most Anticipated",
                    subtitle = "High-buzz future releases on everyone's radar",
                    icon = Icons.Rounded.AutoAwesome,
                    accentColor = colors.gold,
                    items = homeState.mostAnticipatedMovies,
                    vaultById = vaultById,
                    keyPrefix = "anticipated",
                    showUpcomingTag = true,
                    onViewAll = { onViewAllClick("Most Anticipated", homeState.mostAnticipatedMovies) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 10. Recently Added
        if (recentlyAdded.isNotEmpty()) {
            item(key = "section_recently_added") {
                HomeMovieRail(
                    title = "Recently Added",
                    subtitle = "Latest additions to your catalogue & spotlight",
                    icon = Icons.Rounded.BookmarkAdd,
                    accentColor = colors.emerald,
                    items = recentlyAdded,
                    vaultById = vaultById,
                    keyPrefix = "recent",
                    onViewAll = { onViewAllClick("Recently Added", recentlyAdded) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 11. Genre Collections
        item(key = "section_genre_collections") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeSectionHeader(
                    title = "Genre Collections",
                    subtitle = "Explore curated moods & cinematic genres",
                    icon = Icons.Rounded.Category,
                    accentColor = colors.gold,
                    onViewAll = onOpenSearch
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(genreCollections, key = { it.title }) { collection ->
                        GenreCollectionTile(
                            item = collection,
                            onClick = { onGenreCollectionClick(collection.queryGenre) }
                        )
                    }
                }
            }
        }

        // 12. Classic Cinema
        if (homeState.classicMovies.isNotEmpty()) {
            item(key = "section_classic") {
                HomeMovieRail(
                    title = "Classic Cinema",
                    subtitle = "Timeless pre-2000 masterworks",
                    icon = Icons.Rounded.HistoryEdu,
                    accentColor = colors.gold,
                    items = homeState.classicMovies,
                    vaultById = vaultById,
                    keyPrefix = "classic",
                    onViewAll = { onViewAllClick("Classic Cinema", homeState.classicMovies) },
                    onTitleClick = onTitleClick
                )
            }
        }

        // 13. Trending Stars & Creators
        if (homeState.trendingPeople.isNotEmpty()) {
            item(key = "home_trending_people") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeSectionHeader(
                        title = "Trending Stars & Creators",
                        subtitle = "Tap to explore biography & filmography",
                        icon = Icons.Rounded.Public,
                        accentColor = colors.gold
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
private fun HomeSkeletonLoader() {
    val colors = CinemaTheme.colors
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.slateElevated.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.slateElevated.copy(alpha = alpha))
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(142.dp)
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.slateElevated.copy(alpha = alpha))
                )
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
    onViewAll: (() -> Unit)? = null
) {
    val colors = CinemaTheme.colors
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
                    color = colors.alabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.silver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onViewAll != null) {
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "View all $title",
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
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
    showUpcomingTag: Boolean = false,
    onViewAll: () -> Unit,
    onTitleClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HomeSectionHeader(
            title = title,
            subtitle = subtitle,
            icon = icon,
            accentColor = accentColor,
            onViewAll = onViewAll
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { "${keyPrefix}_${it.id}" }) { item ->
                HomeMovieCard(
                    title = item,
                    vaultEntry = vaultById[item.id],
                    showUpcomingTag = showUpcomingTag || !item.isReleased,
                    onClick = { onTitleClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun HomeMovieCard(
    title: CatalogTitle,
    vaultEntry: VaultEntry?,
    showUpcomingTag: Boolean = false,
    onClick: () -> Unit
) {
    val colors = CinemaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "card_press_scale"
    )

    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (pressed) colors.gold.copy(alpha = 0.65f) else colors.borderSubtle
        ),
        modifier = Modifier
            .width(152.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("home_card_${title.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box {
                CinemaPosterArtwork(
                    posterKey = title.posterKey,
                    posterUrl = title.posterUrl,
                    title = title.title,
                    mediaType = title.mediaType,
                    userRating = vaultEntry?.userRating?.takeIf { it > 0.0 } ?: title.criticScore,
                    status = vaultEntry?.statusEnum,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.68f)
                )
                if (showUpcomingTag) {
                    Surface(
                        color = CinemaIndigo.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = colors.onScrimLight,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SOON",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onScrimLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = title.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.alabaster,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    if (title.releaseDate.isNotBlank() && showUpcomingTag) {
                        append(title.releaseDate)
                    } else {
                        append(title.releaseYear)
                    }
                    val sub = title.genres.firstOrNull() ?: title.language
                    if (sub.isNotBlank()) {
                        append(" · ")
                        append(sub)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.silver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GenreCollectionTile(
    item: GenreCollectionCard,
    onClick: () -> Unit
) {
    val colors = CinemaTheme.colors
    Surface(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, item.gradientStart.copy(alpha = 0.55f)),
        modifier = Modifier
            .width(154.dp)
            .height(94.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("genre_collection_${item.title.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            item.gradientStart.copy(alpha = 0.65f),
                            item.gradientEnd
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onScrimLight,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onScrimMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = item.gradientStart,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeHeroCard(
    title: CatalogTitle,
    isLiveTmdb: Boolean,
    onTitleClick: () -> Unit
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.30f)),
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
                    .height(250.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        Brush.verticalGradient(
                            0.0f to colors.scrimDark.copy(alpha = 0.16f),
                            0.45f to colors.scrimDark.copy(alpha = 0.68f),
                            1.0f to colors.scrimDark.copy(alpha = 0.96f)
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
                            color = colors.scrimDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    RatingPill(score = title.criticScore, accentColor = CinemaGold)
                    Text(
                        text = "${title.releaseYear} · ${if (title.mediaType == MediaType.SERIES) "Series" else "Movie"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onScrimMuted
                    )
                }

                Text(
                    text = title.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.onScrimLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = title.synopsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onScrimMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MovieFilter,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tap to explore cast, trailers & actions",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaGold,
                        fontWeight = FontWeight.SemiBold
                    )
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
    val colors = CinemaTheme.colors
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
                    .background(colors.slateElevated)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.slateElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = person.name,
            style = MaterialTheme.typography.labelLarge,
            color = colors.alabaster,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = person.knownForDepartment,
            style = MaterialTheme.typography.labelSmall,
            color = colors.gold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
