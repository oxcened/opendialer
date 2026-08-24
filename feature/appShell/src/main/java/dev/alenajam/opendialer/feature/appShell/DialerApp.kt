package dev.alenajam.opendialer.feature.appShell

import android.content.Intent
import android.telecom.PhoneAccount
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.alenajam.opendialer.core.common.DefaultPhoneUtils
import dev.alenajam.opendialer.core.common.MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.core.common.ui.AppIcons
import dev.alenajam.opendialer.core.common.ui.AppProviders
import dev.alenajam.opendialer.core.common.ui.AppThemeExtension
import dev.alenajam.opendialer.core.common.ui.DefaultAppIcons
import dev.alenajam.opendialer.feature.callDetail.CallDetailRoute
import dev.alenajam.opendialer.feature.callDetail.CallDetailScreen
import dev.alenajam.opendialer.feature.contacts.AddFavoriteScreen
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchRoute
import dev.alenajam.opendialer.feature.contactsSearch.ContactsSearchScreen
import dev.alenajam.opendialer.feature.settings.SettingsRoute
import dev.alenajam.opendialer.feature.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
private data object HomeRoute

@Serializable
data object AddFavoriteRoute

@Composable
fun DialerApp(
    icons: AppIcons = DefaultAppIcons,
    themeExtension: AppThemeExtension = AppThemeExtension(),
) {
    val navController = rememberNavController()

    AppProviders(icons = icons, themeExtension = themeExtension) {
        val activity = LocalContext.current.getActivity()
        var isDefaultPhoneApp by remember(activity) {
            mutableStateOf(activity?.let(DefaultPhoneUtils::hasDefault) == true)
        }
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(activity, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isDefaultPhoneApp = activity?.let(DefaultPhoneUtils::hasDefault) == true
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        if (!isDefaultPhoneApp) {
            DefaultPhoneScreen(
                onSetAsDefault = {
                    activity?.let { DefaultPhoneUtils.requestDefault(it, DEFAULT_PHONE_REQUEST_CODE) }
                },
            )
        } else {
            HandleDialIntent(
                onOpenContactsSearch = { navController.navigate(ContactsSearchRoute(it)) }
            )
            NavHost(navController = navController, startDestination = HomeRoute) {
                composable<HomeRoute> {
                    HomeScreen(
                        onOpenDialpad = { number -> navController.navigate(ContactsSearchRoute(number)) },
                        onOpenHistory = { navController.navigate(CallDetailRoute(callIds = it)) },
                        onOpenSettings = { navController.navigate(SettingsRoute) },
                        onAddFavorite = { navController.navigate(AddFavoriteRoute) }
                    )
                }
                composable<AddFavoriteRoute> {
                    AddFavoriteScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable<ContactsSearchRoute> {
                    ContactsSearchScreen(
                        onOpenHistory = { navController.navigate(CallDetailRoute(callIds = it)) },
                        onDialpadCallStarted = { navController.popBackStack() }
                    )
                }
                composable<CallDetailRoute> {
                    CallDetailScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable<SettingsRoute> {
                    SettingsScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private const val DEFAULT_PHONE_REQUEST_CODE = 1001

@Composable
private fun HandleDialIntent(onOpenContactsSearch: (prefilledNumber: String) -> Unit) {
    val activity = LocalContext.current.getActivity()

    fun handleIntent(intent: Intent?) {
        if (intent == null) return

        if (
            intent.action == Intent.ACTION_DIAL &&
            intent.getBooleanExtra(MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL, false)
        ) {
            onOpenContactsSearch("")
        } else if (
            arrayOf(Intent.ACTION_DIAL, Intent.ACTION_VIEW).contains(intent.action) &&
            intent.data?.scheme == PhoneAccount.SCHEME_TEL
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
