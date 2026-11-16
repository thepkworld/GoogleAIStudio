package com.example.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthCancelledException
import com.example.auth.AuthSessionManager
import com.example.auth.AuthUserSummary
import com.example.data.catalog.CatalogRepository
import com.example.data.model.CatalogTitle
import com.example.data.model.CatalogViewMode
import com.example.data.model.CollectionDetail
import com.example.data.model.CollectionSummary
import com.example.data.model.DataSourceOrigin
import com.example.data.model.DiscoverSort
import com.example.data.model.MediaType
import com.example.data.model.OttPlatform
import com.example.data.model.PersonDetail
import com.example.data.model.PersonSummary
import com.example.data.model.RatingScale
import com.example.data.model.SearchScope
import com.example.data.model.SeasonDetail
import com.example.data.model.TmdbGenre
import com.example.data.model.TmdbSection
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.data.omdb.OmdbRepository
import com.example.data.portability.VaultPortabilityManager
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.preferences.AppPreferencesState
import com.example.data.remote.VaultCloudRepository
import com.example.data.repository.UnifiedMediaService
import com.example.data.repository.VaultRepository
import com.example.data.tmdb.TmdbRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
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

enum class ApiConnectionStatus(val label: String) {
    TMDB_OMDB_LIVE("TMDB + OMDb"),
    OMDB_FALLBACK("OMDb Fallback"),
    OFFLINE("Offline Curated")
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
    RECENT("Recently Watched"),
    RATING("Highest Rated"),
    LOWEST_RATING("Lowest Rated"),
    YEAR("Release Year"),
    TITLE("Title A–Z")
}

enum class WatchedTimeFilter(val label: String) {
    ALL_TIME("All Time"),
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_YEAR("This Year")
}

enum class WatchedRatingFilter(val label: String) {
    ALL("All Ratings"),
    TOP_TIER("9–10 Masterpiece"),
    GREAT("7–8 Great"),
    MID("5–6 Good/Avg"),
    LOW("1–4 Low Rated"),
    REVIEWED("Has Review"),
    UNRATED("Unrated")
}

data class VaultStats(
    val totalWatched: Int = 0,
    val watchedMovies: Int = 0,
    val watchedSeries: Int = 0,
    val watchedThisMonth: Int = 0,
    val watchedMoviesThisMonth: Int = 0,
    val watchedSeriesThisMonth: Int = 0,
    val currentMonthLabel: String = "",
    val totalWatchlist: Int = 0,
    val watchlistMovies: Int = 0,
    val watchlistSeries: Int = 0,
    val priorityWatchlist: Int = 0,
    val averageRating: Double = 0.0,
    val ratedCount: Int = 0,
    val totalReviews: Int = 0,
    val ratingBuckets: List<Int> = listOf(0, 0, 0, 0, 0),
    val topGenres: List<Pair<String, Int>> = emptyList()
)

data class GenreStat(
    val name: String,
    val count: Int,
    val averageRating: Double
)

data class ActivityBucket(
    val label: String,
    val moviesCount: Int,
    val seriesCount: Int
) {
    val total: Int get() = moviesCount + seriesCount
}

data class DetailedStats(
    val totalWatched: Int = 0,
    val watchedMovies: Int = 0,
    val watchedSeries: Int = 0,
    val watchedThisMonth: Int = 0,
    val watchedThisYear: Int = 0,
    val currentMonthLabel: String = "",
    val totalBucketlist: Int = 0,
    val bucketlistMovies: Int = 0,
    val bucketlistSeries: Int = 0,
    val priorityBucketlist: Int = 0,
    val overallCompletionRate: Float = 0f,
    val moviesCompletionRate: Float = 0f,
    val seriesCompletionRate: Float = 0f,
    val totalHoursWatched: Double = 0.0,
    val movieHoursWatched: Double = 0.0,
    val seriesHoursWatched: Double = 0.0,
    val totalEpisodesWatched: Long = 0L,
    val totalEpisodesTarget: Long = 0L,
    val episodeCompletionRate: Float = 0f,
    val averageRating: Double = 0.0,
    val ratedCount: Int = 0,
    val masterpiecesCount: Int = 0,
    val totalReviews: Int = 0,
    val totalRewatches: Int = 0,
    val ratingDistribution1To10: List<Int> = List(10) { 0 },
    val genreBreakdown: List<GenreStat> = emptyList(),
    val topRatedGenres: List<GenreStat> = emptyList(),
    val decadeBreakdown: List<Pair<String, Int>> = emptyList(),
    val languageBreakdown: List<Pair<String, Int>> = emptyList(),
    val platformBreakdown: List<Pair<String, Int>> = emptyList(),
    val monthlyActivity: List<ActivityBucket> = emptyList(),
    val annualActivity: List<ActivityBucket> = emptyList(),
    val topCreators: List<Pair<String, Int>> = emptyList()
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoadingOtt: Boolean = false,
    val isLiveTmdb: Boolean = false,
    val isLiveOmdb: Boolean = false,
    val selectedOttPlatform: OttPlatform = OttPlatform.ALL,
    val featuredTitle: CatalogTitle = CatalogRepository.featuredTitle(),
    val trendingNow: List<CatalogTitle> = CatalogRepository.trendingFallback(),
    val bollywoodMovies: List<CatalogTitle> = CatalogRepository.bollywoodFallback(),
    val hollywoodMovies: List<CatalogTitle> = CatalogRepository.hollywoodFallback(),
    val upcomingMovies: List<CatalogTitle> = CatalogRepository.upcomingFallback(),
    val netflixPopular: List<CatalogTitle> = CatalogRepository.netflixFallback(),
    val primePopular: List<CatalogTitle> = CatalogRepository.primeFallback(),
    val ottNewMovies: List<CatalogTitle> = CatalogRepository.ottNewFallback(),
    val topRatedMovies: List<CatalogTitle> = CatalogRepository.famousFallback(),
    val mostAnticipatedMovies: List<CatalogTitle> = CatalogRepository.upcomingFallback(),
    val classicMovies: List<CatalogTitle> = CatalogRepository.classicFallback(),
    val trendingPeople: List<PersonSummary> = emptyList()
)

data class BrowseUiState(
    val keyConfigured: Boolean = false,
    val isLiveTmdb: Boolean = false,
    val isLiveOmdb: Boolean = false,
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
    val selectedGenres: List<String> = emptyList(),
    val selectedGenreIds: List<Int> = emptyList(),
    val viewMode: CatalogViewMode = CatalogViewMode.GRID,
    val currentPage: Int = 1,
    val totalPages: Int = 1
) {
    val canLoadMore: Boolean
        get() = (isLiveTmdb || isLiveOmdb) && currentPage < totalPages && !isLoading && !isLoadingMore
}

class VaultViewModel(
    private val repository: VaultRepository,
    private val tmdbRepository: TmdbRepository = TmdbRepository(),
    private val omdbRepository: OmdbRepository = OmdbRepository(),
    private val cloudRepository: VaultCloudRepository? = null,
    private val preferencesRepository: AppPreferencesRepository? = null,
    private val authSessionManager: AuthSessionManager? = null
) : ViewModel() {

    private val unifiedMediaService = UnifiedMediaService(
        tmdbRepository = tmdbRepository,
        omdbRepository = omdbRepository
    )

    private val localCatalogTitles: List<CatalogTitle> = CatalogRepository.allTitles()
    private var omdbBrowsePool: List<CatalogTitle> = emptyList()
    private var omdbSearchResultsPool: List<CatalogTitle> = emptyList()
    private var lastOmdbSearchQuery: String = ""
    val localGenres: List<String> = CatalogRepository.allGenres

    // Browse filters
    private val _browseQuery = MutableStateFlow("")
    val browseQuery: StateFlow<String> = _browseQuery.asStateFlow()

    private val _browseMediaFilter = MutableStateFlow(MediaFilter.ALL)
    val browseMediaFilter: StateFlow<MediaFilter> = _browseMediaFilter.asStateFlow()

    private val _browseUiState = MutableStateFlow(
        BrowseUiState(keyConfigured = tmdbRepository.hasConfiguredApiKey() || omdbRepository.hasConfiguredApiKey())
    )
    val browseUiState: StateFlow<BrowseUiState> = _browseUiState.asStateFlow()

    // Detail states
    private val _detailTitle = MutableStateFlow<CatalogTitle?>(null)
    val detailTitle: StateFlow<CatalogTitle?> = _detailTitle.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

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

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    // Watchlist (BucketList) filters
    private val _watchlistFilter = MutableStateFlow(MediaFilter.ALL)
    val watchlistFilter: StateFlow<MediaFilter> = _watchlistFilter.asStateFlow()

    private val _watchlistSort = MutableStateFlow(WatchlistSort.PRIORITY)
    val watchlistSort: StateFlow<WatchlistSort> = _watchlistSort.asStateFlow()

    private val _watchlistViewMode = MutableStateFlow(CatalogViewMode.LIST)
    val watchlistViewMode: StateFlow<CatalogViewMode> = _watchlistViewMode.asStateFlow()

    // Library (Watched) filters
    private val _watchedSearchQuery = MutableStateFlow("")
    val watchedSearchQuery: StateFlow<String> = _watchedSearchQuery.asStateFlow()

    private val _watchedFilter = MutableStateFlow(MediaFilter.ALL)
    val watchedFilter: StateFlow<MediaFilter> = _watchedFilter.asStateFlow()

    private val _watchedTimeFilter = MutableStateFlow(WatchedTimeFilter.ALL_TIME)
    val watchedTimeFilter: StateFlow<WatchedTimeFilter> = _watchedTimeFilter.asStateFlow()

    private val _watchedRatingFilter = MutableStateFlow(WatchedRatingFilter.ALL)
    val watchedRatingFilter: StateFlow<WatchedRatingFilter> = _watchedRatingFilter.asStateFlow()

    private val _watchedGenreFilter = MutableStateFlow("All")
    val watchedGenreFilter: StateFlow<String> = _watchedGenreFilter.asStateFlow()

    private val _watchedSort = MutableStateFlow(WatchedSort.RECENT)
    val watchedSort: StateFlow<WatchedSort> = _watchedSort.asStateFlow()

    // Reviews filter
    private val _reviewsFilter = MutableStateFlow(MediaFilter.ALL)
    val reviewsFilter: StateFlow<MediaFilter> = _reviewsFilter.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    private val _isMutating = MutableStateFlow(false)
    val isMutating: StateFlow<Boolean> = _isMutating.asStateFlow()

    // Preferences, Onboarding & Firebase Auth/Sync state
    private val _preferencesLoaded = MutableStateFlow(preferencesRepository == null)
    val preferencesLoaded: StateFlow<Boolean> = _preferencesLoaded.asStateFlow()

    private val _preferencesState = MutableStateFlow(
        if (preferencesRepository == null) {
            AppPreferencesState(isDarkTheme = true, hasCompletedOnboarding = true)
        } else {
            AppPreferencesState()
        }
    )
    val preferencesState: StateFlow<AppPreferencesState> = _preferencesState.asStateFlow()

    private val _authUser = MutableStateFlow(authSessionManager?.currentUserSummary())
    val authUser: StateFlow<AuthUserSummary?> = _authUser.asStateFlow()

    private val _isAuthBusy = MutableStateFlow(false)
    val isAuthBusy: StateFlow<Boolean> = _isAuthBusy.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    private val _pendingGuestMergeCount = MutableStateFlow(0)
    val pendingGuestMergeCount: StateFlow<Int> = _pendingGuestMergeCount.asStateFlow()

    val apiConnectionStatus: StateFlow<ApiConnectionStatus> = combine(
        _homeUiState,
        _browseUiState
    ) { home, browse ->
        when {
            home.isLiveTmdb || browse.isLiveTmdb -> ApiConnectionStatus.TMDB_OMDB_LIVE
            home.isLiveOmdb || browse.isLiveOmdb -> ApiConnectionStatus.OMDB_FALLBACK
            else -> ApiConnectionStatus.OFFLINE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ApiConnectionStatus.OFFLINE
    )

    private var searchJob: Job? = null
    private var sectionJob: Job? = null
    private var ottJob: Job? = null
    private var detailJob: Job? = null
    private var personJob: Job? = null
    private var collectionJob: Job? = null
    private var seasonJob: Job? = null
    private var cloudSyncJob: Job? = null

    init {
        if (preferencesRepository != null) {
            viewModelScope.launch {
                preferencesRepository.preferencesFlow.collect { state ->
                    _preferencesState.value = state
                    _preferencesLoaded.value = true
                }
            }
        }
        if (authSessionManager != null) {
            viewModelScope.launch {
                authSessionManager.observeAuthUser().collect { user ->
                    _authUser.value = user
                    cloudSyncJob?.cancel()
                    if (user != null) {
                        preferencesRepository?.setOnboardingCompleted(completed = true, isGuest = false)
                        startCloudSyncForUser(user.uid)
                    } else {
                        _pendingGuestMergeCount.value = 0
                    }
                }
            }
        }

        if (tmdbRepository.hasConfiguredApiKey()) {
            loadInitialTmdbFeeds()
        } else if (omdbRepository.hasConfiguredApiKey()) {
            loadOmdbFallbackFeeds(noticeIfFailed = null)
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

    val watchedAvailableGenres: StateFlow<List<String>> = vaultEntriesState
        .map { state ->
            val watched = (state as? UiState.Success)?.data.orEmpty()
                .filter { it.statusEnum == WatchStatus.WATCHED }
            listOf("All") + watched.flatMap { it.genres }.distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = listOf("All")
        )

    private data class WatchedFilterBundle(
        val query: String,
        val media: MediaFilter,
        val time: WatchedTimeFilter,
        val rating: WatchedRatingFilter,
        val genre: String,
        val sort: WatchedSort
    )

    private val watchedFilterBundle = combine(
        combine(_watchedSearchQuery, _watchedFilter, _watchedTimeFilter) { q, m, t -> Triple(q, m, t) },
        combine(_watchedRatingFilter, _watchedGenreFilter, _watchedSort) { r, g, s -> Triple(r, g, s) }
    ) { first, second ->
        WatchedFilterBundle(
            query = first.first,
            media = first.second,
            time = first.third,
            rating = second.first,
            genre = second.second,
            sort = second.third
        )
    }

    val watchedEntries: StateFlow<List<VaultEntry>> = combine(
        vaultEntriesState,
        watchedFilterBundle
    ) { state, filters ->
        val nowCal = Calendar.getInstance()
        val currentYear = nowCal.get(Calendar.YEAR)
        val currentMonth = nowCal.get(Calendar.MONTH)
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L
        val cleanQuery = filters.query.trim().lowercase()

        val entries = (state as? UiState.Success)?.data.orEmpty()
            .filter { it.statusEnum == WatchStatus.WATCHED }
            .filterByMedia(filters.media)
            .filter { entry ->
                val matchesQuery = cleanQuery.isEmpty() ||
                    entry.title.lowercase().contains(cleanQuery) ||
                    entry.directorOrCreator.lowercase().contains(cleanQuery) ||
                    entry.reviewTitle.lowercase().contains(cleanQuery) ||
                    entry.reviewBody.lowercase().contains(cleanQuery) ||
                    entry.genres.any { it.lowercase().contains(cleanQuery) }

                val ts = entry.effectiveWatchedAt
                val matchesTime = when (filters.time) {
                    WatchedTimeFilter.ALL_TIME -> true
                    WatchedTimeFilter.THIS_MONTH -> isSameMonthYear(ts, currentYear, currentMonth)
                    WatchedTimeFilter.LAST_30_DAYS -> ts >= thirtyDaysAgo
                    WatchedTimeFilter.THIS_YEAR -> isSameYear(ts, currentYear)
                }

                val rounded = if (entry.userRating > 0.0) entry.userRating.roundToInt() else 0
                val matchesRating = when (filters.rating) {
                    WatchedRatingFilter.ALL -> true
                    WatchedRatingFilter.TOP_TIER -> rounded in 9..10
                    WatchedRatingFilter.GREAT -> rounded in 7..8
                    WatchedRatingFilter.MID -> rounded in 5..6
                    WatchedRatingFilter.LOW -> rounded in 1..4
                    WatchedRatingFilter.REVIEWED -> entry.hasReview
                    WatchedRatingFilter.UNRATED -> rounded == 0
                }

                val matchesGenre = filters.genre == "All" ||
                    entry.genres.any { it.equals(filters.genre, ignoreCase = true) }

                matchesQuery && matchesTime && matchesRating && matchesGenre
            }

        when (filters.sort) {
            WatchedSort.RECENT -> entries.sortedByDescending { it.effectiveWatchedAt }
            WatchedSort.RATING -> entries.sortedWith(
                compareByDescending<VaultEntry> { it.userRating }
                    .thenByDescending { it.effectiveWatchedAt }
            )
            WatchedSort.LOWEST_RATING -> entries.sortedWith(
                compareBy<VaultEntry> { if (it.userRating > 0.0) it.userRating else 99.0 }
                    .thenByDescending { it.effectiveWatchedAt }
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
            val nowCal = Calendar.getInstance()
            val currentYear = nowCal.get(Calendar.YEAR)
            val currentMonth = nowCal.get(Calendar.MONTH)
            val monthLabel = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
            val watchedThisMonthList = watched.filter {
                isSameMonthYear(it.effectiveWatchedAt, currentYear, currentMonth)
            }
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
                watchedThisMonth = watchedThisMonthList.size,
                watchedMoviesThisMonth = watchedThisMonthList.count { it.mediaTypeEnum == MediaType.MOVIE },
                watchedSeriesThisMonth = watchedThisMonthList.count { it.mediaTypeEnum == MediaType.SERIES },
                currentMonthLabel = monthLabel,
                totalWatchlist = watchlist.size,
                watchlistMovies = watchlist.count { it.mediaTypeEnum == MediaType.MOVIE },
                watchlistSeries = watchlist.count { it.mediaTypeEnum == MediaType.SERIES },
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

    val detailedStats: StateFlow<DetailedStats> = vaultEntriesState
        .map { state ->
            val all = (state as? UiState.Success)?.data.orEmpty()
            computeDetailedStats(all)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DetailedStats()
        )

    val recentlyAddedCatalog: StateFlow<List<CatalogTitle>> = vaultEntriesState
        .map { state ->
            val entries = (state as? UiState.Success)?.data.orEmpty()
                .sortedByDescending { it.createdAt }
                .take(10)
            if (entries.isNotEmpty()) {
                entries.map { entry ->
                    resolveCatalogTitle(entry.catalogId) ?: CatalogTitle(
                        id = entry.id,
                        title = entry.title,
                        tagline = "",
                        mediaType = entry.mediaTypeEnum,
                        releaseYear = entry.releaseYear.toInt(),
                        genres = entry.genres,
                        directorOrCreator = entry.directorOrCreator,
                        studioOrNetwork = entry.studioOrNetwork,
                        country = "",
                        language = entry.language,
                        ageRating = "",
                        runtimeOrSeasons = entry.runtimeOrSeasons,
                        totalEpisodes = entry.totalEpisodes.toInt(),
                        criticScore = entry.userRating,
                        synopsis = entry.synopsis,
                        posterKey = entry.posterKey,
                        tmdbId = entry.tmdbId,
                        posterUrl = entry.posterUrl,
                        backdropUrl = entry.backdropUrl,
                        releaseDate = entry.releaseDate
                    )
                }
            } else {
                CatalogRepository.allTitles().filter { it.isReleased && it.releaseYear >= 2022 }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CatalogRepository.allTitles().filter { it.isReleased && it.releaseYear >= 2022 }
        )

    fun refreshTmdbFeeds() {
        if (!tmdbRepository.hasConfiguredApiKey()) {
            if (omdbRepository.hasConfiguredApiKey()) {
                loadOmdbFallbackFeeds(noticeIfFailed = null)
            } else {
                _snackbarEvents.tryEmit("Add TMDB_API_KEY in AI Studio Secrets for live TMDB discovery.")
                refreshOfflineFilteredBrowse()
            }
            return
        }
        loadInitialTmdbFeeds()
    }

    private fun loadInitialTmdbFeeds() {
        sectionJob?.cancel()
        sectionJob = viewModelScope.launch {
            _browseUiState.update { it.copy(isLoading = true, errorMessage = null, keyConfigured = true) }
            _homeUiState.update { it.copy(isLoading = true) }

            val genresDeferred = async { tmdbRepository.fetchGenres() }
            val currentSection = _browseUiState.value.selectedSection
            val mainDeferred = async { tmdbRepository.fetchSection(currentSection, page = 1) }
            val trendingDeferred = async { tmdbRepository.fetchSection(TmdbSection.TRENDING_ALL, page = 1) }
            val topMoviesDeferred = async { tmdbRepository.fetchSection(TmdbSection.MOVIE_TOP_RATED, page = 1) }
            val popularSeriesDeferred = async { tmdbRepository.fetchSection(TmdbSection.TV_POPULAR, page = 1) }
            val peopleDeferred = async { tmdbRepository.fetchTrendingPeople() }

            val bollywoodDeferred = async { tmdbRepository.fetchBollywoodMovies(page = 1) }
            val hollywoodDeferred = async { tmdbRepository.fetchHollywoodMovies(page = 1) }
            val upcomingDeferred = async { tmdbRepository.fetchUpcomingMovies(page = 1) }
            val netflixDeferred = async { tmdbRepository.fetchPopularOnNetflix(page = 1) }
            val primeDeferred = async { tmdbRepository.fetchPopularOnPrime(page = 1) }
            val ottDeferred = async { tmdbRepository.fetchOttNewMovies(_homeUiState.value.selectedOttPlatform, page = 1) }
            val anticipatedDeferred = async { tmdbRepository.fetchMostAnticipatedMovies(page = 1) }
            val classicDeferred = async { tmdbRepository.fetchClassicMovies(page = 1) }

            val genresResult = genresDeferred.await().getOrNull()
            val mainResult = mainDeferred.await()
            val trendingAll = trendingDeferred.await().getOrNull()?.items.orEmpty()
            val topMovies = topMoviesDeferred.await().getOrNull()?.items.orEmpty()
            val popularSeries = popularSeriesDeferred.await().getOrNull()?.items.orEmpty()
            val trendingPeople = peopleDeferred.await().getOrNull().orEmpty()

            val bollywood = bollywoodDeferred.await().getOrNull().orEmpty()
            val hollywood = hollywoodDeferred.await().getOrNull().orEmpty()
            val upcoming = upcomingDeferred.await().getOrNull().orEmpty()
            val netflix = netflixDeferred.await().getOrNull().orEmpty()
            val prime = primeDeferred.await().getOrNull().orEmpty()
            val ottNew = ottDeferred.await().getOrNull().orEmpty()
            val anticipated = anticipatedDeferred.await().getOrNull().orEmpty()
            val classic = classicDeferred.await().getOrNull().orEmpty()

            mainResult.onSuccess { pageRes ->
                val hero = trendingAll.firstOrNull { it.backdropUrl.isNotBlank() && it.isReleased }
                    ?: pageRes.items.firstOrNull { it.backdropUrl.isNotBlank() }
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

                _homeUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isLiveTmdb = true,
                        isLiveOmdb = omdbRepository.hasConfiguredApiKey(),
                        featuredTitle = hero,
                        trendingNow = trendingAll.ifEmpty { state.trendingNow },
                        bollywoodMovies = bollywood.ifEmpty { state.bollywoodMovies },
                        hollywoodMovies = hollywood.ifEmpty { state.hollywoodMovies },
                        upcomingMovies = upcoming.ifEmpty { state.upcomingMovies },
                        netflixPopular = netflix.ifEmpty { state.netflixPopular },
                        primePopular = prime.ifEmpty { state.primePopular },
                        ottNewMovies = ottNew.ifEmpty { state.ottNewMovies },
                        topRatedMovies = topMovies.ifEmpty { state.topRatedMovies },
                        mostAnticipatedMovies = anticipated.ifEmpty { state.mostAnticipatedMovies },
                        classicMovies = classic.ifEmpty { state.classicMovies },
                        trendingPeople = trendingPeople.take(15)
                    )
                }
            }.onFailure { err ->
                Log.w(TAG, "TMDB initial load failed, falling back to OMDb API", err)
                loadOmdbFallbackFeeds(
                    noticeIfFailed = "TMDB live sync unavailable (${err.localizedMessage ?: "check TMDB_API_KEY"}). Showing curated library."
                )
            }
        }
    }

    private fun loadOmdbFallbackFeeds(noticeIfFailed: String?) {
        sectionJob?.cancel()
        sectionJob = viewModelScope.launch {
            _browseUiState.update { it.copy(isLoading = true, errorMessage = null) }
            _homeUiState.update { it.copy(isLoading = true) }

            unifiedMediaService.fetchOmdbFallbackHomeFeeds()
                .onSuccess { feeds ->
                    val browsePool = (
                        feeds.trendingNow +
                            feeds.bollywoodMovies +
                            feeds.hollywoodMovies +
                            feeds.topRatedMovies +
                            feeds.classicMovies
                        ).distinctBy { it.id }
                    omdbBrowsePool = browsePool
                    val filteredBrowse = filterAndSortTitlesLocally(
                        titles = browsePool.ifEmpty { localCatalogTitles },
                        mediaFilter = _browseMediaFilter.value,
                        selectedGenres = _browseUiState.value.selectedGenres,
                        sort = _browseUiState.value.discoverSort
                    )
                    _browseUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLiveTmdb = false,
                            isLiveOmdb = true,
                            errorMessage = null,
                            featuredTitle = feeds.featuredTitle ?: state.featuredTitle,
                            sectionTitles = filteredBrowse,
                            currentPage = 1,
                            totalPages = 1
                        )
                    }
                    _homeUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLiveTmdb = false,
                            isLiveOmdb = true,
                            featuredTitle = feeds.featuredTitle ?: state.featuredTitle,
                            trendingNow = feeds.trendingNow.ifEmpty { state.trendingNow },
                            bollywoodMovies = feeds.bollywoodMovies.ifEmpty { state.bollywoodMovies },
                            hollywoodMovies = feeds.hollywoodMovies.ifEmpty { state.hollywoodMovies },
                            topRatedMovies = feeds.topRatedMovies.ifEmpty { state.topRatedMovies },
                            classicMovies = feeds.classicMovies.ifEmpty { state.classicMovies }
                        )
                    }
                }
                .onFailure {
                    _browseUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLiveTmdb = false,
                            isLiveOmdb = false,
                            errorMessage = noticeIfFailed
                        )
                    }
                    _homeUiState.update { it.copy(isLoading = false, isLiveTmdb = false, isLiveOmdb = false) }
                    refreshOfflineFilteredBrowse()
                }
        }
    }

    fun selectOttPlatform(platform: OttPlatform) {
        _homeUiState.update { it.copy(selectedOttPlatform = platform) }
        if (!tmdbRepository.hasConfiguredApiKey() || !_homeUiState.value.isLiveTmdb) {
            return
        }
        ottJob?.cancel()
        ottJob = viewModelScope.launch {
            _homeUiState.update { it.copy(isLoadingOtt = true) }
            tmdbRepository.fetchOttNewMovies(platform, page = 1)
                .onSuccess { list ->
                    _homeUiState.update { state ->
                        state.copy(
                            isLoadingOtt = false,
                            ottNewMovies = list.ifEmpty { state.ottNewMovies }
                        )
                    }
                }
                .onFailure {
                    _homeUiState.update { it.copy(isLoadingOtt = false) }
                }
        }
    }

    fun selectTmdbSection(section: TmdbSection) {
        _browseUiState.update {
            it.copy(
                selectedSection = section,
                selectedGenreId = null,
                selectedGenreLabel = "All",
                selectedGenres = emptyList(),
                selectedGenreIds = emptyList(),
                searchPeople = emptyList(),
                searchCollections = emptyList()
            )
        }
        val adjustedMedia = section.mediaHint?.let { hint ->
            if (hint == MediaType.MOVIE) MediaFilter.MOVIES else MediaFilter.SERIES
        } ?: MediaFilter.ALL
        _browseMediaFilter.value = adjustedMedia

        if (!tmdbRepository.hasConfiguredApiKey() || !_browseUiState.value.isLiveTmdb) {
            if (_browseQuery.value.isNotBlank() && omdbRepository.hasConfiguredApiKey()) {
                triggerSearch(_browseQuery.value)
            } else {
                refreshOfflineFilteredBrowse()
            }
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
        val nextGenres = if (genreLabel.equals("All", ignoreCase = true)) emptyList() else listOf(genreLabel)
        val nextIds = if (genreId == null || genreLabel.equals("All", ignoreCase = true)) emptyList() else listOf(genreId)
        _browseUiState.update {
            it.copy(
                selectedGenreId = nextIds.firstOrNull(),
                selectedGenreLabel = nextGenres.joinToString(", ").ifBlank { "All" },
                selectedGenres = nextGenres,
                selectedGenreIds = nextIds
            )
        }
        applyGenreFilterUpdate()
    }

    fun toggleBrowseGenre(genreId: Int?, genreName: String) {
        if (genreName.equals("All", ignoreCase = true)) {
            clearBrowseGenres()
            return
        }
        val currentGenres = _browseUiState.value.selectedGenres
        val currentIds = _browseUiState.value.selectedGenreIds
        val removing = currentGenres.any { it.equals(genreName, ignoreCase = true) }

        val nextGenres = if (removing) {
            currentGenres.filterNot { it.equals(genreName, ignoreCase = true) }
        } else {
            (currentGenres + genreName).distinct()
        }

        val nextIds = if (genreId != null) {
            if (removing) currentIds.filterNot { it == genreId } else (currentIds + genreId).distinct()
        } else {
            currentIds
        }

        _browseUiState.update {
            it.copy(
                selectedGenres = nextGenres,
                selectedGenreIds = nextIds,
                selectedGenreId = nextIds.firstOrNull(),
                selectedGenreLabel = nextGenres.joinToString(", ").ifBlank { "All" }
            )
        }
        applyGenreFilterUpdate()
    }

    fun clearBrowseGenres() {
        _browseUiState.update {
            it.copy(
                selectedGenres = emptyList(),
                selectedGenreIds = emptyList(),
                selectedGenreId = null,
                selectedGenreLabel = "All"
            )
        }
        applyGenreFilterUpdate()
    }

    private fun applyGenreFilterUpdate() {
        if (!tmdbRepository.hasConfiguredApiKey() || !_browseUiState.value.isLiveTmdb) {
            refreshOfflineFilteredBrowse()
            return
        }

        if (_browseQuery.value.isNotBlank()) {
            triggerSearch(_browseQuery.value)
            return
        }

        val state = _browseUiState.value
        if (state.selectedGenres.isEmpty() && state.discoverSort == DiscoverSort.POPULARITY) {
            selectTmdbSection(state.selectedSection)
            return
        }

        runDiscover()
    }

    fun openGenreCollectionInSearch(genreKeyword: String) {
        _browseQuery.value = ""
        _browseMediaFilter.value = MediaFilter.MOVIES
        val liveGenres = _browseUiState.value.movieGenres
        val match = liveGenres.firstOrNull {
            it.name.equals(genreKeyword, ignoreCase = true) ||
                it.name.contains(genreKeyword, ignoreCase = true)
        }
        if (match != null) {
            selectGenre(match.id, match.name)
        } else {
            selectGenre(null, genreKeyword)
        }
    }

    fun selectDiscoverSort(sort: DiscoverSort) {
        _browseUiState.update { it.copy(discoverSort = sort) }
        if (!tmdbRepository.hasConfiguredApiKey() || !_browseUiState.value.isLiveTmdb) {
            refreshOfflineFilteredBrowse()
            return
        }
        if (_browseQuery.value.isNotBlank()) {
            _browseUiState.update { current ->
                current.copy(sectionTitles = sortTitlesLocally(current.sectionTitles, sort))
            }
            return
        }
        runDiscover()
    }

    fun selectBrowseViewMode(mode: CatalogViewMode) {
        _browseUiState.update { it.copy(viewMode = mode) }
    }

    fun cycleBrowseViewMode() {
        val modes = CatalogViewMode.entries
        val next = modes[(_browseUiState.value.viewMode.ordinal + 1) % modes.size]
        _browseUiState.update { it.copy(viewMode = next) }
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
                genreIds = state.selectedGenreIds,
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
        if (_browseQuery.value.isNotBlank() && (tmdbRepository.hasConfiguredApiKey() || omdbRepository.hasConfiguredApiKey())) {
            triggerSearch(_browseQuery.value)
        }
    }

    fun updateBrowseQuery(query: String) {
        _browseQuery.value = query
        if (!tmdbRepository.hasConfiguredApiKey() && !omdbRepository.hasConfiguredApiKey()) {
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
            if (tmdbRepository.hasConfiguredApiKey() && state.isLiveTmdb) {
                if (state.selectedGenres.isNotEmpty() || state.discoverSort != DiscoverSort.POPULARITY) {
                    runDiscover()
                } else {
                    selectTmdbSection(state.selectedSection)
                }
            } else if (omdbRepository.hasConfiguredApiKey() && state.isLiveOmdb) {
                loadOmdbFallbackFeeds(noticeIfFailed = null)
            } else {
                refreshOfflineFilteredBrowse()
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            _browseUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val activeScope = _browseUiState.value.searchScope
            unifiedMediaService.searchWithFallback(trimmed, activeScope, page = 1).onSuccess { (bundle, origin) ->
                val usingTmdb = origin == DataSourceOrigin.TMDB_WITH_OMDB
                if (!usingTmdb) {
                    omdbSearchResultsPool = bundle.titles
                    lastOmdbSearchQuery = trimmed
                }
                val state = _browseUiState.value
                val processedTitles = filterAndSortTitlesLocally(
                    titles = bundle.titles,
                    mediaFilter = _browseMediaFilter.value,
                    selectedGenres = state.selectedGenres,
                    sort = state.discoverSort
                )
                _browseUiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isLiveTmdb = usingTmdb,
                        isLiveOmdb = !usingTmdb || omdbRepository.hasConfiguredApiKey(),
                        errorMessage = null,
                        sectionTitles = processedTitles,
                        searchPeople = bundle.people,
                        searchCollections = bundle.collections,
                        currentPage = bundle.page,
                        totalPages = bundle.totalPages
                    )
                }
                if (!usingTmdb) {
                    _homeUiState.update { it.copy(isLiveTmdb = false, isLiveOmdb = true) }
                }
            }.onFailure { err ->
                val isNotFound = err.localizedMessage?.contains("not found", ignoreCase = true) == true
                if (isNotFound) {
                    _browseUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = null,
                            sectionTitles = emptyList(),
                            searchPeople = emptyList(),
                            searchCollections = emptyList(),
                            currentPage = 1,
                            totalPages = 1
                        )
                    }
                } else {
                    omdbSearchResultsPool = emptyList()
                    lastOmdbSearchQuery = ""
                    refreshOfflineFilteredBrowse()
                    _browseUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Search unavailable"
                        )
                    }
                }
            }
        }
    }

    fun updateBrowseMediaFilter(filter: MediaFilter) {
        _browseMediaFilter.value = filter
        if (!tmdbRepository.hasConfiguredApiKey() || !_browseUiState.value.isLiveTmdb) {
            if (_browseQuery.value.isNotBlank() && omdbRepository.hasConfiguredApiKey()) {
                val nextScope = when (filter) {
                    MediaFilter.ALL -> SearchScope.MULTI
                    MediaFilter.MOVIES -> SearchScope.MOVIES
                    MediaFilter.SERIES -> SearchScope.SERIES
                }
                _browseUiState.update { it.copy(searchScope = nextScope) }
                triggerSearch(_browseQuery.value)
            } else {
                refreshOfflineFilteredBrowse()
            }
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
            if (_browseUiState.value.selectedGenres.isNotEmpty() || _browseUiState.value.discoverSort != DiscoverSort.POPULARITY) {
                _browseUiState.update { it.copy(selectedSection = targetSection) }
                runDiscover()
            } else {
                selectTmdbSection(targetSection)
            }
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
                unifiedMediaService.searchWithFallback(query, state.searchScope, page = nextPage).onSuccess { (bundle, origin) ->
                    if (origin == DataSourceOrigin.OMDB_FALLBACK) {
                        omdbSearchResultsPool = (omdbSearchResultsPool + bundle.titles).distinctBy { it.id }
                    }
                    val filteredBatch = filterAndSortTitlesLocally(
                        titles = bundle.titles,
                        mediaFilter = _browseMediaFilter.value,
                        selectedGenres = state.selectedGenres,
                        sort = state.discoverSort
                    )
                    _browseUiState.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            sectionTitles = (current.sectionTitles + filteredBatch).distinctBy { it.id },
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

            val result = if (state.selectedGenreIds.isNotEmpty() || state.selectedGenreId != null || state.discoverSort != DiscoverSort.POPULARITY) {
                val mediaType = when (_browseMediaFilter.value) {
                    MediaFilter.SERIES -> MediaType.SERIES
                    else -> state.selectedSection.mediaHint ?: MediaType.MOVIE
                }
                tmdbRepository.discover(
                    mediaType = mediaType,
                    genreId = state.selectedGenreId,
                    genreIds = state.selectedGenreIds,
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
        val rawQuery = _browseQuery.value.trim()
        val query = rawQuery.lowercase()
        val mediaFilter = _browseMediaFilter.value
        val state = _browseUiState.value

        val sourcePool = when {
            rawQuery.isNotEmpty() && state.isLiveOmdb && omdbSearchResultsPool.isNotEmpty() &&
                lastOmdbSearchQuery.equals(rawQuery, ignoreCase = true) -> omdbSearchResultsPool
            rawQuery.isEmpty() && state.isLiveOmdb && omdbBrowsePool.isNotEmpty() -> omdbBrowsePool
            else -> localCatalogTitles
        }

        val queryMatched = if (sourcePool === omdbSearchResultsPool) {
            sourcePool
        } else {
            sourcePool.filter { item ->
                query.isEmpty() ||
                    item.title.lowercase().contains(query) ||
                    item.directorOrCreator.lowercase().contains(query) ||
                    item.genres.any { it.lowercase().contains(query) } ||
                    item.cast.any { it.actor.lowercase().contains(query) }
            }
        }
        val finalTitles = filterAndSortTitlesLocally(
            titles = queryMatched,
            mediaFilter = mediaFilter,
            selectedGenres = state.selectedGenres,
            sort = state.discoverSort
        )

        _browseUiState.update { current ->
            current.copy(
                sectionTitles = finalTitles,
                currentPage = if (sourcePool === omdbSearchResultsPool) current.currentPage else 1,
                totalPages = if (sourcePool === omdbSearchResultsPool) current.totalPages else 1
            )
        }
    }

    private fun filterAndSortTitlesLocally(
        titles: List<CatalogTitle>,
        mediaFilter: MediaFilter,
        selectedGenres: List<String>,
        sort: DiscoverSort
    ): List<CatalogTitle> {
        val filtered = titles.filter { item ->
            val matchesMedia = when (mediaFilter) {
                MediaFilter.ALL -> true
                MediaFilter.MOVIES -> item.mediaType == MediaType.MOVIE
                MediaFilter.SERIES -> item.mediaType == MediaType.SERIES
            }
            val matchesGenres = selectedGenres.isEmpty() || selectedGenres.any { sel ->
                item.genres.any { g -> g.equals(sel, ignoreCase = true) || g.contains(sel, ignoreCase = true) }
            }
            matchesMedia && matchesGenres
        }
        return sortTitlesLocally(filtered, sort)
    }

    private fun sortTitlesLocally(titles: List<CatalogTitle>, sort: DiscoverSort): List<CatalogTitle> {
        return when (sort) {
            DiscoverSort.POPULARITY -> titles
            DiscoverSort.TOP_RATED -> titles.sortedByDescending { it.criticScore }
            DiscoverSort.NEWEST -> titles.sortedWith(
                compareByDescending<CatalogTitle> { it.releaseYear }.thenByDescending { it.releaseDate }
            )
            DiscoverSort.MOST_VOTED -> titles.sortedByDescending { it.voteCount }
        }
    }

    fun resolveCatalogTitle(titleId: String): CatalogTitle? {
        return unifiedMediaService.findCachedById(titleId)
    }

    fun loadTitleDetails(titleId: String) {
        val immediate = resolveCatalogTitle(titleId)
        _detailTitle.value = immediate
        _detailError.value = null

        val matchingEntry = (vaultEntriesState.value as? UiState.Success)?.data
            ?.firstOrNull { it.id == titleId || it.catalogId == titleId }

        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _isLoadingDetail.value = true
            unifiedMediaService.fetchUnifiedTitleDetails(
                titleId = titleId,
                hintTitle = immediate,
                hintEntry = matchingEntry
            ).onSuccess { unified ->
                _detailTitle.value = unified
                _detailError.value = null
            }.onFailure { err ->
                Log.w(TAG, "Failed to fetch unified TMDB/OMDb details for $titleId", err)
                if (_detailTitle.value == null && matchingEntry == null) {
                    _detailError.value = err.localizedMessage ?: "Unable to load details from TMDB or OMDb."
                }
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

    fun selectWatchlistViewMode(mode: CatalogViewMode) {
        _watchlistViewMode.value = mode
    }

    fun cycleWatchlistViewMode() {
        val modes = CatalogViewMode.entries
        val next = modes[(_watchlistViewMode.value.ordinal + 1) % modes.size]
        _watchlistViewMode.value = next
    }

    fun updateWatchedSearchQuery(query: String) {
        _watchedSearchQuery.value = query
    }

    fun updateWatchedFilter(filter: MediaFilter) {
        _watchedFilter.value = filter
    }

    fun updateWatchedTimeFilter(filter: WatchedTimeFilter) {
        _watchedTimeFilter.value = filter
    }

    fun updateWatchedRatingFilter(filter: WatchedRatingFilter) {
        _watchedRatingFilter.value = filter
    }

    fun updateWatchedGenreFilter(genre: String) {
        _watchedGenreFilter.value = genre
    }

    fun updateWatchedSort(sort: WatchedSort) {
        _watchedSort.value = sort
    }

    fun resetWatchedFilters() {
        _watchedSearchQuery.value = ""
        _watchedFilter.value = MediaFilter.ALL
        _watchedTimeFilter.value = WatchedTimeFilter.ALL_TIME
        _watchedRatingFilter.value = WatchedRatingFilter.ALL
        _watchedGenreFilter.value = "All"
        _watchedSort.value = WatchedSort.RECENT
    }

    fun updateReviewsFilter(filter: MediaFilter) {
        _reviewsFilter.value = filter
    }

    fun addOrMoveCatalogTitle(
        catalogTitle: CatalogTitle,
        targetStatus: WatchStatus,
        isPriority: Boolean = false
    ) {
        if (targetStatus == WatchStatus.WATCHED && !catalogTitle.isReleased) {
            _snackbarEvents.tryEmit("This movie isn't released yet.")
            return
        }
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.upsertFromCatalog(
                catalogTitle = catalogTitle,
                targetStatus = targetStatus,
                isPriority = isPriority
            )
            _isMutating.value = false
            result.onSuccess { savedId ->
                mirrorEntryToCloud(savedId)
                val msg = if (targetStatus == WatchStatus.WANT_TO_WATCH) {
                    "Added \"${catalogTitle.title}\" to Bucketlist"
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
                mirrorEntryToCloud(entry.id)
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
        if (!entry.isReleased) {
            _snackbarEvents.tryEmit("This movie isn't released yet.")
            return
        }
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
                mirrorEntryToCloud(entry.id)
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
                mirrorEntryToCloud(entry.id)
                _snackbarEvents.tryEmit("Moved \"${entry.title}\" to Bucketlist")
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to move to Bucketlist")
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
            result.onSuccess {
                mirrorEntryToCloud(entry.id)
            }.onFailure { e ->
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
            result.onSuccess {
                mirrorEntryToCloud(entry.id)
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to update rewatch count")
            }
        }
    }

    fun markSeenWithDetails(
        catalogTitle: CatalogTitle?,
        existingEntry: VaultEntry?,
        rating: Int,
        reviewTitle: String,
        watchedAtMillis: Long,
        onComplete: () -> Unit = {}
    ) {
        val released = when {
            catalogTitle != null -> catalogTitle.isReleased
            existingEntry != null -> existingEntry.isReleased
            else -> true
        }
        if (!released) {
            _snackbarEvents.tryEmit("This movie isn't released yet.")
            return
        }
        val displayName = existingEntry?.title ?: catalogTitle?.title ?: "Title"
        val cleanRating = rating.coerceIn(0, 10)
        viewModelScope.launch {
            _isMutating.value = true
            val targetId: String?
            val result = if (existingEntry != null) {
                targetId = existingEntry.id
                repository.saveReviewAndRating(
                    catalogTitle = catalogTitle,
                    existingEntry = existingEntry,
                    entryId = existingEntry.id,
                    status = WatchStatus.WATCHED,
                    userRating = if (cleanRating > 0) cleanRating.toDouble() else existingEntry.userRating,
                    reviewTitle = reviewTitle.ifBlank { existingEntry.reviewTitle },
                    reviewBody = existingEntry.reviewBody,
                    favoriteQuote = existingEntry.favoriteQuote,
                    containsSpoilers = existingEntry.containsSpoilers,
                    watchedAt = watchedAtMillis
                )
            } else if (catalogTitle != null) {
                targetId = catalogTitle.id
                repository.upsertFromCatalog(
                    catalogTitle = catalogTitle,
                    targetStatus = WatchStatus.WATCHED,
                    isPriority = false,
                    userRating = cleanRating.toDouble(),
                    reviewTitle = reviewTitle,
                    watchedAt = watchedAtMillis
                ).map { Unit }
            } else {
                targetId = null
                Result.failure(IllegalStateException("Missing title"))
            }
            _isMutating.value = false
            result.onSuccess {
                if (targetId != null) mirrorEntryToCloud(targetId)
                val ratingNote = if (cleanRating > 0) " · $cleanRating/10 (${RatingScale.labelFor(cleanRating)})" else ""
                _snackbarEvents.tryEmit("Marked \"$displayName\" as Seen$ratingNote")
                onComplete()
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to save watched entry")
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
        watchedAt: Long? = null,
        onComplete: () -> Unit = {}
    ) {
        val released = when {
            catalogTitle != null -> catalogTitle.isReleased
            existingEntry != null -> existingEntry.isReleased
            else -> true
        }
        if (status == WatchStatus.WATCHED && !released) {
            _snackbarEvents.tryEmit("This movie isn't released yet.")
            return
        }
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
                containsSpoilers = containsSpoilers,
                watchedAt = watchedAt
            )
            _isMutating.value = false
            result.onSuccess {
                mirrorEntryToCloud(entryId)
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
                mirrorEntryToCloud(id)
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
                deleteEntryFromCloud(entry.id)
                _snackbarEvents.tryEmit("Removed \"${entry.title}\" from your Vault")
                onDeleted()
            }.onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to remove entry")
            }
        }
    }

    // ---- Onboarding, Auth, Theme & Cloud Sync ----

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun signInWithGoogle(activityContext: Context, isSignUp: Boolean = false) {
        val manager = authSessionManager
        if (manager == null) {
            val msg = "Cloud sign-in is unavailable in this build because Firebase google-services.json is not configured. Continue as Guest or add GOOGLE_SERVICES_JSON to GitHub Secrets."
            _authErrorMessage.value = msg
            _snackbarEvents.tryEmit(msg)
            return
        }
        viewModelScope.launch {
            _isAuthBusy.value = true
            _authErrorMessage.value = null
            val res = manager.signInWithGoogle(activityContext)
            _isAuthBusy.value = false
            res.onSuccess { user ->
                preferencesRepository?.setOnboardingCompleted(completed = true, isGuest = false)
                _snackbarEvents.tryEmit(
                    if (isSignUp) "Account created! Welcome, ${user.displayName}"
                    else "Welcome back, ${user.displayName}"
                )
            }.onFailure { err ->
                if (err is AuthCancelledException) {
                    _authErrorMessage.value = null
                } else {
                    val msg = err.localizedMessage ?: "Google Sign-In failed."
                    _authErrorMessage.value = msg
                    _snackbarEvents.tryEmit(msg)
                }
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _authErrorMessage.value = null
            preferencesRepository?.setOnboardingCompleted(completed = true, isGuest = true)
            _snackbarEvents.tryEmit("Exploring in Guest Mode — your Vault is saved on this device.")
        }
    }

    fun signOut(context: Context) {
        val manager = authSessionManager ?: return
        cloudSyncJob?.cancel()
        viewModelScope.launch {
            _isAuthBusy.value = true
            manager.signOut(context)
            preferencesRepository?.setLastSyncedUid(null)
            preferencesRepository?.setGuestMode(true)
            _pendingGuestMergeCount.value = 0
            _isAuthBusy.value = false
            _snackbarEvents.tryEmit("Signed out. Switched to Guest Mode.")
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository?.setDarkTheme(enabled)
        }
    }

    fun dismissGuestMergePrompt() {
        _pendingGuestMergeCount.value = 0
    }

    fun syncLocalVaultToCloud() {
        val uid = _authUser.value?.uid ?: return
        val cloud = cloudRepository ?: return
        viewModelScope.launch {
            _isSyncingCloud.value = true
            val localEntries = repository.getAllEntriesOnce().getOrDefault(emptyList())
            val res = cloud.mergeLocalEntriesToCloud(uid, localEntries)
            _isSyncingCloud.value = false
            res.onSuccess { count ->
                _pendingGuestMergeCount.value = 0
                preferencesRepository?.setLastSyncedUid(uid)
                _snackbarEvents.tryEmit("Synced $count ${if (count == 1) "title" else "titles"} to your cloud account")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Cloud sync failed")
            }
        }
    }

    suspend fun exportVaultJson(): Result<String> {
        return repository.getAllEntriesOnce().map { entries ->
            VaultPortabilityManager.exportToJson(
                entries = entries,
                stats = vaultStats.value
            )
        }
    }

    fun importVaultJson(rawJson: String) {
        viewModelScope.launch {
            _isMutating.value = true
            val parsed = VaultPortabilityManager.importFromJson(rawJson)
            parsed.onSuccess { pack ->
                if (pack.entries.isEmpty()) {
                    _isMutating.value = false
                    _snackbarEvents.tryEmit("No entries found inside the selected JSON file.")
                    return@onSuccess
                }
                val mergeRes = repository.importAndMergeEntries(pack.entries)
                mergeRes.onSuccess { (inserted, updated) ->
                    val uid = _authUser.value?.uid
                    if (uid != null && cloudRepository != null) {
                        cloudRepository.mergeLocalEntriesToCloud(uid, pack.entries)
                    }
                    _isMutating.value = false
                    _snackbarEvents.tryEmit("Imported JSON: $inserted added, $updated updated.")
                }.onFailure { err ->
                    _isMutating.value = false
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Failed to import JSON entries")
                }
            }.onFailure { err ->
                _isMutating.value = false
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Invalid JSON backup file")
            }
        }
    }

    private fun startCloudSyncForUser(uid: String) {
        val cloud = cloudRepository ?: return
        cloudSyncJob?.cancel()
        cloudSyncJob = viewModelScope.launch {
            _isSyncingCloud.value = true
            val initialCloud = cloud.fetchUserEntries(uid)
            initialCloud.onSuccess { remoteList ->
                if (remoteList.isNotEmpty()) {
                    repository.mergeRemoteEntries(remoteList)
                }
                val localList = repository.getAllEntriesOnce().getOrDefault(emptyList())
                val remoteIds = remoteList.map { it.id }.toSet()
                val unsyncedLocal = localList.count { it.id !in remoteIds }
                if (unsyncedLocal > 0) {
                    _pendingGuestMergeCount.value = unsyncedLocal
                }
                preferencesRepository?.setLastSyncedUid(uid)
            }.onFailure { err ->
                Log.w(TAG, "Initial cloud fetch failed for $uid", err)
            }
            _isSyncingCloud.value = false

            cloud.observeUserEntries(uid)
                .catch { err ->
                    if (err is CancellationException) throw err
                    Log.w(TAG, "Cloud realtime listener error", err)
                }
                .collect { remoteUpdates ->
                    if (remoteUpdates.isNotEmpty()) {
                        repository.mergeRemoteEntries(remoteUpdates)
                    }
                }
        }
    }

    private fun mirrorEntryToCloud(entryId: String) {
        val uid = _authUser.value?.uid ?: return
        val cloud = cloudRepository ?: return
        viewModelScope.launch {
            val entry = repository.getVaultEntry(entryId).getOrNull() ?: return@launch
            cloud.upsertEntry(uid, entry).onFailure { err ->
                Log.w(TAG, "Cloud upsert mirror failed for $entryId", err)
            }
        }
    }

    private fun deleteEntryFromCloud(entryId: String) {
        val uid = _authUser.value?.uid ?: return
        val cloud = cloudRepository ?: return
        viewModelScope.launch {
            cloud.deleteEntry(uid, entryId).onFailure { err ->
                Log.w(TAG, "Cloud delete mirror failed for $entryId", err)
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

    private fun isSameMonthYear(timestamp: Long, targetYear: Int, targetMonth: Int): Boolean {
        if (timestamp <= 0L) return false
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.YEAR) == targetYear && cal.get(Calendar.MONTH) == targetMonth
    }

    private fun isSameYear(timestamp: Long, targetYear: Int): Boolean {
        if (timestamp <= 0L) return false
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.YEAR) == targetYear
    }

    private fun computeDetailedStats(all: List<VaultEntry>): DetailedStats {
        val watched = all.filter { it.statusEnum == WatchStatus.WATCHED }
        val bucketlist = all.filter { it.statusEnum == WatchStatus.WANT_TO_WATCH }
        val watchedMovies = watched.filter { it.mediaTypeEnum == MediaType.MOVIE }
        val watchedSeries = watched.filter { it.mediaTypeEnum == MediaType.SERIES }
        val bucketMovies = bucketlist.count { it.mediaTypeEnum == MediaType.MOVIE }
        val bucketSeries = bucketlist.count { it.mediaTypeEnum == MediaType.SERIES }

        val nowCal = Calendar.getInstance()
        val currentYear = nowCal.get(Calendar.YEAR)
        val currentMonth = nowCal.get(Calendar.MONTH)
        val monthLabel = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())

        val watchedThisMonth = watched.count { isSameMonthYear(it.effectiveWatchedAt, currentYear, currentMonth) }
        val watchedThisYear = watched.count { isSameYear(it.effectiveWatchedAt, currentYear) }

        val totalPool = (watched.size + bucketlist.size).coerceAtLeast(1)
        val moviesPool = (watchedMovies.size + bucketMovies).coerceAtLeast(1)
        val seriesPool = (watchedSeries.size + bucketSeries).coerceAtLeast(1)

        val movieMinutes = watchedMovies.sumOf { estimateEntryMinutes(it) * (1 + it.rewatchCount.toInt()) }
        val seriesMinutes = watchedSeries.sumOf { estimateEntryMinutes(it) * (1 + it.rewatchCount.toInt()) }
        val totalMinutes = movieMinutes + seriesMinutes

        val seriesEntriesAll = all.filter { it.mediaTypeEnum == MediaType.SERIES }
        val episodesWatchedTotal = seriesEntriesAll.sumOf { entry ->
            if (entry.statusEnum == WatchStatus.WATCHED && entry.episodesWatched == 0L && entry.totalEpisodes > 0L) {
                entry.totalEpisodes
            } else {
                entry.episodesWatched
            }
        }
        val episodesTargetTotal = seriesEntriesAll.sumOf { maxOf(it.totalEpisodes, it.episodesWatched) }

        val rated = all.filter { it.userRating > 0.0 }
        val avgRating = if (rated.isNotEmpty()) rated.sumOf { it.userRating } / rated.size else 0.0
        val ratingDist = IntArray(10)
        rated.forEach { entry ->
            val score = entry.userRating.roundToInt().coerceIn(1, 10)
            ratingDist[score - 1]++
        }

        val genreBuckets = mutableMapOf<String, MutableList<Double>>()
        watched.forEach { entry ->
            entry.genres.forEach { genre ->
                genreBuckets.getOrPut(genre) { mutableListOf() }.add(entry.userRating)
            }
        }
        val genreStats = genreBuckets.entries.map { (name, ratings) ->
            val validRatings = ratings.filter { it > 0.0 }
            val genreAvg = if (validRatings.isNotEmpty()) validRatings.average() else 0.0
            GenreStat(name = name, count = ratings.size, averageRating = genreAvg)
        }.sortedByDescending { it.count }

        val topRatedGenres = genreStats
            .filter { it.averageRating > 0.0 }
            .sortedWith(compareByDescending<GenreStat> { it.averageRating }.thenByDescending { it.count })

        val decadeCounts = linkedMapOf(
            "2020s" to 0,
            "2010s" to 0,
            "2000s" to 0,
            "1990s" to 0,
            "Pre-1990" to 0
        )
        watched.forEach { entry ->
            val y = entry.releaseYear.toInt()
            val key = when {
                y >= 2020 -> "2020s"
                y >= 2010 -> "2010s"
                y >= 2000 -> "2000s"
                y >= 1990 -> "1990s"
                else -> "Pre-1990"
            }
            decadeCounts[key] = (decadeCounts[key] ?: 0) + 1
        }

        val languageCounts = mutableMapOf<String, Int>()
        val platformCounts = mutableMapOf<String, Int>()
        val creatorCounts = mutableMapOf<String, Int>()
        watched.forEach { entry ->
            val catalog = resolveCatalogTitle(entry.catalogId)
            val lang = entry.language.ifBlank { catalog?.language.orEmpty() }
            if (lang.isNotBlank()) {
                lang.split("/", ",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { l ->
                    languageCounts[l] = (languageCounts[l] ?: 0) + 1
                }
            }
            val studio = entry.studioOrNetwork.ifBlank {
                catalog?.studioOrNetwork?.ifBlank {
                    catalog.watchProviders.firstOrNull()?.name.orEmpty()
                }.orEmpty()
            }
            if (studio.isNotBlank()) {
                studio.split("/", ",").firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }?.let { p ->
                    platformCounts[p] = (platformCounts[p] ?: 0) + 1
                }
            }
            if (entry.directorOrCreator.isNotBlank()) {
                creatorCounts[entry.directorOrCreator] = (creatorCounts[entry.directorOrCreator] ?: 0) + 1
            }
        }

        // Monthly activity over last 6 months
        val monthFormatter = SimpleDateFormat("MMM", Locale.US)
        val monthlyActivity = (5 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -offset)
            }
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val inMonth = watched.filter { isSameMonthYear(it.effectiveWatchedAt, y, m) }
            ActivityBucket(
                label = monthFormatter.format(cal.time),
                moviesCount = inMonth.count { it.mediaTypeEnum == MediaType.MOVIE },
                seriesCount = inMonth.count { it.mediaTypeEnum == MediaType.SERIES }
            )
        }

        // Annual activity over last 5 years
        val annualActivity = (currentYear - 4..currentYear).map { y ->
            val inYear = watched.filter { isSameYear(it.effectiveWatchedAt, y) }
            ActivityBucket(
                label = y.toString(),
                moviesCount = inYear.count { it.mediaTypeEnum == MediaType.MOVIE },
                seriesCount = inYear.count { it.mediaTypeEnum == MediaType.SERIES }
            )
        }

        return DetailedStats(
            totalWatched = watched.size,
            watchedMovies = watchedMovies.size,
            watchedSeries = watchedSeries.size,
            watchedThisMonth = watchedThisMonth,
            watchedThisYear = watchedThisYear,
            currentMonthLabel = monthLabel,
            totalBucketlist = bucketlist.size,
            bucketlistMovies = bucketMovies,
            bucketlistSeries = bucketSeries,
            priorityBucketlist = bucketlist.count { it.priority },
            overallCompletionRate = if (watched.isEmpty() && bucketlist.isEmpty()) 0f else watched.size.toFloat() / totalPool.toFloat(),
            moviesCompletionRate = if (watchedMovies.isEmpty() && bucketMovies == 0) 0f else watchedMovies.size.toFloat() / moviesPool.toFloat(),
            seriesCompletionRate = if (watchedSeries.isEmpty() && bucketSeries == 0) 0f else watchedSeries.size.toFloat() / seriesPool.toFloat(),
            totalHoursWatched = totalMinutes / 60.0,
            movieHoursWatched = movieMinutes / 60.0,
            seriesHoursWatched = seriesMinutes / 60.0,
            totalEpisodesWatched = episodesWatchedTotal,
            totalEpisodesTarget = episodesTargetTotal,
            episodeCompletionRate = if (episodesTargetTotal > 0L) (episodesWatchedTotal.toFloat() / episodesTargetTotal.toFloat()).coerceIn(0f, 1f) else 0f,
            averageRating = avgRating,
            ratedCount = rated.size,
            masterpiecesCount = rated.count { it.userRating.roundToInt() >= 9 },
            totalReviews = all.count { it.hasReview },
            totalRewatches = watched.sumOf { it.rewatchCount.toInt() },
            ratingDistribution1To10 = ratingDist.toList(),
            genreBreakdown = genreStats.take(8),
            topRatedGenres = topRatedGenres.take(6),
            decadeBreakdown = decadeCounts.entries.filter { it.value > 0 || watched.isNotEmpty() }.map { it.key to it.value },
            languageBreakdown = languageCounts.entries.sortedByDescending { it.value }.take(6).map { it.key to it.value },
            platformBreakdown = platformCounts.entries.sortedByDescending { it.value }.take(6).map { it.key to it.value },
            monthlyActivity = monthlyActivity,
            annualActivity = annualActivity,
            topCreators = creatorCounts.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }
        )
    }

    private fun estimateEntryMinutes(entry: VaultEntry): Int {
        if (entry.mediaTypeEnum == MediaType.SERIES) {
            val eps = when {
                entry.episodesWatched > 0L -> entry.episodesWatched.toInt()
                entry.totalEpisodes > 0L -> entry.totalEpisodes.toInt()
                else -> 8
            }
            return eps * 45
        }
        val raw = entry.runtimeOrSeasons.lowercase(Locale.US)
        val hoursMatch = Regex("(\\d+)\\s*h").find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
        val minsMatch = Regex("(\\d+)\\s*m").find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
        val total = hoursMatch * 60 + minsMatch
        if (total > 0) return total
        val plainMin = Regex("(\\d+)").find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
        return if (plainMin in 20..400) plainMin else 115
    }

    private companion object {
        const val TAG = "VaultViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
