package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogTitle
import com.example.data.model.VaultEntry
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.theme.CinemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionGridScreen(
    sectionTitle: String,
    items: List<CatalogTitle>,
    vaultById: Map<String, VaultEntry>,
    onBack: () -> Unit,
    onTitleClick: (String) -> Unit
) {
    val colors = CinemaTheme.colors
    Scaffold(
        containerColor = colors.obsidian,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.obsidian,
                    titleContentColor = colors.alabaster
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("section_grid_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.alabaster
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.alabaster,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${items.size} TITLES",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.gold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 148.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(colors.obsidian)
                .padding(innerPadding)
        ) {
            items(items, key = { "grid_${it.id}" }) { item ->
                val vaultEntry = vaultById[item.id]
                Surface(
                    color = colors.slate,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.borderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTitleClick(item.id) }
                        .testTag("section_grid_card_${item.id}")
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
                                val sub = item.genres.firstOrNull() ?: item.language
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
        }
    }
}
