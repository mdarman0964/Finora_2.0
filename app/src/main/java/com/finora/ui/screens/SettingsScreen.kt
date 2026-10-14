package com.finora.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.R
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.FinoraCard

@Composable
fun SettingsScreen(viewModel: FinoraViewModel) {
    val theme by viewModel.themeChoice.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        androidx.compose.material3.Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("system" to R.string.theme_system, "dark" to R.string.theme_dark, "light" to R.string.theme_light).forEach { (value, label) ->
                            FilterChip(selected = theme == value, onClick = { viewModel.setThemeChoice(value) }, label = { Text(stringResource(label)) })
                        }
                    }
                }
            }
        }
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        androidx.compose.material3.Icon(Icons.Outlined.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = language == "en", onClick = { setLanguage(viewModel, "en") }, label = { Text("English") })
                        FilterChip(selected = language == "bn", onClick = { setLanguage(viewModel, "bn") }, label = { Text("বাংলা") })
                    }
                }
            }
        }
        item {
            FinoraCard {
                Row(
                    Modifier.fillMaxWidth().padding(17.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        androidx.compose.material3.Icon(Icons.Outlined.Numbers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(stringResource(R.string.bangla_numerals), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.bangla_numerals_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = banglaDigits, onCheckedChange = viewModel::setBanglaNumerals)
                }
            }
        }
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.about_finora), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.about_finora_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Finora · 1.0.0", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun setLanguage(viewModel: FinoraViewModel, language: String) {
    viewModel.setLanguage(language)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
}
