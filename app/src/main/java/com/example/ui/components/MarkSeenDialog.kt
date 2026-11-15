package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.theme.CinemaTheme
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
    val colors = CinemaTheme.colors
    val label = RatingScale.labelFor(selectedStars)
    val badgeColor = when {
        selectedStars >= 9 -> colors.gold
        selectedStars >= 7 -> colors.emerald
        selectedStars >= 5 -> colors.alabaster
        selectedStars >= 1 -> colors.crimson
        else -> colors.silver
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
                        tint = if (filled) colors.gold else colors.silver.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$star",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (filled) colors.gold else colors.silver.copy(alpha = 0.7f),
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
    val colors = CinemaTheme.colors
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
            color = colors.slate,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.35f)),
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
                            .background(colors.emerald.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.emerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MARK AS SEEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.emerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.alabaster,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$releaseYear · ${mediaType.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.silver
                        )
                    }
                }

                HorizontalDivider(color = colors.borderSubtle)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Your Rating (1–10)",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.alabaster
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
                        focusedBorderColor = colors.gold,
                        unfocusedBorderColor = colors.borderSubtle,
                        focusedLabelColor = colors.gold,
                        unfocusedLabelColor = colors.silver,
                        focusedTextColor = colors.alabaster,
                        unfocusedTextColor = colors.alabaster,
                        cursorColor = colors.gold
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
                            color = colors.alabaster
                        )
                        Text(
                            text = formatWatchedDate(watchedAtMillis),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.gold,
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
                                containerColor = if (isTodaySelected) colors.gold else colors.slateElevated,
                                contentColor = if (isTodaySelected) colors.onGold else colors.alabaster
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isTodaySelected) colors.gold else colors.borderSubtle
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
                                if (!isTodaySelected) colors.gold else colors.borderSubtle
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!isTodaySelected) colors.gold.copy(alpha = 0.16f) else colors.slateElevated,
                                contentColor = if (!isTodaySelected) colors.gold else colors.alabaster
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
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel", color = colors.silver)
                    }

                    Button(
                        onClick = { onConfirm(selectedStars, reviewHeadline.trim(), watchedAtMillis) },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.emerald,
                            contentColor = MaterialTheme.colorScheme.onTertiary
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
    val colors = CinemaTheme.colors
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
        colors = DatePickerDefaults.colors(containerColor = colors.slate),
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
                Text("Select", color = colors.gold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.silver)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = colors.slate,
                titleContentColor = colors.alabaster,
                headlineContentColor = colors.gold,
                weekdayContentColor = colors.silver,
                subheadContentColor = colors.silver,
                yearContentColor = colors.alabaster,
                currentYearContentColor = colors.gold,
                selectedYearContentColor = colors.onGold,
                selectedYearContainerColor = colors.gold,
                dayContentColor = colors.alabaster,
                selectedDayContentColor = colors.onGold,
                selectedDayContainerColor = colors.gold,
                todayContentColor = colors.gold,
                todayDateBorderColor = colors.gold
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

@Composable
fun UnreleasedTitleDialog(
    title: String,
    releaseDate: String,
    releaseYear: Int,
    alreadyInBucketlist: Boolean,
    onDismiss: () -> Unit,
    onAddToBucketlist: () -> Unit
) {
    val colors = CinemaTheme.colors
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "unreleased_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(220),
        label = "unreleased_alpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = colors.slate,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .testTag("unreleased_title_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.gold.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = colors.gold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "This movie isn’t released yet.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.alabaster,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                val releaseInfo = if (releaseDate.isNotBlank()) {
                    "scheduled for $releaseDate"
                } else {
                    "scheduled for $releaseYear"
                }
                Text(
                    text = "\"$title\" is $releaseInfo. You can save it to your Bucketlist now and mark it as watched once it releases.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.silver,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("unreleased_dismiss_button")
                    ) {
                        Text("Got It", color = colors.alabaster, fontWeight = FontWeight.SemiBold)
                    }

                    if (!alreadyInBucketlist) {
                        Button(
                            onClick = onAddToBucketlist,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.gold,
                                contentColor = colors.onGold
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("unreleased_bucketlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BookmarkAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add to Bucketlist",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
