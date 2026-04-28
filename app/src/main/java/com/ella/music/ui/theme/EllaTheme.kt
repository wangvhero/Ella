package com.ella.music.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

const val THEME_FOLLOW_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

@Composable
fun EllaTheme(
    themeMode: Int = THEME_FOLLOW_SYSTEM,
    content: @Composable () -> Unit
) {
    val colorSchemeMode = when (themeMode) {
        THEME_LIGHT -> ColorSchemeMode.Light
        THEME_DARK -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }

    val controller = remember(colorSchemeMode) {
        ThemeController(colorSchemeMode = colorSchemeMode)
    }

    MiuixTheme(
        controller = controller,
        content = content
    )
}
