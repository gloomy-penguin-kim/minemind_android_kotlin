package com.kim.minemind.ui.settings

import kotlin.String
import kotlin.collections.Map

//
//enum class DisplayType {
//    COLORS, NUMBERS, LETTERS
//}
//
//enum class DisplayRandom {
//    NOT_RANDOM, RANDOM
//}
//
//enum class DisplayLanguage(str: String) {
//    ENGLISH ("english"),
//    SPANISH ("spanish"), // same as english but not!
//    HINDI ("hindi"),
//    CYRILLIC ("cyrillic"),
//    HANGZHOU ("hangzhou"),
//    ETHIOPIAN ("ethiopian"),
//    ARABIC ("arabic"),
//    LATIN ("latin"),
//    BENGALI ("bengali"),
//    HEBREW ("hebrew")
//}
//
//
//
//@Suppress("SpellCheckingInspection")
//object DisplaySettings {
//    var displayAdjMineType: DisplayType = DisplayType.NUMBERS // what to show for adjacent mines
//    var displayAdjMineRandom: DisplayRandom = DisplayRandom.NOT_RANDOM  // randomized for letters/colors or not
//    var displayAdjMinesChars: List<String> = displayIdiomas["English"]?.numbersAdjMines ?: listOf("0","1","2","3","4","5","6","7","8","9")
//    var displayAdjMineColors: MutableList<Color> = mutableListOf(
//        Color(0xFF21B615),
//        Color(0xFFA335EC),
//        Color(0xFF1C721A),
//        Color(0xFFE53935),
//        Color(0xFF4653C7),
//        Color(0xFFFFEB3B),
//        Color(0xFFE59630),
//        Color(0xFFC485B9),
//        Color(0xFF2196F3),
//    )
//
//    var displayLanguage: DisplayLanguage = DisplayLanguage.ENGLISH // to be used for the menu labels/items
//
//    var flagBlueColor: Color = Color(0xFF61AEEF) // background color of a flag
//    var revealedFontColor: Color = Color(0xFFFFFFFF)
//    var revealedBackgroundColor: Color = Color(0xFF4D515D)
//    var notRevealedBackgroundColor: Color = Color(0xFF282C34)
//    var explodedBackgroundColor: Color = Color(0xFFf78c6c) // 0xFF9B859D 0xFF80cbc4
//    var incorrectPinkColor: Color = Color(0xFFc792ea) // Conflicts, incorrect flags, and mines if game is over and win is false
//    var probabilityColor: Color = Color(0xff7286BF) // probability gliphs
//    var rulesColor: Color = Color(0xff7286BF) // rules X's and O's
//
//    fun shuffleAdjMineChars() {
//        val dAMC = displayAdjMinesChars.toMutableList()
//        dAMC.shuffle()
//        displayAdjMinesChars = dAMC
//    }
//
//    fun shuffleColors() {
//        displayAdjMineColors.shuffle<Color>()
//    }
//
//}
//
//
//// this is not a finite list of menu labels/items, just what i'm starting with as an example
//@Suppress("SpellCheckingInspection")
//data class Idioma(
//    val open: String = "open",
//    val flag: String = "flag",
//    val chord: String = "chord",
//    val info: String = "info",
//    val stats: String = "statistics",
//    val findChords: String = "find chords",
//    val conflicts: String = "conflicts",
//
//    val undo: String = "undo",
//    val auto: String = "autobot",
//    val verify: String = "verify flags",
//    val probability: String = "probability",
//    val rules: String = "rules",

//
//val glyphSetMenu: Map<String, String> = mapOf(
//    "open" to "O",
//    "flag" to "F",
//    "chord" to "\\uD834\\uDD60",
//    "info" to "",
//    "stats" to "",
//    "findChords" to "",
//    "conflicts" to "",
//
//    "undo" to "",
//    "auto" to "",
//    "verify" to "",
//    "probability" to "",
//    "rules" to "",
//}

//
//    val new: String = "new",
//    val save: String = "save",
//    val load: String = "load",
//    val settings: String = "settings",
//    val help: String = "help",
//    val about: String = "about",
//
//    val displayTypeText: String = "visualization of nearby mines",

//    val colors: String = "colors",
//    val colorsFlag: String = "flag",
//    val colorOpenFont: String = "open font",
//    val colorOpenBackground: String = "open background",
//    val colorHidden: String = "hidden",
//    val colorExploded: String = "exploded mine",
//    val colorIncorrect: String = "incorrect",
//    val colorProbability: String = "probability",
//    val colorRules: String = "rules",
//data class ThemeColors(
//    val flagBlueArgb: Long = 0xFF61AEEF,
//    val revealedFontArgb: Long = 0xFFFFFFFF,
//    val revealedBgArgb: Long = 0xFF4D515D,
//    val hiddenBgArgb: Long = 0xFF282C34,
//    val explodedBgArgb: Long = 0xFFF78C6C,
//    val incorrectArgb: Long = 0xFFC792EA,
//    val probabilityArgb: Long = 0xFF7286BF,
//    val rulesArgb: Long = 0xFF7286BF,
//)
//    val numbers: String = "numbers",
//    val letters: String = "letters",
//    val shuffle: String = "random arrangement",
//    val random: String = "random",
//
//    // not sure if i should do it this way
////    val adjMinesChars: Map<String, List<String>> = mapOf(
////        "numbers" to listOf("0","1","2","3","4","5","6","7","8","9"),
////        "letters" to listOf("","A","B","C","D","E","F","G","H","I","J")
////    )
//    // or this way
//    val numbersAdjMines: List<String> = listOf("0","1","2","3","4","5","6","7","8","9"),
//    val lettersAdjMines: List<String> = listOf("","A","B","C","D","E","F","G","H","I","J")
//)
//
//
//
//@Suppress("SpellCheckingInspection")
//val displayIdiomas: Map<String, Idioma> = mapOf(
//    "English" to Idioma(),
//    "Hindi" to Idioma(
//        open="खुला",
//        flag="झंडा",
//        chord="कॉर्ड्स",
//        info="डेटा",
//        auto="स्वचल",
//        undo="पूर्ववत",
//        findChords="कॉर्ड्स ढूंढें",
//        verify="झंडे सत्यापित करें",
//        conflicts="संघर्ष",
//        probability="संभावना",
//
//        new="नया",
//        save="सेव",
//        load="लोडं",
//        settings="सेटिंग्स",
//        help="सहायता",
//        about="के बारे में",
//
//        colors="रंगों",
//        numbers="अंक",
//        letters="पत्र",
//        random="यादृच्छिक",
//
//        numbersAdjMines=listOf("०","१","२","३","४","५","६","७","८","९"),
//        lettersAdjMines=listOf("क","ख","ग","घ","ङ","च","छ","ज","झ")),
//    "Hangzhou" to Idioma(
//        open="打开",
//        chord="和弦",
//        flag="旗帜",
//        info="信息",
//        auto="微型机器",
//        verify="请核实。",
//        numbersAdjMines=listOf("〇","一","二","三","四","五","六","七","八","九"), //listOf("〇","〡","〢","〣","〤","〥","〦","〧","〨","〩"),
//        lettersAdjMines=listOf("光","日","月","山","水","天","  日 (sun), 月 (moon), and 山 (mountain), 水 (water), 天 (sky), 云 (cloud), (乐) happy
//    )
//
//
//)


//enum class DisplayLanguage(str: String) {
//    ENGLISH ("english"),
//    SPANISH ("spanish"), // same as english but not!
//    HINDI ("hindi"),
//    CYRILLIC ("cyrillic"),
//    HANGZHOU ("hangzhou"),
//    ETHIOPIAN ("ethiopian"),
//    ARABIC ("arabic"),
//    LATIN ("latin"),
//    BENGALI ("bengali"),
//    HEBREW ("hebrew")
//}

//enum class GlyphSet(val description: String) {
//
//    ARABIC_DIGITS("arabic digits"),
//    ARABIC_LETTERS("arabic letters"),
//
//    BENGALI_DIGITS("bengali digits"),
//    BENGALI_LETTERS("bengali letters"),
//
//    CHINESE_DIGITS("chinese digits"),
//    CHINESE_LETTERS("chinese letters"),
//
//    CYRILLIC_DIGITS("cyrillic digits"),
//    CYRILLIC_LETTERS("cyrillic letters"),
//
//    ENGLISH_DIGITS("english digits"),
//    ENGLISH_LETTERS("english letters"),
//
//    ETHIOPIAN_DIGITS("ethiopian digits"),
//    ETHIOPIAN_LETTERS("ethiopian letters"),
//
//    ROMAN_NUMERALS("roman numerals"),
//
//    GREEK_LETTERS("greek letters"),
//    GREEK_DIGITS("greek digits"),
//}


data class ThemeColors(
    val flagBlueArgb: Long = 0xFF61AEEF,
    val revealedFontArgb: Long = 0xFFFFFFFF,
    val revealedBgArgb: Long = 0xFF4D515D,
    val hiddenBgArgb: Long = 0xFF282C34,
    val explodedBgArgb: Long = 0xFFF78C6C,
    val incorrectArgb: Long = 0xFFC792EA,
    val probabilityArgb: Long = 0xFF7286BF,
    val rulesArgb: Long = 0xFF7286BF,
)

data class DisplaySettings(
    val uiLang: LangTag = LangTag("en"),
    val adjMode: AdjDisplayMode = AdjDisplayMode.NUMBERS,
    val glyphSetId: String = "latin_digits",
    val paletteId: String = "classic",
    val theme: ThemeColors = ThemeColors(),
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
)

fun DisplaySettings.withAdjMode(mode: AdjDisplayMode) =
    copy(adjMode = mode)

fun DisplaySettings.toggleShuffle() =
    if (shuffleMode == ShuffleMode.OFF)
        copy(shuffleMode = ShuffleMode.ON)
    else copy(shuffleMode = ShuffleMode.OFF)

fun DisplaySettings.withGlyphSet(id: String) =
    copy(glyphSetId = id)

fun DisplaySettings.withPalette(id: String) =
    copy(paletteId = id)

fun DisplaySettings.withLang(lang: LangTag) =
    copy(uiLang = lang)

fun DisplaySettings.withTheme(theme: ThemeColors) =
    copy(theme = theme)

fun DisplaySettings.withShuffle(shuffle: ShuffleMode) =
    copy(shuffleMode = shuffle)




enum class AdjDisplayMode { NUMBERS, LETTERS, COLORS, EMOJI }
enum class ShuffleMode { OFF, ON }

@JvmInline value class LangTag(val tag: String)

data class UiStrings(
    val open: String,
    val flag: String,
    val chord: String,
    val info: String,
    val settings: String,
    // ...פ)ן
)

// What the user is choosing to display in cell

// A set of glyphs you can render as text (numbers, letters, symbols, etc.)
data class GlyphSet(
    val id: String,
    val displayName: String,
    val description: String = "",
    val glyphs: List<String>, // store as Strings (Unicode)
) {
    fun preview(max: Int = 8): List<String> = glyphs.take(max)

    /** For Minesweeper adjacency: typically 1..8 */
    fun requireAtLeast(n: Int): GlyphSet {
        require(glyphs.size >= n) { "GlyphSet '$id' needs at least $n glyphs, has ${glyphs.size}" }
        return this
    }
}

data class ColorSet(
    val id: String,
    val displayName: String,
    val description: String = "",
    val colors: List<Long>,
) {
    fun preview(max: Int = 8): List<Long> = colors.take(max)
    fun requireAtLeast(n: Int): ColorSet {
        require(colors.size >= n) { "ColorSet '$id' needs at least $n colors, has ${colors.size}" }
        return this
    }
}
val uiStringsByLang: Map<LangTag, UiStrings> = mapOf(
    LangTag("en") to UiStrings(open="open", flag="flag", chord="chord", info="info", settings="settings"),
    LangTag("hi") to UiStrings(open="खोलें", flag="झंडा", chord="कॉर्ड", info="जानकारी", settings="सेटिंग्स"),
    // ...
)

// https://www.ssec.wisc.edu/~tomw/java/unicode.html


object VisualCatalog {

    val numeralSets: List<GlyphSet> = listOf(
       GlyphSet(
            id = "latin_digits",
            displayName = "Latin Numerals",
            description = "Numeri Latini",
            glyphs = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        ),
       GlyphSet(
            id = "sanskrit_digits",
            displayName = "Sanskrit Numerals",
            description = "संस्कृत संख्या",
            glyphs = listOf("१", "२", "३", "४", "५", "६", "७", "८")
       ),
       GlyphSet(
            id = "chinese_digits",
            displayName = "Chinese (Simplified) Numerals",
            description = "中文数字",
            glyphs = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
        ),
        GlyphSet(
            id = "kannada_digits",
            displayName = "Kannada Numerals",
            description = "മലയാളം സംഖ്യകൾ",
            glyphs = listOf("൧", "൨", "൩", "൪", "൫", "൬", "൭", "൮", "൯")
        ),
        GlyphSet(
            id = "myanmar_digits",
            displayName = "Myanmar Numerals",
            description = "မြန်မာဂဏန်းများ",
            glyphs = listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉")
        ),
        GlyphSet(
            id = "glagolitic_digits",
            displayName = "Glagolitic Numerals",
            description = "ⰳⰾⰰⰳⱁⰾⰻⱌⰰ",
            glyphs = listOf("Ⰰ", "Ⰱ", "Ⰲ", "Ⰳ", "Ⰴ", "Ⰵ", "Ⰶ", "Ⰷ", "Ⰸ")
        ),
        GlyphSet(
            id = "geez_digits",
            displayName = "Ge'ez Numerals",
            description = "ግዕዝ",
            glyphs = listOf("፩", "፪", "፫", "፬", "፭", "፮", "፯", "፰", "፱")
        ),
        GlyphSet(
            id = "roman_digits",
            displayName = "Roman Numerals",
            description = "",
            glyphs = listOf("Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ", "Ⅷ", "Ⅸ", "Ⅹ")
        ),
        GlyphSet(
            id = "khmer_digits",
            displayName = "Khmer Numerals",
            description = "",
            glyphs = listOf("១", "២", "៣", "៤", "៥", "៦", "៧", "៨", "៩")
        ),
        GlyphSet(
            id = "arabic_digits",
            displayName = "Arabic Numerals",
            description = "لأرقام العربية الشرقية", // الأرقام العربية
            glyphs = listOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        ),
        GlyphSet(
            id = "bengali_digits",
            displayName = "Bengali Numerals",
            description = "বাংলা সংখ্যা",
            glyphs = listOf("১ ", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
        ),
        GlyphSet(
            id = "hebrew_digits",
            displayName = "Hebrew Numerals",
            description = "",
            glyphs = listOf("א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט")
        ),

        GlyphSet(
            id = "kannada_digits",
            displayName = "Kannada Numerals",
            description = "ಕನ್ನಡ ಸಂಖ್ಯೆಗಳು",
            glyphs = listOf("೧", "೨", "೩", "೪", "೫", "೬", "೭", "೮", "೯")
        ),
        GlyphSet(
            id = "thai_digits",
            displayName = "Thai Numerals",
            description = "ตัวเลขไทย",
            glyphs = listOf("๑", "๒", "๓", "๔", "๕", "๖", "๗", "๘", "๙")
        ),
        GlyphSet(
            id = "lao_digits",
            displayName = "Lao Numerals",
            description = "ຕົວເລກລາວ",
            glyphs = listOf("໑", "໒", "໓", "໔", "໕", "໖", "໗", "໘", "໙")
        ),
        GlyphSet(
            id = "tibetan_digits",
            displayName = "Tibetan Numerals",
            description = "བོད་ཡིག་ཨང་གྲངས།",
            glyphs = listOf("༡", "༢", "༣", "༤", "༥", "༦", "༧", "༨", "༩")
        ),
    )

    val alphaSets: List<GlyphSet> = listOf(
        GlyphSet(
            id="brahmi_letters",
            displayName = "Brahmi Script",
            description = "Brahmi",
            glyphs = listOf(
                "𑀓",
                "𑀔",
                "𑀕",
                "𑀖",
                "𑀗",
                "𑀘",
                "𑀙",
                "𑀚",
                "𑀛",
                "𑀜",
                "𑀝",
                "𑀞",
                "𑀟",
                "𑀠",
                "𑀡",
                "𑀢",
                "𑀣",
                "𑀤",
                "𑀥",
                "𑀦",
                "𑀧",
                "𑀨",
                "𑀩",
                "𑀪",
                "𑀫",
                "𑀬",
                "𑀭",
                "𑀮",
                "𑀯",
                "𑀰",
                "𑀱",
                "𑀲",
                "𑀳"
            )
        ),
        GlyphSet(
            id="chinese_letters",
            displayName = "Chinese Simplified Script",
            description = "中国人",
            glyphs = listOf(
                "光",
                "日",
                "月",
                "山",
                "水",
                "天",
                "云",
                "乐",
                "树",
                "人",
                "火",
                "河",
                "书",
                "马",
                "猫",
                "狗",
                "鱼",
                "波",
                "米",
                "词",
                "氧",
                "物",
                "星",
                "玉",
                "丝",
                "龙"
            )
        ),
        GlyphSet(
            id="cyrillic_letters",
            displayName = "Cyrillic Script",
            description = "Кирилица",
            glyphs = listOf(
                "а",
                "б",
                "в",
                "г",
                "д",
                "е",
                "ж",
                "з",
                "и",
                "й",
                "к",
                "л",
                "м",
                "н",
                "о",
                "п",
                "р",
                "с",
                "т",
                "у",
                "ф",
                "х",
                "ц",
                "ч",
                "ш",
                "щ",
                "ъ",
                "ь",
                "ю",
                "я"
            )
        ),
        GlyphSet(
            id="hindi_letters",
            displayName = "Hindi Script",
            description = "हिंदी",
            glyphs = listOf(
                "क",
                "ख",
                "ग",
                "घ",
                "ङ",
                "च",
                "छ",
                "ज",
                "झ",
                "ञ",
                "ट",
                "ठ",
                "ड",
                "ढ",
                "ण",
                "त",
                "थ",
                "द",
                "ध",
                "न",
                "प",
                "फ",
                "ब",
                "भ",
                "म",
                "य",
                "र",
                "ल",
                "व",
                "श",
                "ष",
                "स",
                "ह"
            )
        ),
        GlyphSet(
            id="hiragana_letters",
            displayName = "Hiragana Script",
            description = "ひらがな",
            glyphs = listOf(
                "あ",
                "い",
                "う",
                "え",
                "お",
                "か",
                "き",
                "く",
                "け",
                "こ",
                "さ",
                "し",
                "す",
                "せ",
                "そ",
                "た",
                "ち",
                "つ",
                "て",
                "と",
                "な",
                "に",
                "ぬ",
                "ね",
                "の",
                "は",
                "ひ",
                "ふ",
                "へ",
                "ほ",
                "ま",
                "み",
                "む",
                "め",
                "も",
                "や",
                "ゆ",
                "よ",
                "ら",
                "り",
                "る",
                "れ",
                "ろ",
                "わ",
                "ゐ",
                "ゑ",
                "を"
            )
        ),
        GlyphSet(
            id="geez_letters",
            displayName = "Ge'ez Script",
            description = "ግዕዝ",
            glyphs = listOf(
                "ሀ",
                "ለ",
                "ሐ",
                "መ",
                "ሠ",
                "ረ",
                "ሰ",
                "ቀ",
                "በ",
                "ተ",
                "ኀ",
                "ነ",
                "አ",
                "ከ",
                "ወ",
                "ዐ",
                "ዘ",
                "የ",
                "ደ",
                "ገ",
                "ጠ",
                "ጸ",
                "ፀ",
                "ፈ",
                "ፐ"
            )
        ),
        GlyphSet(
            id="greek_letters",
            displayName = "Greek Script",
            description = "ελληνικό αλφάβητο",
            glyphs = listOf(
                "α",
                "β",
                "γ",
                "δ",
                "ε",
                "ζ",
                "η",
                "θ",
                "ι",
                "κ",
                "λ",
                "μ",
                "ν",
                "ξ",
                "ο",
                "π",
                "ρ",
                "Σ",
                "σ",
                "ς",
                "τ",
                "υ",
                "φ",
                "χ",
                "ψ",
                "Ω",
                "ω"
            )
        ),
        GlyphSet(
            id="latin_letters",
            displayName = "Latin Script",
            description = "alphabetum Latinum",
            glyphs = listOf(
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H",
                "I",
                "J",
                "K",
                "L",
                "M",
                "N",
                "O",
                "P",
                "Q",
                "R",
                "S",
                "T",
                "U",
                "V",
                "W",
                "X",
                "Y",
                "Z"
            )
        ),
        GlyphSet(
            id="khmer_letters",
            displayName = "Khmer Script",
            description = "អក្ខរក្រមខ្មែរ",
            glyphs = listOf(
                "ក",
                "ខ",
                "គ",
                "ឃ",
                "ង",
                "ច",
                "ឆ",
                "ជ",
                "ឈ",
                "ញ",
                "ដ",
                "ឋ",
                "ឌ",
                "ឍ",
                "ណ",
                "ត",
                "ថ",
                "ទ",
                "ធ",
                "ន",
                "ប",
                "ផ",
                "ព",
                "ភ",
                "ម",
                "យ",
                "រ",
                "ល",
                "វ",
                "ឝ",
                "ឞ",
                "ស",
                "ហ",
                "ឡ",
                "អ"
            )
        ),
        GlyphSet(
            id="arabic_letters",
            displayName = "Arabic Script",
            description = "الأبجدية العربية",
            glyphs = listOf(
                "ا",
                "ب",
                "ت",
                "ث",
                "ج",
                "ح",
                "خ",
                "د",
                "ذ",
                "ر",
                "ز",
                "س",
                "ش",
                "ص",
                "ض",
                "ط",
                "ظ",
                "ع",
                "غ",
                "ف",
                "ق",
                "ك",
                "ل",
                "م",
                "ن",
                "ه",
                "و",
                "ي"
            )
        ),
    )

    val colorSets: List<ColorSet> = listOf(
        ColorSet(
            id = "classic",
            displayName = "Classic",
            colors = listOf(
                0xFF1E88E5, 0xFF43A047, 0xFFE53935, 0xFF8E24AA,
                0xFFFB8C00, 0xFF00897B, 0xFF3949AB, 0xFF6D4C41
            )
        ),
        ColorSet(
            id = "pastel",
            displayName = "Pastel",
            colors = listOf(
                0xFF90CAF9, 0xFFA5D6A7, 0xFFEF9A9A, 0xFFCE93D8,
                0xFFFFCC80, 0xFF80CBC4, 0xFF9FA8DA, 0xFFBCAAA4
            )
        )
    )



    fun numeral(id: String) = numeralSets.first { it.id == id }
    fun alpha(id: String) = alphaSets.first { it.id == id }
    fun colors(id: String) = colorSets.first { it.id == id }
}
