package com.kim.minemind

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
    // Inflate the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.miani_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Handle search action
//                Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_settings -> {
                // Handle settings action
//                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                // Open the Settings Activity (which you'll create in Step 2)
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}


//
//resolver = visualResolver,
//settingsRepo = settingsRepo,
//savedState= savedStateHandle