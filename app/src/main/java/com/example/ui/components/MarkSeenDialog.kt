package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MediaType
import com.example.data.model.RatingScale
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaSlateElevated
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun TenStarRatingBar(
    selectedStars: Int,
    onStarSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = RatingScale.labelFor(selectedStars)
    val badgeColor = when {
        selectedStars >= 9 -> CinemaGold
        selectedStars >= 7 -> CinemaEmerald
        selectedStars >= 5 -> CinemaAlabaster
        selectedStars >= 1 -> CinemaCrimson
        else -> CinemaSilver
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (star in 1..10) {
                val filled = star <= selectedStars
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onStarSelected(star) }
                        .semantics { contentDescription = "Rate $star out of 10 (${RatingScale.labelFor(star)})" }
                        .testTag("star_rating_$star"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = if (filled) CinemaGold else CinemaSilver.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$star",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (filled) CinemaGold else CinemaSilver.copy(alpha = 0.7f),
                        fontWeight = if (filled) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Surface(
            color = badgeColor.copy(alpha = 0.14f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedStars > 0) "$selectedStars / 10" else "Tap a star (1–10)",
                    style = MaterialTheme.typography.titleSmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkSeenDialog(
    title: String,
    releaseYear: Int,
    mediaType: MediaType,
    initialRating: Int = 0,
    initialHeadline: String = "",
    initialWatchedAt: Long = 0L,
    isSaving: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, reviewHeadline: String, watchedAtMillis: Long) -> Unit
) {
    var selectedStars by remember(title) { mutableIntStateOf(initialRating.coerceIn(0, 10)) }
    var reviewHeadline by remember(title) { mutableStateOf(initialHeadline) }
    var watchedAtMillis by remember(title) {
        mutableLongStateOf(
            if (initialWatchedAt > 0L) normalizeToLocalNoonMillis(initialWatchedAt)
            else todayLocalNoonMillis()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val isTodaySelected = remember(watchedAtMillis) { isSameLocalDay(watchedAtMillis, todayLocalNoonMillis()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = CinemaSlate,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("mark_seen_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaEmerald.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = CinemaEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MARK AS SEEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = CinemaAlabaster,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$releaseYear · ${mediaType.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaSilver
                        )
                    }
                }

                HorizontalDivider(color = CinemaBorderSubtle)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Your Rating (1–10)",
                        style = MaterialTheme.typography.titleSmall,
                        color = CinemaAlabaster
                    )
                    TenStarRatingBar(
                        selectedStars = selectedStars,
                        onStarSelected = { star -> selectedStars = star }
                    )
                }

                OutlinedTextField(
                    value = reviewHeadline,
                    onValueChange = { if (it.length <= 160) reviewHeadline = it },
                    label = { Text("Review Headline") },
                    placeholder = { Text("e.g., Unforgettable cinema!") },
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
                        .testTag("mark_seen_headline_input")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Watched Date",
                            style = MaterialTheme.typography.titleSmall,
                            color = CinemaAlabaster
                        )
                        Text(
                            text = formatWatchedDate(watchedAtMillis),
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { watchedAtMillis = todayLocalNoonMillis() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTodaySelected) CinemaGold else CinemaSlateElevated,
                                contentColor = if (isTodaySelected) CinemaObsidian else CinemaAlabaster
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isTodaySelected) CinemaGold else CinemaBorderSubtle
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mark_seen_today_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Today,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (!isTodaySelected) CinemaGold else CinemaBorderSubtle
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!isTodaySelected) CinemaGold.copy(alpha = 0.16f) else CinemaSlateElevated,
                                contentColor = if (!isTodaySelected) CinemaGold else CinemaAlabaster
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mark_seen_calendar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "Pick watched date",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calendar",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CinemaBorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel", color = CinemaSilver)
                    }

                    Button(
                        onClick = { onConfirm(selectedStars, reviewHeadline.trim(), watchedAtMillis) },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaEmerald,
                            contentColor = CinemaObsidian
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(50.dp)
                            .testTag("mark_seen_confirm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save to Library",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        WatchedDatePickerDialog(
            initialLocalMillis = watchedAtMillis,
            onDismiss = { showDatePicker = false },
            onDateSelected = { pickedLocalMillis ->
                watchedAtMillis = pickedLocalMillis
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedDatePickerDialog(
    initialLocalMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val todayUtcMillis = remember { localMillisToUtcDatePickerMillis(todayLocalNoonMillis()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localMillisToUtcDatePickerMillis(initialLocalMillis),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayUtcMillis + 24L * 60L * 60L * 1000L - 1L
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(containerColor = CinemaSlate),
        confirmButton = {
            TextButton(
                onClick = {
                    val utcSelected = datePickerState.selectedDateMillis
                    if (utcSelected != null) {
                        onDateSelected(utcDatePickerMillisToLocalNoonMillis(utcSelected))
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Select", color = CinemaGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CinemaSilver)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = CinemaSlate,
                titleContentColor = CinemaAlabaster,
                headlineContentColor = CinemaGold,
                weekdayContentColor = CinemaSilver,
                subheadContentColor = CinemaSilver,
                yearContentColor = CinemaAlabaster,
                currentYearContentColor = CinemaGold,
                selectedYearContentColor = CinemaObsidian,
                selectedYearContainerColor = CinemaGold,
                dayContentColor = CinemaAlabaster,
                selectedDayContentColor = CinemaObsidian,
                selectedDayContainerColor = CinemaGold,
                todayContentColor = CinemaGold,
                todayDateBorderColor = CinemaGold
            )
        )
    }
}

fun todayLocalNoonMillis(): Long = normalizeToLocalNoonMillis(System.currentTimeMillis())

fun normalizeToLocalNoonMillis(millis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun utcDatePickerMillisToLocalNoonMillis(utcMillis: Long): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    val localCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return localCal.timeInMillis
}

fun localMillisToUtcDatePickerMillis(localMillis: Long): Long {
    val localCal = Calendar.getInstance().apply {
        timeInMillis = localMillis
    }
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            localCal.get(Calendar.YEAR),
            localCal.get(Calendar.MONTH),
            localCal.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
    }
    return utcCal.timeInMillis
}

fun isSameLocalDay(aMillis: Long, bMillis: Long): Boolean {
    if (aMillis <= 0L || bMillis <= 0L) return false
    val calA = Calendar.getInstance().apply { timeInMillis = aMillis }
    val calB = Calendar.getInstance().apply { timeInMillis = bMillis }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}

fun formatWatchedDate(millis: Long): String {
    if (millis <= 0L) return "Not set"
    return SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(millis))
}
