package com.ella.music.data.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.ella.music.data.model.Album
import com.ella.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset

class MusicScanner(private val context: Context) {

    companion object {
        private const val TAG = "MusicScanner"
    }

    suspend fun scanAllSongs(minDurationMs: Long = 0): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection, projection, selection, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                var title = cursor.getString(titleCol) ?: ""
                var artist = cursor.getString(artistCol) ?: ""
                var album = cursor.getString(albumCol) ?: ""
                val albumId = cursor.getLong(albumIdCol)
                var duration = cursor.getLong(durationCol)
                val path = cursor.getString(dataCol) ?: ""
                val fileName = cursor.getString(nameCol) ?: ""
                val size = cursor.getLong(sizeCol)
                val mime = cursor.getString(mimeCol) ?: ""

                if (path.isEmpty()) continue
                val file = File(path)
                if (!file.exists()) continue

                val needsRetriever = title.isBlank() || artist.isBlank() || album.isBlank() || duration <= 0

                if (needsRetriever) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(path)
                        if (title.isBlank()) title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                        if (artist.isBlank()) artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                        if (album.isBlank()) album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
                        if (duration <= 0) duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        retriever.release()
                    } catch (_: Exception) {}
                }

                if (title.isBlank()) title = fileName.substringBeforeLast('.')
                if (artist.isBlank()) artist = "Unknown"
                if (album.isBlank()) album = "Unknown"

                if (duration > 0 && duration >= minDurationMs) {
                    songs.add(Song(id, title, artist, album, albumId, duration, path, fileName, size, mime))
                }
            }
        }
        songs
    }

    suspend fun scanAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        val collection = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )
        context.contentResolver.query(collection, projection, null, null, "${MediaStore.Audio.Albums.ALBUM} ASC")?.use { cursor ->
            while (cursor.moveToNext()) {
                albums.add(Album(
                    cursor.getLong(0),
                    cursor.getString(1) ?: "Unknown",
                    cursor.getString(2) ?: "Unknown",
                    cursor.getInt(3),
                    cursor.getInt(4)
                ))
            }
        }
        albums
    }

    fun extractEmbeddedLyrics(path: String): String? {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val lyrics = try { retriever.extractMetadata(1000) } catch (_: Exception) { null }
            retriever.release()
            if (!lyrics.isNullOrBlank()) return lyrics
        } catch (_: Exception) {}

        return try { parseId3Lyrics(File(path)) } catch (_: Exception) { null }
    }

    fun extractReplayGain(path: String): Float? {
        return try { parseId3ReplayGain(File(path)) } catch (_: Exception) { null }
    }

    fun getAlbumArtUri(albumId: Long): Uri =
        ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)

    private fun parseId3Lyrics(file: File): String? {
        RandomAccessFile(file, "r").use { raf ->
            val header = ByteArray(10)
            if (raf.read(header) < 10) return null
            if (header[0] != 'I'.code.toByte() || header[1] != 'D'.code.toByte() || header[2] != '3'.code.toByte()) return null

            val majorVer = header[3].toInt() and 0xFF
            val tagSize = synchsafeToInt(header.copyOfRange(6, 10))
            val endPos = raf.filePointer + tagSize

            while (raf.filePointer < endPos && raf.filePointer < file.length()) {
                if (majorVer >= 3) {
                    val frameId = String(raf.readNBytes(4), Charset.forName("ISO-8859-1"))
                    if (frameId.isBlank() || frameId[0] == '\u0000') break
                    val frameSize = if (majorVer == 4) synchsafeToInt(raf.readNBytes(4)) else raf.readInt()
                    raf.skipBytes(2)
                    if (frameSize <= 0 || raf.filePointer + frameSize > endPos) break
                    val frameData = raf.readNBytes(frameSize)

                    if (frameId == "USLT") {
                        return parseUsltFrame(frameData)
                    }
                } else {
                    break
                }
            }
        }
        return null
    }

    private fun parseId3ReplayGain(file: File): Float? {
        RandomAccessFile(file, "r").use { raf ->
            val header = ByteArray(10)
            if (raf.read(header) < 10) return null
            if (header[0] != 'I'.code.toByte() || header[1] != 'D'.code.toByte() || header[2] != '3'.code.toByte()) return null

            val majorVer = header[3].toInt() and 0xFF
            val tagSize = synchsafeToInt(header.copyOfRange(6, 10))
            val endPos = raf.filePointer + tagSize

            while (raf.filePointer < endPos && raf.filePointer < file.length()) {
                if (majorVer >= 3) {
                    val frameId = String(raf.readNBytes(4), Charset.forName("ISO-8859-1"))
                    if (frameId.isBlank() || frameId[0] == '\u0000') break
                    val frameSize = if (majorVer == 4) synchsafeToInt(raf.readNBytes(4)) else raf.readInt()
                    raf.skipBytes(2)
                    if (frameSize <= 0 || raf.filePointer + frameSize > endPos) break
                    val frameData = raf.readNBytes(frameSize)

                    if (frameId == "TXXX") {
                        val text = parseTxxxFrame(frameData)
                        if (text?.first?.equals("REPLAYGAIN_TRACK_GAIN", ignoreCase = true) == true) {
                            return Regex("([+-]?[0-9.]+)").find(text.second ?: "")?.groupValues?.get(1)?.toFloatOrNull()
                        }
                    }
                } else {
                    break
                }
            }
        }
        return null
    }

    private fun parseUsltFrame(data: ByteArray): String? {
        if (data.size < 4) return null
        val encoding = data[0].toInt() and 0xFF
        val charset = when (encoding) {
            0 -> Charset.forName("ISO-8859-1")
            1 -> Charset.forName("UTF-16")
            2 -> Charset.forName("UTF-16BE")
            3 -> Charset.forName("UTF-8")
            else -> Charset.forName("UTF-8")
        }
        val lang = String(data, 1, 3, Charset.forName("ISO-8859-1"))
        var offset = 4
        if (encoding == 1 || encoding == 2) {
            offset += 2
            while (offset + 1 < data.size && (data[offset] != 0.toByte() || data[offset + 1] != 0.toByte())) offset += 2
            offset += 2
        } else {
            while (offset < data.size && data[offset] != 0.toByte()) offset++
            offset++
        }
        if (offset >= data.size) return null
        return String(data, offset, data.size - offset, charset).trim()
    }

    private fun parseTxxxFrame(data: ByteArray): Pair<String?, String?>? {
        if (data.size < 2) return null
        val encoding = data[0].toInt() and 0xFF
        val charset = when (encoding) {
            0 -> Charset.forName("ISO-8859-1")
            1 -> Charset.forName("UTF-16")
            2 -> Charset.forName("UTF-16BE")
            3 -> Charset.forName("UTF-8")
            else -> Charset.forName("UTF-8")
        }
        var offset = 1
        var descEnd = data.size
        for (i in offset until data.size) {
            if (data[i] == 0.toByte()) { descEnd = i; break }
        }
        val desc = String(data, offset, descEnd - offset, charset)
        offset = descEnd + 1
        if (offset >= data.size) return Pair(desc, null)
        val value = String(data, offset, data.size - offset, charset)
        return Pair(desc, value)
    }

    private fun synchsafeToInt(bytes: ByteArray): Int {
        var result = 0
        for (b in bytes) {
            result = (result shl 7) or (b.toInt() and 0x7F)
        }
        return result
    }

    private fun RandomAccessFile.readNBytes(n: Int): ByteArray {
        val buf = ByteArray(n)
        readFully(buf)
        return buf
    }
}
