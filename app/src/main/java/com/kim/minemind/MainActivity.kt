package com.kim.minemind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kim.minemind.ui.GameScreen
import com.kim.minemind.ui.theme.MineMindTheme
import com.kim.minemind.ui.state.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MineMindTheme {
                val vm: GameViewModel = viewModel()
                GameScreen(vm = vm)
            }
        }
    }
}
