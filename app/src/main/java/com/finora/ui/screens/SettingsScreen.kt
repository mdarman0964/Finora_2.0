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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.R
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.FinoraCard

@Composable
fun SettingsScreen(
    viewModel: FinoraViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val theme by viewModel.themeChoice.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()
    var showLicenseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onOpenDrawer, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.open_menu))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Appearance
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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

        // Language
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = language == "en", onClick = { setLanguage(viewModel, "en") }, label = { Text("English") })
                        FilterChip(selected = language == "bn", onClick = { setLanguage(viewModel, "bn") }, label = { Text("বাংলা") })
                    }
                }
            }
        }

        // About Finora, Developer & MIT License
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.about_finora), style = MaterialTheme.typography.titleMedium)
                    }
                    Text(stringResource(R.string.about_finora_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Developer Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text("${stringResource(R.string.developer_label)}:", style = MaterialTheme.typography.titleSmall)
                            }
                            Text(stringResource(R.string.developer_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // MIT License Option Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Outlined.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Text(stringResource(R.string.mit_license_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(stringResource(R.string.mit_license_subtitle), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(stringResource(R.string.mit_license_notice), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(
                                onClick = { showLicenseDialog = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(stringResource(R.string.mit_license_view_full), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Text("Finora · 1.0.0", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // App Usage Instructions
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.instructions_title), style = MaterialTheme.typography.titleMedium)
                    }

                    InstructionItem(
                        icon = Icons.Outlined.Restaurant,
                        title = stringResource(R.string.instruction_bachelor_title),
                        description = stringResource(R.string.instruction_bachelor_body)
                    )

                    InstructionItem(
                        icon = Icons.Outlined.Person,
                        title = stringResource(R.string.instruction_personal_title),
                        description = stringResource(R.string.instruction_personal_body)
                    )

                    InstructionItem(
                        icon = Icons.Outlined.Storefront,
                        title = stringResource(R.string.instruction_business_title),
                        description = stringResource(R.string.instruction_business_body)
                    )

                    InstructionItem(
                        icon = Icons.Outlined.Book,
                        title = stringResource(R.string.instruction_notebook_title),
                        description = stringResource(R.string.instruction_notebook_body)
                    )

                    InstructionItem(
                        icon = Icons.Outlined.PictureAsPdf,
                        title = stringResource(R.string.instruction_reports_title),
                        description = stringResource(R.string.instruction_reports_body)
                    )
                }
            }
        }
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.mit_license_title))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(R.string.mit_license_full_text),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
private fun InstructionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun setLanguage(viewModel: FinoraViewModel, language: String) {
    viewModel.setLanguage(language)
    viewModel.setBanglaNumerals(language == "bn")
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
}
