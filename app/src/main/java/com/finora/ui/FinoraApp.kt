package com.finora.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finora.R
import com.finora.ui.screens.HomeScreen
import com.finora.ui.screens.MessScreen
import com.finora.ui.screens.ReportsScreen
import com.finora.ui.screens.SettingsScreen
import com.finora.ui.screens.EntryEditorSheet

private data class Destination(val route: String, val label: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun FinoraApp(viewModel: FinoraViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "home"
    val books by viewModel.books.collectAsState()
    val members by viewModel.members.collectAsState()
    var showEntryEditor by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FinoraBottomBar(
                selectedRoute = route,
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAdd = { showEntryEditor = true }
            )
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(contentPadding)
        ) {
            composable("home") { HomeScreen(viewModel) }
            composable("mess") { MessScreen(viewModel) }
            composable("reports") { ReportsScreen(viewModel) }
            composable("settings") { SettingsScreen(viewModel) }
        }
    }

    if (showEntryEditor) {
        EntryEditorSheet(
            viewModel = viewModel,
            bookId = books.firstOrNull()?.id ?: 0,
            members = members.filter { it.isActive },
            initialEntry = null,
            onDismiss = { showEntryEditor = false }
        )
    }
}

@Composable
private fun FinoraBottomBar(selectedRoute: String, onNavigate: (String) -> Unit, onAdd: () -> Unit) {
    val destinations = listOf(
        Destination("home", R.string.nav_home, Icons.Filled.Home),
        Destination("mess", R.string.nav_mess, Icons.Filled.Storefront),
        Destination("reports", R.string.nav_reports, Icons.Filled.BarChart),
        Destination("settings", R.string.nav_settings, Icons.Filled.Settings)
    )
    NavigationBar(
        modifier = Modifier.navigationBarsPadding().height(76.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            destinations.take(2).forEach { item ->
                NavigationBarItem(
                    selected = selectedRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    icon = { Icon(item.icon, contentDescription = stringResource(item.label), modifier = Modifier.size(21.dp)) },
                    label = { Text(stringResource(item.label), style = MaterialTheme.typography.labelLarge) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                )
            }
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier.size(54.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_entry), modifier = Modifier.size(27.dp))
            }
            destinations.drop(2).forEach { item ->
                NavigationBarItem(
                    selected = selectedRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    icon = { Icon(item.icon, contentDescription = stringResource(item.label), modifier = Modifier.size(21.dp)) },
                    label = { Text(stringResource(item.label), style = MaterialTheme.typography.labelLarge) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                )
            }
        }
    }
}
