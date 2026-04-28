package com.ella.music.player

import android.content.Context
import android.content.Intent
import android.util.Log

class TickerBridge(private val context: Context) {

    companion object {
        private const val TAG = "TickerBridge"
        private const val ACTION_SEND_LYRIC = "com.meizu.flyme.ticker.ACTION_SEND"
        private const val ACTION_CLEAR_LYRIC = "com.meizu.flyme.ticker.ACTION_CLEAR"
    }

    private var enabled = true
    private var lastText: String? = null

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) clearLyric()
    }

    fun isEnabled() = enabled

    fun sendLyric(text: String?) {
        if (!enabled) return
        if (text == lastText) return
        lastText = text

        try {
            if (text.isNullOrEmpty()) {
                clearLyric()
                return
            }
            val intent = Intent(ACTION_SEND_LYRIC).apply {
                putExtra("ticker_text", text)
                putExtra("ticker_package", context.packageName)
                putExtra("ticker_app_name", "Ella Music")
            }
            context.sendBroadcast(intent)
            Log.d(TAG, "Ticker lyric sent: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send ticker lyric", e)
        }
    }

    fun clearLyric() {
        lastText = null
        try {
            val intent = Intent(ACTION_CLEAR_LYRIC).apply {
                putExtra("ticker_package", context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (_: Exception) {}
    }
}
