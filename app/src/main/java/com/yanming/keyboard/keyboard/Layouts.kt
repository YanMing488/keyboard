package com.yanming.keyboard.keyboard

// ═══════════════════════════════════════════════════════
// Keyboard layouts — Latin, Hebrew, Korean, Pinyin
// ═══════════════════════════════════════════════════════

enum class Lang { LATIN, HEBREW, KOREAN, PINYIN }
enum class KeyType { CHAR, BACKSPACE, ENTER, SPACE, SHIFT, LANG_SWITCH, HEBREW_TOGGLE, SYMBOLS, EMOJI }

data class Key(
    val label: String,
    val altLabel: String = "",   // long-press or shift
    val type: KeyType = KeyType.CHAR,
    val widthRatio: Float = 1f,  // relative to standard key width
    val code: Int = 0,           // char code (0 = use label)
)

// ── LATIN (EN / ID) ────────────────────────────────────
val LATIN_LOWER = listOf(
    listOf("q","w","e","r","t","y","u","i","o","p").map { Key(it, it.uppercase()) },
    listOf("a","s","d","f","g","h","j","k","l").map { Key(it, it.uppercase()) },
    listOf(
        Key("⇧", type = KeyType.SHIFT, widthRatio = 1.5f),
        *"zxcvbnm".map { Key(it.toString(), it.uppercaseChar().toString()) }.toTypedArray(),
        Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
    ),
    listOf(
        Key("עב", type = KeyType.HEBREW_TOGGLE, widthRatio = 1.2f),
        Key("한", type = KeyType.LANG_SWITCH, widthRatio = 1.2f, code = Lang.KOREAN.ordinal),
        Key("拼", type = KeyType.LANG_SWITCH, widthRatio = 1.2f, code = Lang.PINYIN.ordinal),
        Key("", type = KeyType.SPACE, widthRatio = 3.5f),
        Key(",", "!"),
        Key(".", "?"),
        Key("↵", type = KeyType.ENTER, widthRatio = 1.5f),
    )
)

val LATIN_UPPER = LATIN_LOWER.map { row ->
    row.map { k ->
        if (k.type == KeyType.CHAR) k.copy(label = k.altLabel.ifEmpty { k.label.uppercase() })
        else k
    }
}

// ── SYMBOLS ────────────────────────────────────────────
val SYMBOLS_ROW1 = listOf("1","2","3","4","5","6","7","8","9","0").map { Key(it) }
val SYMBOLS_ROW2 = listOf("@","#","$","€","¢","£","¥","%","&","*").map { Key(it) }
val SYMBOLS_ROW3 = listOf("-","+","=","(",")","/","\\","'","\"",":").map { Key(it) }
val SYMBOLS_ROW4 = listOf(
    Key("ABC", type = KeyType.SYMBOLS, widthRatio = 1.5f),
    *listOf(";","!","?","_","~","`").map { Key(it) }.toTypedArray(),
    Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
)
val SYMBOLS_BOTTOM = listOf(
    Key("עב", type = KeyType.HEBREW_TOGGLE, widthRatio = 1.2f),
    Key("", type = KeyType.SPACE, widthRatio = 5f),
    Key(".", "."),
    Key("↵", type = KeyType.ENTER, widthRatio = 1.5f),
)

// ── HEBREW (overlay on Latin positions) ────────────────
// Maps Latin key positions to Hebrew characters
// Same physical layout — just different characters
val HEBREW_MAP = mapOf(
    "q" to "/", "w" to "'", "e" to "ק", "r" to "ר", "t" to "א",
    "y" to "ט", "u" to "ו", "i" to "ן", "o" to "ם", "p" to "פ",
    "a" to "ש", "s" to "ד", "d" to "ג", "f" to "כ", "g" to "ע",
    "h" to "י", "j" to "ח", "k" to "ל", "l" to "ך",
    "z" to "ז", "x" to "ס", "c" to "ב", "v" to "ה", "b" to "נ",
    "n" to "מ", "m" to "צ",
)

fun latinToHebrew(rows: List<List<Key>>): List<List<Key>> {
    return rows.map { row ->
        row.map { k ->
            val heb = HEBREW_MAP[k.label.lowercase()]
            if (k.type == KeyType.CHAR && heb != null) k.copy(label = heb, altLabel = k.label)
            else k
        }
    }
}

// ── KOREAN (Hangul) ────────────────────────────────────
// Standard Korean 2-set layout
val KOREAN_LAYOUT = listOf(
    listOf("ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ","ㅐ","ㅔ").map { Key(it) },
    listOf("ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ").map { Key(it) },
    listOf(
        Key("⇧", type = KeyType.SHIFT, widthRatio = 1.5f),
        *listOf("ㅋ","ㅌ","ㅊ","ㅍ","ㅠ","ㅜ","ㅡ").map { Key(it) }.toTypedArray(),
        Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
    ),
    listOf(
        Key("EN", type = KeyType.LANG_SWITCH, widthRatio = 1.5f, code = Lang.LATIN.ordinal),
        Key("", type = KeyType.SPACE, widthRatio = 5f),
        Key("↵", type = KeyType.ENTER, widthRatio = 1.5f),
    )
)

// ── PINYIN ─────────────────────────────────────────────
val PINYIN_LAYOUT = listOf(
    listOf("q","w","e","r","t","y","u","i","o","p").map { Key(it) },
    listOf("a","s","d","f","g","h","j","k","l").map { Key(it) },
    listOf(
        Key("⇧", type = KeyType.SHIFT, widthRatio = 1.5f),
        *"zxcvbnm".map { Key(it.toString()) }.toTypedArray(),
        Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
    ),
    listOf(
        Key("EN", type = KeyType.LANG_SWITCH, widthRatio = 1.5f, code = Lang.LATIN.ordinal),
        Key("", type = KeyType.SPACE, widthRatio = 5f),
        Key("↵", type = KeyType.ENTER, widthRatio = 1.5f),
    )
)

// ── HANGUL COMPOSER ────────────────────────────────────
// Combines Jamo into syllable blocks
object HangulComposer {
    private val CHO  = listOf("ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ")
    private val JUNG = listOf("ㅏ","ㅐ","ㅑ","ㅒ","ㅓ","ㅔ","ㅕ","ㅖ","ㅗ","ㅘ","ㅙ","ㅚ","ㅛ","ㅜ","ㅝ","ㅞ","ㅟ","ㅠ","ㅡ","ㅢ","ㅣ")
    private val JONG = listOf("","ㄱ","ㄲ","ㄳ","ㄴ","ㄵ","ㄶ","ㄷ","ㄹ","ㄺ","ㄻ","ㄼ","ㄽ","ㄾ","ㄿ","ㅀ","ㅁ","ㅂ","ㅄ","ㅅ","ㅆ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ")

    fun isVowel(j: String) = JUNG.contains(j)
    fun isConsonant(j: String) = CHO.contains(j)

    fun compose(cho: String, jung: String, jong: String = ""): String {
        val ci = CHO.indexOf(cho)
        val vi = JUNG.indexOf(jung)
        val ji = JONG.indexOf(jong)
        if (ci < 0 || vi < 0 || ji < 0) return cho + jung + jong
        return (0xAC00 + ci * 21 * 28 + vi * 28 + ji).toChar().toString()
    }
}
