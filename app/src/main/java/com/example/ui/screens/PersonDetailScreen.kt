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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PersonDetail
import com.example.data.model.VaultEntry
import com.example.ui.components.CinemaPosterArtwork
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated

@Composable
fun PersonDetailScreen(
    personDetail: PersonDetail?,
    isLoading: Boolean,
    vaultById: Map<String, VaultEntry>,
    onBack: () -> Unit,
    onTitleClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaObsidian)
    ) {
        if (isLoading && personDetail == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CinemaGold)
            }
        } else if (personDetail != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item(key = "person_header") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = CinemaSlate,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("person_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = CinemaAlabaster
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (personDetail.profileUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(personDetail.profileUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = personDetail.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(CinemaSlateElevated)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(CinemaSlateElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (personDetail.knownForDepartment.isNotBlank()) {
                                    Surface(
                                        color = CinemaGold.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = personDetail.knownForDepartment.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaGold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = personDetail.name,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = CinemaAlabaster
                                )
                                if (personDetail.birthday.isNotBlank()) {
                                    Text(
                                        text = buildString {
                                            append("Born ${personDetail.birthday}")
                                            if (personDetail.deathday.isNotBlank()) {
                                                append(" · Died ${personDetail.deathday}")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaSilver
                                    )
                                }
                                if (personDetail.placeOfBirth.isNotBlank()) {
                                    Text(
                                        text = personDetail.placeOfBirth,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaSilver
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "person_bio") {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Biography",
                            style = MaterialTheme.typography.headlineSmall,
                            color = CinemaAlabaster
                        )
                        Text(
                            text = personDetail.biography,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaSilver
                        )
                    }
                }

                if (personDetail.photos.size > 1) {
                    item(key = "person_photos") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Portraits",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CinemaAlabaster,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(personDetail.photos, key = { it }) { url ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = personDetail.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(108.dp)
                                            .height(152.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(CinemaSlateElevated)
                                    )
                                }
                            }
                        }
                    }
                }

                if (personDetail.credits.isNotEmpty()) {
                    item(key = "person_credits_header") {
                        Text(
                            text = "Known For & Filmography (${personDetail.credits.size})",
                            style = MaterialTheme.typography.headlineSmall,
                            color = CinemaAlabaster,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    items(personDetail.credits, key = { "credit_${it.id}" }) { credit ->
                        val entry = vaultById[credit.id]
                        Surface(
                            color = CinemaSlate,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CinemaBorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onTitleClick(credit.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CinemaPosterArtwork(
                                    posterKey = credit.posterKey,
                                    posterUrl = credit.posterUrl,
                                    title = credit.title,
                                    mediaType = credit.mediaType,
                                    showBottomBadge = false,
                                    status = entry?.statusEnum,
                                    modifier = Modifier
                                        .width(66.dp)
                                        .aspectRatio(0.70f)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = credit.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CinemaAlabaster,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${credit.releaseYear} · ${credit.directorOrCreator}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaGold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = credit.synopsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaSilver,
                                        maxLines = 2,
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
}
