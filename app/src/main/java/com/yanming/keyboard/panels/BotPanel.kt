package com.yanming.keyboard.panels

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import com.yanming.keyboard.ServerClient

// ═══════════════════════════════════════════════════════
// Bot Panel — chat with your personal AI model
// Shows real error if model has no data yet — no fake responses
// ═══════════════════════════════════════════════════════

class BotPanel(context: Context) : LinearLayout(context) {

    var onInsert: ((String) -> Unit)? = null

    private val BG    = Color.parseColor("#0c0805")
    private val SURF  = Color.parseColor("#1a1410")
    private val SURF2 = Color.parseColor("#241c14")
    private val GOLD  = Color.parseColor("#c9932b")
    private val TEXT  = Color.parseColor("#f0dfc8")
    private val MUTED = Color.parseColor("#7a6a58")

    private val msgContainer: LinearLayout
    private val inputField: EditText
    private val sendBtn: TextView
    private val scroll: ScrollView

    data class Msg(val text: String, val fromUser: Boolean)
    private val history = mutableListOf<Msg>()

    init {
        orientation = VERTICAL
        setBackgroundColor(BG)

        // Header
        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(SURF)
        }
        val dot = TextView(context).apply {
            text     = "●"
            textSize = 8f
            setTextColor(MUTED) // grey = no model yet
            setPadding(0, 0, 8, 0)
        }
        val title = TextView(context).apply {
            text     = "YanMing AI"
            textSize = 12f
            setTextColor(TEXT)
            typeface = Typeface.create("serif", Typeface.ITALIC)
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }
        val version = TextView(context).apply {
            text     = "no data yet"
            textSize = 9f
            setTextColor(MUTED)
        }
        header.addView(dot)
        header.addView(title)
        header.addView(version)
        addView(header)

        // Message list
        scroll = ScrollView(context)
        msgContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(12, 8, 12, 8)
        }
        scroll.addView(msgContainer)
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        // Input row
        val inputRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setPadding(8, 6, 8, 6)
            setBackgroundColor(SURF)
        }
        inputField = EditText(context).apply {
            hint     = "say something..."
            textSize = 12f
            setTextColor(TEXT)
            setHintTextColor(MUTED)
            background = null
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            maxLines = 3
        }
        sendBtn = TextView(context).apply {
            text     = "▸"
            textSize = 18f
            setTextColor(GOLD)
            setPadding(12, 0, 4, 0)
            setOnClickListener { send() }
        }
        inputRow.addView(inputField)
        inputRow.addView(sendBtn)
        addView(inputRow)

        // Initial state — honest
        addSystemMsg("Model has no data yet. Keep writing — I'll learn from your sentences.")
    }

    private fun send() {
        val text = inputField.text.toString().trim()
        if (text.isEmpty()) return
        inputField.text.clear()

        addMsg(Msg(text, fromUser = true))

        val thinking = addSystemMsg("...")
        sendBtn.isEnabled = false

        ServerClient.askBot(text) { response ->
            msgContainer.removeView(thinking)
            sendBtn.isEnabled = true
            if (response.isNullOrBlank()) {
                addSystemMsg("No response yet — the model needs more labeled data.")
            } else {
                addMsg(Msg(response, fromUser = false))
            }
            scroll.post { scroll.fullScroll(FOCUS_DOWN) }
        }
    }

    private fun addMsg(msg: Msg): android.view.View {
        val bubble = LinearLayout(context).apply {
            orientation = VERTICAL
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 4, 0, 4)
            if (msg.fromUser) {
                lp.gravity = Gravity.END
                gravity    = Gravity.END
            }
            layoutParams = lp
            setPadding(12, 8, 12, 8)
            setBackgroundColor(if (msg.fromUser) SURF2 else SURF)
        }

        val txt = TextView(context).apply {
            text     = msg.text
            textSize = 12f
            setTextColor(TEXT)
            if (!msg.fromUser) {
                // Long press to insert into text field
                setOnLongClickListener {
                    onInsert?.invoke(msg.text)
                    true
                }
            }
        }

        bubble.addView(txt)
        msgContainer.addView(bubble)
        return bubble
    }

    private fun addSystemMsg(text: String): android.view.View {
        val txt = TextView(context).apply {
            this.text = text
            textSize  = 10f
            setTextColor(MUTED)
            gravity   = Gravity.CENTER
            setPadding(8, 6, 8, 6)
        }
        msgContainer.addView(txt)
        return txt
    }
}
