package com.yanming.keyboard.keyboard

enum class Lang { LATIN, HEBREW, KOREAN, PINYIN }
enum class KeyType { CHAR, BACKSPACE, ENTER, SPACE, SHIFT, LANG_SWITCH, HEBREW_TOGGLE, SYMBOLS, EMOJI }

data class Key(
    val label: String,
    val altLabel: String = "",
    val type: KeyType = KeyType.CHAR,
    val widthRatio: Float = 1f,
    val code: Int = 0,
)

// ── LATIN ──────────────────────────────────────────────
val LATIN_LOWER = listOf(
    listOf("q","w","e","r","t","y","u","i","o","p").map { Key(it, it.uppercase()) },
    listOf("a","s","d","f","g","h","j","k","l").map { Key(it, it.uppercase()) },
    listOf(
        Key("⇧", type = KeyType.SHIFT, widthRatio = 1.5f),
        Key("z","Z"), Key("x","X"), Key("c","C"), Key("v","V"),
        Key("b","B"), Key("n","N"), Key("m","M"),
        Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
    ),
    listOf(
        Key("עב", type = KeyType.HEBREW_TOGGLE, widthRatio = 1.3f),
        Key("한", type = KeyType.LANG_SWITCH, widthRatio = 1.2f, code = Lang.KOREAN.ordinal),
        Key("拼", type = KeyType.LANG_SWITCH, widthRatio = 1.2f, code = Lang.PINYIN.ordinal),
        Key("", type = KeyType.SPACE, widthRatio = 3f),
        Key(",","!"),
        Key(".","?"),
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
    Key(";"), Key("!"), Key("?"), Key("_"), Key("~"), Key("`"),
    Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
)
val SYMBOLS_BOTTOM = listOf(
    Key("עב", type = KeyType.HEBREW_TOGGLE, widthRatio = 1.2f),
    Key("", type = KeyType.SPACE, widthRatio = 5f),
    Key("."),
    Key("↵", type = KeyType.ENTER, widthRatio = 1.5f),
)

// ── HEBREW ─────────────────────────────────────────────
// Standard Israeli keyboard layout — exactly as on PC
val HEBREW_LAYOUT = listOf(
    // Row 1: / ' ק ר א ט ו ן ם פ
    listOf(
        Key("/"), Key("'"), Key("ק"), Key("ר"), Key("א"),
        Key("ט"), Key("ו"), Key("ן"), Key("ם"), Key("פ"),
    ),
    // Row 2: ש ד ג כ ע י ח ל ך
    listOf(
        Key("ש"), Key("ד"), Key("ג"), Key("כ"), Key("ע"),
        Key("י"), Key("ח"), Key("ל"), Key("ך"),
    ),
    // Row 3: shift + ז ס ב ה נ מ צ + backspace
    listOf(
        Key("⇧", type = KeyType.SHIFT, widthRatio = 1.5f),
        Key("ז"), Key("ס"), Key("ב"), Key("ה"),
        Key("נ"), Key("מ"), Key("צ"),
        Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
    ),
    // Row 4: bottom
    listOf(
        Key("EN", type = KeyType.HEBREW_TOGGLE, widthRatio = 1.3f),
        Key("한", type = KeyType.LANG_SWITCH, widthRatio = 1.2f, code = Lang.KOREAN.ordinal),
        Key("拼", type = KeyType.LANG_SWITCH, widthRatio = 1.2f, code = Lang.PINYIN.ordinal),
        Key("", type = KeyType.SPACE, widthRatio = 3f),
        Key(","), Key("."),
        Key("↵", type = KeyType.ENTER, widthRatio = 1.5f),
    ),
)

// Final sofit forms on shift
val HEBREW_SHIFT_MAP = mapOf(
    "כ" to "ך", "מ" to "ם", "נ" to "ן", "פ" to "ף", "צ" to "ץ",
)

fun getHebrewLayout(shifted: Boolean): List<List<Key>> {
    if (!shifted) return HEBREW_LAYOUT
    return HEBREW_LAYOUT.map { row ->
        row.map { k ->
            val sofit = HEBREW_SHIFT_MAP[k.label]
            if (k.type == KeyType.CHAR && sofit != null) k.copy(label = sofit)
            else k
        }
    }
}

// ── KOREAN ─────────────────────────────────────────────
val KOREAN_LAYOUT = listOf(
    listOf("ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ","ㅐ","ㅔ").map { Key(it) },
    listOf("ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ").map { Key(it) },
    listOf(
        Key("⇧", type = KeyType.SHIFT, widthRatio = 1.5f),
        Key("ㅋ"), Key("ㅌ"), Key("ㅊ"), Key("ㅍ"),
        Key("ㅠ"), Key("ㅜ"), Key("ㅡ"),
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
        Key("z"), Key("x"), Key("c"), Key("v"),
        Key("b"), Key("n"), Key("m"),
        Key("⌫", type = KeyType.BACKSPACE, widthRatio = 1.5f),
    ),
    listOf(
        Key("EN", type = KeyType.LANG_SWITCH, widthRatio = 1.5f, code = Lang.LATIN.ordinal),
        Key("", type = KeyType.SPACE, widthRatio = 5f),
        Key("↵", type = KeyType.ENTER, widthRatio = 1.5f),
    )
)

// ── COMMON PINYIN CANDIDATES ───────────────────────────
// Basic map of Pinyin → most common characters
val PINYIN_MAP = mapOf(
    "ni" to listOf("你","妮","倪","腻"),
    "hao" to listOf("好","号","毫","豪"),
    "wo" to listOf("我","握","卧","窝"),
    "shi" to listOf("是","时","事","十","市","使"),
    "de" to listOf("的","得","地","德"),
    "zai" to listOf("在","再","载","栽"),
    "he" to listOf("和","喝","河","何","核"),
    "ta" to listOf("他","她","它","塔"),
    "men" to listOf("们","门","闷"),
    "you" to listOf("你","有","又","友","右"),
    "yi" to listOf("一","以","已","意","义","医"),
    "ge" to listOf("个","各","哥","格","歌"),
    "lai" to listOf("来","赖","莱"),
    "qu" to listOf("去","取","趣","区"),
    "dui" to listOf("对","队","堆"),
    "bu" to listOf("不","步","部","布"),
    "ma" to listOf("吗","妈","马","骂"),
    "le" to listOf("了","乐","勒"),
    "ren" to listOf("人","任","忍","认"),
    "da" to listOf("大","打","达","答"),
    "zhong" to listOf("中","重","种","众"),
    "guo" to listOf("国","过","果","锅"),
    "wei" to listOf("为","位","味","未","围"),
    "wo" to listOf("我","握","卧","蜗"),
    "xiao" to listOf("小","笑","效","校","消"),
    "shuo" to listOf("说","朔"),
    "kan" to listOf("看","坎","砍"),
    "wen" to listOf("问","文","温","闻"),
    "zhi" to listOf("知","只","之","志","值","制"),
    "mei" to listOf("没","每","美","妹","媒"),
    "jiu" to listOf("就","旧","九","救","酒"),
    "yao" to listOf("要","药","摇","腰"),
    "hui" to listOf("会","回","汇","灰"),
    "hai" to listOf("还","海","害","孩"),
    "ba" to listOf("把","吧","八","爸","罢"),
    "na" to listOf("那","拿","哪","纳"),
    "zen" to listOf("怎"),
    "zenme" to listOf("怎么"),
    "xie" to listOf("谢","些","写","鞋"),
    "ai" to listOf("爱","哎","唉","矮"),
    "wo" to listOf("我","握"),
    "ya" to listOf("呀","压","哑","亚"),
    "jin" to listOf("今","进","金","近","紧"),
    "tian" to listOf("天","田","甜","填"),
    "lao" to listOf("老","劳","牢","捞"),
    "gao" to listOf("高","告","搞","稿"),
    "ming" to listOf("明","名","命","鸣"),
)

// ── HANGUL COMPOSER ────────────────────────────────────
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
