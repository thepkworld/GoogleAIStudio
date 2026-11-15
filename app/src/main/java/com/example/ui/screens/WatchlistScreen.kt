package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogViewMode
import com.example.data.model.VaultEntry
import com.example.ui.MediaFilter
import com.example.ui.VaultStats
import com.example.ui.WatchlistSort
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.components.EmptyVaultState
import com.example.ui.theme.CinemaTheme

@Composable
fun WatchlistScreen(
    entries: List<VaultEntry>,
    stats: VaultStats,
    selectedFilter: MediaFilter,
    selectedSort: WatchlistSort,
    viewMode: CatalogViewMode,
    onFilterChange: (MediaFilter) -> Unit,
    onSortChange: (WatchlistSort) -> Unit,
    onViewModeChange: (CatalogViewMode) -> Unit,
    onEntryClick: (String) -> Unit,
    onMarkWatched: (VaultEntry) -> Unit,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    var showSortDropdown by remember { mutableStateOf(false) }

    val movieWord = if (stats.watchlistMovies == 1) "Movie" else "Movies"
    val breakdownText = "${stats.watchlistMovies} $movieWord and ${stats.watchlistSeries} Series to watch"

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gridColumns = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Banner
            item(key = "watchlist_summary") {
                Surface(
                    color = colors.slate,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bucketlist_summary_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "BUCKETLIST",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.gold
                            )
                            Text(
                                text = breakdownText,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.alabaster,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Special",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.silver
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            color = colors.gold.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BookmarkAdd,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier
                                    .padding(14.dp)
                                    .size(28.dp)
                            )
                        }
                    }
                }
            }

            // Filter Tabs + Sort Dropdown & View Toggle
            item(key = "watchlist_controls") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MediaFilter.entries.forEach { filter ->
                            val selected = selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { onFilterChange(filter) },
                                label = { Text(filter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.gold,
                                    selectedLabelColor = colors.onGold,
                                    containerColor = colors.slate,
                                    labelColor = colors.silver
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sort Dropdown Menu
                        Box(modifier = Modifier.weight(1f)) {
                            val arrowRotation by animateFloatAsState(
                                targetValue = if (showSortDropdown) 180f else 0f,
                                animationSpec = spring(),
                                label = "watchlist_sort_arrow"
                            )
                            Surface(
                                color = colors.slate,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showSortDropdown = true }
                                    .testTag("bucketlist_sort_dropdown")
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                                            contentDescription = null,
                                            tint = colors.gold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Sort: ${selectedSort.label}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = colors.alabaster,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = colors.gold,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .graphicsLayer { rotationZ = arrowRotation }
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSortDropdown,
                                onDismissRequest = { showSortDropdown = false },
                                modifier = Modifier
                                    .width(220.dp)
                                    .background(colors.slateElevated)
                            ) {
                                WatchlistSort.entries.forEach { sort ->
                                    val selected = selectedSort == sort
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
                                            onSortChange(sort)
                                            showSortDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // View Mode Toggle (Tile/Grid, List, Compact)
                        BucketlistViewToggleButton(
                            mode = viewMode,
                            onCycle = {
                                val modes = CatalogViewMode.entries
                                val next = modes[(viewMode.ordinal + 1) % modes.size]
                                onViewModeChange(next)
                            },
                            modifier = Modifier.testTag("bucketlist_view_mode_toggle")
                        )
                    }
                }
            }

            if (entries.isEmpty()) {
                item(key = "watchlist_empty") {
                    EmptyVaultState(
                        icon = Icons.Rounded.BookmarkAdd,
                        title = "Your Bucketlist is empty",
                        subtitle = "Open any movie or series from Home or Search and tap Add to Bucketlist.",
                        actionLabel = "Explore Movies & Series",
                        onActionClick = onBrowseClick
                    )
                }
            } else {
                when (viewMode) {
                    CatalogViewMode.GRID -> {
                        val rows = entries.chunked(gridColumns)
                        items(rows, key = { "wl_grid_row_${it.first().id}" }) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { entry ->
                                    BucketlistGridCard(
                                        entry = entry,
                                        onClick = { onEntryClick(entry.id) },
                                        onMarkSeen = { onMarkWatched(entry) },
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
                        items(entries, key = { "wl_list_${it.id}" }) { entry ->
                            BucketlistListCard(
                                entry = entry,
                                onClick = { onEntryClick(entry.id) },
                                onMarkSeen = { onMarkWatched(entry) }
                            )
                        }
                    }

                    CatalogViewMode.COMPACT -> {
                        items(entries, key = { "wl_compact_${it.id}" }) { entry ->
                            BucketlistCompactCard(
                                entry = entry,
                                onClick = { onEntryClick(entry.id) },
                                onMarkSeen = { onMarkWatched(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BucketlistViewToggleButton(
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
            label = "bucketlist_view_toggle"
        ) { currentMode ->
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (currentMode) {
                        CatalogViewMode.GRID -> Icons.Rounded.GridView
                        CatalogViewMode.LIST -> Icons.AutoMirrored.Rounded.ViewList
                        CatalogViewMode.COMPACT -> Icons.Rounded.ViewAgenda
                    },
                    contentDescription = "Switch view: ${currentMode.label}",
                    tint = colors.gold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (currentMode == CatalogViewMode.GRID) "Tile" else currentMode.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.alabaster
                )
            }
        }
    }
}

@Composable
private fun BucketlistGridCard(
    entry: VaultEntry,
    onClick: () -> Unit,
    onMarkSeen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "wl_grid_scale"
    )

    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("watchlist_grid_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CinemaPosterArtwork(
                posterKey = entry.posterKey,
                posterUrl = entry.posterUrl,
                title = entry.title,
                mediaType = entry.mediaTypeEnum,
                showBottomBadge = false,
                showMediaTypeBadge = false,
                status = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.70f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.alabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${entry.releaseYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.silver
                )
            }

            MarkSeenAccentButton(
                onClick = onMarkSeen,
                compact = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("watchlist_mark_seen_${entry.id}")
            )
        }
    }
}

@Composable
private fun BucketlistListCard(
    entry: VaultEntry,
    onClick: () -> Unit,
    onMarkSeen: () -> Unit
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("watchlist_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaPosterArtwork(
                posterKey = entry.posterKey,
                posterUrl = entry.posterUrl,
                title = entry.title,
                mediaType = entry.mediaTypeEnum,
                showBottomBadge = false,
                showMediaTypeBadge = false,
                status = null,
                modifier = Modifier
                    .width(76.dp)
                    .height(108.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.alabaster,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${entry.releaseYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.silver
                    )
                }

                MarkSeenAccentButton(
                    onClick = onMarkSeen,
                    compact = false,
                    modifier = Modifier.testTag("watchlist_mark_seen_${entry.id}")
                )
            }
        }
    }
}

@Composable
private fun BucketlistCompactCard(
    entry: VaultEntry,
    onClick: () -> Unit,
    onMarkSeen: () -> Unit
) {
    val colors = CinemaTheme.colors
    Surface(
        color = colors.slate,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("watchlist_compact_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaPosterArtwork(
                posterKey = entry.posterKey,
                posterUrl = entry.posterUrl,
                title = entry.title,
                mediaType = entry.mediaTypeEnum,
                cornerRadius = 10.dp,
                showBottomBadge = false,
                showMediaTypeBadge = false,
                status = null,
                modifier = Modifier
                    .width(48.dp)
                    .height(68.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.alabaster,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${entry.releaseYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.silver
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            MarkSeenAccentButton(
                onClick = onMarkSeen,
                compact = true,
                modifier = Modifier.testTag("watchlist_mark_seen_${entry.id}")
            )
        }
    }
}

@Composable
private fun MarkSeenAccentButton(
    onClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val buttonInk = MaterialTheme.colorScheme.onTertiary
    val buttonBrush = Brush.horizontalGradient(
        colors = listOf(colors.emerald, colors.emerald.copy(alpha = 0.85f))
    )
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.alabaster.copy(alpha = 0.18f)),
        shadowElevation = 4.dp,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonBrush)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = buttonInk,
                modifier = Modifier.size(if (compact) 16.dp else 18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Mark Seen",
                style = MaterialTheme.typography.labelLarge,
                color = buttonInk,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
