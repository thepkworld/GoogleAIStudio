package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepository
import com.example.data.model.CatalogTitle
import com.example.data.model.CollectionDetail
import com.example.data.model.CollectionSummary
import com.example.data.model.DiscoverSort
import com.example.data.model.MediaType
import com.example.data.model.PersonDetail
import com.example.data.model.PersonSummary
import com.example.data.model.SearchScope
import com.example.data.model.SeasonDetail
import com.example.data.model.TmdbGenre
import com.example.data.model.TmdbSection
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.data.repository.VaultRepository
import com.example.data.tmdb.TmdbRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.flow.update
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
    val ratingBuckets: List<Int> = listOf(0, 0, 0, 0, 0),
    val topGenres: List<Pair<String, Int>> = emptyList()
)

data class BrowseUiState(
    val keyConfigured: Boolean = false,
    val isLiveTmdb: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val selectedSection: TmdbSection = TmdbSection.TRENDING_ALL,
    val discoverSort: DiscoverSort = DiscoverSort.POPULARITY,
    val searchScope: SearchScope = SearchScope.MULTI,
    val featuredTitle: CatalogTitle = CatalogRepository.featuredTitle(),
    val trendingTitles: List<CatalogTitle> = emptyList(),
    val acclaimedTitles: List<CatalogTitle> = CatalogRepository.allTitles().filter { it.criticScore >= 8.8 }.take(6),
    val trendingPeople: List<PersonSummary> = emptyList(),
    val sectionTitles: List<CatalogTitle> = CatalogRepository.allTitles(),
    val searchPeople: List<PersonSummary> = emptyList(),
    val searchCollections: List<CollectionSummary> = emptyList(),
    val movieGenres: List<TmdbGenre> = emptyList(),
    val tvGenres: List<TmdbGenre> = emptyList(),
    val selectedGenreId: Int? = null,
    val selectedGenreLabel: String = "All",
    val currentPage: Int = 1,
    val totalPages: Int = 1
) {
    val canLoadMore: Boolean
        get() = isLiveTmdb && currentPage < totalPages && !isLoading && !isLoadingMore
}

class VaultViewModel(
    private val repository: VaultRepository,
    private val tmdbRepository: TmdbRepository = TmdbRepository()
) : ViewModel() {

    private val localCatalogTitles: List<CatalogTitle> = CatalogRepository.allTitles()
    val localGenres: List<String> = CatalogRepository.allGenres

    // Browse filters
    private val _browseQuery = MutableStateFlow("")
    val browseQuery: StateFlow<String> = _browseQuery.asStateFlow()

    private val _browseMediaFilter = MutableStateFlow(MediaFilter.ALL)
    val browseMediaFilter: StateFlow<MediaFilter> = _browseMediaFilter.asStateFlow()

    private val _browseUiState = MutableStateFlow(
        BrowseUiState(keyConfigured = tmdbRepository.hasConfiguredApiKey())
    )
    val browseUiState: StateFlow<BrowseUiState> = _browseUiState.asStateFlow()

    // Detail states
    private val _detailTitle = MutableStateFlow<CatalogTitle?>(null)
    val detailTitle: StateFlow<CatalogTitle?> = _detailTitle.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _personDetail = MutableStateFlow<PersonDetail?>(null)
    val personDetail: StateFlow<PersonDetail?> = _personDetail.asStateFlow()

    private val _isLoadingPerson = MutableStateFlow(false)
    val isLoadingPerson: StateFlow<Boolean> = _isLoadingPerson.asStateFlow()

    private val _collectionDetail = MutableStateFlow<CollectionDetail?>(null)
    val collectionDetail: StateFlow<CollectionDetail?> = _collectionDetail.asStateFlow()

    private val _isLoadingCollection = MutableStateFlow(false)
    val isLoadingCollection: StateFlow<Boolean> = _isLoadingCollection.asStateFlow()

    private val _seasonDetail = MutableStateFlow<SeasonDetail?>(null)
    val seasonDetail: StateFlow<SeasonDetail?> = _seasonDetail.asStateFlow()

    private val _isLoadingSeason = MutableStateFlow(false)
    val isLoadingSeason: StateFlow<Boolean> = _isLoadingSeason.asStateFlow()

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

    private var searchJob: Job? = null
    private var sectionJob: Job? = null
    private var detailJob: Job? = null
    private var personJob: Job? = null
    private var collectionJob: Job? = null
    private var seasonJob: Job? = null

    init {
        if (tmdbRepository.hasConfiguredApiKey()) {
            loadInitialTmdbFeeds()
        } else {
            refreshOfflineFilteredBrowse()
        }
    }

    val vaultEntriesState: StateFlow<UiState<List<VaultEntry>>> =
        repository.observeVaultEntries()
            .map<List<VaultEntry>, UiState<List<VaultEntry>>> { entries ->
                UiState.Success(
                    entries.sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
                )
            }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Error observing Room vault entries", error)
                emit(UiState.Error(error.message ?: "Failed to load your local cinema vault"))
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
                    .thenByDescending { maxOf(it.updatedAt, it.createdAt) }
            )
            WatchlistSort.RECENT -> entries.sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
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
            WatchedSort.RECENT -> entries.sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
            WatchedSort.RATING -> entries.sortedWith(
                compareByDescending<VaultEntry> { it.userRating }
                    .thenByDescending { maxOf(it.updatedAt, it.createdAt) }
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
            .sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
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

    fun refreshTmdbFeeds() {
        if (!tmdbRepository.hasConfiguredApiKey()) {
            _snackbarEvents.tryEmit("Add your TMDB_API_KEY in AI Studio Secrets for live TMDB feeds.")
            refreshOfflineFilteredBrowse()
            return
        }
        loadInitialTmdbFeeds()
    }

    private fun loadInitialTmdbFeeds() {
        sectionJob?.cancel()
        sectionJob = viewModelScope.launch {
            _browseUiState.update { it.copy(isLoading = true, errorMessage = null, keyConfigured = true) }

            val genresDeferred = async { tmdbRepository.fetchGenres() }
            val currentSection = _browseUiState.value.selectedSection
            val mainDeferred = async { tmdbRepository.fetchSection(currentSection, page = 1) }
            val topMoviesDeferred = async { tmdbRepository.fetchSection(TmdbSection.MOVIE_TOP_RATED, page = 1) }
            val popularSeriesDeferred = async { tmdbRepository.fetchSection(TmdbSection.TV_POPULAR, page = 1) }
            val peopleDeferred = async { tmdbRepository.fetchTrendingPeople() }

            val genresResult = genresDeferred.await().getOrNull()
            val mainResult = mainDeferred.await()
            val topMovies = topMoviesDeferred.await().getOrNull()?.items.orEmpty()
            val popularSeries = popularSeriesDeferred.await().getOrNull()?.items.orEmpty()
            val trendingPeople = peopleDeferred.await().getOrNull().orEmpty()

            mainResult.onSuccess { pageRes ->
                val hero = pageRes.items.firstOrNull { it.backdropUrl.isNotBlank() }
                    ?: pageRes.items.firstOrNull()
                    ?: CatalogRepository.featuredTitle()
                val acclaimed = (topMovies.take(4) + popularSeries.take(4)).distinctBy { it.id }

                _browseUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isLiveTmdb = true,
                        errorMessage = null,
                        featuredTitle = hero,
                        trendingTitles = popularSeries.take(10),
                        acclaimedTitles = acclaimed.ifEmpty { state.acclaimedTitles },
                        trendingPeople = trendingPeople.take(15),
                        sectionTitles = filterByMediaType(pageRes.items, _browseMediaFilter.value),
                        movieGenres = genresResult?.first.orEmpty(),
                        tvGenres = genresResult?.second.orEmpty(),
                        currentPage = pageRes.page,
                        totalPages = pageRes.totalPages
                    )
                }
            }.onFailure { err ->
                Log.w(TAG, "TMDB initial load failed, using curated fallback", err)
                _browseUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isLiveTmdb = false,
                        errorMessage = "TMDB live sync unavailable (${err.localizedMessage ?: "check TMDB_API_KEY"}). Showing curated library."
                    )
                }
                refreshOfflineFilteredBrowse()
            }
        }
    }

    fun selectTmdbSection(section: TmdbSection) {
        _browseUiState.update {
            it.copy(
                selectedSection = section,
                selectedGenreId = null,
                selectedGenreLabel = "All",
                searchPeople = emptyList(),
                searchCollections = emptyList()
            )
        }
        val adjustedMedia = section.mediaHint?.let { hint ->
            if (hint == MediaType.MOVIE) MediaFilter.MOVIES else MediaFilter.SERIES
        } ?: MediaFilter.ALL
        _browseMediaFilter.value = adjustedMedia

        if (!tmdbRepository.hasConfiguredApiKey()) {
            refreshOfflineFilteredBrowse()
            return
        }

        if (_browseQuery.value.isNotBlank()) {
            triggerSearch(_browseQuery.value)
            return
        }

        sectionJob?.cancel()
        sectionJob = viewModelScope.launch {
            _browseUiState.update { it.copy(isLoading = true, errorMessage = null) }
            tmdbRepository.fetchSection(section, page = 1).onSuccess { res ->
                _browseUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isLiveTmdb = true,
                        featuredTitle = res.items.firstOrNull { it.backdropUrl.isNotBlank() } ?: state.featuredTitle,
                        sectionTitles = res.items,
                        currentPage = res.page,
                        totalPages = res.totalPages
                    )
                }
            }.onFailure { err ->
                _browseUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to load ${section.label}"
                    )
                }
            }
        }
    }

    fun selectGenre(genreId: Int?, genreLabel: String) {
        _browseUiState.update {
            it.copy(selectedGenreId = genreId, selectedGenreLabel = genreLabel)
        }

        if (!tmdbRepository.hasConfiguredApiKey() || !_browseUiState.value.isLiveTmdb) {
            refreshOfflineFilteredBrowse()
            return
        }

        if (genreId == null && _browseUiState.value.discoverSort == DiscoverSort.POPULARITY) {
            selectTmdbSection(_browseUiState.value.selectedSection)
            return
        }

        runDiscover()
    }

    fun selectDiscoverSort(sort: DiscoverSort) {
        _browseUiState.update { it.copy(discoverSort = sort) }
        if (!tmdbRepository.hasConfiguredApiKey() || !_browseUiState.value.isLiveTmdb) return
        runDiscover()
    }

    private fun runDiscover() {
        val state = _browseUiState.value
        sectionJob?.cancel()
        sectionJob = viewModelScope.launch {
            _browseUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val mediaType = when (_browseMediaFilter.value) {
                MediaFilter.SERIES -> MediaType.SERIES
                else -> state.selectedSection.mediaHint ?: MediaType.MOVIE
            }
            tmdbRepository.discover(
                mediaType = mediaType,
                genreId = state.selectedGenreId,
                sort = state.discoverSort,
                page = 1
            ).onSuccess { res ->
                _browseUiState.update { current ->
                    current.copy(
                        isLoading = false,
                        sectionTitles = res.items,
                        currentPage = res.page,
                        totalPages = res.totalPages
                    )
                }
            }.onFailure { err ->
                _browseUiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Discover query failed"
                    )
                }
            }
        }
    }

    fun selectSearchScope(scope: SearchScope) {
        _browseUiState.update { it.copy(searchScope = scope) }
        if (_browseQuery.value.isNotBlank() && tmdbRepository.hasConfiguredApiKey()) {
            triggerSearch(_browseQuery.value)
        }
    }

    fun updateBrowseQuery(query: String) {
        _browseQuery.value = query
        if (!tmdbRepository.hasConfiguredApiKey()) {
            refreshOfflineFilteredBrowse()
            return
        }
        triggerSearch(query)
    }

    private fun triggerSearch(query: String) {
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _browseUiState.update { it.copy(searchPeople = emptyList(), searchCollections = emptyList()) }
            val state = _browseUiState.value
            if (state.selectedGenreId != null) {
                selectGenre(state.selectedGenreId, state.selectedGenreLabel)
            } else {
                selectTmdbSection(state.selectedSection)
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            _browseUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val activeScope = _browseUiState.value.searchScope
            tmdbRepository.search(trimmed, activeScope, page = 1).onSuccess { bundle ->
                _browseUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isLiveTmdb = true,
                        sectionTitles = bundle.titles,
                        searchPeople = bundle.people,
                        searchCollections = bundle.collections,
                        currentPage = bundle.page,
                        totalPages = bundle.totalPages
                    )
                }
            }.onFailure { err ->
                _browseUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Search failed"
                    )
                }
            }
        }
    }

    fun updateBrowseMediaFilter(filter: MediaFilter) {
        _browseMediaFilter.value = filter
        if (!tmdbRepository.hasConfiguredApiKey() || !_browseUiState.value.isLiveTmdb) {
            refreshOfflineFilteredBrowse()
            return
        }
        if (_browseQuery.value.isNotBlank()) {
            val nextScope = when (filter) {
                MediaFilter.ALL -> SearchScope.MULTI
                MediaFilter.MOVIES -> SearchScope.MOVIES
                MediaFilter.SERIES -> SearchScope.SERIES
            }
            selectSearchScope(nextScope)
        } else {
            val targetSection = when (filter) {
                MediaFilter.ALL -> TmdbSection.TRENDING_ALL
                MediaFilter.MOVIES -> if (_browseUiState.value.selectedSection.mediaHint == MediaType.MOVIE) {
                    _browseUiState.value.selectedSection
                } else TmdbSection.MOVIE_POPULAR
                MediaFilter.SERIES -> if (_browseUiState.value.selectedSection.mediaHint == MediaType.SERIES) {
                    _browseUiState.value.selectedSection
                } else TmdbSection.TV_POPULAR
            }
            selectTmdbSection(targetSection)
        }
    }

    fun loadNextPage() {
        val state = _browseUiState.value
        if (!state.canLoadMore) return
        val nextPage = state.currentPage + 1

        viewModelScope.launch {
            _browseUiState.update { it.copy(isLoadingMore = true) }
            val query = _browseQuery.value.trim()
            if (query.isNotEmpty()) {
                tmdbRepository.search(query, state.searchScope, page = nextPage).onSuccess { bundle ->
                    _browseUiState.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            sectionTitles = (current.sectionTitles + bundle.titles).distinctBy { it.id },
                            searchPeople = (current.searchPeople + bundle.people).distinctBy { it.id },
                            searchCollections = (current.searchCollections + bundle.collections).distinctBy { it.id },
                            currentPage = bundle.page,
                            totalPages = bundle.totalPages
                        )
                    }
                }.onFailure { err ->
                    _browseUiState.update { it.copy(isLoadingMore = false) }
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Unable to load next page")
                }
                return@launch
            }

            val result = if (state.selectedGenreId != null || state.discoverSort != DiscoverSort.POPULARITY) {
                val mediaType = when (_browseMediaFilter.value) {
                    MediaFilter.SERIES -> MediaType.SERIES
                    else -> state.selectedSection.mediaHint ?: MediaType.MOVIE
                }
                tmdbRepository.discover(
                    mediaType = mediaType,
                    genreId = state.selectedGenreId,
                    sort = state.discoverSort,
                    page = nextPage
                )
            } else {
                tmdbRepository.fetchSection(state.selectedSection, page = nextPage)
            }

            result.onSuccess { res ->
                _browseUiState.update { current ->
                    current.copy(
                        isLoadingMore = false,
                        sectionTitles = (current.sectionTitles + res.items).distinctBy { it.id },
                        currentPage = res.page,
                        totalPages = res.totalPages
                    )
                }
            }.onFailure { err ->
                _browseUiState.update { it.copy(isLoadingMore = false) }
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Unable to load next page")
            }
        }
    }

    private fun refreshOfflineFilteredBrowse() {
        val query = _browseQuery.value.trim().lowercase()
        val mediaFilter = _browseMediaFilter.value
        val genre = _browseUiState.value.selectedGenreLabel

        val filtered = localCatalogTitles.filter { item ->
            val matchesMedia = when (mediaFilter) {
                MediaFilter.ALL -> true
                MediaFilter.MOVIES -> item.mediaType == MediaType.MOVIE
                MediaFilter.SERIES -> item.mediaType == MediaType.SERIES
            }
            val matchesGenre = genre == "All" || item.genres.any { it.equals(genre, ignoreCase = true) }
            val matchesQuery = query.isEmpty() ||
                item.title.lowercase().contains(query) ||
                item.directorOrCreator.lowercase().contains(query) ||
                item.genres.any { it.lowercase().contains(query) } ||
                item.cast.any { it.actor.lowercase().contains(query) }
            matchesMedia && matchesGenre && matchesQuery
        }

        _browseUiState.update { state ->
            state.copy(
                sectionTitles = filtered,
                currentPage = 1,
                totalPages = 1
            )
        }
    }

    fun resolveCatalogTitle(titleId: String): CatalogTitle? {
        return CatalogRepository.findById(titleId)
            ?: tmdbRepository.findCachedById(titleId)
    }

    fun loadTitleDetails(titleId: String) {
        val immediate = resolveCatalogTitle(titleId)
        _detailTitle.value = immediate

        if (!tmdbRepository.hasConfiguredApiKey() || TmdbRepository.parseTmdbId(titleId) == null) {
            _isLoadingDetail.value = false
            return
        }

        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _isLoadingDetail.value = true
            tmdbRepository.fetchFullDetails(titleId)
                .onSuccess { detailed ->
                    _detailTitle.value = detailed
                }
                .onFailure { err ->
                    Log.w(TAG, "Failed to fetch full TMDB details for $titleId", err)
                }
            _isLoadingDetail.value = false
        }
    }

    fun loadPersonDetails(personId: Int) {
        personJob?.cancel()
        personJob = viewModelScope.launch {
            _isLoadingPerson.value = true
            _personDetail.value = null
            tmdbRepository.fetchPersonDetails(personId)
                .onSuccess { detail -> _personDetail.value = detail }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Failed to load person profile")
                }
            _isLoadingPerson.value = false
        }
    }

    fun loadCollectionDetails(collectionId: Int) {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            _isLoadingCollection.value = true
            _collectionDetail.value = null
            tmdbRepository.fetchCollectionDetails(collectionId)
                .onSuccess { detail -> _collectionDetail.value = detail }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Failed to load collection")
                }
            _isLoadingCollection.value = false
        }
    }

    fun loadSeasonDetails(seriesId: Int, seasonNumber: Int) {
        seasonJob?.cancel()
        seasonJob = viewModelScope.launch {
            _isLoadingSeason.value = true
            _seasonDetail.value = null
            tmdbRepository.fetchSeasonDetails(seriesId, seasonNumber)
                .onSuccess { detail -> _seasonDetail.value = detail }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Failed to load season episodes")
                }
            _isLoadingSeason.value = false
        }
    }

    fun clearSeasonDetail() {
        _seasonDetail.value = null
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

    private fun filterByMediaType(items: List<CatalogTitle>, filter: MediaFilter): List<CatalogTitle> {
        return when (filter) {
            MediaFilter.ALL -> items
            MediaFilter.MOVIES -> items.filter { it.mediaType == MediaType.MOVIE }
            MediaFilter.SERIES -> items.filter { it.mediaType == MediaType.SERIES }
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
