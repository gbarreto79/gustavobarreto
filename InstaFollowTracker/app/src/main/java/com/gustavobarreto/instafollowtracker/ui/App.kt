package com.gustavobarreto.instafollowtracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gustavobarreto.instafollowtracker.R
import com.gustavobarreto.instafollowtracker.ui.navigation.Destination
import com.gustavobarreto.instafollowtracker.ui.navigation.HISTORY_ROUTE
import com.gustavobarreto.instafollowtracker.ui.screens.dashboard.DashboardScreen
import com.gustavobarreto.instafollowtracker.ui.screens.history.HistoryScreen
import com.gustavobarreto.instafollowtracker.ui.screens.importdata.ImportScreen
import com.gustavobarreto.instafollowtracker.ui.screens.userlist.UserListKind
import com.gustavobarreto.instafollowtracker.ui.screens.userlist.UserListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstaFollowTrackerApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val currentDestination = Destination.bottomBarItems.find { it.route == currentRoute }
    val title = currentDestination?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.app_name)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(HISTORY_ROUTE) }) {
                        Icon(Icons.Filled.History, contentDescription = stringResource(R.string.history_title))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                Destination.bottomBarItems.forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                        label = { Text(stringResource(destination.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Dashboard.route) {
                DashboardScreen(
                    onNavigateToUnfollowers = { navController.navigate(Destination.Unfollowers.route) },
                    onNavigateToNewFollowers = { navController.navigate(Destination.NewFollowers.route) },
                    onNavigateToNotFollowingBack = { navController.navigate(Destination.NotFollowingBack.route) },
                    onNavigateToImport = { navController.navigate(Destination.Import.route) }
                )
            }
            composable(Destination.Unfollowers.route) {
                UserListScreen(kind = UserListKind.UNFOLLOWERS)
            }
            composable(Destination.NewFollowers.route) {
                UserListScreen(kind = UserListKind.NEW_FOLLOWERS)
            }
            composable(Destination.NotFollowingBack.route) {
                UserListScreen(kind = UserListKind.NOT_FOLLOWING_BACK)
            }
            composable(Destination.Import.route) {
                ImportScreen()
            }
            composable(HISTORY_ROUTE) {
                HistoryScreen()
            }
        }
    }
}
