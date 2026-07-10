package com.kaze.player.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kaze.player.player.PlayerManager
import com.kaze.player.ui.components.MiniPlayer
import com.kaze.player.ui.screens.AlbumDetailScreen
import com.kaze.player.ui.screens.AlbumsScreen
import com.kaze.player.ui.screens.ArtistDetailScreen
import com.kaze.player.ui.screens.ArtistsScreen
import com.kaze.player.ui.screens.HomeScreen
import com.kaze.player.ui.screens.PlayerScreen
import com.kaze.player.ui.screens.QueueScreen
import com.kaze.player.ui.screens.SearchScreen
import com.kaze.player.ui.screens.SettingsScreen
import com.kaze.player.ui.screens.SongsScreen
import com.kaze.player.viewmodel.LibraryViewModel
import com.kaze.player.viewmodel.PlayerViewModel

@Composable
fun KazeApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val playerManager = remember { PlayerManager(context.applicationContext) }
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.factory(context.applicationContext as android.app.Application)
    )
    val playerViewModel: PlayerViewModel = viewModel(factory = PlayerViewModel.factory(playerManager))

    androidx.compose.runtime.LaunchedEffect(Unit) {
        playerManager.connect()
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            playerManager.disconnect()
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home,
                modifier = Modifier.fillMaxSize()
            ) {
                composable<Screen.Home> {
                    HomeScreen(
                        libraryViewModel = libraryViewModel,
                        playerViewModel = playerViewModel,
                        onNavigate = { navController.navigate(it) },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.Songs> {
                    SongsScreen(
                        viewModel = libraryViewModel,
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.Albums> {
                    AlbumsScreen(
                        viewModel = libraryViewModel,
                        onAlbumClick = { navController.navigate(Screen.AlbumDetail(it)) },
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.AlbumDetail> { backStackEntry ->
                    val route = backStackEntry.toRoute<Screen.AlbumDetail>()
                    AlbumDetailScreen(
                        albumId = route.albumId,
                        viewModel = libraryViewModel,
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.Artists> {
                    ArtistsScreen(
                        viewModel = libraryViewModel,
                        onArtistClick = { navController.navigate(Screen.ArtistDetail(it)) },
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.ArtistDetail> { backStackEntry ->
                    val route = backStackEntry.toRoute<Screen.ArtistDetail>()
                    ArtistDetailScreen(
                        artistId = route.artistId,
                        viewModel = libraryViewModel,
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.Search> {
                    SearchScreen(
                        viewModel = libraryViewModel,
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.Player> {
                    PlayerScreen(
                        viewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        onQueue = { navController.navigate(Screen.Queue) },
                        contentPadding = PaddingValues(0.dp)
                    )
                }
                composable<Screen.Queue> {
                    QueueScreen(
                        viewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable<Screen.Settings> {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
            }

            AnimatedVisibility(
                visible = playerViewModel.state.value.currentSong != null &&
                    navController.currentDestination?.route != Screen.Player::class.qualifiedName,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            ) {
                MiniPlayer(
                    playerViewModel = playerViewModel,
                    onClick = { navController.navigate(Screen.Player) },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }
    }
}
