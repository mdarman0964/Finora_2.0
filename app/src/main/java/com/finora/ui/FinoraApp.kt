package com.finora.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finora.R
import com.finora.ui.screens.BusinessScreen
import com.finora.ui.screens.EntryEditorSheet
import com.finora.ui.screens.HomeScreen
import com.finora.ui.screens.MessScreen
import com.finora.ui.screens.NotebookScreen
import com.finora.ui.screens.ReportsScreen
import com.finora.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun FinoraApp(viewModel: FinoraViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "personal"
    val books by viewModel.books.collectAsState()
    val members by viewModel.members.collectAsState()
    var showEntryEditor by remember { mutableStateOf(false) }
    var selectedBookId by rememberSaveable { mutableIntStateOf(0) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navigateTo: (String) -> Unit = { destination ->
        navController.navigate(destination) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    if (!drawerState.isOpen && route != "personal") {
        BackHandler {
            navigateTo("personal")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 16.dp)
                ) {
                    // Header with App logo & Branding
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_finora),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(46.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.app_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                    Text(
                        stringResource(R.string.nav_sections),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    // 1. Personal Section
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.section_personal), style = MaterialTheme.typography.titleMedium) },
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        selected = route == "personal",
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo("personal")
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                    )

                    // 2. Business Section
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.section_business), style = MaterialTheme.typography.titleMedium) },
                        icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                        selected = route == "business",
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo("business")
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                    )

                    // 3. Bachelor Mess Section
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.section_bachelor), style = MaterialTheme.typography.titleMedium) },
                        icon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                        selected = route == "bachelor",
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo("bachelor")
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                    )

                    // 4. Notebook Section
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.section_notebook), style = MaterialTheme.typography.titleMedium) },
                        icon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                        selected = route == "notebook",
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo("notebook")
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))

                    // Reports Navigation
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_reports), style = MaterialTheme.typography.titleMedium) },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                        selected = route == "reports",
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo("reports")
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                    )

                    Spacer(Modifier.weight(1f))

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                    // Gear (Settings) option
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.titleMedium) },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        selected = route == "settings",
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo("settings")
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { contentPadding ->
            NavHost(
                navController = navController,
                startDestination = "personal",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                composable("personal") {
                    HomeScreen(
                        viewModel = viewModel,
                        selectedBookId = selectedBookId,
                        onBookSelected = { selectedBookId = it },
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                composable("business") {
                    BusinessScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                composable("bachelor") {
                    MessScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                composable("notebook") {
                    NotebookScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                composable("reports") {
                    ReportsScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }

    if (showEntryEditor) {
        val defaultSection = when (route) {
            "business" -> "Business"
            else -> "Personal"
        }
        EntryEditorSheet(
            viewModel = viewModel,
            bookId = selectedBookId.takeIf { id -> books.any { it.id == id } } ?: books.firstOrNull()?.id ?: 0,
            members = members.filter { it.isActive },
            initialEntry = null,
            defaultSection = defaultSection,
            onDismiss = { showEntryEditor = false }
        )
    }
}
