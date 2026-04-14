package com.yanming.keyboard.panels

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import com.yanming.keyboard.ServerClient
import com.yanming.keyboard.Song

// ═══════════════════════════════════════════════════════
// Songs Panel — lyrics editor, synced to server
// ═══════════════════════════════════════════════════════

class SongsPanel(context: Context) : LinearLayout(context) {

    var onInsert: ((String) -> Unit)? = null

    private val BG    = Color.parseColor("#0c0805")
    private val SURF  = Color.parseColor("#1a1410")
    private val GOLD  = Color.parseColor("#c9932b")
    private val TEXT  = Color.parseColor("#f0dfc8")
    private val MUTED = Color.parseColor("#7a6a58")

    private val listView: LinearLayout
    private val editorView: LinearLayout
    private var currentSong: Song? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(BG)

        listView   = buildListView()
        editorView = buildEditorView()

        addView(listView)
        addView(editorView)

        showList()
        loadSongs()
    }

    private fun buildListView(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(12, 8, 12, 8)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
    }

    private fun buildEditorView(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(12, 8, 12, 8)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
    }

    private fun showList() {
        listView.visibility   = VISIBLE
        editorView.visibility = GONE
    }

    private fun showEditor(song: Song) {
        currentSong = song
        editorView.removeAllViews()

        val backBtn = TextView(context).apply {
            text     = "← back"
            textSize = 10f
            setTextColor(GOLD)
            setPadding(0, 0, 0, 8)
            setOnClickListener { showList() }
        }

        val title = TextView(context).apply {
            text     = song.title
            textSize = 15f
            setTextColor(TEXT)
            typeface = Typeface.create("serif", Typeface.ITALIC)
            setPadding(0, 0, 0, 10)
        }

        val editor = EditText(context).apply {
            setText(song.lyrics)
            textSize = 12f
            setTextColor(TEXT)
            setHintTextColor(MUTED)
            hint     = "write lyrics here..."
            background = null
            setBackgroundColor(SURF)
            setPadding(10, 10, 10, 10)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            gravity  = Gravity.TOP or Gravity.START
        }

        val actions = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(0, 8, 0, 0)
            gravity = Gravity.END
        }

        val insertBtn = TextView(context).apply {
            text     = "insert line"
            textSize = 10f
            setTextColor(GOLD)
            setPadding(0, 0, 16, 0)
            setOnClickListener {
                val sel = editor.selectionStart
                val line = editor.text.toString()
                    .substring(0, sel).substringAfterLast("\n")
                onInsert?.invoke(line)
            }
        }

        val saveBtn = TextView(context).apply {
            text     = "save"
            textSize = 10f
            setTextColor(GOLD)
        }

        actions.addView(insertBtn)
        actions.addView(saveBtn)

        editorView.addView(backBtn)
        editorView.addView(title)
        editorView.addView(editor)
        editorView.addView(actions)

        listView.visibility   = GONE
        editorView.visibility = VISIBLE
    }

    private fun loadSongs() {
        listView.removeAllViews()

        val header = TextView(context).apply {
            text      = "🎵 SONGS"
            textSize  = 9f
            setTextColor(MUTED)
            letterSpacing = 0.2f
            setPadding(0, 0, 0, 8)
        }
        listView.addView(header)

        val loading = TextView(context).apply {
            text     = "loading..."
            textSize = 11f
            setTextColor(MUTED)
            gravity  = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        listView.addView(loading)

        ServerClient.getSongs { songs ->
            listView.removeAllViews()
            listView.addView(header)

            if (songs.isEmpty()) {
                listView.addView(TextView(context).apply {
                    text     = "No songs yet — add them from the dashboard"
                    textSize = 11f
                    setTextColor(MUTED)
                    gravity  = Gravity.CENTER
                    setPadding(0, 20, 0, 0)
                })
                return@getSongs
            }

            val scroll = ScrollView(context)
            val inner  = LinearLayout(context).apply { orientation = VERTICAL }

            songs.forEach { song ->
                val row = LinearLayout(context).apply {
                    orientation = VERTICAL
                    setPadding(12, 10, 12, 10)
                    setBackgroundColor(SURF)
                    val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    lp.setMargins(0, 0, 0, 6)
                    layoutParams = lp
                    setOnClickListener { showEditor(song) }
                }

                val t = TextView(context).apply {
                    text     = song.title
                    textSize = 13f
                    setTextColor(TEXT)
                    typeface = Typeface.create("serif", Typeface.ITALIC)
                }
                val sub = TextView(context).apply {
                    val preview = song.lyrics.take(40).replace("\n", " ")
                    text     = if (preview.isNotEmpty()) preview + "…" else "empty"
                    textSize = 10f
                    setTextColor(MUTED)
                }

                row.addView(t)
                row.addView(sub)
                inner.addView(row)
            }

            scroll.addView(inner)
            listView.addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        }
    }
}
