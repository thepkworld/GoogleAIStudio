package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepository
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.data.repository.VaultRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class MediaFilter(val label: String) {
    ALL("All"),
    MOVIES("Movies"),
    SERIES("Series")
}

enum class WatchlistSort(val label: String) {
    PRIORITY("Up Next First"),
    RECENT("Recently Added"),
    YEAR("Release Year"),
    TITLE("Title A–Z")
}

enum class WatchedSort(val label: String) {
    RECENT("Recently Logged"),
    RATING("Highest Rated"),
    YEAR("Release Year"),
    TITLE("Title A–Z")
}

data class VaultStats(
    val totalWatched: Int = 0,
    val watchedMovies: Int = 0,
    val watchedSeries: Int = 0,
    val totalWatchlist: Int = 0,
    val priorityWatchlist: Int = 0,
    val averageRating: Double = 0.0,
    val ratedCount: Int = 0,
    val totalReviews: Int = 0,
    val ratingBuckets: List<Int> = listOf(0, 0, 0, 0, 0), // 1-2, 3-4, 5-6, 7-8, 9-10
    val topGenres: List<Pair<String, Int>> = emptyList()
)

class VaultViewModel(
    private val repository: VaultRepository,
    private val currentUserId: String
) : ViewModel() {

    val catalogTitles: List<CatalogTitle> = CatalogRepository.allTitles()
    val featuredTitle: CatalogTitle = CatalogRepository.featuredTitle()
    val allGenres: List<String> = CatalogRepository.allGenres

    // Browse filters
    private val _browseQuery = MutableStateFlow("")
    val browseQuery: StateFlow<String> = _browseQuery.asStateFlow()

    private val _browseMediaFilter = MutableStateFlow(MediaFilter.ALL)
    val browseMediaFilter: StateFlow<MediaFilter> = _browseMediaFilter.asStateFlow()

    private val _browseGenre = MutableStateFlow("All")
    val browseGenre: StateFlow<String> = _browseGenre.asStateFlow()

    // Watchlist filters
    private val _watchlistFilter = MutableStateFlow(MediaFilter.ALL)
    val watchlistFilter: StateFlow<MediaFilter> = _watchlistFilter.asStateFlow()

    private val _watchlistSort = MutableStateFlow(WatchlistSort.PRIORITY)
    val watchlistSort: StateFlow<WatchlistSort> = _watchlistSort.asStateFlow()

    // Watched filters
    private val _watchedFilter = MutableStateFlow(MediaFilter.ALL)
    val watchedFilter: StateFlow<MediaFilter> = _watchedFilter.asStateFlow()

    private val _watchedSort = MutableStateFlow(WatchedSort.RECENT)
    val watchedSort: StateFlow<WatchedSort> = _watchedSort.asStateFlow()

    // Reviews filter
    private val _reviewsFilter = MutableStateFlow(MediaFilter.ALL)
    val reviewsFilter: StateFlow<MediaFilter> = _reviewsFilter.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    private val _isMutating = MutableStateFlow(false)
    val isMutating: StateFlow<Boolean> = _isMutating.asStateFlow()

    val vaultEntriesState: StateFlow<UiState<List<VaultEntry>>> =
        repository.observeVaultEntries(currentUserId)
            .map<List<VaultEntry>, UiState<List<VaultEntry>>> { entries ->
                UiState.Success(
                    entries.sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing vault entries", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to sync your cinema vault"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val vaultById: StateFlow<Map<String, VaultEntry>> = vaultEntriesState
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.associateBy { it.id }
                else -> emptyMap()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyMap()
        )

    val filteredCatalog: StateFlow<List<CatalogTitle>> = combine(
        _browseQuery,
        _browseMediaFilter,
        _browseGenre
    ) { query, mediaFilter, genre ->
        val normalizedQuery = query.trim().lowercase()
        catalogTitles.filter { item ->
            val matchesMedia = when (mediaFilter) {
                MediaFilter.ALL -> true
                MediaFilter.MOVIES -> item.mediaType == MediaType.MOVIE
                MediaFilter.SERIES -> item.mediaType == MediaType.SERIES
            }
            val matchesGenre = genre == "All" || item.genres.any { it.equals(genre, ignoreCase = true) }
            val matchesQuery = normalizedQuery.isEmpty() ||
                item.title.lowercase().contains(normalizedQuery) ||
                item.directorOrCreator.lowercase().contains(normalizedQuery) ||
                item.genres.any { it.lowercase().contains(normalizedQuery) } ||
                item.cast.any { it.actor.lowercase().contains(normalizedQuery) }
            matchesMedia && matchesGenre && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = catalogTitles
    )

    val watchlistEntries: StateFlow<List<VaultEntry>> = combine(
        vaultEntriesState,
        _watchlistFilter,
        _watchlistSort
    ) { state, filter, sort ->
        val entries = (state as? UiState.Success)?.data.orEmpty()
            .filter { it.statusEnum == WatchStatus.WANT_TO_WATCH }
            .filterByMedia(filter)
        when (sort) {
            WatchlistSort.PRIORITY -> entries.sortedWith(
                compareByDescending<VaultEntry> { it.priority }
                    .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            )
            WatchlistSort.RECENT -> entries.sortedByDescending {
                it.updatedAt ?: it.createdAt ?: Timestamp(0, 0)
            }
            WatchlistSort.YEAR -> entries.sortedByDescending { it.releaseYear }
            WatchlistSort.TITLE -> entries.sortedBy { it.title.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    val watchedEntries: StateFlow<List<VaultEntry>> = combine(
        vaultEntriesState,
        _watchedFilter,
        _watchedSort
    ) { state, filter, sort ->
        val entries = (state as? UiState.Success)?.data.orEmpty()
            .filter { it.statusEnum == WatchStatus.WATCHED }
            .filterByMedia(filter)
        when (sort) {
            WatchedSort.RECENT -> entries.sortedByDescending {
                it.updatedAt ?: it.createdAt ?: Timestamp(0, 0)
            }
            WatchedSort.RATING -> entries.sortedWith(
                compareByDescending<VaultEntry> { it.userRating }
                    .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            )
            WatchedSort.YEAR -> entries.sortedByDescending { it.releaseYear }
            WatchedSort.TITLE -> entries.sortedBy { it.title.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    val reviewedEntries: StateFlow<List<VaultEntry>> = combine(
        vaultEntriesState,
        _reviewsFilter
    ) { state, filter ->
        (state as? UiState.Success)?.data.orEmpty()
            .filter { it.hasReview || it.userRating > 0.0 }
            .filterByMedia(filter)
            .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    val vaultStats: StateFlow<VaultStats> = vaultEntriesState
        .map { state ->
            val all = (state as? UiState.Success)?.data.orEmpty()
            val watched = all.filter { it.statusEnum == WatchStatus.WATCHED }
            val watchlist = all.filter { it.statusEnum == WatchStatus.WANT_TO_WATCH }
            val rated = all.filter { it.userRating > 0.0 }
            val avg = if (rated.isNotEmpty()) rated.sumOf { it.userRating } / rated.size else 0.0
            val buckets = IntArray(5)
            rated.forEach { entry ->
                val idx = when {
                    entry.userRating <= 2.5 -> 0
                    entry.userRating <= 4.5 -> 1
                    entry.userRating <= 6.5 -> 2
                    entry.userRating <= 8.5 -> 3
                    else -> 4
                }
                buckets[idx]++
            }
            val genreCounts = mutableMapOf<String, Int>()
            watched.forEach { entry ->
                entry.genres.forEach { g ->
                    genreCounts[g] = (genreCounts[g] ?: 0) + 1
                }
            }
            VaultStats(
                totalWatched = watched.size,
                watchedMovies = watched.count { it.mediaTypeEnum == MediaType.MOVIE },
                watchedSeries = watched.count { it.mediaTypeEnum == MediaType.SERIES },
                totalWatchlist = watchlist.size,
                priorityWatchlist = watchlist.count { it.priority },
                averageRating = avg,
                ratedCount = rated.size,
                totalReviews = all.count { it.hasReview },
                ratingBuckets = buckets.toList(),
                topGenres = genreCounts.entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { it.key to it.value }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = VaultStats()
        )

    fun updateBrowseQuery(query: String) {
        _browseQuery.value = query
    }

    fun updateBrowseMediaFilter(filter: MediaFilter) {
        _browseMediaFilter.value = filter
    }

    fun updateBrowseGenre(genre: String) {
        _browseGenre.value = genre
    }

    fun updateWatchlistFilter(filter: MediaFilter) {
        _watchlistFilter.value = filter
    }

    fun updateWatchlistSort(sort: WatchlistSort) {
        _watchlistSort.value = sort
    }

    fun updateWatchedFilter(filter: MediaFilter) {
        _watchedFilter.value = filter
    }

    fun updateWatchedSort(sort: WatchedSort) {
        _watchedSort.value = sort
    }

    fun updateReviewsFilter(filter: MediaFilter) {
        _reviewsFilter.value = filter
    }

    fun addOrMoveCatalogTitle(
        catalogTitle: CatalogTitle,
        targetStatus: WatchStatus,
        isPriority: Boolean = false
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.upsertFromCatalog(
                catalogTitle = catalogTitle,
                targetStatus = targetStatus,
                isPriority = isPriority
            )
            _isMutating.value = false
            result.onSuccess {
                val msg = if (targetStatus == WatchStatus.WANT_TO_WATCH) {
                    "Added \"${catalogTitle.title}\" to Want to Watch"
                } else {
                    "Marked \"${catalogTitle.title}\" as Watched"
                }
                _snackbarEvents.tryEmit(msg)
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to update your vault")
            }
        }
    }

    fun togglePriority(entry: VaultEntry) {
        viewModelScope.launch {
            val nextPriority = !entry.priority
            val result = repository.updateStatusAndProgress(
                entryId = entry.id,
                status = entry.statusEnum,
                isPriority = nextPriority,
                episodesWatched = entry.episodesWatched,
                rewatchCount = entry.rewatchCount
            )
            result.onSuccess {
                _snackbarEvents.tryEmit(
                    if (nextPriority) "Pinned \"${entry.title}\" to Up Next"
                    else "Removed Up Next pin"
                )
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Unable to update priority")
            }
        }
    }

    fun moveToWatched(entry: VaultEntry) {
        viewModelScope.launch {
            val nextEpisodes = if (entry.mediaTypeEnum == MediaType.SERIES && entry.totalEpisodes > 0) {
                entry.totalEpisodes
            } else {
                entry.episodesWatched
            }
            val result = repository.updateStatusAndProgress(
                entryId = entry.id,
                status = WatchStatus.WATCHED,
                isPriority = false,
                episodesWatched = nextEpisodes,
                rewatchCount = entry.rewatchCount
            )
            result.onSuccess {
                _snackbarEvents.tryEmit("Moved \"${entry.title}\" to Watched")
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to mark watched")
            }
        }
    }

    fun moveToWatchlist(entry: VaultEntry) {
        viewModelScope.launch {
            val result = repository.updateStatusAndProgress(
                entryId = entry.id,
                status = WatchStatus.WANT_TO_WATCH,
                isPriority = entry.priority,
                episodesWatched = entry.episodesWatched,
                rewatchCount = entry.rewatchCount
            )
            result.onSuccess {
                _snackbarEvents.tryEmit("Moved \"${entry.title}\" to Want to Watch")
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to move to watchlist")
            }
        }
    }

    fun updateEpisodeProgress(entry: VaultEntry, newEpisodesWatched: Long) {
        viewModelScope.launch {
            val clamped = newEpisodesWatched.coerceIn(0L, maxOf(entry.totalEpisodes, 5000L))
            val result = repository.updateStatusAndProgress(
                entryId = entry.id,
                status = entry.statusEnum,
                isPriority = entry.priority,
                episodesWatched = clamped,
                rewatchCount = entry.rewatchCount
            )
            result.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to update episode progress")
            }
        }
    }

    fun updateRewatchCount(entry: VaultEntry, newRewatchCount: Long) {
        viewModelScope.launch {
            val result = repository.updateStatusAndProgress(
                entryId = entry.id,
                status = entry.statusEnum,
                isPriority = entry.priority,
                episodesWatched = entry.episodesWatched,
                rewatchCount = newRewatchCount.coerceIn(0L, 1000L)
            )
            result.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to update rewatch count")
            }
        }
    }

    fun saveReviewAndRating(
        catalogTitle: CatalogTitle?,
        existingEntry: VaultEntry?,
        entryId: String,
        status: WatchStatus,
        userRating: Double,
        reviewTitle: String,
        reviewBody: String,
        favoriteQuote: String,
        containsSpoilers: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.saveReviewAndRating(
                catalogTitle = catalogTitle,
                existingEntry = existingEntry,
                entryId = entryId,
                status = status,
                userRating = userRating,
                reviewTitle = reviewTitle,
                reviewBody = reviewBody,
                favoriteQuote = favoriteQuote,
                containsSpoilers = containsSpoilers
            )
            _isMutating.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("Saved rating & review")
                onComplete()
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to save review")
            }
        }
    }

    fun createCustomTitle(
        title: String,
        mediaType: MediaType,
        status: WatchStatus,
        releaseYear: Int,
        genres: List<String>,
        directorOrCreator: String,
        runtimeOrSeasons: String,
        synopsis: String,
        posterKey: String,
        totalEpisodes: Int,
        userRating: Double,
        reviewTitle: String,
        reviewBody: String,
        favoriteQuote: String,
        isPriority: Boolean,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.createCustomTitle(
                title = title,
                mediaType = mediaType,
                status = status,
                releaseYear = releaseYear,
                genres = genres,
                directorOrCreator = directorOrCreator,
                runtimeOrSeasons = runtimeOrSeasons,
                synopsis = synopsis,
                posterKey = posterKey,
                totalEpisodes = totalEpisodes,
                userRating = userRating,
                reviewTitle = reviewTitle,
                reviewBody = reviewBody,
                favoriteQuote = favoriteQuote,
                isPriority = isPriority
            )
            _isMutating.value = false
            result.onSuccess { id ->
                _snackbarEvents.tryEmit("Added \"$title\" to your Vault")
                onCreated(id)
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to add title")
            }
        }
    }

    fun deleteEntry(entry: VaultEntry, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.deleteEntry(entry.id)
            _isMutating.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("Removed \"${entry.title}\" from your Vault")
                onDeleted()
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to remove entry")
            }
        }
    }

    private fun List<VaultEntry>.filterByMedia(filter: MediaFilter): List<VaultEntry> {
        return when (filter) {
            MediaFilter.ALL -> this
            MediaFilter.MOVIES -> filter { it.mediaTypeEnum == MediaType.MOVIE }
            MediaFilter.SERIES -> filter { it.mediaTypeEnum == MediaType.SERIES }
        }
    }

    private companion object {
        const val TAG = "VaultViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
