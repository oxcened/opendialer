package dev.alenajam.opendialer.ui

import android.content.Intent
import android.telecom.PhoneAccount
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.alenajam.opendialer.core.common.MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.feature.callDetail.CallDetailRoute
import dev.alenajam.opendialer.feature.callDetail.CallDetailScreen
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchRoute
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchScreen
import dev.alenajam.opendialer.feature.settings.SettingsRoute
import dev.alenajam.opendialer.feature.settings.SettingsScreen

@Composable
internal fun AppComposable() {
    val navController = rememberNavController()

    HandleIntent(
        onOpenContactsSearch = { navController.navigate(ContactsSearchRoute(it)) }
    )

    AppTheme {
        NavHost(navController = navController, startDestination = HomeRoute) {
            composable<HomeRoute> {
                HomeScreen(
                    onOpenDialpad = {
                        navController.navigate(ContactsSearchRoute())
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

@Composable
fun HandleIntent(
    onOpenContactsSearch: (prefilledNumber: String) -> Unit
) {
    val activity = LocalContext.current.getActivity()

    fun handleIntent(intent: Intent?) {
        if (intent == null) return

        if (
            intent.action == Intent.ACTION_DIAL
            && intent.getBooleanExtra(MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL, false)
        ) {
            onOpenContactsSearch("")
        } else if (
            arrayOf(Intent.ACTION_DIAL, Intent.ACTION_VIEW).contains(intent.action)
            && intent.data?.scheme == PhoneAccount.SCHEME_TEL
        ) {
            onOpenContactsSearch(intent.data!!.schemeSpecificPart)
        }
    }

    LaunchedEffect(Unit) {
        handleIntent(activity?.intent)
    }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent>(::handleIntent)
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }
}