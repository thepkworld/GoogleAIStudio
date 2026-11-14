package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomTitleSheet(
    isMutating: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        mediaType: MediaType,
        status: WatchStatus,
        releaseYear: Int,
        genres: List<String>,
        directorOrCreator: String,
        runtimeOrSeasons: String,
        synopsis: String,
        posterKey: String,
        totalEpisodes: Int
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf(MediaType.MOVIE) }
    var status by remember { mutableStateOf(WatchStatus.WANT_TO_WATCH) }
    var releaseYearText by remember { mutableStateOf("2025") }
    var genresText by remember { mutableStateOf("Drama") }
    var director by remember { mutableStateOf("") }
    var runtimeOrSeasons by remember { mutableStateOf("") }
    var totalEpisodesText by remember { mutableStateOf("10") }
    var synopsis by remember { mutableStateOf("") }
    var posterKey by remember { mutableStateOf("cinema") }

    val posterThemes = listOf(
        "cinema" to "Theatre",
        "scifi" to "Sci-Fi",
        "noir" to "Noir",
        "drama" to "Drama"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CinemaSlate,
        contentColor = CinemaAlabaster
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Movie or Series",
                style = MaterialTheme.typography.headlineMedium,
                color = CinemaAlabaster
            )
            Text(
                text = "Log any film or show directly into your cloud-synced Vault.",
                style = MaterialTheme.typography.bodySmall,
                color = CinemaSilver
            )

            // Media Type Switch
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MediaType.entries.forEach { type ->
                    val selected = mediaType == type
                    FilterChip(
                        selected = selected,
                        onClick = { mediaType = type },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaGold,
                            selectedLabelColor = CinemaObsidian
                        )
                    )
                }
            }

            // Status Switch
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WatchStatus.entries.forEach { ws ->
                    val selected = status == ws
                    val activeColor = if (ws == WatchStatus.WATCHED) CinemaEmerald else CinemaGold
                    FilterChip(
                        selected = selected,
                        onClick = { status = ws },
                        label = { Text(ws.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeColor,
                            selectedLabelColor = CinemaObsidian
                        )
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 160) title = it },
                label = { Text("Title *") },
                placeholder = { Text("e.g., Interstellar") },
                singleLine = true,
                colors = customFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_title_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = releaseYearText,
                    onValueChange = { if (it.length <= 4) releaseYearText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = customFieldColors(),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = runtimeOrSeasons,
                    onValueChange = { if (it.length <= 60) runtimeOrSeasons = it },
                    label = { Text(if (mediaType == MediaType.SERIES) "Seasons" else "Runtime") },
                    placeholder = { Text(if (mediaType == MediaType.SERIES) "2 Seasons" else "2h 15m") },
                    singleLine = true,
                    colors = customFieldColors(),
                    modifier = Modifier.weight(1.4f)
                )
            }

            if (mediaType == MediaType.SERIES) {
                OutlinedTextField(
                    value = totalEpisodesText,
                    onValueChange = { if (it.length <= 4) totalEpisodesText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total Episodes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = customFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = director,
                onValueChange = { if (it.length <= 120) director = it },
                label = { Text(if (mediaType == MediaType.SERIES) "Creator / Showrunner" else "Director") },
                singleLine = true,
                colors = customFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = genresText,
                onValueChange = { genresText = it },
                label = { Text("Genres (comma separated)") },
                singleLine = true,
                colors = customFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = synopsis,
                onValueChange = { if (it.length <= 1500) synopsis = it },
                label = { Text("Synopsis / Notes") },
                minLines = 3,
                maxLines = 5,
                colors = customFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Poster Aesthetic",
                    style = MaterialTheme.typography.labelLarge,
                    color = CinemaSilver
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    posterThemes.forEach { (key, label) ->
                        val selected = posterKey == key
                        Surface(
                            color = if (selected) CinemaGold else CinemaObsidian,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) CinemaGold else CinemaBorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { posterKey = key }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CinemaObsidian else CinemaSilver,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val year = releaseYearText.toIntOrNull()?.coerceIn(1888, 2100) ?: 2025
                    val genres = genresText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(10)
                    val totalEp = if (mediaType == MediaType.SERIES) {
                        totalEpisodesText.toIntOrNull()?.coerceIn(0, 5000) ?: 0
                    } else 0
                    onSubmit(
                        title.trim(),
                        mediaType,
                        status,
                        year,
                        genres,
                        director.trim(),
                        runtimeOrSeasons.trim(),
                        synopsis.trim(),
                        posterKey,
                        totalEp
                    )
                },
                enabled = title.trim().isNotEmpty() && !isMutating,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaGold,
                    contentColor = CinemaObsidian
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_custom_title_button")
            ) {
                Text(
                    text = "Save to Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CinemaGold,
    unfocusedBorderColor = CinemaBorderSubtle,
    focusedLabelColor = CinemaGold,
    unfocusedLabelColor = CinemaSilver,
    focusedTextColor = CinemaAlabaster,
    unfocusedTextColor = CinemaAlabaster,
    cursorColor = CinemaGold
)
