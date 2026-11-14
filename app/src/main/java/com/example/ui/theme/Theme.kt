package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkCinemaColorScheme = darkColorScheme(
    primary = CinemaGold,
    onPrimary = CinemaOnGold,
    primaryContainer = CinemaGoldContainer,
    onPrimaryContainer = CinemaGold,
    secondary = CinemaCrimson,
    onSecondary = CinemaAlabaster,
    secondaryContainer = CinemaCrimsonContainer,
    onSecondaryContainer = CinemaCrimson,
    tertiary = CinemaEmerald,
    onTertiary = CinemaObsidian,
    tertiaryContainer = CinemaEmeraldContainer,
    onTertiaryContainer = CinemaEmerald,
    background = CinemaObsidian,
    onBackground = CinemaAlabaster,
    surface = CinemaVelvet,
    onSurface = CinemaAlabaster,
    surfaceVariant = CinemaSlate,
    onSurfaceVariant = CinemaSilver,
    surfaceContainerLowest = CinemaObsidian,
    surfaceContainerLow = CinemaVelvet,
    surfaceContainer = CinemaSlate,
    surfaceContainerHigh = CinemaSlateElevated,
    surfaceContainerHighest = CinemaBorderSubtle,
    outline = CinemaBorderSubtle,
    outlineVariant = CinemaSlateElevated,
    error = CinemaCrimson,
    onError = CinemaAlabaster
)

private val LightArchiveColorScheme = lightColorScheme(
    primary = ArchiveGoldDeep,
    onPrimary = ArchivePaper,
    primaryContainer = ArchiveGoldSurface,
    onPrimaryContainer = ArchiveInk,
    secondary = CinemaCrimson,
    onSecondary = ArchivePaper,
    secondaryContainer = CinemaCrimsonContainer,
    onSecondaryContainer = CinemaAlabaster,
    tertiary = CinemaEmerald,
    onTertiary = ArchivePaper,
    background = ArchivePaper,
    onBackground = ArchiveInk,
    surface = ArchivePaper,
    onSurface = ArchiveInk,
    surfaceVariant = ArchiveCard,
    onSurfaceVariant = ArchiveInkMuted,
    outline = ArchiveInkMuted.copy(alpha = 0.35f)
)

val CineVaultShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CineVaultTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkCinemaColorScheme else LightArchiveColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CineVaultTypography,
        shapes = CineVaultShapes,
        content = content
    )
}
