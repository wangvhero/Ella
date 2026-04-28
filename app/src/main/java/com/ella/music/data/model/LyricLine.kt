package com.ella.music.data.model

data class LyricLine(
    val timeMs: Long,
    val text: String,
    val words: List<LyricWord> = emptyList()
)

data class LyricWord(
    val text: String,
    val startMs: Long,
    val endMs: Long
)
