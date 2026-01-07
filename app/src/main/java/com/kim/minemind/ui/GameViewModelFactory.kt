package com.kim.minemind.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kim.minemind.ui.settings.VisualResolver
import com.kim.minemind.ui.settings.VisualSettingsRepository
import com.kim.minemind.ui.state.GameStateRepository
import com.kim.minemind.ui.state.GameViewModel

class GameViewModelFactory(
    private val settingsRepo: VisualSettingsRepository,
    private val visualResolver: VisualResolver,
    private val gameStateRepo: GameStateRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(settingsRepo, visualResolver, gameStateRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
