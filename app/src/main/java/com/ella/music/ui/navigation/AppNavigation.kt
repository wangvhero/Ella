package com.ella.music.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ella.music.ui.about.AboutScreen
import com.ella.music.ui.album.AlbumDetailScreen
import com.ella.music.ui.album.AlbumScreen
import com.ella.music.ui.folder.FolderDetailScreen
import com.ella.music.ui.folder.FolderScreen
import com.ella.music.ui.home.HomeScreen
import com.ella.music.ui.player.PlayerScreen
import com.ella.music.ui.settings.SettingsScreen
import com.ella.music.viewmodel.MainViewModel
import com.ella.music.viewmodel.PlayerViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Album : Screen("album")
    data object AlbumDetail : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    data object Folder : Screen("folder")
    data object FolderDetail : Screen("folder/{folderPath}") {
        fun createRoute(folderPath: String) = "folder/${java.net.URLEncoder.encode(folderPath, "UTF-8")}"
    }
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object Player : Screen("player")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
            )
        },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(300)
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                mainViewModel = mainViewModel,
                playerViewModel = playerViewModel,
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Album.route) {
            AlbumScreen(
                mainViewModel = mainViewModel,
                playerViewModel = playerViewModel,
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                }
            )
        }

        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
            AlbumDetailScreen(
                albumId = albumId,
                mainViewModel = mainViewModel,
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
            )
        }

        composable(Screen.Folder.route) {
            FolderScreen(
                mainViewModel = mainViewModel,
                onFolderClick = { folderPath ->
                    navController.navigate(Screen.FolderDetail.createRoute(folderPath))
                }
            )
        }

        composable(
            route = Screen.FolderDetail.route,
            arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderPath = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("folderPath") ?: "",
                "UTF-8"
            )
            FolderDetailScreen(
                folderPath = folderPath,
                mainViewModel = mainViewModel,
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                playerViewModel = playerViewModel
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Player.route) {
            PlayerScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
