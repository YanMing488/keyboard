package com.yanming.keyboard.panels

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*

// ═══════════════════════════════════════════════════════
// Custom Emoji Panel — text symbols, no standard emoji
// ═══════════════════════════════════════════════════════

object YanEmoji {
    val EMOTIONS = listOf(
        "(っ◔◡◔)っ", "ヽ(•‿•)ノ", "(^_^)", "(-_-)", "(T_T)",
        "(≧◡≦)", "(¬‿¬)", "ʕ•ᴥ•ʔ", "(づ｡◕‿‿◕｡)づ", "＼(^o^)／",
        "(ง'̀-'́)ง", "(；￣Д￣)", "(°ロ°)", "(＃￣ω￣)", "(-_-)zzz",
        "(｡•́︿•̀｡)", "( ˘ ³˘)♥", "ヽ(ಠ益ಠ)ノ", "(•̀ᴗ•́)و", "(╥﹏╥)",
    )
    val MUSIC = listOf(
        "♪(´▽`)", "♫·*:.｡.", "~(˘▾˘~)", "(~˘▾˘)~", "ヾ(⌐■_■)ノ♪",
        "♩ ♪ ♫ ♬", "(っ˘ڡ˘ς)", "∩(︶▽︶)∩",
    )
    val PERSONAL = listOf(
        "颜明✦", "yanshine ✦", "✦ 颜明 ✦",
        "๑ᴖ◡ᴖ๑", "꒰˘̩̩̩⌣˘̩̩̩꒱", "( ˊ•̮ˋ )",
        "(っ•̀ω•́)っ✉", "✦•·····•✦",
    )
    val SYMBOLS = listOf(
        "✦", "◈", "◉", "◎", "▸", "△", "▣", "✕", "—",
        "·", "…", "「」", "『』", "【】", "《》",
    )
}

class EmojiPanel(context: Context) : ScrollView(context) {

    var onEmoji: ((String) -> Unit)? = null

    private val BG    = Color.parseColor("#0c0805")
    private val SURF  = Color.parseColor("#1a1410")
    private val GOLD  = Color.parseColor("#c9932b")
    private val TEXT  = Color.parseColor("#f0dfc8")
    private val MUTED = Color.parseColor("#7a6a58")

    init {
        setBackgroundColor(BG)
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
        }

        addSection(container, "EMOTIONS", YanEmoji.EMOTIONS)
        addSection(container, "MUSIC", YanEmoji.MUSIC)
        addSection(container, "PERSONAL", YanEmoji.PERSONAL)
        addSection(container, "SYMBOLS", YanEmoji.SYMBOLS)

        addView(container)
    }

    private fun addSection(parent: LinearLayout, title: String, items: List<String>) {
        val label = TextView(context).apply {
            text      = title
            textSize  = 8f
            setTextColor(MUTED)
            letterSpacing = 0.2f
            setPadding(4, 10, 4, 4)
        }
        parent.addView(label)

        val grid = GridLayout(context).apply {
            columnCount = 5
            setBackgroundColor(BG)
        }

        items.forEach { emoji ->
            val btn = TextView(context).apply {
                text      = emoji
                textSize  = if (emoji.length > 4) 11f else 16f
                setTextColor(TEXT)
                gravity   = Gravity.CENTER
                setPadding(8, 10, 8, 10)
                setBackgroundColor(SURF)
                val lp = GridLayout.LayoutParams().apply {
                    width  = GridLayout.LayoutParams.WRAP_CONTENT
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    setMargins(3, 3, 3, 3)
                }
                layoutParams = lp
                setOnClickListener { onEmoji?.invoke(emoji) }
            }
            grid.addView(btn)
        }

        parent.addView(grid)
    }
}
