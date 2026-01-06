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
                val glyphs = set.forGlyphSet()
                VisualState(
                    glyphs = glyphs,
                    colors = emptyList()
                )
            }
            GlyphMode.ALPHABET -> {
                val set = alphaSets.first { it.id == settings.alphaSetId }
                val base = set.forGlyphSet()
                val glyphs = if (settings.shuffleGlyphs) base.shuffled(rng) else base
                VisualState(glyphs = glyphs, colors = emptyList())
            }
            GlyphMode.COLORS -> {
                val set = colorSets.first { it.id == settings.colorSetId }
                val base = set.colors.take(n=8)
                val colors = if (settings.shuffleColors) base.shuffled(rng) else base
                // glyphs can be "•" repeated, or "", or "●"
                VisualState(glyphs = List(8) { "●" }, colors = colors)
            }
        }
    }
    private fun <T> List<T>.shuffled(rng: java.util.Random): List<T> =
        this.shuffled(kotlin.random.Random(rng.nextLong()))
}

