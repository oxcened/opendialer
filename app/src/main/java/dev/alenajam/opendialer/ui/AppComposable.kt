package dev.alenajam.opendialer.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.alenajam.opendialer.feature.callDetail.CallDetailRoute
import dev.alenajam.opendialer.feature.callDetail.CallDetailScreen
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchRoute
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchScreen
import dev.alenajam.opendialer.feature.settings.SettingsRoute
import dev.alenajam.opendialer.feature.settings.SettingsScreen

@Composable
internal fun AppComposable() {
    val navController = rememberNavController()

    AppTheme {
        NavHost(navController = navController, startDestination = HomeRoute) {
            composable<HomeRoute> {
                HomeScreen(
                    onOpenDialpad = {
                        navController.navigate(ContactsSearchRoute)
                    },
                    onOpenHistory = {
                        navController.navigate(CallDetailRoute(callIds = it))
                    },
                    onOpenSettings = {
                        navController.navigate(SettingsRoute)
                    }
                )
            }
            composable<ContactsSearchRoute> {
                ContactsSearchScreen()
            }
            composable<CallDetailRoute> {
                CallDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}