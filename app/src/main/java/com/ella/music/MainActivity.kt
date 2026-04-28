package com.ella.music

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ella.music.data.SettingsManager
import com.ella.music.ui.components.MiniPlayer
import com.ella.music.ui.navigation.AppNavigation
import com.ella.music.ui.navigation.Screen
import com.ella.music.ui.theme.EllaTheme
import com.ella.music.ui.theme.THEME_DARK
import com.ella.music.ui.theme.THEME_FOLLOW_SYSTEM
import com.ella.music.viewmodel.MainViewModel
import com.ella.music.viewmodel.PlayerViewModel
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Settings

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mainViewModel?.scanMusic()
        }
    }

    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainVm: MainViewModel = viewModel()
            val playerVm: PlayerViewModel = viewModel()
            mainViewModel = mainVm

            val settingsManager = remember { SettingsManager(this@MainActivity) }
            val themeMode by settingsManager.themeMode.collectAsState(initial = 0)

            val isDark = when (themeMode) {
                THEME_DARK -> true
                THEME_FOLLOW_SYSTEM -> isSystemInDarkTheme()
                else -> false
            }

            val view = LocalView.current
            LaunchedEffect(isDark) {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }

            LaunchedEffect(Unit) {
                checkAndRequestPermissions()
                mainVm.scanMusic()
            }

            EllaTheme(themeMode = themeMode) {
                EllaApp(mainVm, playerVm)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        }
    }
}

@Composable
fun EllaApp(
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Home.route,
        Screen.Album.route,
        Screen.Folder.route,
        Screen.Settings.route
    )
    val showBottomBar = currentRoute in bottomBarScreens

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val showMiniPlayer = currentSong != null && currentRoute != Screen.Player.route

    Scaffold(
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = showMiniPlayer,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            albumArtUri = mainViewModel.getAlbumArtUri(song.albumId),
                            onClick = { navController.navigate(Screen.Player.route) },
                            onPlayPause = { playerViewModel.togglePlayPause() },
                            onSkipNext = { playerViewModel.skipToNext() },
                            onSkipPrevious = { playerViewModel.skipToPrevious() }
                        )
                    }
                }

                AnimatedVisibility(visible = showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == Screen.Home.route,
                            onClick = {
                                if (currentRoute != Screen.Home.route) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            },
                            icon = MiuixIcons.Regular.Music,
                            label = "首页"
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Album.route,
                            onClick = {
                                if (currentRoute != Screen.Album.route) {
                                    navController.navigate(Screen.Album.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            },
                            icon = MiuixIcons.Regular.Album,
                            label = "专辑"
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Folder.route,
                            onClick = {
                                if (currentRoute != Screen.Folder.route) {
                                    navController.navigate(Screen.Folder.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            },
                            icon = MiuixIcons.Regular.Folder,
                            label = "文件夹"
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Settings.route,
                            onClick = {
                                if (currentRoute != Screen.Settings.route) {
                                    navController.navigate(Screen.Settings.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            },
                            icon = MiuixIcons.Regular.Settings,
                            label = "设置"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AppNavigation(
                navController = navController,
                mainViewModel = mainViewModel,
                playerViewModel = playerViewModel
            )
        }
    }
}
