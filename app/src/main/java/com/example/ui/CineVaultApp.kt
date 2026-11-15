package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
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
import com.example.auth.AuthSessionManager
import com.example.data.model.CatalogTitle
import com.example.data.model.DiscoverSort
import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.data.omdb.OmdbRepository
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.remote.VaultCloudRepository
import com.example.data.repository.VaultRepository
import com.example.data.tmdb.TmdbRepository
import com.example.ui.components.CineVaultDrawerHost
import com.example.ui.components.MarkSeenDialog
import com.example.ui.components.UnreleasedTitleDialog
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.CollectionDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingAuthScreen
import com.example.ui.screens.PersonDetailScreen
import com.example.ui.screens.ReviewsScreen
import com.example.ui.screens.SeasonEpisodesSheet
import com.example.ui.screens.SectionGridScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TitleDetailScreen
import com.example.ui.screens.WatchedScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.CinemaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

fun defaultCineVaultViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
            "APPLICATION_KEY missing from CreationExtras"
        }
        VaultViewModel(
            repository = VaultRepository(app),
            tmdbRepository = TmdbRepository(),
            omdbRepository = OmdbRepository(),
            cloudRepository = VaultCloudRepository(app),
            preferencesRepository = AppPreferencesRepository(app),
            authSessionManager = AuthSessionManager()
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineVaultApp(
    vaultViewModel: VaultViewModel = viewModel(factory = defaultCineVaultViewModelFactory())
) {
    val colors = CinemaTheme.colors
    val context = LocalContext.current
    val activityContext = remember(context) { context.findActivity() ?: context }
    val coroutineScope = rememberCoroutineScope()

    val preferencesLoaded by vaultViewModel.preferencesLoaded.collectAsStateWithLifecycle()
    val preferencesState by vaultViewModel.preferencesState.collectAsStateWithLifecycle()
    val authUser by vaultViewModel.authUser.collectAsStateWithLifecycle()
    val isAuthBusy by vaultViewModel.isAuthBusy.collectAsStateWithLifecycle()
    val authErrorMessage by vaultViewModel.authErrorMessage.collectAsStateWithLifecycle()
    val isSyncingCloud by vaultViewModel.isSyncingCloud.collectAsStateWithLifecycle()
    val pendingGuestMergeCount by vaultViewModel.pendingGuestMergeCount.collectAsStateWithLifecycle()
    val apiConnectionStatus by vaultViewModel.apiConnectionStatus.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vaultViewModel) {
        vaultViewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (!preferencesLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.obsidian),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.gold)
        }
        return
    }

    if (!preferencesState.hasCompletedOnboarding && authUser == null) {
        OnboardingAuthScreen(
            isBusy = isAuthBusy,
            errorMessage = authErrorMessage,
            onSignUpClick = { vaultViewModel.signInWithGoogle(activityContext, isSignUp = true) },
            onSignInClick = { vaultViewModel.signInWithGoogle(activityContext, isSignUp = false) },
            onContinueGuestClick = { vaultViewModel.continueAsGuest() },
            onDismissError = { vaultViewModel.clearAuthError() }
        )
        return
    }

    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableStateOf(VaultTab.HOME) }
    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var searchRetapTrigger by remember { mutableIntStateOf(0) }
    var searchFilterMenuTrigger by remember { mutableIntStateOf(0) }
    var showSeasonSheet by remember { mutableStateOf(false) }
    var pendingMarkSeen by remember { mutableStateOf<PendingMarkSeen?>(null) }
    var pendingUnreleased by remember { mutableStateOf<PendingUnreleased?>(null) }
    var activeSectionTitle by remember { mutableStateOf("") }
    var activeSectionItems by remember { mutableStateOf<List<CatalogTitle>>(emptyList()) }

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
    val watchlistViewMode by vaultViewModel.watchlistViewMode.collectAsStateWithLifecycle()

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
    val detailError by vaultViewModel.detailError.collectAsStateWithLifecycle()
    val personDetail by vaultViewModel.personDetail.collectAsStateWithLifecycle()
    val isLoadingPerson by vaultViewModel.isLoadingPerson.collectAsStateWithLifecycle()
    val collectionDetail by vaultViewModel.collectionDetail.collectAsStateWithLifecycle()
    val isLoadingCollection by vaultViewModel.isLoadingCollection.collectAsStateWithLifecycle()
    val seasonDetail by vaultViewModel.seasonDetail.collectAsStateWithLifecycle()
    val isLoadingSeason by vaultViewModel.isLoadingSeason.collectAsStateWithLifecycle()

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            vaultViewModel.exportVaultJson()
                .onSuccess { jsonPayload ->
                    val writeResult = withContext(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openOutputStream(uri)?.use { stream ->
                                stream.write(jsonPayload.toByteArray(Charsets.UTF_8))
                                stream.flush()
                            } ?: error("Could not open file for writing.")
                        }
                    }
                    writeResult
                        .onSuccess { snackbarHostState.showSnackbar("Vault & stats exported as JSON") }
                        .onFailure { err ->
                            snackbarHostState.showSnackbar(err.localizedMessage ?: "Export failed")
                        }
                }
                .onFailure { err ->
                    snackbarHostState.showSnackbar(err.localizedMessage ?: "Could not prepare export")
                }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val readResult = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                        it.readText()
                    } ?: error("Could not read selected file.")
                }
            }
            readResult
                .onSuccess { rawJson -> vaultViewModel.importVaultJson(rawJson) }
                .onFailure { err ->
                    snackbarHostState.showSnackbar(err.localizedMessage ?: "Failed to read backup file")
                }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_MAIN
    ) {
        composable(ROUTE_MAIN) {
            CineVaultDrawerHost(
                isOpen = isDrawerOpen,
                onClose = { isDrawerOpen = false },
                authUser = authUser,
                isAuthBusy = isAuthBusy,
                isSyncingCloud = isSyncingCloud,
                pendingGuestMergeCount = pendingGuestMergeCount,
                isDarkTheme = preferencesState.isDarkTheme,
                apiStatus = apiConnectionStatus,
                totalWatched = vaultStats.totalWatched,
                totalBucketlist = vaultStats.totalWatchlist,
                onSignInClick = {
                    isDrawerOpen = false
                    vaultViewModel.signInWithGoogle(activityContext, isSignUp = false)
                },
                onSignOutClick = {
                    isDrawerOpen = false
                    vaultViewModel.signOut(activityContext)
                },
                onSyncLocalClick = { vaultViewModel.syncLocalVaultToCloud() },
                onToggleTheme = { enabled -> vaultViewModel.setDarkTheme(enabled) },
                onRefreshFeeds = { vaultViewModel.refreshTmdbFeeds() },
                onExportJsonClick = {
                    exportJsonLauncher.launch("cinemapaglu_vault_backup.json")
                },
                onImportJsonClick = {
                    importJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.obsidian)
                ) {
                    val useNavigationRail = maxWidth >= 700.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = colors.velvet,
                                contentColor = colors.alabaster,
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
                                        onClick = {
                                            if (selectedTab == VaultTab.SEARCH && tab == VaultTab.SEARCH) {
                                                searchRetapTrigger++
                                            } else {
                                                selectedTab = tab
                                            }
                                        },
                                        icon = {
                                            TabIconWithBadge(
                                                icon = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title,
                                                countBadge = countBadge
                                            )
                                        },
                                        label = { Text(tab.title) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = colors.onGold,
                                            selectedTextColor = colors.gold,
                                            indicatorColor = colors.gold,
                                            unselectedIconColor = colors.silver,
                                            unselectedTextColor = colors.silver
                                        ),
                                        modifier = Modifier.testTag(tab.tag)
                                    )
                                }
                            }
                        }

                        Scaffold(
                            containerColor = colors.obsidian,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            snackbarHost = {
                                SnackbarHost(hostState = snackbarHostState) { data ->
                                    Snackbar(
                                        snackbarData = data,
                                        containerColor = colors.slate,
                                        contentColor = colors.alabaster,
                                        actionColor = colors.gold
                                    )
                                }
                            },
                            topBar = {
                                TopAppBar(
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = colors.obsidian,
                                        titleContentColor = colors.alabaster
                                    ),
                                    navigationIcon = {
                                        IconButton(
                                            onClick = { isDrawerOpen = true },
                                            modifier = Modifier.testTag("drawer_menu_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Menu,
                                                contentDescription = "Open navigation menu",
                                                tint = colors.alabaster
                                            )
                                        }
                                    },
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(9.dp))
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
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    color = colors.alabaster,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = selectedTab.title.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.gold
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
                                                tint = colors.gold
                                            )
                                        }

                                        StatusLivePill(
                                            apiStatus = apiConnectionStatus,
                                            onClick = { vaultViewModel.refreshTmdbFeeds() },
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                    }
                                )
                            },
                            floatingActionButton = {
                                if (selectedTab == VaultTab.SEARCH) {
                                    ExtendedFloatingActionButton(
                                        onClick = { searchFilterMenuTrigger++ },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Tune,
                                                contentDescription = null
                                            )
                                        },
                                        text = {
                                            val activeCount = browseUiState.selectedGenres.size +
                                                if (browseUiState.discoverSort != DiscoverSort.POPULARITY) 1 else 0
                                            Text(
                                                text = if (activeCount > 0) "Genre + Sort ($activeCount)" else "Genre + Sort",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        containerColor = colors.slateElevated,
                                        contentColor = colors.gold,
                                        shape = CircleShape,
                                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                                        modifier = Modifier.testTag("fab_genre_sort")
                                    )
                                }
                            },
                            bottomBar = {
                                if (!useNavigationRail) {
                                    NavigationBar(
                                        containerColor = colors.velvet,
                                        contentColor = colors.alabaster,
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
                                                onClick = {
                                                    if (selectedTab == VaultTab.SEARCH && tab == VaultTab.SEARCH) {
                                                        searchRetapTrigger++
                                                    } else {
                                                        selectedTab = tab
                                                    }
                                                },
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
                                                    selectedIconColor = colors.onGold,
                                                    selectedTextColor = colors.gold,
                                                    indicatorColor = colors.gold,
                                                    unselectedIconColor = colors.silver,
                                                    unselectedTextColor = colors.silver
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
                                        color = colors.crimsonContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = (vaultState as UiState.Error).message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.alabaster,
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
                                        retapTrigger = searchRetapTrigger,
                                        filterMenuTrigger = searchFilterMenuTrigger,
                                        onSearchQueryChange = vaultViewModel::updateBrowseQuery,
                                        onMediaFilterChange = vaultViewModel::updateBrowseMediaFilter,
                                        onGenreToggle = vaultViewModel::toggleBrowseGenre,
                                        onClearGenres = vaultViewModel::clearBrowseGenres,
                                        onDiscoverSortSelect = vaultViewModel::selectDiscoverSort,
                                        onSearchScopeSelect = vaultViewModel::selectSearchScope,
                                        onViewModeChange = vaultViewModel::selectBrowseViewMode,
                                        onLoadMore = vaultViewModel::loadNextPage,
                                        onRefreshTmdb = vaultViewModel::refreshTmdbFeeds,
                                        onTitleClick = { id -> navController.navigate("detail/$id") },
                                        onPersonClick = { pid -> navController.navigate("person/$pid") },
                                        onCollectionClick = { cid -> navController.navigate("collection/$cid") }
                                    )

                                    VaultTab.BUCKET_LIST -> WatchlistScreen(
                                        entries = watchlistEntries,
                                        stats = vaultStats,
                                        selectedFilter = watchlistFilter,
                                        selectedSort = watchlistSort,
                                        viewMode = watchlistViewMode,
                                        onFilterChange = vaultViewModel::updateWatchlistFilter,
                                        onSortChange = vaultViewModel::updateWatchlistSort,
                                        onViewModeChange = vaultViewModel::selectWatchlistViewMode,
                                        onEntryClick = { id -> navController.navigate("detail/$id") },
                                        onMarkWatched = { entry ->
                                            if (!entry.isReleased) {
                                                pendingUnreleased = PendingUnreleased(
                                                    title = entry.title,
                                                    releaseDate = entry.releaseDate,
                                                    releaseYear = entry.releaseYear.toInt(),
                                                    catalogTitle = vaultViewModel.resolveCatalogTitle(entry.catalogId),
                                                    existingEntry = entry
                                                )
                                            } else {
                                                pendingMarkSeen = PendingMarkSeen(
                                                    catalogTitle = vaultViewModel.resolveCatalogTitle(entry.catalogId),
                                                    existingEntry = entry
                                                )
                                            }
                                        },
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
                containerColor = colors.obsidian,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.obsidian,
                            titleContentColor = colors.alabaster
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.alabaster
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Reviews Journal",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.alabaster
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
                detailError = detailError,
                isMutating = isMutating,
                onRetry = { vaultViewModel.loadTitleDetails(titleId) },
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

    if (authUser != null && pendingGuestMergeCount > 0) {
        AlertDialog(
            onDismissRequest = { vaultViewModel.dismissGuestMergePrompt() },
            containerColor = colors.slate,
            titleContentColor = colors.alabaster,
            textContentColor = colors.silver,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CloudSync,
                    contentDescription = null,
                    tint = colors.gold
                )
            },
            title = { Text("Sync Local Vault to Cloud?") },
            text = {
                Text(
                    "You have $pendingGuestMergeCount local ${if (pendingGuestMergeCount == 1) "title" else "titles"} on this device that aren't backed up to ${authUser?.email?.ifBlank { "your account" } ?: "your account"} yet. Merge them now?"
                )
            },
            confirmButton = {
                Button(
                    onClick = { vaultViewModel.syncLocalVaultToCloud() },
                    enabled = !isSyncingCloud,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.gold,
                        contentColor = colors.onGold
                    ),
                    modifier = Modifier.testTag("guest_merge_confirm_button")
                ) {
                    Text(if (isSyncingCloud) "Syncing…" else "Sync Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vaultViewModel.dismissGuestMergePrompt() },
                    modifier = Modifier.testTag("guest_merge_dismiss_button")
                ) {
                    Text("Later", color = colors.silver)
                }
            },
            modifier = Modifier.testTag("guest_merge_dialog")
        )
    }
}

@Composable
private fun StatusLivePill(
    apiStatus: ApiConnectionStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemaTheme.colors
    val isLive = apiStatus != ApiConnectionStatus.OFFLINE
    val accent = when (apiStatus) {
        ApiConnectionStatus.TMDB_OMDB_LIVE -> colors.emerald
        ApiConnectionStatus.OMDB_FALLBACK -> colors.gold
        ApiConnectionStatus.OFFLINE -> colors.silver
    }
    val label = when (apiStatus) {
        ApiConnectionStatus.TMDB_OMDB_LIVE -> stringResource(id = R.string.tmdb_live)
        ApiConnectionStatus.OMDB_FALLBACK -> "OMDb Live"
        ApiConnectionStatus.OFFLINE -> stringResource(id = R.string.tmdb_offline)
    }

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
        color = if (isLive) accent.copy(alpha = 0.14f) else colors.slate,
        shape = CircleShape,
        border = BorderStroke(
            1.dp,
            if (isLive) accent.copy(alpha = 0.45f) else colors.borderSubtle
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
                            .background(accent)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh feeds",
                    tint = colors.gold,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isLive) accent else colors.alabaster,
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
    val colors = CinemaTheme.colors
    if (countBadge > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = colors.gold,
                    contentColor = colors.onGold
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
