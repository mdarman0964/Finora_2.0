package com.finora

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.ui.FinoraApp
import com.finora.ui.FinoraViewModel
import com.finora.ui.theme.FinoraTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: FinoraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeChoice by viewModel.themeChoice.collectAsStateWithLifecycle()
            val language by viewModel.language.collectAsStateWithLifecycle()

            val locale = remember(language) {
                if (language == "bn") Locale("bn", "BD") else Locale.ENGLISH
            }

            LaunchedEffect(locale) {
                Locale.setDefault(locale)
            }

            val currentConfig = LocalConfiguration.current
            val localizedConfiguration = remember(currentConfig, locale) {
                Configuration(currentConfig).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }
            val currentContext = LocalContext.current
            val localizedContext = remember(currentContext, locale) {
                val conf = Configuration(currentContext.resources.configuration).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                currentContext.createConfigurationContext(conf)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext
            ) {
                FinoraTheme(themeChoice = themeChoice) {
                    FinoraApp(viewModel)
                }
            }
        }
    }
}
