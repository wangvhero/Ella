package com.ella.music.ui.about

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ella.music.BuildConfig
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scrollValue = scrollState.value.toFloat()
    val maxScroll = 400f

    val logoScale = (1f - (scrollValue / maxScroll) * 0.2f).coerceIn(0.7f, 1f)
    val logoAlpha = (1f - (scrollValue / maxScroll) * 1.5f).coerceIn(0f, 1f)
    val titleAlpha = (scrollValue / maxScroll * 2f).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animTime"
    )

    val bgColors = listOf(
        Color(0xFF93C5FD), Color(0xFFFDA4AF), Color(0xFFC4B5FD),
        Color(0xFFFDE68A), Color(0xFF86EFAC)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val rad = Math.toRadians(animTime.toDouble()).toFloat()
                    for (i in bgColors.indices) {
                        val angle = rad + i * 1.2f
                        val cx = w * (0.3f + 0.4f * kotlin.math.cos(angle + i * 0.7f))
                        val cy = h * (0.3f + 0.3f * kotlin.math.sin(angle + i * 0.9f))
                        drawCircle(
                            color = bgColors[i].copy(alpha = 0.25f),
                            radius = w * 0.5f,
                            center = Offset(cx, cy)
                        )
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = MiuixIcons.Regular.Back,
                        contentDescription = "返回",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "关于",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = titleAlpha },
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                            alpha = logoAlpha
                        }
                        .padding(top = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MiuixTheme.colorScheme.primary,
                                        MiuixTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Regular.Music,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Ella Music", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "v1.0.0",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "编译于 ${BuildConfig.BUILD_TIME}",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "一款简洁优雅的本地音乐播放器",
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "致谢",
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                val uriHandler = LocalUriHandler.current

                Card(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "Mimo-V2.5-Pro",
                        summary = "主要开发",
                        onClick = { uriHandler.openUri("https://github.com/Kifranei/Ella") },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    )
                }

                Card(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "Miuix",
                        summary = "MIUI/HyperOS 风格 Compose UI 组件库",
                        onClick = { uriHandler.openUri("https://github.com/miuix-kmp/miuix") },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    )
                }

                Card(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "ExoPlayer (Media3)",
                        summary = "Google 开源媒体播放库",
                        onClick = { uriHandler.openUri("https://github.com/androidx/media") },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    )
                }

                Card(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "Lyricon",
                        summary = "状态栏歌词扩展模块",
                        onClick = { uriHandler.openUri("https://github.com/proify/lyricon") },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    )
                }

                Card(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "JAudioTagger",
                        summary = "音频标签读写库",
                        onClick = { uriHandler.openUri("https://github.com/ijabergern/jaudiotagger") },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    )
                }

                Card(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "Coil",
                        summary = "Kotlin 图片加载库",
                        onClick = { uriHandler.openUri("https://github.com/coil-kt/coil") },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "© 2026 Ella Music. All rights reserved.",
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}
