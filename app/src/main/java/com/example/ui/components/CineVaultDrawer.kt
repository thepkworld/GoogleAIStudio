package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.auth.AuthUserSummary
import com.example.ui.ApiConnectionStatus
import com.example.ui.theme.CinemaTheme

@Composable
fun CineVaultDrawerHost(
    isOpen: Boolean,
    onClose: () -> Unit,
    authUser: AuthUserSummary?,
    isAuthBusy: Boolean,
    isSyncingCloud: Boolean,
    pendingGuestMergeCount: Int,
    isDarkTheme: Boolean,
    apiStatus: ApiConnectionStatus,
    totalWatched: Int,
    totalBucketlist: Int,
    onSignInClick: () -> Unit,
    onConnectGoogleClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onSyncLocalClick: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onRefreshFeeds: () -> Unit,
    onExportJsonClick: () -> Unit,
    onImportJsonClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = CinemaTheme.colors
    val drawerWidth = 320.dp

    if (isOpen) {
        BackHandler(onBack = onClose)
    }

    val drawerOffsetX by animateDpAsState(
        targetValue = if (isOpen) 0.dp else -drawerWidth - 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "drawer_slide_x"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.56f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "drawer_scrim_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
                    .testTag("drawer_scrim")
            )
        }

        if (isOpen || drawerOffsetX > -drawerWidth) {
            Surface(
                color = colors.velvet,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, colors.borderSubtle),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = drawerWidth)
                    .fillMaxWidth(0.86f)
                    .offset(x = drawerOffsetX)
                    .testTag("global_navigation_drawer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Drawer Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.gold.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MovieFilter,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.alabaster,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalWatched watched · $totalBucketlist bucketlist",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.silver
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close navigation menu",
                                tint = colors.silver
                            )
                        }
                    }

                    HorizontalDivider(color = colors.borderSubtle)

                    // Account Section
                    DrawerSectionLabel(title = "ACCOUNT & SYNC")
                    Surface(
                        color = colors.slate,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AccountAvatar(authUser = authUser)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = authUser?.displayName ?: "Guest Explorer",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.alabaster,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = authUser?.email?.takeIf { it.isNotBlank() }
                                            ?: "Local device vault · Not synced",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.silver,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (authUser == null) {
                                Button(
                                    onClick = onSignInClick,
                                    enabled = !isAuthBusy,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.gold,
                                        contentColor = colors.onGold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("drawer_sign_in_button")
                                ) {
                                    if (isAuthBusy) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = colors.onGold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Login,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = "Sign In / Create Account",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (!authUser.isCloudSynced) {
                                Surface(
                                    color = colors.gold.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "Standard Account · Saved on this device",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.gold,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Button(
                                    onClick = onConnectGoogleClick,
                                    enabled = !isAuthBusy,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.gold,
                                        contentColor = colors.onGold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("drawer_connect_google_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudSync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Connect Google for Cloud Sync",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(
                                    onClick = onSignOutClick,
                                    enabled = !isAuthBusy,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, colors.crimson.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colors.crimson
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("drawer_sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign Out", maxLines = 1)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onSyncLocalClick,
                                        enabled = !isSyncingCloud && !isAuthBusy,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (pendingGuestMergeCount > 0) colors.gold else colors.emerald.copy(alpha = 0.55f)
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (pendingGuestMergeCount > 0) colors.gold else colors.emerald
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("drawer_sync_cloud_button")
                                    ) {
                                        if (isSyncingCloud) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = colors.emerald
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Syncing", maxLines = 1)
                                        } else {
                                            Icon(
                                                imageVector = if (pendingGuestMergeCount > 0) {
                                                    Icons.Rounded.CloudSync
                                                } else {
                                                    Icons.Rounded.CloudDone
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (pendingGuestMergeCount > 0) {
                                                    "Sync ($pendingGuestMergeCount)"
                                                } else {
                                                    "Backup"
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onSignOutClick,
                                        enabled = !isAuthBusy,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, colors.crimson.copy(alpha = 0.5f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = colors.crimson
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("drawer_sign_out_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sign Out", maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    // Appearance Section
                    DrawerSectionLabel(title = "APPEARANCE")
                    Surface(
                        color = colors.slate,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleTheme(!isDarkTheme) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colors.gold.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                        contentDescription = null,
                                        tint = colors.gold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isDarkTheme) "Cinema Dark Mode" else "Editorial Light Mode",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.alabaster,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Switch between obsidian dark & archival paper",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.silver
                                    )
                                }
                            }
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleTheme,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.onGold,
                                    checkedTrackColor = colors.gold,
                                    uncheckedThumbColor = colors.silver,
                                    uncheckedTrackColor = colors.slateElevated
                                ),
                                modifier = Modifier.testTag("drawer_theme_toggle")
                            )
                        }
                    }

                    // API Connection Status Section
                    DrawerSectionLabel(title = "API FEED STATUS")
                    val statusTint = when (apiStatus) {
                        ApiConnectionStatus.TMDB_OMDB_LIVE -> colors.emerald
                        ApiConnectionStatus.OMDB_FALLBACK -> colors.gold
                        ApiConnectionStatus.OFFLINE -> colors.crimson
                    }
                    val statusHint = when (apiStatus) {
                        ApiConnectionStatus.TMDB_OMDB_LIVE -> "TMDB discovery & OMDb ratings online"
                        ApiConnectionStatus.OMDB_FALLBACK -> "TMDB unreachable · Running live on OMDb"
                        ApiConnectionStatus.OFFLINE -> "Showing bundled curated catalog"
                    }
                    Surface(
                        color = colors.slate,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, statusTint.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawer_api_status_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(statusTint)
                                )
                                Column {
                                    Text(
                                        text = apiStatus.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = statusTint,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = statusHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.silver
                                    )
                                }
                            }
                            IconButton(
                                onClick = onRefreshFeeds,
                                modifier = Modifier.testTag("drawer_refresh_api_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Refresh API feeds",
                                    tint = colors.gold
                                )
                            }
                        }
                    }

                    // Data Management Section (JSON Portability)
                    DrawerSectionLabel(title = "DATA MANAGEMENT (.JSON)")
                    Surface(
                        color = colors.slate,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            DrawerActionRow(
                                icon = Icons.Rounded.FileUpload,
                                tint = colors.gold,
                                title = "Export Vault & Stats",
                                subtitle = "Save watch history, ratings & analytics as JSON",
                                testTag = "drawer_export_json",
                                onClick = {
                                    onClose()
                                    onExportJsonClick()
                                }
                            )
                            HorizontalDivider(
                                color = colors.borderSubtle,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            DrawerActionRow(
                                icon = Icons.Rounded.FileDownload,
                                tint = colors.emerald,
                                title = "Import Vault Backup",
                                subtitle = "Merge titles & ratings from a JSON file",
                                testTag = "drawer_import_json",
                                onClick = {
                                    onClose()
                                    onImportJsonClick()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(title: String) {
    val colors = CinemaTheme.colors
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = colors.ash,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun AccountAvatar(authUser: AuthUserSummary?) {
    val colors = CinemaTheme.colors
    if (authUser != null && authUser.photoUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(authUser.photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "${authUser.displayName} avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.slateElevated)
        )
    } else if (authUser != null) {
        val initials = authUser.displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "CV" }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.gold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                color = colors.gold,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.slateElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PersonOutline,
                contentDescription = "Guest user",
                tint = colors.silver,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DrawerActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = CinemaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.alabaster,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.silver
            )
        }
    }
}
