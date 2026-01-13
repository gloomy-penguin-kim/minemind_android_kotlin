package com.kim.minemind.ui.settings


class VisualResolver(
    private val numeralSets: List<GlyphSet> = VisualCatalog.numeralSets,
    private val alphaSets: List<GlyphSet> = VisualCatalog.alphaSets,
    private val colorSets: List<ColorSet> = VisualCatalog.colorSets,
    private val themeSets: List<ThemeColorSet> = VisualCatalog.ThemeSets
) {
    fun resolve(settings: VisualSettings, rng: java.util.Random): VisualState {
        return when (settings.glyphMode) {
            GlyphMode.NUMERALS -> {
                val set = numeralSets.first { it.id == settings.numeralSetId }
                val glyphs = set.glyphs.take(8)
                VisualState(
                    glyphs = glyphs,
                    colors = emptyList()
                )
            }
            GlyphMode.ALPHABET -> {
                val set = alphaSets.first { it.id == settings.alphaSetId }
                val base = set.glyphs
                var glyphs = if (settings.shuffleGlyphs) base.shuffled(rng) else base
                glyphs = glyphs.take(8)
                VisualState(glyphs = glyphs, colors = emptyList())
            }
            GlyphMode.COLORS -> {
                val set = colorSets.first { it.id == settings.colorSetId }
                val base = set.colors
                var colors = if (settings.shuffleColors) base.shuffled(rng) else base
                colors = colors.take(8)
                // glyphs can be "•" repeated, or "", or "●"
                VisualState(glyphs = List(8) { "●" }, colors = colors)
            }
        }
    }
    private fun <T> List<T>.shuffled(rng: java.util.Random): List<T> =
        this.shuffled(kotlin.random.Random(rng.nextLong()))
}

