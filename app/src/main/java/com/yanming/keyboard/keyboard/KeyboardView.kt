package com.yanming.keyboard.keyboard

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

// ═══════════════════════════════════════════════════════
// YanMing Keyboard — Custom Canvas View
// No XML layouts for keys — drawn entirely on Canvas
// ═══════════════════════════════════════════════════════

class KeyboardView(context: Context) : View(context) {

    // Colors — Dark Cinematic (yanshine.id palette)
    private val BG       = Color.parseColor("#0c0805")
    private val KEY_BG   = Color.parseColor("#1a1410")
    private val KEY_BG2  = Color.parseColor("#241c14")  // special keys
    private val KEY_PR   = Color.parseColor("#c9932b")  // pressed
    private val BORDER   = Color.parseColor("#2a2018")
    private val TEXT_C   = Color.parseColor("#f0dfc8")
    private val MUTED    = Color.parseColor("#7a6a58")
    private val GOLD     = Color.parseColor("#c9932b")

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = TEXT_C
        textAlign = Paint.Align.CENTER
        typeface  = Typeface.create("serif", Typeface.NORMAL)
    }
    private val altPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = MUTED
        textAlign = Paint.Align.RIGHT
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style     = Paint.Style.STROKE
        color     = BORDER
        strokeWidth = 0.8f
    }

    var lang: Lang = Lang.LATIN
    var shifted = false
    var hebrewMode = false
    var symbolMode = false

    var onKey: ((Key) -> Unit)? = null

    private var pressedKey: Key? = null
    private var keys: List<Pair<Key, RectF>> = emptyList()

    private val HPAD = 6f  // horizontal padding between keys
    private val VPAD = 5f  // vertical padding between rows
    private val RADIUS = 8f

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(BG)
        buildLayout()
        drawKeys(canvas)
    }

    private fun buildLayout() {
        val rows = getRows()
        val w    = width.toFloat()
        val rowH = (height - VPAD * (rows.size + 1)) / rows.size
        val built = mutableListOf<Pair<Key, RectF>>()

        rows.forEachIndexed { ri, row ->
            val totalUnits = row.sumOf { it.widthRatio.toDouble() }.toFloat()
            val unitW = (w - HPAD * (row.size + 1)) / totalUnits
            var x = HPAD
            val y = VPAD + ri * (rowH + VPAD)

            row.forEach { key ->
                val kw = key.widthRatio * unitW
                built.add(Pair(key, RectF(x, y, x + kw, y + rowH)))
                x += kw + HPAD
            }
        }
        keys = built
    }

    private fun drawKeys(canvas: Canvas) {
        val rows = getRows()
        val w    = width.toFloat()
        val rowH = (height - VPAD * (rows.size + 1)) / rows.size
        txtPaint.textSize = rowH * 0.38f
        altPaint.textSize = rowH * 0.22f

        keys.forEach { (key, rect) ->
            // Background
            val isSpecial = key.type != KeyType.CHAR
            val isPressed = pressedKey == key
            keyPaint.color = when {
                isPressed    -> KEY_PR
                isSpecial    -> KEY_BG2
                else         -> KEY_BG
            }
            canvas.drawRoundRect(rect, RADIUS, RADIUS, keyPaint)
            canvas.drawRoundRect(rect, RADIUS, RADIUS, borderPaint)

            // Label
            val cx = rect.centerX()
            val cy = rect.centerY() - (txtPaint.descent() + txtPaint.ascent()) / 2f

            txtPaint.color = when {
                isPressed                 -> BG
                key.type == KeyType.SHIFT && shifted -> GOLD
                key.type == KeyType.HEBREW_TOGGLE && hebrewMode -> GOLD
                isSpecial                 -> MUTED
                else                      -> TEXT_C
            }

            canvas.drawText(displayLabel(key), cx, cy, txtPaint)

            // Alt label (top-right corner)
            if (key.altLabel.isNotEmpty() && key.type == KeyType.CHAR) {
                altPaint.textSize = rowH * 0.2f
                canvas.drawText(key.altLabel, rect.right - 5f, rect.top + 14f, altPaint)
            }
        }
    }

    private fun displayLabel(key: Key): String {
        return when (key.type) {
            KeyType.SPACE -> ""
            KeyType.SHIFT -> if (shifted) "⇪" else "⇧"
            else -> key.label
        }
    }

    private fun getRows(): List<List<Key>> {
        return when {
            symbolMode -> listOf(SYMBOLS_ROW1, SYMBOLS_ROW2, SYMBOLS_ROW3, SYMBOLS_ROW4, SYMBOLS_BOTTOM)
            lang == Lang.KOREAN -> KOREAN_LAYOUT
            lang == Lang.PINYIN -> PINYIN_LAYOUT
            hebrewMode -> latinToHebrew(if (shifted) LATIN_UPPER else LATIN_LOWER)
            shifted    -> LATIN_UPPER
            else       -> LATIN_LOWER
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val hit = hitTest(event.x, event.y)
                pressedKey = hit
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val hit = hitTest(event.x, event.y)
                if (hit != null && hit == pressedKey) {
                    onKey?.invoke(hit)
                }
                pressedKey = null
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedKey = null
                invalidate()
            }
        }
        return true
    }

    private fun hitTest(x: Float, y: Float): Key? {
        return keys.firstOrNull { (_, rect) -> rect.contains(x, y) }?.first
    }

    fun refresh() { invalidate() }
}
