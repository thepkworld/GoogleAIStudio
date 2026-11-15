package com.example.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.data.repository.VaultRepository
import com.example.data.tmdb.TmdbRepository
import com.example.ui.components.MarkSeenDialog
import com.example.ui.components.UnreleasedTitleDialog
import com.example.ui.screens.AddCustomTitleSheet
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.CollectionDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PersonDetailScreen
import com.example.ui.screens.ReviewsScreen
import com.example.ui.screens.SeasonEpisodesSheet
import com.example.ui.screens.SectionGridScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TitleDetailScreen
import com.example.ui.screens.WatchedScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaCrimsonContainer
import com.example.ui.theme.CinemaEmerald
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaVelvet
import kotlin.math.roundToInt

private enum class VaultTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    SEARCH("Search", Icons.Rounded.Search, Icons.Outlined.Search, "nav_search"),
    BUCKET_LIST("Bucketlist", Icons.Rounded.Bookmark, Icons.Outlined.BookmarkBorder, "nav_bucketlist"),
    LIBRARY("Library", Icons.Rounded.VideoLibrary, Icons.Outlined.VideoLibrary, "nav_library"),
    STATS("Stats", Icons.Rounded.BarChart, Icons.Outlined.BarChart, "nav_stats")
}

private data class PendingMarkSeen(
    val catalogTitle: CatalogTitle?,
    val existingEntry: VaultEntry?
)

private data class PendingUnreleased(
    val title: String,
    val releaseDate: String,
    val releaseYear: Int,
    val catalogTitle: CatalogTitle?,
    val existingEntry: VaultEntry?
)

private const val ROUTE_MAIN = "main"
private const val ROUTE_DETAIL = "detail/{titleId}"
private const val ROUTE_PERSON = "person/{personId}"
private const val ROUTE_COLLECTION = "collection/{collectionId}"
private const val ROUTE_REVIEWS = "reviews"
private const val ROUTE_SECTION = "section"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineVaultApp() {
    val vaultViewModel: VaultViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                VaultViewModel(
                    repository = VaultRepository(app),
                    tmdbRepository = TmdbRepository()
                )
            }
        }
    )

    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableStateOf(VaultTab.HOME) }
    var showAddCustomSheet by remember { mutableStateOf(false) }
    var showSeasonSheet by remember { mutableStateOf(false) }
    var pendingMarkSeen by remember { mutableStateOf<PendingMarkSeen?>(null) }
    var pendingUnreleased by remember { mutableStateOf<PendingUnreleased?>(null) }
    var activeSectionTitle by remember { mutableStateOf("") }
    var activeSectionItems by remember { mutableStateOf<List<CatalogTitle>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val vaultState by vaultViewModel.vaultEntriesState.collectAsStateWithLifecycle()
    val vaultById by vaultViewModel.vaultById.collectAsStateWithLifecycle()
    val homeUiState by vaultViewModel.homeUiState.collectAsStateWithLifecycle()
    val recentlyAddedCatalog by vaultViewModel.recentlyAddedCatalog.collectAsStateWithLifecycle()
    val browseUiState by vaultViewModel.browseUiState.collectAsStateWithLifecycle()
    val browseQuery by vaultViewModel.browseQuery.collectAsStateWithLifecycle()
    val browseMediaFilter by vaultViewModel.browseMediaFilter.collectAsStateWithLifecycle()

    val watchlistEntries by vaultViewModel.watchlistEntries.collectAsStateWithLifecycle()
    val watchlistFilter by vaultViewModel.watchlistFilter.collectAsStateWithLifecycle()
    val watchlistSort by vaultViewModel.watchlistSort.collectAsStateWithLifecycle()

    val watchedEntries by vaultViewModel.watchedEntries.collectAsStateWithLifecycle()
    val watchedSearchQuery by vaultViewModel.watchedSearchQuery.collectAsStateWithLifecycle()
    val watchedFilter by vaultViewModel.watchedFilter.collectAsStateWithLifecycle()
    val watchedTimeFilter by vaultViewModel.watchedTimeFilter.collectAsStateWithLifecycle()
    val watchedRatingFilter by vaultViewModel.watchedRatingFilter.collectAsStateWithLifecycle()
    val watchedGenreFilter by vaultViewModel.watchedGenreFilter.collectAsStateWithLifecycle()
    val watchedAvailableGenres by vaultViewModel.watchedAvailableGenres.collectAsStateWithLifecycle()
    val watchedSort by vaultViewModel.watchedSort.collectAsStateWithLifecycle()

    val reviewedEntries by vaultViewModel.reviewedEntries.collectAsStateWithLifecycle()
    val reviewsFilter by vaultViewModel.reviewsFilter.collectAsStateWithLifecycle()
    val vaultStats by vaultViewModel.vaultStats.collectAsStateWithLifecycle()
    val detailedStats by vaultViewModel.detailedStats.collectAsStateWithLifecycle()
    val isMutating by vaultViewModel.isMutating.collectAsStateWithLifecycle()

    val detailTitle by vaultViewModel.detailTitle.collectAsStateWithLifecycle()
    val isLoadingDetail by vaultViewModel.isLoadingDetail.collectAsStateWithLifecycle()
    val personDetail by vaultViewModel.personDetail.collectAsStateWithLifecycle()
    val isLoadingPerson by vaultViewModel.isLoadingPerson.collectAsStateWithLifecycle()
    val collectionDetail by vaultViewModel.collectionDetail.collectAsStateWithLifecycle()
    val isLoadingCollection by vaultViewModel.isLoadingCollection.collectAsStateWithLifecycle()
    val seasonDetail by vaultViewModel.seasonDetail.collectAsStateWithLifecycle()
    val isLoadingSeason by vaultViewModel.isLoadingSeason.collectAsStateWithLifecycle()

    LaunchedEffect(vaultViewModel) {
        vaultViewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_MAIN
    ) {
        composable(ROUTE_MAIN) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CinemaObsidian)
            ) {
                val useNavigationRail = maxWidth >= 700.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = CinemaVelvet,
                            contentColor = CinemaAlabaster,
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                        ) {
                            Spacer(modifier = Modifier.size(12.dp))
                            VaultTab.entries.forEach { tab ->
                                val selected = selectedTab == tab
                                val countBadge = when (tab) {
                                    VaultTab.BUCKET_LIST -> vaultStats.totalWatchlist
                                    VaultTab.LIBRARY -> vaultStats.totalWatched
                                    else -> 0
                                }
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { selectedTab = tab },
                                    icon = {
                                        TabIconWithBadge(
                                            icon = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            countBadge = countBadge
                                        )
                                    },
                                    label = { Text(tab.title) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = CinemaObsidian,
                                        selectedTextColor = CinemaGold,
                                        indicatorColor = CinemaGold,
                                        unselectedIconColor = CinemaSilver,
                                        unselectedTextColor = CinemaSilver
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }

                    Scaffold(
                        containerColor = CinemaObsidian,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    containerColor = CinemaSlate,
                                    contentColor = CinemaAlabaster,
                                    actionColor = CinemaGold
                                )
                            }
                        },
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = CinemaObsidian,
                                    titleContentColor = CinemaAlabaster
                                ),
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .background(CinemaGold.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MovieFilter,
                                                contentDescription = null,
                                                tint = CinemaGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = stringResource(id = R.string.app_name),
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = CinemaAlabaster
                                            )
                                            Text(
                                                text = selectedTab.title.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CinemaGold
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { navController.navigate(ROUTE_REVIEWS) },
                                        modifier = Modifier.testTag("open_reviews_journal_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.RateReview,
                                            contentDescription = "Open Reviews Journal",
                                            tint = CinemaGold
                                        )
                                    }

                                    StatusLivePill(
                                        isLive = browseUiState.isLiveTmdb,
                                        onClick = { vaultViewModel.refreshTmdbFeeds() },
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }
                            )
                        },
                        floatingActionButton = {
                            if (selectedTab != VaultTab.STATS) {
                                FloatingActionButton(
                                    onClick = { showAddCustomSheet = true },
                                    containerColor = CinemaGold,
                                    contentColor = CinemaObsidian,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("fab_add_title")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = stringResource(id = R.string.add_custom_title)
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (!useNavigationRail) {
                                NavigationBar(
                                    containerColor = CinemaVelvet,
                                    contentColor = CinemaAlabaster,
                                    windowInsets = WindowInsets.navigationBars
                                ) {
                                    VaultTab.entries.forEach { tab ->
                                        val selected = selectedTab == tab
                                        val countBadge = when (tab) {
                                            VaultTab.BUCKET_LIST -> vaultStats.totalWatchlist
                                            VaultTab.LIBRARY -> vaultStats.totalWatched
                                            else -> 0
                                        }
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { selectedTab = tab },
                                            icon = {
                                                TabIconWithBadge(
                                                    icon = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title,
                                                    countBadge = countBadge
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CinemaObsidian,
                                                selectedTextColor = CinemaGold,
                                                indicatorColor = CinemaGold,
                                                unselectedIconColor = CinemaSilver,
                                                unselectedTextColor = CinemaSilver
                                            ),
                                            modifier = Modifier.testTag(tab.tag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (vaultState is UiState.Error) {
                                Surface(
                                    color = CinemaCrimsonContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = (vaultState as UiState.Error).message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaAlabaster,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            when (selectedTab) {
                                VaultTab.HOME -> HomeScreen(
                                    homeState = homeUiState,
                                    recentlyAdded = recentlyAddedCatalog,
                                    keyConfigured = browseUiState.keyConfigured,
                                    errorMessage = browseUiState.errorMessage,
                                    vaultById = vaultById,
                                    onRefreshTmdb = vaultViewModel::refreshTmdbFeeds,
                                    onOttPlatformSelect = vaultViewModel::selectOttPlatform,
                                    onOpenSearch = { selectedTab = VaultTab.SEARCH },
                                    onGenreCollectionClick = { genreKeyword ->
                                        vaultViewModel.openGenreCollectionInSearch(genreKeyword)
                                        selectedTab = VaultTab.SEARCH
                                    },
                                    onViewAllClick = { title, items ->
                                        activeSectionTitle = title
                                        activeSectionItems = items
                                        navController.navigate(ROUTE_SECTION)
                                    },
                                    onTitleClick = { id -> navController.navigate("detail/$id") },
                                    onPersonClick = { pid -> navController.navigate("person/$pid") }
                                )

                                VaultTab.SEARCH -> BrowseScreen(
                                    browseState = browseUiState,
                                    localGenres = vaultViewModel.localGenres,
                                    searchQuery = browseQuery,
                                    selectedMediaFilter = browseMediaFilter,
                                    vaultById = vaultById,
                                    onSearchQueryChange = vaultViewModel::updateBrowseQuery,
                                    onMediaFilterChange = vaultViewModel::updateBrowseMediaFilter,
                                    onSectionSelect = vaultViewModel::selectTmdbSection,
                                    onGenreSelect = vaultViewModel::selectGenre,
                                    onDiscoverSortSelect = vaultViewModel::selectDiscoverSort,
                                    onSearchScopeSelect = vaultViewModel::selectSearchScope,
                                    onLoadMore = vaultViewModel::loadNextPage,
                                    onRefreshTmdb = vaultViewModel::refreshTmdbFeeds,
                                    onTitleClick = { id -> navController.navigate("detail/$id") },
                                    onPersonClick = { pid -> navController.navigate("person/$pid") },
                                    onCollectionClick = { cid -> navController.navigate("collection/$cid") },
                                    onAddCustomClick = { showAddCustomSheet = true }
                                )

                                VaultTab.BUCKET_LIST -> WatchlistScreen(
                                    entries = watchlistEntries,
                                    stats = vaultStats,
                                    selectedFilter = watchlistFilter,
                                    selectedSort = watchlistSort,
                                    onFilterChange = vaultViewModel::updateWatchlistFilter,
                                    onSortChange = vaultViewModel::updateWatchlistSort,
                                    onEntryClick = { id -> navController.navigate("detail/$id") },
                                    onTogglePriority = vaultViewModel::togglePriority,
                                    onBrowseClick = { selectedTab = VaultTab.HOME }
                                )

                                VaultTab.LIBRARY -> WatchedScreen(
                                    entries = watchedEntries,
                                    stats = vaultStats,
                                    searchQuery = watchedSearchQuery,
                                    selectedFilter = watchedFilter,
                                    selectedTimeFilter = watchedTimeFilter,
                                    selectedRatingFilter = watchedRatingFilter,
                                    selectedGenre = watchedGenreFilter,
                                    availableGenres = watchedAvailableGenres,
                                    selectedSort = watchedSort,
                                    onSearchQueryChange = vaultViewModel::updateWatchedSearchQuery,
                                    onFilterChange = vaultViewModel::updateWatchedFilter,
                                    onTimeFilterChange = vaultViewModel::updateWatchedTimeFilter,
                                    onRatingFilterChange = vaultViewModel::updateWatchedRatingFilter,
                                    onGenreChange = vaultViewModel::updateWatchedGenreFilter,
                                    onSortChange = vaultViewModel::updateWatchedSort,
                                    onResetFilters = vaultViewModel::resetWatchedFilters,
                                    onEntryClick = { id -> navController.navigate("detail/$id") },
                                    onOpenStats = { selectedTab = VaultTab.STATS },
                                    onBrowseClick = { selectedTab = VaultTab.HOME }
                                )

                                VaultTab.STATS -> StatsScreen(
                                    stats = detailedStats,
                                    onBrowseClick = { selectedTab = VaultTab.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }

        composable(ROUTE_SECTION) {
            SectionGridScreen(
                sectionTitle = activeSectionTitle.ifBlank { "Catalog" },
                items = activeSectionItems,
                vaultById = vaultById,
                onBack = { navController.popBackStack() },
                onTitleClick = { id -> navController.navigate("detail/$id") }
            )
        }

        composable(ROUTE_REVIEWS) {
            Scaffold(
                containerColor = CinemaObsidian,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CinemaObsidian,
                            titleContentColor = CinemaAlabaster
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = CinemaAlabaster
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Reviews Journal",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CinemaAlabaster
                            )
                        }
                    )
                }
            ) { innerPadding ->
                ReviewsScreen(
                    reviewedEntries = reviewedEntries,
                    stats = vaultStats,
                    selectedFilter = reviewsFilter,
                    onFilterChange = vaultViewModel::updateReviewsFilter,
                    onEntryClick = { id -> navController.navigate("detail/$id") },
                    onBrowseClick = {
                        navController.popBackStack()
                        selectedTab = VaultTab.HOME
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("titleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val titleId = backStackEntry.arguments?.getString("titleId") ?: return@composable
            LaunchedEffect(titleId) {
                vaultViewModel.loadTitleDetails(titleId)
            }
            val catalogTitle = detailTitle?.takeIf { it.id == titleId }
                ?: remember(titleId) { vaultViewModel.resolveCatalogTitle(titleId) }
            val vaultEntry = vaultById[titleId]

            TitleDetailScreen(
                titleId = titleId,
                catalogTitle = catalogTitle,
                vaultEntry = vaultEntry,
                isLoadingDetail = isLoadingDetail,
                isMutating = isMutating,
                onBack = { navController.popBackStack() },
                onRecommendationClick = { recId -> navController.navigate("detail/$recId") },
                onPersonClick = { pid -> navController.navigate("person/$pid") },
                onCollectionClick = { cid -> navController.navigate("collection/$cid") },
                onSeasonClick = { seriesId, seasonNum ->
                    showSeasonSheet = true
                    vaultViewModel.loadSeasonDetails(seriesId, seasonNum)
                },
                onSetStatus = { status ->
                    if (status == WatchStatus.WATCHED) {
                        val released = catalogTitle?.isReleased ?: vaultEntry?.isReleased ?: true
                        if (!released) {
                            val displayTitle = catalogTitle?.title?.ifBlank { vaultEntry?.title }
                                ?: vaultEntry?.title ?: "This movie"
                            val relDate = catalogTitle?.releaseDate?.ifBlank { vaultEntry?.releaseDate }
                                ?: vaultEntry?.releaseDate.orEmpty()
                            val relYear = catalogTitle?.releaseYear
                                ?: vaultEntry?.releaseYear?.toInt()
                                ?: 2027
                            pendingUnreleased = PendingUnreleased(
                                title = displayTitle,
                                releaseDate = relDate,
                                releaseYear = relYear,
                                catalogTitle = catalogTitle,
                                existingEntry = vaultEntry
                            )
                        } else {
                            pendingMarkSeen = PendingMarkSeen(
                                catalogTitle = catalogTitle,
                                existingEntry = vaultEntry
                            )
                        }
                    } else if (vaultEntry != null) {
                        vaultViewModel.moveToWatchlist(vaultEntry)
                    } else if (catalogTitle != null) {
                        vaultViewModel.addOrMoveCatalogTitle(catalogTitle, WatchStatus.WANT_TO_WATCH)
                    }
                },
                onTogglePriority = {
                    if (vaultEntry != null) vaultViewModel.togglePriority(vaultEntry)
                },
                onUpdateEpisodes = { nextEp ->
                    if (vaultEntry != null) vaultViewModel.updateEpisodeProgress(vaultEntry, nextEp)
                },
                onUpdateRewatch = { nextRewatch ->
                    if (vaultEntry != null) vaultViewModel.updateRewatchCount(vaultEntry, nextRewatch)
                },
                onDeleteEntry = {
                    if (vaultEntry != null) {
                        vaultViewModel.deleteEntry(vaultEntry) {
                            if (catalogTitle == null) navController.popBackStack()
                        }
                    }
                }
            )
        }

        composable(
            route = ROUTE_PERSON,
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: return@composable
            LaunchedEffect(personId) {
                vaultViewModel.loadPersonDetails(personId)
            }
            PersonDetailScreen(
                personDetail = personDetail?.takeIf { it.id == personId },
                isLoading = isLoadingPerson,
                vaultById = vaultById,
                onBack = { navController.popBackStack() },
                onTitleClick = { id -> navController.navigate("detail/$id") }
            )
        }

        composable(
            route = ROUTE_COLLECTION,
            arguments = listOf(navArgument("collectionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getInt("collectionId") ?: return@composable
            LaunchedEffect(collectionId) {
                vaultViewModel.loadCollectionDetails(collectionId)
            }
            CollectionDetailScreen(
                collectionDetail = collectionDetail?.takeIf { it.id == collectionId },
                isLoading = isLoadingCollection,
                vaultById = vaultById,
                onBack = { navController.popBackStack() },
                onTitleClick = { id -> navController.navigate("detail/$id") }
            )
        }
    }

    val activeMarkSeen = pendingMarkSeen
    if (activeMarkSeen != null) {
        val displayTitle = activeMarkSeen.existingEntry?.title
            ?: activeMarkSeen.catalogTitle?.title
            ?: "Untitled"
        val displayYear = activeMarkSeen.existingEntry?.releaseYear?.toInt()
            ?: activeMarkSeen.catalogTitle?.releaseYear
            ?: 2024
        val displayMediaType = activeMarkSeen.existingEntry?.mediaTypeEnum
            ?: activeMarkSeen.catalogTitle?.mediaType
            ?: MediaType.MOVIE
        val initialRating = activeMarkSeen.existingEntry?.userRating
            ?.takeIf { it > 0.0 }
            ?.roundToInt()
            ?: 0
        val initialHeadline = activeMarkSeen.existingEntry?.reviewTitle.orEmpty()
        val initialWatchedAt = activeMarkSeen.existingEntry?.watchedAt ?: 0L

        MarkSeenDialog(
            title = displayTitle,
            releaseYear = displayYear,
            mediaType = displayMediaType,
            initialRating = initialRating,
            initialHeadline = initialHeadline,
            initialWatchedAt = initialWatchedAt,
            isSaving = isMutating,
            onDismiss = { pendingMarkSeen = null },
            onConfirm = { rating, reviewHeadline, watchedAtMillis ->
                vaultViewModel.markSeenWithDetails(
                    catalogTitle = activeMarkSeen.catalogTitle,
                    existingEntry = activeMarkSeen.existingEntry,
                    rating = rating,
                    reviewTitle = reviewHeadline,
                    watchedAtMillis = watchedAtMillis,
                    onComplete = { pendingMarkSeen = null }
                )
            }
        )
    }

    val activeUnreleased = pendingUnreleased
    if (activeUnreleased != null) {
        val alreadyInBucketlist = activeUnreleased.existingEntry?.statusEnum == WatchStatus.WANT_TO_WATCH
        UnreleasedTitleDialog(
            title = activeUnreleased.title,
            releaseDate = activeUnreleased.releaseDate,
            releaseYear = activeUnreleased.releaseYear,
            alreadyInBucketlist = alreadyInBucketlist,
            onDismiss = { pendingUnreleased = null },
            onAddToBucketlist = {
                val cat = activeUnreleased.catalogTitle
                if (cat != null) {
                    vaultViewModel.addOrMoveCatalogTitle(cat, WatchStatus.WANT_TO_WATCH)
                }
                pendingUnreleased = null
            }
        )
    }

    if (showSeasonSheet) {
        SeasonEpisodesSheet(
            seasonDetail = seasonDetail,
            isLoading = isLoadingSeason,
            onDismiss = {
                showSeasonSheet = false
                vaultViewModel.clearSeasonDetail()
            }
        )
    }

    if (showAddCustomSheet) {
        AddCustomTitleSheet(
            isMutating = isMutating,
            onDismiss = { showAddCustomSheet = false },
            onSubmit = { title, mediaType, status, year, genres, director, runtime, synopsis, posterKey, totalEp ->
                vaultViewModel.createCustomTitle(
                    title = title,
                    mediaType = mediaType,
                    status = status,
                    releaseYear = year,
                    genres = genres,
                    directorOrCreator = director,
                    runtimeOrSeasons = runtime,
                    synopsis = synopsis,
                    posterKey = posterKey,
                    totalEpisodes = totalEp,
                    userRating = 0.0,
                    reviewTitle = "",
                    reviewBody = "",
                    favoriteQuote = "",
                    isPriority = status == WatchStatus.WANT_TO_WATCH,
                    onCreated = { newId ->
                        showAddCustomSheet = false
                        navController.navigate("detail/$newId")
                    }
                )
            }
        )
    }
}

@Composable
private fun StatusLivePill(
    isLive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "status_live_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Surface(
        color = if (isLive) CinemaEmerald.copy(alpha = 0.14f) else CinemaSlate,
        shape = CircleShape,
        border = BorderStroke(
            1.dp,
            if (isLive) CinemaEmerald.copy(alpha = 0.45f) else CinemaBorderSubtle
        ),
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("tmdb_status_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLive) {
                Box(
                    modifier = Modifier.size(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha * 0.35f
                            }
                            .clip(CircleShape)
                            .background(CinemaEmerald)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CinemaEmerald)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh feeds",
                    tint = CinemaGold,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isLive) {
                    stringResource(id = R.string.tmdb_live)
                } else {
                    stringResource(id = R.string.tmdb_offline)
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (isLive) CinemaEmerald else CinemaAlabaster,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TabIconWithBadge(
    icon: ImageVector,
    contentDescription: String,
    countBadge: Int
) {
    if (countBadge > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = CinemaGold,
                    contentColor = CinemaObsidian
                ) {
                    Text(
                        text = if (countBadge > 99) "99+" else "$countBadge",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    } else {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
