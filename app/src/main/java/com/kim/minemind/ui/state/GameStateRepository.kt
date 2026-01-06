package com.kim.minemind.ui.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameStateRepository(private val ds: DataStore<Preferences>) {
    private val KEY = stringPreferencesKey("game_snapshot")

    val snapshotFlow: Flow<String?> = ds.data.map { it[KEY] }

    suspend fun save(json: String) {
        ds.edit { it[KEY] = json }
    }

    suspend fun clear() {
        ds.edit { it.remove(KEY) }
    }
}