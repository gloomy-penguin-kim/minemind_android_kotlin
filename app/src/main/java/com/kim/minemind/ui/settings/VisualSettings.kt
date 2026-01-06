package com.kim.minemind.ui.settings

import androidx.compose.runtime.Immutable

enum class GlyphMode { NUMERALS, ALPHABET, COLORS }
@Immutable
data class VisualSettings(
    val glyphMode: GlyphMode = GlyphMode.NUMERALS,

    val numeralSetId: String = "latin_digits",
    val alphaSetId: String = "latin_letters",
    val colorSetId: String = "classic_dots",
//
//    val shuffleSeed: Long = 0L,
    val shuffleGlyphs: Boolean = false,
    val shuffleColors: Boolean = false,

    val themeId: String = "default",
)

data class VisualState(
    val glyphs: List<String>, // 1..8
    val colors: List<Long>,   // 1..8 (only used in color mode)
)


//fun VisualSettings.resolve(settings: VisualSettings,seed: Long): VisualState {
//    val counts = (1..8).toList()
//
//    fun <T> shuffled(list: List<T>, doShuffle: Boolean): List<T> {
//        if (!doShuffle) return list
//        val rnd = java.util.Random(shuffleSeed)
//        return list.shuffled(rnd)
//    }
//
//    return when (glyphMode) {
//        GlyphMode.NUMERALS -> {
//            val set = VisualCatalog.numeral(numeralSetId).requireAtLeast(8)
//            val glyphs = set.glyphs
//            val onlyEight = glyphs.take(n = 8)
//            VisualState(
//                glyphs = counts.zip(onlyEight).toMap(),
//                colorForCount = emptyMap()
//            )
//        }
//
//        GlyphMode.ALPHABET -> {
//            var picked: List<String> = emptyList()
//            if (shuffleMode == ShuffleMode.ON) {
//                val set = VisualCatalog.alpha(alphaSetId).requireAtLeast(8)
//                picked = shuffled(set.glyphs.take(8), shuffleGlyphs)
//            }
//            else {
//                val set = VisualCatalog.alpha(alphaSetId).requireAtLeast(8)
//                picked = set.glyphs.take(8)
//            }
//            VisualState(
//                glyphs = counts.zip(picked).toMap(),
//                colorForCount = emptyMap()
//            )
//        }
//
//        GlyphMode.COLORS -> {
//            var picked: List<Long> = emptyList()
//            if (shuffleMode == ShuffleMode.ON) {
//                val set = VisualCatalog.colors(colorSetId).requireAtLeast(8)
//                picked = shuffled(set.colors.take(8), shuffleColors)
//            }
//            else {
//                val set = VisualCatalog.colors(colorSetId).requireAtLeast(8)
//                picked = set.colors.take(8)
//            }
//            // Use a neutral glyph like "•" (or empty) and color carries meaning
//            VisualState(
//                glyphs = counts.associateWith { "•" },
//                colorForCount = counts.zip(picked).toMap()
//            )
//        }
//    }
//}
//
//data class VisualMapping2(
//    val glyphForCount: List<String?>, // index 0..8, null means "show nothing"
//    val colorForCount: List<Long?>    // index 0..8, null means "use default"
//)