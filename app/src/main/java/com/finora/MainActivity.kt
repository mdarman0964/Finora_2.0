package com.finora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.finora.ui.FinoraApp
import com.finora.ui.FinoraViewModel
import com.finora.ui.theme.FinoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: FinoraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeChoice by viewModel.themeChoice.collectAsStateWithLifecycle()
            FinoraTheme(themeChoice = themeChoice) {
                FinoraApp(viewModel)
            }
        }
    }
}
