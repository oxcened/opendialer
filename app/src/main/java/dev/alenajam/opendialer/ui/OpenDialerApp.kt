package dev.alenajam.opendialer.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.alenajam.opendialer.feature.callDetail.CallDetailRoute
import dev.alenajam.opendialer.feature.callDetail.CallDetailScreen
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchRoute
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpenDialerApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenDialpad = {
                    navController.navigate(ContactsSearchRoute)
                },
                onOpenHistory = {
                    navController.navigate(CallDetailRoute(callIds = it))
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
    }
}