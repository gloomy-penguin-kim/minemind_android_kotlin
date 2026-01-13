package com.kim.minemind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kim.minemind.ui.GameScreen
import com.kim.minemind.ui.theme.MineMindTheme
import com.kim.minemind.ui.state.GameViewModel

import com.kim.minemind.ui.settings.VisualSettingsRepository
import com.kim.minemind.ui.state.visualSettingsDataStore
import com.kim.minemind.ui.GameViewModelFactory
import com.kim.minemind.ui.settings.VisualResolver

import com.kim.minemind.ui.state.GameStateRepository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kim.minemind.ui.state.gameStateDataStore

class MainActivity : ComponentActivity() {

    private val settingsRepo by lazy {
        VisualSettingsRepository(applicationContext.visualSettingsDataStore)
    }

    private val visualResolver by lazy {
        VisualResolver()
    }

    private val gameStateRepo by lazy {
        GameStateRepository(applicationContext.gameStateDataStore)
    }


//    class GameStateRepository(
//        private val dataStore: DataStore<Preferences>
//    ) {
    private val vmFactory by lazy {
        GameViewModelFactory(settingsRepo, visualResolver, gameStateRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MineMindTheme {
                val vm: GameViewModel = viewModel(factory = vmFactory)
                GameScreen(vm = vm)
            }
        }
    }
}


//
//resolver = visualResolver,
//settingsRepo = settingsRepo,
//savedState= savedStateHandle