package com.yanming.keyboard.panels

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import com.yanming.keyboard.ServerClient
import com.yanming.keyboard.VaultItem

// ═══════════════════════════════════════════════════════
// Vault Panel — passwords from server, nothing stored locally
// Auto-closes after 30 seconds of inactivity
// ═══════════════════════════════════════════════════════

class VaultPanel(context: Context) : LinearLayout(context) {

    var onPaste: ((String) -> Unit)? = null

    private val BG    = Color.parseColor("#0c0805")
    private val SURF  = Color.parseColor("#1a1410")
    private val GOLD  = Color.parseColor("#c9932b")
    private val TEXT  = Color.parseColor("#f0dfc8")
    private val MUTED = Color.parseColor("#7a6a58")
    private val RED   = Color.parseColor("#9a4a4a")

    private val list: LinearLayout
    private val timerTxt: TextView
    private var secondsLeft = 30
    private val timerHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            secondsLeft--
            timerTxt.text = "auto-close in ${secondsLeft}s"
            if (secondsLeft <= 0) {
                onAutoClose?.invoke()
            } else {
                timerHandler.postDelayed(this, 1000)
            }
        }
    }
    var onAutoClose: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(BG)
        setPadding(12, 8, 12, 8)

        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
        }
        val title = TextView(context).apply {
            text      = "🔑 VAULT"
            textSize  = 9f
            setTextColor(MUTED)
            letterSpacing = 0.2f
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }
        timerTxt = TextView(context).apply {
            text     = "auto-close in 30s"
            textSize = 9f
            setTextColor(RED)
        }
        header.addView(title)
        header.addView(timerTxt)
        addView(header)

        val warn = TextView(context).apply {
            text      = "⚠ requires internet · nothing stored locally"
            textSize  = 9f
            setTextColor(MUTED)
            setPadding(0, 4, 0, 8)
        }
        addView(warn)

        val scroll = ScrollView(context)
        list = LinearLayout(context).apply { orientation = VERTICAL }
        scroll.addView(list)
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        load()
        startTimer()
    }

    fun load() {
        list.removeAllViews()
        list.addView(TextView(context).apply {
            text     = "connecting to vault..."
            textSize = 11f
            setTextColor(MUTED)
            gravity  = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        })

        ServerClient.getVault { items ->
            list.removeAllViews()
            if (items.isEmpty()) {
                list.addView(TextView(context).apply {
                    text     = "Vault is empty or unreachable"
                    textSize = 11f
                    setTextColor(MUTED)
                    gravity  = Gravity.CENTER
                    setPadding(0, 20, 0, 0)
                })
                return@getVault
            }
            items.forEach { addItem(it) }
        }
    }

    private fun addItem(item: VaultItem) {
        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(12, 10, 12, 10)
            setBackgroundColor(SURF)
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 0, 6)
            layoutParams = lp
        }

        val name = TextView(context).apply {
            text     = item.name
            textSize = 12f
            setTextColor(TEXT)
            android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD).also { typeface = it }
        }

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setPadding(0, 4, 0, 0)
        }

        val user = TextView(context).apply {
            text     = item.username
            textSize = 10f
            setTextColor(MUTED)
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val copyUser = TextView(context).apply {
            text     = "user"
            textSize = 9f
            setTextColor(GOLD)
            setPadding(8, 0, 8, 0)
            setOnClickListener {
                resetTimer()
                onPaste?.invoke(item.username)
            }
        }

        val copyPass = TextView(context).apply {
            text     = "pass"
            textSize = 9f
            setTextColor(GOLD)
            setOnClickListener {
                resetTimer()
                onPaste?.invoke(item.password)
            }
        }

        row.addView(user)
        row.addView(copyUser)
        row.addView(copyPass)
        card.addView(name)
        card.addView(row)
        list.addView(card)
    }

    private fun startTimer() {
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    fun resetTimer() {
        secondsLeft = 30
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
    }
}
