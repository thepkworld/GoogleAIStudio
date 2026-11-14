package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
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
import com.example.data.catalog.CatalogRepository
import com.example.data.model.WatchStatus
import com.example.data.repository.VaultRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOut
import com.example.ui.screens.AddCustomTitleSheet
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.ReviewsScreen
import com.example.ui.screens.TitleDetailScreen
import com.example.ui.screens.WatchedScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.CinemaAlabaster
import com.example.ui.theme.CinemaBorderSubtle
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaCrimsonContainer
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSilver
import com.example.ui.theme.CinemaSlate
import com.example.ui.theme.CinemaVelvet
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class VaultTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    BROWSE("Browse", Icons.Rounded.Explore, Icons.Outlined.Explore, "nav_browse"),
    WATCHLIST("Watchlist", Icons.Rounded.Bookmark, Icons.Outlined.BookmarkBorder, "nav_watchlist"),
    WATCHED("Watched", Icons.Rounded.CheckCircle, Icons.Outlined.CheckCircle, "nav_watched"),
    REVIEWS("Reviews", Icons.Rounded.RateReview, Icons.Outlined.RateReview, "nav_reviews")
}

private const val ROUTE_MAIN = "main"
private const val ROUTE_DETAIL = "detail/{titleId}"

@Composable
fun CineVaultApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = { /* authStateFlow reacts automatically */ })
    } else {
        key(user.uid) {
            AuthenticatedCineVault(currentUser = user)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedCineVault(currentUser: FirebaseUser) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val vaultViewModel: VaultViewModel = viewModel(
        key = currentUser.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                VaultViewModel(
                    repository = VaultRepository(db, Firebase.auth),
                    currentUserId = currentUser.uid
                )
            }
        }
    )

    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableStateOf(VaultTab.BROWSE) }
    var showAddCustomSheet by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val vaultState by vaultViewModel.vaultEntriesState.collectAsStateWithLifecycle()
    val vaultById by vaultViewModel.vaultById.collectAsStateWithLifecycle()
    val filteredCatalog by vaultViewModel.filteredCatalog.collectAsStateWithLifecycle()
    val browseQuery by vaultViewModel.browseQuery.collectAsStateWithLifecycle()
    val browseMediaFilter by vaultViewModel.browseMediaFilter.collectAsStateWithLifecycle()
    val browseGenre by vaultViewModel.browseGenre.collectAsStateWithLifecycle()

    val watchlistEntries by vaultViewModel.watchlistEntries.collectAsStateWithLifecycle()
    val watchlistFilter by vaultViewModel.watchlistFilter.collectAsStateWithLifecycle()
    val watchlistSort by vaultViewModel.watchlistSort.collectAsStateWithLifecycle()

    val watchedEntries by vaultViewModel.watchedEntries.collectAsStateWithLifecycle()
    val watchedFilter by vaultViewModel.watchedFilter.collectAsStateWithLifecycle()
    val watchedSort by vaultViewModel.watchedSort.collectAsStateWithLifecycle()

    val reviewedEntries by vaultViewModel.reviewedEntries.collectAsStateWithLifecycle()
    val reviewsFilter by vaultViewModel.reviewsFilter.collectAsStateWithLifecycle()
    val vaultStats by vaultViewModel.vaultStats.collectAsStateWithLifecycle()
    val isMutating by vaultViewModel.isMutating.collectAsStateWithLifecycle()

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
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { selectedTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
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
                                    Box {
                                        Surface(
                                            color = CinemaSlate,
                                            shape = CircleShape,
                                            border = BorderStroke(1.dp, CinemaBorderSubtle),
                                            modifier = Modifier.padding(end = 10.dp)
                                        ) {
                                            IconButton(
                                                onClick = { showAccountMenu = true },
                                                modifier = Modifier.testTag("account_menu_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Person,
                                                    contentDescription = "Account menu",
                                                    tint = CinemaGold
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = showAccountMenu,
                                            onDismissRequest = { showAccountMenu = false },
                                            containerColor = CinemaSlate
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = currentUser.displayName ?: "Cinema Member",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = CinemaAlabaster,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (!currentUser.email.isNullOrBlank()) {
                                                    Text(
                                                        text = currentUser.email ?: "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = CinemaSilver,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            HorizontalDivider(color = CinemaBorderSubtle)
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(id = R.string.sign_out),
                                                        color = CinemaCrimson
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                                        contentDescription = null,
                                                        tint = CinemaCrimson
                                                    )
                                                },
                                                onClick = {
                                                    showAccountMenu = false
                                                    signOut(
                                                        credentialManager = credentialManager,
                                                        scope = coroutineScope
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
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
                                            VaultTab.WATCHLIST -> vaultStats.totalWatchlist
                                            VaultTab.WATCHED -> vaultStats.totalWatched
                                            VaultTab.REVIEWS -> vaultStats.totalReviews
                                            else -> 0
                                        }
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { selectedTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title
                                                )
                                            },
                                            label = {
                                                val labelText = if (countBadge > 0) {
                                                    "${tab.title} ($countBadge)"
                                                } else {
                                                    tab.title
                                                }
                                                Text(
                                                    text = labelText,
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
                                VaultTab.BROWSE -> BrowseScreen(
                                    featuredTitle = vaultViewModel.featuredTitle,
                                    catalogTitles = filteredCatalog,
                                    allGenres = vaultViewModel.allGenres,
                                    searchQuery = browseQuery,
                                    selectedMediaFilter = browseMediaFilter,
                                    selectedGenre = browseGenre,
                                    vaultById = vaultById,
                                    onSearchQueryChange = vaultViewModel::updateBrowseQuery,
                                    onMediaFilterChange = vaultViewModel::updateBrowseMediaFilter,
                                    onGenreChange = vaultViewModel::updateBrowseGenre,
                                    onTitleClick = { id -> navController.navigate("detail/$id") },
                                    onQuickWatchlist = { title ->
                                        vaultViewModel.addOrMoveCatalogTitle(title, WatchStatus.WANT_TO_WATCH)
                                    },
                                    onQuickWatched = { title ->
                                        vaultViewModel.addOrMoveCatalogTitle(title, WatchStatus.WATCHED)
                                    },
                                    onAddCustomClick = { showAddCustomSheet = true }
                                )

                                VaultTab.WATCHLIST -> WatchlistScreen(
                                    entries = watchlistEntries,
                                    stats = vaultStats,
                                    selectedFilter = watchlistFilter,
                                    selectedSort = watchlistSort,
                                    onFilterChange = vaultViewModel::updateWatchlistFilter,
                                    onSortChange = vaultViewModel::updateWatchlistSort,
                                    onEntryClick = { id -> navController.navigate("detail/$id") },
                                    onTogglePriority = vaultViewModel::togglePriority,
                                    onMarkWatched = vaultViewModel::moveToWatched,
                                    onBrowseClick = { selectedTab = VaultTab.BROWSE }
                                )

                                VaultTab.WATCHED -> WatchedScreen(
                                    entries = watchedEntries,
                                    stats = vaultStats,
                                    selectedFilter = watchedFilter,
                                    selectedSort = watchedSort,
                                    onFilterChange = vaultViewModel::updateWatchedFilter,
                                    onSortChange = vaultViewModel::updateWatchedSort,
                                    onEntryClick = { id -> navController.navigate("detail/$id") },
                                    onBrowseClick = { selectedTab = VaultTab.BROWSE }
                                )

                                VaultTab.REVIEWS -> ReviewsScreen(
                                    reviewedEntries = reviewedEntries,
                                    stats = vaultStats,
                                    selectedFilter = reviewsFilter,
                                    onFilterChange = vaultViewModel::updateReviewsFilter,
                                    onEntryClick = { id -> navController.navigate("detail/$id") },
                                    onBrowseClick = { selectedTab = VaultTab.BROWSE }
                                )
                            }
                        }
                    }
                }
            }
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("titleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val titleId = backStackEntry.arguments?.getString("titleId") ?: return@composable
            val catalogTitle = remember(titleId) { CatalogRepository.findById(titleId) }
            val vaultEntry = vaultById[titleId]

            TitleDetailScreen(
                titleId = titleId,
                catalogTitle = catalogTitle,
                vaultEntry = vaultEntry,
                isMutating = isMutating,
                onBack = { navController.popBackStack() },
                onSetStatus = { status ->
                    if (vaultEntry != null) {
                        if (status == WatchStatus.WATCHED) {
                            vaultViewModel.moveToWatched(vaultEntry)
                        } else {
                            vaultViewModel.moveToWatchlist(vaultEntry)
                        }
                    } else if (catalogTitle != null) {
                        vaultViewModel.addOrMoveCatalogTitle(catalogTitle, status)
                    }
                },
                onTogglePriority = {
                    if (vaultEntry != null) {
                        vaultViewModel.togglePriority(vaultEntry)
                    }
                },
                onUpdateEpisodes = { nextEp ->
                    if (vaultEntry != null) {
                        vaultViewModel.updateEpisodeProgress(vaultEntry, nextEp)
                    }
                },
                onUpdateRewatch = { nextRewatch ->
                    if (vaultEntry != null) {
                        vaultViewModel.updateRewatchCount(vaultEntry, nextRewatch)
                    }
                },
                onSaveReview = { status, rating, rTitle, rBody, quote, spoilers ->
                    vaultViewModel.saveReviewAndRating(
                        catalogTitle = catalogTitle,
                        existingEntry = vaultEntry,
                        entryId = titleId,
                        status = status,
                        userRating = rating,
                        reviewTitle = rTitle,
                        reviewBody = rBody,
                        favoriteQuote = quote,
                        containsSpoilers = spoilers
                    )
                },
                onDeleteEntry = {
                    if (vaultEntry != null) {
                        vaultViewModel.deleteEntry(vaultEntry) {
                            if (catalogTitle == null) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            )
        }
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
