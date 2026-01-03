package com.kim.minemind.ui.settings

import androidx.compose.runtime.Immutable

enum class GlyphMode { NUMERALS, ALPHABET, COLORS }
@Immutable
data class GameVisualSettings(
    val mode: GlyphMode = GlyphMode.NUMERALS,

    // Selected sets (ids refer into a catalog)
    val numeralSetId: String = "latin_digits",
    val alphabetSetId: String = "latin_letters",
    val colorSetId: String = "classic_dots",

    // Shuffle behavior (only relevant for certain modes)
    val shuffleGlyphs: Boolean = false,
    val shuffleColors: Boolean = false,

    // If you want deterministic shuffles per game/seed:
    val shuffleSeed: Long = 0L,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,

    val theme: ThemeColors = ThemeColors(),
)


data class ResolvedVisualMapping(
    val glyphForCount: Map<Int, String>, // 1..8
    val colorForCount: Map<Int, Long>,   // 1..8 (only used in color mode)
)

fun GameVisualSettings.resolveMapping(): ResolvedVisualMapping {
    val counts = (1..8).toList()

    fun <T> shuffled(list: List<T>, doShuffle: Boolean): List<T> {
        if (!doShuffle) return list
        val rnd = java.util.Random(shuffleSeed)
        return list.shuffled(rnd)
    }

    return when (mode) {
        GlyphMode.NUMERALS -> {
            val set = VisualCatalog.numeral(numeralSetId).requireAtLeast(8)
            val glyphs = set.glyphs
            val onlyEight = glyphs.take(n = 8)
            ResolvedVisualMapping(
                glyphForCount = counts.zip(onlyEight).toMap(),
                colorForCount = emptyMap()
            )
        }

        GlyphMode.ALPHABET -> {
            var picked: List<String> = emptyList()
            if (shuffleMode == ShuffleMode.ON) {
                val set = VisualCatalog.alpha(alphabetSetId).requireAtLeast(8)
                picked = shuffled(set.glyphs.take(8), shuffleGlyphs)
            }
            else {
                val set = VisualCatalog.alpha(alphabetSetId).requireAtLeast(8)
                picked = set.glyphs.take(8)
            }
            ResolvedVisualMapping(
                glyphForCount = counts.zip(picked).toMap(),
                colorForCount = emptyMap()
            )
        }

        GlyphMode.COLORS -> {
            var picked: List<Long> = emptyList()
            if (shuffleMode == ShuffleMode.ON) {
                val set = VisualCatalog.colors(colorSetId).requireAtLeast(8)
                picked = shuffled(set.colors.take(8), shuffleColors)
            }
            else {
                val set = VisualCatalog.colors(colorSetId).requireAtLeast(8)
                picked = set.colors.take(8)
            }
            // Use a neutral glyph like "•" (or empty) and color carries meaning
            ResolvedVisualMapping(
                glyphForCount = counts.associateWith { "•" },
                colorForCount = counts.zip(picked).toMap()
            )
        }
    }
}
