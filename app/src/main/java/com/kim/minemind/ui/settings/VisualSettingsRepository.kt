package com.kim.minemind.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class VisualSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val GLYPH_MODE = stringPreferencesKey("mode")
        private val NUMERAL_SET = stringPreferencesKey("numeral_set")
        private val ALPHA_SET = stringPreferencesKey("alpha_set")
        private val COLOR_SET = stringPreferencesKey("color_set")
        private val SHUFFLE_GLYPHS = booleanPreferencesKey("shuffle_glyphs")
        private val SHUFFLE_COLORS = booleanPreferencesKey("shuffle_colors")
    }

    val settingsFlow: Flow<VisualSettings> =
        dataStore.data.map { prefs ->
            VisualSettings(
                glyphMode = prefs[GLYPH_MODE]?.let { GlyphMode.valueOf(it) }
                    ?: GlyphMode.NUMERALS,
                numeralSetId = prefs[NUMERAL_SET] ?: "latin_digits",
                alphaSetId = prefs[ALPHA_SET] ?: "latin_letters",
                colorSetId = prefs[COLOR_SET] ?: "classic_dots",
                shuffleGlyphs = prefs[SHUFFLE_GLYPHS] ?: false,
                shuffleColors = prefs[SHUFFLE_COLORS] ?: false,
            )
        }

    suspend fun set(settings: VisualSettings) {
        dataStore.edit { prefs ->
            prefs[GLYPH_MODE] = settings.glyphMode.name
            prefs[NUMERAL_SET] = settings.numeralSetId
            prefs[ALPHA_SET] = settings.alphaSetId
            prefs[COLOR_SET] = settings.colorSetId
            prefs[SHUFFLE_GLYPHS] = settings.shuffleGlyphs
            prefs[SHUFFLE_COLORS] = settings.shuffleColors
        }
    }
}
