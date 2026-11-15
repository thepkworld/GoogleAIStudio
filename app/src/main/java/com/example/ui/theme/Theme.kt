package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

val LocalCinemaColors = staticCompositionLocalOf { DarkCinemaColors }

object CinemaTheme {
    val colors: CinemaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCinemaColors.current
}

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
    onPrimary = ArchiveOnGold,
    primaryContainer = ArchiveGoldSurface,
    onPrimaryContainer = ArchiveInk,
    secondary = ArchiveCrimson,
    onSecondary = ArchivePaper,
    secondaryContainer = ArchiveCrimsonContainer,
    onSecondaryContainer = ArchiveCrimson,
    tertiary = ArchiveEmerald,
    onTertiary = ArchivePaper,
    tertiaryContainer = ArchiveEmeraldContainer,
    onTertiaryContainer = ArchiveEmerald,
    background = ArchivePaper,
    onBackground = ArchiveInk,
    surface = ArchiveVelvet,
    onSurface = ArchiveInk,
    surfaceVariant = ArchiveCard,
    onSurfaceVariant = ArchiveInkMuted,
    surfaceContainerLowest = ArchivePaper,
    surfaceContainerLow = ArchiveVelvet,
    surfaceContainer = ArchiveCard,
    surfaceContainerHigh = ArchiveElevated,
    surfaceContainerHighest = ArchiveBorder,
    outline = ArchiveBorder,
    outlineVariant = ArchiveElevated,
    error = ArchiveCrimson,
    onError = ArchivePaper
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
    val cinemaColors = if (darkTheme) DarkCinemaColors else LightArchiveColors

    CompositionLocalProvider(LocalCinemaColors provides cinemaColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CineVaultTypography,
            shapes = CineVaultShapes,
            content = content
        )
    }
}
