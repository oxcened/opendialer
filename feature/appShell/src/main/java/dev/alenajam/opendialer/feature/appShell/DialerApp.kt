package dev.alenajam.opendialer.feature.appShell

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.PhoneAccount
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.navigation.toRoute
import dev.alenajam.opendialer.core.common.DefaultPhoneManager
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
import dev.alenajam.opendialer.feature.settings.QuickResponsesRoute
import dev.alenajam.opendialer.feature.settings.QuickResponsesScreen
import dev.alenajam.opendialer.feature.settings.DisplayOptionsRoute
import dev.alenajam.opendialer.feature.settings.DisplayOptionsScreen
import dev.alenajam.opendialer.feature.settings.AboutRoute
import dev.alenajam.opendialer.feature.settings.AboutScreen
import dev.alenajam.opendialer.feature.settings.SettingsSubpage
import dev.alenajam.opendialer.feature.settings.SettingsSubpageRoute
import dev.alenajam.opendialer.feature.settings.SettingsSubpageDestinationRoute
import dev.alenajam.opendialer.feature.settings.SettingsSubpageScreen
import kotlinx.serialization.Serializable

@Serializable
private data object HomeRoute

@Serializable
data object AddFavoriteRoute

@Composable
fun DialerApp(
    defaultPhoneManager: DefaultPhoneManager,
    icons: AppIcons = DefaultAppIcons,
    themeExtension: AppThemeExtension = AppThemeExtension(),
    settingsSubpages: List<SettingsSubpage> = emptyList(),
) {
    val navController = rememberNavController()

    AppProviders(icons = icons, themeExtension = themeExtension) {
        val activity = LocalContext.current.getActivity()
        var isDefaultPhoneApp by remember(activity) {
            mutableStateOf(defaultPhoneManager.isDefaultDialer())
        }
        var hasFullScreenIntentAccess by remember(activity) {
            mutableStateOf(activity.canUseFullScreenIntent())
        }
        var defaultPhoneRequestWasDenied by remember { mutableStateOf(false) }
        val lifecycleOwner = LocalLifecycleOwner.current

        fun refreshSetupState() {
            isDefaultPhoneApp = defaultPhoneManager.isDefaultDialer()
            hasFullScreenIntentAccess = activity.canUseFullScreenIntent()
        }

        val defaultPhoneLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                refreshSetupState()
                defaultPhoneRequestWasDenied = !isDefaultPhoneApp
            }
        )
        val fullScreenIntentLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { refreshSetupState() }
        )

        DisposableEffect(activity, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    refreshSetupState()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        if (!isDefaultPhoneApp || !hasFullScreenIntentAccess) {
            SetupScreen(
                isDefaultPhoneApp = isDefaultPhoneApp,
                hasFullScreenIntentAccess = hasFullScreenIntentAccess,
                showDefaultPhoneRecovery = defaultPhoneRequestWasDenied && !isDefaultPhoneApp,
                onSetAsDefault = {
                    defaultPhoneRequestWasDenied = false
                    defaultPhoneManager.createRequestDefaultDialerIntent()?.let { intent ->
                        defaultPhoneLauncher.launch(intent)
                    }
                },
                onOpenAppInfo = {
                    activity?.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", activity.packageName, null)
                        }
                    )
                },
                onEnableFullScreenIntent = {
                    activity?.let {
                        fullScreenIntentLauncher.launch(
                            Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                data = Uri.parse("package:${it.packageName}")
                            }
                        )
                    }
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
                        onOpenAbout = { navController.navigate(AboutRoute) },
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
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onOpenQuickResponses = { navController.navigate(QuickResponsesRoute) },
                        onOpenDisplayOptions = { navController.navigate(DisplayOptionsRoute) },
                        subpages = settingsSubpages,
                        onOpenSubpage = { navController.navigate(SettingsSubpageRoute(it)) }
                    )
                }
                composable<AboutRoute> {
                    AboutScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable<QuickResponsesRoute> {
                    QuickResponsesScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable<DisplayOptionsRoute> {
                    DisplayOptionsScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable<SettingsSubpageRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<SettingsSubpageRoute>()
                    settingsSubpages.getOrNull(route.index)?.let { page ->
                        SettingsSubpageScreen(
                            page = page,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDestination = { destinationIndex ->
                                navController.navigate(SettingsSubpageDestinationRoute(route.index, destinationIndex))
                            }
                        )
                    }
                }
                composable<SettingsSubpageDestinationRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<SettingsSubpageDestinationRoute>()
                    settingsSubpages.getOrNull(route.subpageIndex)
                        ?.destinations
                        ?.getOrNull(route.destinationIndex)
                        ?.content { navController.popBackStack() }
                }
            }
        }
    }
}

private fun android.app.Activity?.canUseFullScreenIntent(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            this?.getSystemService(NotificationManager::class.java)?.canUseFullScreenIntent() == true

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
