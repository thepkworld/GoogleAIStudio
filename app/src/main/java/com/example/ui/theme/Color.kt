package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Dark Editorial Cinema Palette
val CinemaObsidian = Color(0xFF0A0B0F)
val CinemaVelvet = Color(0xFF13151D)
val CinemaSlate = Color(0xFF1B1E29)
val CinemaSlateElevated = Color(0xFF242836)
val CinemaBorderSubtle = Color(0xFF2E3346)

val CinemaGold = Color(0xFFF5B942)
val CinemaGoldDim = Color(0xFFC9922B)
val CinemaGoldContainer = Color(0xFF33260F)
val CinemaOnGold = Color(0xFF1A1204)

val CinemaCrimson = Color(0xFFE85D75)
val CinemaCrimsonContainer = Color(0xFF3B1520)

val CinemaEmerald = Color(0xFF3DD598)
val CinemaEmeraldContainer = Color(0xFF103326)

val CinemaIndigo = Color(0xFF7B8CDE)
val CinemaIndigoContainer = Color(0xFF1E2340)

val CinemaAlabaster = Color(0xFFF4F1EA)
val CinemaSilver = Color(0xFFA7ADC2)
val CinemaAsh = Color(0xFF6F758B)

// Light Editorial Archival Paper Palette
val ArchivePaper = Color(0xFFFBF8F2)
val ArchiveVelvet = Color(0xFFF3ECE0)
val ArchiveCard = Color(0xFFFFFFFF)
val ArchiveElevated = Color(0xFFECE4D5)
val ArchiveBorder = Color(0xFFDAD0BE)

val ArchiveInk = Color(0xFF181612)
val ArchiveInkMuted = Color(0xFF575146)
val ArchiveInkFaint = Color(0xFF7E776A)

val ArchiveGoldDeep = Color(0xFFB37408)
val ArchiveGoldDim = Color(0xFF8C5904)
val ArchiveGoldSurface = Color(0xFFF7E7C4)
val ArchiveOnGold = Color(0xFFFFFFFF)

val ArchiveCrimson = Color(0xFFC62849)
val ArchiveCrimsonContainer = Color(0xFFFCE4E9)

val ArchiveEmerald = Color(0xFF0E8758)
val ArchiveEmeraldContainer = Color(0xFFD8F3E6)

val ArchiveIndigo = Color(0xFF4254B5)
val ArchiveIndigoContainer = Color(0xFFE5E9FA)

@Immutable
data class CinemaColors(
    val isDark: Boolean,
    val obsidian: Color,
    val velvet: Color,
    val slate: Color,
    val slateElevated: Color,
    val borderSubtle: Color,
    val gold: Color,
    val goldDim: Color,
    val goldContainer: Color,
    val onGold: Color,
    val crimson: Color,
    val crimsonContainer: Color,
    val emerald: Color,
    val emeraldContainer: Color,
    val indigo: Color,
    val indigoContainer: Color,
    val alabaster: Color,
    val silver: Color,
    val ash: Color,
    val scrimDark: Color = CinemaObsidian,
    val onScrimLight: Color = CinemaAlabaster,
    val onScrimMuted: Color = CinemaSilver
)

val DarkCinemaColors = CinemaColors(
    isDark = true,
    obsidian = CinemaObsidian,
    velvet = CinemaVelvet,
    slate = CinemaSlate,
    slateElevated = CinemaSlateElevated,
    borderSubtle = CinemaBorderSubtle,
    gold = CinemaGold,
    goldDim = CinemaGoldDim,
    goldContainer = CinemaGoldContainer,
    onGold = CinemaOnGold,
    crimson = CinemaCrimson,
    crimsonContainer = CinemaCrimsonContainer,
    emerald = CinemaEmerald,
    emeraldContainer = CinemaEmeraldContainer,
    indigo = CinemaIndigo,
    indigoContainer = CinemaIndigoContainer,
    alabaster = CinemaAlabaster,
    silver = CinemaSilver,
    ash = CinemaAsh
)

val LightArchiveColors = CinemaColors(
    isDark = false,
    obsidian = ArchivePaper,
    velvet = ArchiveVelvet,
    slate = ArchiveCard,
    slateElevated = ArchiveElevated,
    borderSubtle = ArchiveBorder,
    gold = ArchiveGoldDeep,
    goldDim = ArchiveGoldDim,
    goldContainer = ArchiveGoldSurface,
    onGold = ArchiveOnGold,
    crimson = ArchiveCrimson,
    crimsonContainer = ArchiveCrimsonContainer,
    emerald = ArchiveEmerald,
    emeraldContainer = ArchiveEmeraldContainer,
    indigo = ArchiveIndigo,
    indigoContainer = ArchiveIndigoContainer,
    alabaster = ArchiveInk,
    silver = ArchiveInkMuted,
    ash = ArchiveInkFaint
)
