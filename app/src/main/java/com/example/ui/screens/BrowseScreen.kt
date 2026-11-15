package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CatalogTitle
import com.example.data.model.CatalogViewMode
import com.example.data.model.CollectionSummary
import com.example.data.model.DiscoverSort
import com.example.data.model.MediaType
import com.example.data.model.PersonSummary
import com.example.data.model.SearchScope
import com.example.data.model.VaultEntry
import com.example.ui.BrowseUiState
import com.example.ui.MediaFilter
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.RatingPill
import com.example.ui.theme.CinemaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrowseScreen(
    browseState: BrowseUiState,
    localGenres: List<String>,
    searchQuery: String,
    selectedMediaFilter: MediaFilter,
    vaultById: Map<String, VaultEntry>,
    retapTrigger: Int,
    filterMenuTrigger: Int,
    onSearchQueryChange: (String) -> Unit,
    onMediaFilterChange: (MediaFilter) -> Unit,
    onGenreToggle: (Int?, String) -> Unit,
    onClearGenres: () -> Unit,
    onDiscoverSortSelect: (DiscoverSort) -> Unit,
    onSearchScopeSelect: (SearchScope) -> Unit,
    onViewModeChange: (CatalogViewMode) -> Unit,
    onLoadMore: () -> Unit,
    onRefreshTmdb: () -> Unit,
    onTitleClick: (String) -> Unit,
    onPersonClick: (Int) -> Unit,
    onCollectionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val catalogTitles = browseState.sectionTitles
    val listState = rememberLazyListState()
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showGenreDropdown by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(retapTrigger) {
        if (retapTrigger > 0) {
            listState.animateScrollToItem(0)
            runCatching { searchFocusRequester.requestFocus() }
            keyboardController?.show()
        }
    }

    LaunchedEffect(filterMenuTrigger) {
        if (filterMenuTrigger > 0) {
            showFilterSheet = true
        }
    }

    val availableGenres: List<Pair<Int?, String>> = remember(
        browseState.isLiveTmdb,
        selectedMediaFilter,
        browseState.movieGenres,
        browseState.tvGenres,
        localGenres
    ) {
        if (browseState.isLiveTmdb) {
            val raw = when (selectedMediaFilter) {
                MediaFilter.SERIES -> browseState.tvGenres
                MediaFilter.MOVIES -> browseState.movieGenres
                MediaFilter.ALL -> (browseState.movieGenres + browseState.tvGenres).distinctBy { it.name }
            }
            raw.sortedBy { it.name }.map { it.id to it.name }
        } else {
            localGenres.filterNot { it.equals("All", ignoreCase = true) }.map { null to it }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gridColumns = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!browseState.keyConfigured || browseState.errorMessage != null) {
                item(key = "tmdb_notice") {
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
                                text = browseState.errorMessage
                                    ?: "Curated catalog active. Add TMDB_API_KEY in AI Studio Secrets for live TMDB discovery.",
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
                                    contentDescription = "Retry TMDB sync",
                                    tint = colors.gold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar + Structured Filter Controls (Genre Dropdown, Sort Dropdown, View Toggle)
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
                                color = colors.silver.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = colors.gold
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Clear search",
                                        tint = colors.silver
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.gold,
                            unfocusedBorderColor = colors.borderSubtle,
                            focusedContainerColor = colors.slate,
                            unfocusedContainerColor = colors.slate,
                            focusedTextColor = colors.alabaster,
                            unfocusedTextColor = colors.alabaster,
                            cursorColor = colors.gold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester)
                            .testTag("browse_search_input")
                    )

                    // Structured Filter Bar directly beneath Search Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Multi-select Genre Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            val hasSelectedGenres = browseState.selectedGenres.isNotEmpty()
                            val genreLabel = when (browseState.selectedGenres.size) {
                                0 -> "Genre"
                                1 -> browseState.selectedGenres.first()
                                else -> "Genre (${browseState.selectedGenres.size})"
                            }
                            DropdownTriggerButton(
                                label = genreLabel,
                                icon = Icons.Rounded.Category,
                                isActive = hasSelectedGenres,
                                isExpanded = showGenreDropdown,
                                onClick = { showGenreDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_genre_dropdown")
                            )

                            DropdownMenu(
                                expanded = showGenreDropdown,
                                onDismissRequest = { showGenreDropdown = false },
                                modifier = Modifier
                                    .width(240.dp)
                                    .heightIn(max = 360.dp)
                                    .background(colors.slateElevated)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "All Genres",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (browseState.selectedGenres.isEmpty()) colors.gold else colors.alabaster,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (browseState.selectedGenres.isNotEmpty()) {
                                                Text(
                                                    text = "Clear",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.gold
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onClearGenres()
                                        showGenreDropdown = false
                                    }
                                )
                                HorizontalDivider(color = colors.borderSubtle)
                                availableGenres.forEach { (genreId, genreName) ->
                                    val checked = browseState.selectedGenres.any {
                                        it.equals(genreName, ignoreCase = true)
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Checkbox(
                                                    checked = checked,
                                                    onCheckedChange = null,
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = colors.gold,
                                                        uncheckedColor = colors.silver,
                                                        checkmarkColor = colors.onGold
                                                    )
                                                )
                                                Text(
                                                    text = genreName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (checked) colors.gold else colors.alabaster,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        },
                                        onClick = { onGenreToggle(genreId, genreName) }
                                    )
                                }
                            }
                        }

                        // Consolidated Sort + Type Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            val sortLabel = if (selectedMediaFilter == MediaFilter.ALL) {
                                browseState.discoverSort.label
                            } else {
                                "${selectedMediaFilter.label} · ${browseState.discoverSort.label}"
                            }
                            val isSortNonDefault = browseState.discoverSort != DiscoverSort.POPULARITY ||
                                selectedMediaFilter != MediaFilter.ALL

                            DropdownTriggerButton(
                                label = sortLabel,
                                icon = Icons.AutoMirrored.Rounded.Sort,
                                isActive = isSortNonDefault,
                                isExpanded = showSortDropdown,
                                onClick = { showSortDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_sort_dropdown")
                            )

                            DropdownMenu(
                                expanded = showSortDropdown,
                                onDismissRequest = { showSortDropdown = false },
                                modifier = Modifier
                                    .width(236.dp)
                                    .background(colors.slateElevated)
                            ) {
                                Text(
                                    text = "SORT ORDER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.silver,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                                DiscoverSort.entries.forEach { sort ->
                                    val selected = browseState.discoverSort == sort
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = sort.label,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (selected) colors.gold else colors.alabaster,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (selected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = colors.gold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onDiscoverSortSelect(sort)
                                            showSortDropdown = false
                                        }
                                    )
                                }

                                HorizontalDivider(
                                    color = colors.borderSubtle,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Text(
                                    text = "TYPE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.silver,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                                MediaFilter.entries.forEach { mediaFilter ->
                                    val selected = selectedMediaFilter == mediaFilter
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = mediaFilter.label,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (selected) colors.emerald else colors.alabaster,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (selected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = colors.emerald,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onMediaFilterChange(mediaFilter)
                                            showSortDropdown = false
                                        }
                                    )
                                }

                                if (searchQuery.isNotBlank() && browseState.isLiveTmdb) {
                                    HorizontalDivider(
                                        color = colors.borderSubtle,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Text(
                                        text = "SEARCH TARGET",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.silver,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                    listOf(SearchScope.PEOPLE, SearchScope.COLLECTIONS).forEach { scope ->
                                        val selected = browseState.searchScope == scope
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = scope.label,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (selected) colors.gold else colors.alabaster
                                                    )
                                                    if (selected) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Check,
                                                            contentDescription = null,
                                                            tint = colors.gold,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                onSearchScopeSelect(scope)
                                                showSortDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Compact View Mode Toggle Button (Grid / List / Compact)
                        ViewModeCycleButton(
                            mode = browseState.viewMode,
                            onCycle = {
                                val modes = CatalogViewMode.entries
                                val next = modes[(browseState.viewMode.ordinal + 1) % modes.size]
                                onViewModeChange(next)
                            },
                            modifier = Modifier.testTag("search_view_mode_toggle")
                        )
                    }
                }
            }

            if (browseState.isLoading) {
                item(key = "loading_indicator") {
                    LinearProgressIndicator(
                        color = colors.gold,
                        trackColor = colors.slateElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // Skeleton Loaders when initial load / query is in progress with empty list
            if (browseState.isLoading && catalogTitles.isEmpty()) {
                items(4, key = { "search_skeleton_$it" }) {
                    SearchSkeletonRow(modifier = Modifier.padding(horizontal = 16.dp))
                }
            } else if (searchQuery.isNotBlank() && browseState.searchScope == SearchScope.PEOPLE) {
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
                // Results Header
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
                            browseState.selectedGenres.isNotEmpty() -> browseState.selectedGenres.joinToString(" · ")
                            selectedMediaFilter != MediaFilter.ALL -> selectedMediaFilter.label
                            else -> "Explore Catalogue"
                        }
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.alabaster,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${catalogTitles.size} TITLES",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.gold
                        )
                    }
                }

                if (catalogTitles.isEmpty() && !browseState.isLoading) {
                    item(key = "empty_browse") {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            EmptyVaultState(
                                icon = Icons.Rounded.MovieFilter,
                                title = "No matching titles",
                                subtitle = "Try adjusting your genre selections, sort order, or search query.",
                                actionLabel = if (browseState.selectedGenres.isNotEmpty() || searchQuery.isNotBlank()) {
                                    "Reset Filters"
                                } else null,
                                onActionClick = if (browseState.selectedGenres.isNotEmpty() || searchQuery.isNotBlank()) {
                                    {
                                        onClearGenres()
                                        onSearchQueryChange("")
                                    }
                                } else null
                            )
                        }
                    }
                } else {
                    when (browseState.viewMode) {
                        CatalogViewMode.GRID -> {
                            val rows = catalogTitles.chunked(gridColumns)
                            items(
                                items = rows,
                                key = { row -> "grid_row_${row.first().id}" }
                            ) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        CatalogGridCard(
                                            item = item,
                                            vaultEntry = vaultById[item.id],
                                            onClick = { onTitleClick(item.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(gridColumns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        CatalogViewMode.LIST -> {
                            items(catalogTitles, key = { "list_${it.id}" }) { item ->
                                CatalogListCard(
                                    item = item,
                                    vaultEntry = vaultById[item.id],
                                    onClick = { onTitleClick(item.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        CatalogViewMode.COMPACT -> {
                            items(catalogTitles, key = { "compact_${it.id}" }) { item ->
                                CatalogCompactCard(
                                    item = item,
                                    vaultEntry = vaultById[item.id],
                                    onClick = { onTitleClick(item.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
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
                                color = colors.gold,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            OutlinedButton(
                                onClick = onLoadMore,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Load More (Page ${browseState.currentPage} of ${browseState.totalPages})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.gold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.slate,
            contentColor = colors.alabaster
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = colors.gold
                        )
                        Text(
                            text = "Genre + Sort",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.alabaster
                        )
                    }
                    if (browseState.selectedGenres.isNotEmpty() ||
                        browseState.discoverSort != DiscoverSort.POPULARITY ||
                        selectedMediaFilter != MediaFilter.ALL
                    ) {
                        TextButton(
                            onClick = {
                                onClearGenres()
                                onDiscoverSortSelect(DiscoverSort.POPULARITY)
                                onMediaFilterChange(MediaFilter.ALL)
                            }
                        ) {
                            Text("Reset All", color = colors.gold)
                        }
                    }
                }

                // Media Type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.silver
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MediaFilter.entries.forEach { mediaFilter ->
                            val selected = selectedMediaFilter == mediaFilter
                            FilterChip(
                                selected = selected,
                                onClick = { onMediaFilterChange(mediaFilter) },
                                label = { Text(mediaFilter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.gold,
                                    selectedLabelColor = colors.onGold,
                                    containerColor = colors.slateElevated,
                                    labelColor = colors.silver
                                )
                            )
                        }
                    }
                }

                // Sort Order
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SORT BY",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.silver
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiscoverSort.entries.forEach { sort ->
                            val selected = browseState.discoverSort == sort
                            FilterChip(
                                selected = selected,
                                onClick = { onDiscoverSortSelect(sort) },
                                label = { Text(sort.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.emerald,
                                    selectedLabelColor = if (colors.isDark) colors.obsidian else colors.onGold,
                                    containerColor = colors.slateElevated,
                                    labelColor = colors.silver
                                )
                            )
                        }
                    }
                }

                // Multi-Select Genres
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GENRES (MULTI-SELECT)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.silver
                        )
                        if (browseState.selectedGenres.isNotEmpty()) {
                            Text(
                                text = "${browseState.selectedGenres.size} selected",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.gold
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableGenres.forEach { (genreId, genreName) ->
                            val selected = browseState.selectedGenres.any {
                                it.equals(genreName, ignoreCase = true)
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { onGenreToggle(genreId, genreName) },
                                label = { Text(genreName) },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.gold.copy(alpha = 0.20f),
                                    selectedLabelColor = colors.gold,
                                    selectedLeadingIconColor = colors.gold,
                                    containerColor = colors.slateElevated,
                                    labelColor = colors.silver
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = colors.borderSubtle,
                                    selectedBorderColor = colors.gold
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.gold,
                        contentColor = colors.onGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Show Results",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DropdownTriggerButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(),
        label = "dropdown_chevron"
    )
    Surface(
        color = if (isActive) colors.gold.copy(alpha = 0.14f) else colors.slate,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isActive) colors.gold else colors.borderSubtle),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) colors.gold else colors.silver,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive) colors.gold else colors.alabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isActive) colors.gold else colors.silver,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }
    }
}

@Composable
private fun ViewModeCycleButton(
    mode: CatalogViewMode,
    onCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onCycle)
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(150)) },
            label = "view_mode_anim"
        ) { currentMode ->
            Row(
                modifier = Modifier.padding(horizontal = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = when (currentMode) {
                        CatalogViewMode.GRID -> Icons.Rounded.GridView
                        CatalogViewMode.LIST -> Icons.AutoMirrored.Rounded.ViewList
                        CatalogViewMode.COMPACT -> Icons.Rounded.ViewAgenda
                    },
                    contentDescription = "Switch layout: ${currentMode.label}",
                    tint = colors.gold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = currentMode.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.alabaster
                )
            }
        }
    }
}

@Composable
private fun SearchSkeletonRow(modifier: Modifier = Modifier) {
    val colors = CinemaTheme.colors
    val transition = rememberInfiniteTransition(label = "search_skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "search_skeleton_alpha"
    )
    Surface(
        color = colors.slate.copy(alpha = alpha),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
    ) {}
}

@Composable
private fun PersonSearchCard(
    person: PersonSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
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
                        .background(colors.slateElevated)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(colors.slateElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = colors.gold
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
                    color = colors.alabaster
                )
                Text(
                    text = person.knownForDepartment,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.gold
                )
                if (person.knownForTitles.isNotBlank()) {
                    Text(
                        text = "Known for: ${person.knownForTitles}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.silver,
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
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.25f)),
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
                    color = colors.gold
                )
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.alabaster
                )
                Text(
                    text = "Tap to explore all films in this collection",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.silver
                )
            }
        }
    }
}

@Composable
private fun CatalogGridCard(
    item: CatalogTitle,
    vaultEntry: VaultEntry?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "search_grid_scale"
    )

    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier
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
            .testTag("catalog_grid_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CinemaPosterArtwork(
                posterKey = item.posterKey,
                posterUrl = item.posterUrl,
                title = item.title,
                mediaType = item.mediaType,
                userRating = vaultEntry?.userRating?.takeIf { it > 0.0 } ?: item.criticScore,
                status = vaultEntry?.statusEnum,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.alabaster,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(if (item.releaseDate.isNotBlank() && !item.isReleased) item.releaseDate else item.releaseYear)
                    val g = item.genres.firstOrNull()
                    if (!g.isNullOrBlank()) {
                        append(" · ")
                        append(g)
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
private fun CatalogListCard(
    item: CatalogTitle,
    vaultEntry: VaultEntry?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
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
                        color = colors.silver
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.alabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = listOf(item.directorOrCreator, item.runtimeOrSeasons)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.silver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.genres.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.gold.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CatalogCompactCard(
    item: CatalogTitle,
    vaultEntry: VaultEntry?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("catalog_compact_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaPosterArtwork(
                posterKey = item.posterKey,
                posterUrl = item.posterUrl,
                title = item.title,
                mediaType = item.mediaType,
                cornerRadius = 10.dp,
                showBottomBadge = false,
                showMediaTypeBadge = false,
                status = vaultEntry?.statusEnum,
                modifier = Modifier
                    .width(48.dp)
                    .height(68.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.alabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(item.releaseYear)
                        if (item.genres.isNotEmpty()) {
                            append(" · ")
                            append(item.genres.take(2).joinToString(", "))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.silver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.criticScore > 0.0) {
                Spacer(modifier = Modifier.width(8.dp))
                RatingPill(score = item.criticScore)
            }
        }
    }
}
