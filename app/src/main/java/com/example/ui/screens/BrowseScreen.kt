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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.CollectionSummary
import com.example.data.model.DiscoverSort
import com.example.data.model.MediaType
import com.example.data.model.PersonSummary
import com.example.data.model.SearchScope
import com.example.data.model.TmdbSection
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.BrowseUiState
import com.example.ui.MediaFilter
import com.example.ui.components.CinemaBackdropImage
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
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
fun BrowseScreen(
    browseState: BrowseUiState,
    localGenres: List<String>,
    searchQuery: String,
    selectedMediaFilter: MediaFilter,
    vaultById: Map<String, VaultEntry>,
    onSearchQueryChange: (String) -> Unit,
    onMediaFilterChange: (MediaFilter) -> Unit,
    onSectionSelect: (TmdbSection) -> Unit,
    onGenreSelect: (Int?, String) -> Unit,
    onDiscoverSortSelect: (DiscoverSort) -> Unit,
    onSearchScopeSelect: (SearchScope) -> Unit,
    onLoadMore: () -> Unit,
    onRefreshTmdb: () -> Unit,
    onTitleClick: (String) -> Unit,
    onPersonClick: (Int) -> Unit,
    onCollectionClick: (Int) -> Unit,
    onQuickWatchlist: (CatalogTitle) -> Unit,
    onQuickWatched: (CatalogTitle) -> Unit,
    onAddCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredTitle = browseState.featuredTitle
    val catalogTitles = browseState.sectionTitles
    val acclaimedTitles = browseState.acclaimedTitles

    val genreChips: List<Pair<Int?, String>> = if (browseState.isLiveTmdb) {
        val raw = when (selectedMediaFilter) {
            MediaFilter.SERIES -> browseState.tvGenres
            MediaFilter.MOVIES -> browseState.movieGenres
            MediaFilter.ALL -> (browseState.movieGenres + browseState.tvGenres).distinctBy { it.name }
        }
        listOf(null to "All") + raw.map { it.id to it.name }
    } else {
        localGenres.map { null to it }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!browseState.keyConfigured || browseState.errorMessage != null) {
            item(key = "tmdb_notice") {
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
                            text = browseState.errorMessage
                                ?: "Curated catalog active. Add TMDB_API_KEY in AI Studio Secrets for live TMDB discovery.",
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

        // Search & Filter Controls (Top-first for dedicated Search tab)
        item(key = "search_controls") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = if (browseState.isLiveTmdb) {
                                "Search movies, series, actors, collections…"
                            } else {
                                "Search movies, series, directors, cast…"
                            },
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

                if (searchQuery.isNotBlank() && browseState.isLiveTmdb) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SearchScope.entries, key = { it.name }) { scope ->
                            val selected = browseState.searchScope == scope
                            FilterChip(
                                selected = selected,
                                onClick = { onSearchScopeSelect(scope) },
                                label = { Text(scope.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaGold,
                                    selectedLabelColor = CinemaObsidian,
                                    containerColor = CinemaSlate,
                                    labelColor = CinemaSilver
                                )
                            )
                        }
                    }
                } else {
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
        }

        // TMDB Discovery Sections Carousel
        if (searchQuery.isBlank()) {
            item(key = "tmdb_sections") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TmdbSection.entries, key = { it.name }) { section ->
                        val selected = browseState.selectedSection == section &&
                            browseState.selectedGenreId == null &&
                            browseState.discoverSort == DiscoverSort.POPULARITY
                        Surface(
                            color = if (selected) CinemaGold else CinemaSlate,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (selected) CinemaGold else CinemaBorderSubtle
                            ),
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSectionSelect(section) }
                                .testTag("tmdb_section_${section.name.lowercase()}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = section.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) CinemaObsidian else CinemaAlabaster
                                )
                            }
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
                    items(genreChips, key = { "${it.first}_${it.second}" }) { (genreId, genreName) ->
                        val selected = browseState.selectedGenreLabel == genreName
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
                                .clickable { onGenreSelect(genreId, genreName) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = genreName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) CinemaGold else CinemaSilver
                                )
                            }
                        }
                    }
                }
            }

            // Discover Sort Bar (active when TMDB Live)
            if (browseState.isLiveTmdb) {
                item(key = "discover_sort_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DiscoverSort.entries, key = { it.name }) { sort ->
                            val selected = browseState.discoverSort == sort
                            Surface(
                                color = if (selected) CinemaEmerald.copy(alpha = 0.16f) else CinemaSlate,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) CinemaEmerald else CinemaBorderSubtle
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onDiscoverSortSelect(sort) }
                            ) {
                                Text(
                                    text = "Sort: ${sort.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) CinemaEmerald else CinemaSilver,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (browseState.isLoading) {
            item(key = "loading_indicator") {
                LinearProgressIndicator(
                    color = CinemaGold,
                    trackColor = CinemaSlateElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        // Trending Stars & Filmmakers Carousel
        if (searchQuery.isBlank() &&
            browseState.selectedGenreLabel == "All" &&
            browseState.trendingPeople.isNotEmpty()
        ) {
            item(key = "trending_people_section") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trending Stars & Creators",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CinemaAlabaster
                        )
                        Text(
                            text = "${browseState.trendingPeople.size} PEOPLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGold
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(browseState.trendingPeople, key = { "person_${it.id}" }) { person ->
                            TrendingPersonCard(
                                person = person,
                                onClick = { onPersonClick(person.id) }
                            )
                        }
                    }
                }
            }
        }

        // Critically Acclaimed Spotlight Carousel
        if (searchQuery.isBlank() &&
            browseState.selectedGenreLabel == "All" &&
            acclaimedTitles.isNotEmpty()
        ) {
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
                            text = if (browseState.isLiveTmdb) "Critically Acclaimed on TMDB" else "Critically Acclaimed",
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
                            SpotlightPosterCard(
                                title = item,
                                vaultEntry = vaultById[item.id],
                                onClick = { onTitleClick(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // Popular Series Carousel
        if (searchQuery.isBlank() &&
            browseState.selectedGenreLabel == "All" &&
            browseState.trendingTitles.isNotEmpty()
        ) {
            item(key = "series_spotlight_section") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Series Spotlight",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CinemaAlabaster
                        )
                        Text(
                            text = "${browseState.trendingTitles.size} SHOWS",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaEmerald
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(browseState.trendingTitles, key = { "series_spot_${it.id}" }) { item ->
                            SpotlightPosterCard(
                                title = item,
                                vaultEntry = vaultById[item.id],
                                onClick = { onTitleClick(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // Search Results: People
        if (searchQuery.isNotBlank() && browseState.searchScope == SearchScope.PEOPLE) {
            if (browseState.searchPeople.isEmpty() && !browseState.isLoading) {
                item(key = "empty_people") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        EmptyVaultState(
                            icon = Icons.Rounded.Person,
                            title = "No matching people",
                            subtitle = "Try another actor, director, or writer name."
                        )
                    }
                }
            } else {
                items(browseState.searchPeople, key = { "search_person_${it.id}" }) { person ->
                    PersonSearchCard(
                        person = person,
                        onClick = { onPersonClick(person.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else if (searchQuery.isNotBlank() && browseState.searchScope == SearchScope.COLLECTIONS) {
            if (browseState.searchCollections.isEmpty() && !browseState.isLoading) {
                item(key = "empty_collections") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        EmptyVaultState(
                            icon = Icons.Rounded.CollectionsBookmark,
                            title = "No matching collections",
                            subtitle = "Try searching franchises like Dune, Spider-Man, Avatar, or Lord of the Rings."
                        )
                    }
                }
            } else {
                items(browseState.searchCollections, key = { "search_col_${it.id}" }) { collection ->
                    CollectionSearchCard(
                        collection = collection,
                        onClick = { onCollectionClick(collection.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            // Full Explore / Movie & Series Search List Header
            item(key = "catalog_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heading = when {
                        searchQuery.isNotBlank() -> "Results for \"${searchQuery.trim()}\""
                        browseState.selectedGenreLabel != "All" -> "${browseState.selectedGenreLabel} Discover"
                        browseState.isLiveTmdb -> browseState.selectedSection.label
                        else -> "Explore Library"
                    }
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.headlineMedium,
                        color = CinemaAlabaster,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${catalogTitles.size} TITLES",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaSilver
                    )
                }
            }

            if (catalogTitles.isEmpty() && !browseState.isLoading) {
                item(key = "empty_browse") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        EmptyVaultState(
                            icon = Icons.Rounded.MovieFilter,
                            title = "No matching titles",
                            subtitle = "Try another search query, switch TMDB categories, or add any movie or series manually.",
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

        if (browseState.canLoadMore || browseState.isLoadingMore) {
            item(key = "load_more_button") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (browseState.isLoadingMore) {
                        CircularProgressIndicator(
                            color = CinemaGold,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        OutlinedButton(
                            onClick = onLoadMore,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Load More (Page ${browseState.currentPage} of ${browseState.totalPages})",
                                style = MaterialTheme.typography.labelLarge,
                                color = CinemaGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingPersonCard(
    person: PersonSummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
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
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(CinemaSlateElevated)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(CinemaSlateElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(30.dp)
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

@Composable
private fun PersonSearchCard(
    person: PersonSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CinemaBorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CinemaSlateElevated)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CinemaSlateElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = CinemaGold
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaAlabaster
                )
                Text(
                    text = person.knownForDepartment,
                    style = MaterialTheme.typography.labelMedium,
                    color = CinemaGold
                )
                if (person.knownForTitles.isNotBlank()) {
                    Text(
                        text = "Known for: ${person.knownForTitles}",
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

@Composable
private fun CollectionSearchCard(
    collection: CollectionSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaSlate,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaPosterArtwork(
                posterKey = "cinema",
                posterUrl = collection.posterUrl,
                title = collection.name,
                mediaType = MediaType.MOVIE,
                showBottomBadge = false,
                modifier = Modifier
                    .width(70.dp)
                    .aspectRatio(0.70f)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "FRANCHISE COLLECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold
                )
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemaAlabaster
                )
                Text(
                    text = "Tap to explore all films in this collection",
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaSilver
                )
            }
        }
    }
}

@Composable
private fun FeaturedCinemaHero(
    title: CatalogTitle,
    isLiveTmdb: Boolean,
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
            CinemaBackdropImage(
                imageUrl = title.backdropUrl.ifBlank { title.posterUrl },
                fallbackKey = title.posterKey,
                contentDescription = "${title.title} backdrop",
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
                            0.0f to CinemaObsidian.copy(alpha = 0.22f),
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
                            text = if (isLiveTmdb) "TMDB SPOTLIGHT" else "FEATURED PREMIERE",
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
            posterUrl = title.posterUrl,
            title = title.title,
            mediaType = title.mediaType,
            userRating = vaultEntry?.userRating?.takeIf { it > 0 } ?: title.criticScore,
            status = vaultEntry?.statusEnum,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
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
                posterUrl = item.posterUrl,
                title = item.title,
                mediaType = item.mediaType,
                showBottomBadge = false,
                status = vaultEntry?.statusEnum,
                modifier = Modifier
                    .width(84.dp)
                    .height(122.dp)
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
                        text = buildString {
                            append(item.releaseYear)
                            if (item.ageRating.isNotBlank()) {
                                append(" · ")
                                append(item.ageRating)
                            } else if (item.voteCount > 0) {
                                append(" · ")
                                append("${item.voteCount} votes")
                            }
                        },
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
                    text = listOf(item.directorOrCreator, item.runtimeOrSeasons)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
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
                                text = if (isWatchlist) "In Bucket" else "BucketList",
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
