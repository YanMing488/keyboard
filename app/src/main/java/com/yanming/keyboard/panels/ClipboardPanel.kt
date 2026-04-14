package com.yanming.keyboard.panels

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import com.yanming.keyboard.ClipItem
import com.yanming.keyboard.ServerClient

// ═══════════════════════════════════════════════════════
// Clipboard Panel — fetched from server, nothing local
// ═══════════════════════════════════════════════════════

class ClipboardPanel(context: Context) : LinearLayout(context) {

    var onPaste: ((String) -> Unit)? = null

    private val BG    = Color.parseColor("#0c0805")
    private val SURF  = Color.parseColor("#1a1410")
    private val GOLD  = Color.parseColor("#c9932b")
    private val TEXT  = Color.parseColor("#f0dfc8")
    private val MUTED = Color.parseColor("#7a6a58")

    private val list: LinearLayout

    init {
        orientation = VERTICAL
        setBackgroundColor(BG)
        setPadding(12, 8, 12, 8)

        val header = TextView(context).apply {
            text      = "CLIPBOARD"
            textSize  = 9f
            setTextColor(MUTED)
            letterSpacing = 0.2f
            setPadding(4, 0, 4, 8)
        }
        addView(header)

        val scroll = ScrollView(context)
        list = LinearLayout(context).apply { orientation = VERTICAL }
        scroll.addView(list)
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        load()
    }

    fun load() {
        list.removeAllViews()
        val loading = TextView(context).apply {
            text      = "loading..."
            textSize  = 11f
            setTextColor(MUTED)
            gravity   = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        list.addView(loading)

        ServerClient.getClipboard { items ->
            list.removeAllViews()
            if (items.isEmpty()) {
                list.addView(TextView(context).apply {
                    text    = "No clipboard items yet"
                    textSize = 11f
                    setTextColor(MUTED)
                    gravity  = Gravity.CENTER
                    setPadding(0, 20, 0, 0)
                })
                return@getClipboard
            }
            items.take(20).forEach { addItem(it) }
        }
    }

    private fun addItem(item: ClipItem) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(12, 10, 12, 10)
            setBackgroundColor(SURF)
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 0, 4)
            layoutParams = lp
        }

        val txt = TextView(context).apply {
            text      = item.text
            textSize  = 11f
            setTextColor(TEXT)
            maxLines  = 2
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val btn = TextView(context).apply {
            text      = "paste"
            textSize  = 10f
            setTextColor(GOLD)
            setPadding(12, 0, 0, 0)
            setOnClickListener { onPaste?.invoke(item.text) }
        }

        row.addView(txt)
        row.addView(btn)
        list.addView(row)
    }
}
