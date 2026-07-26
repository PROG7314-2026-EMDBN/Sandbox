package com.prog7313.sandbox.navigation

import AddGadgetScreen
import GadgetsScreen
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.prog7313.sandbox.data.BiometricPreferencesRepository
import com.prog7313.sandbox.model.Gadget
import com.prog7313.sandbox.ui.FormScreen
import com.prog7313.sandbox.ui.HelloScreen
import com.prog7313.sandbox.viewmodel.PersonViewModel
import com.prog7313.sandbox.ui.HomeScreen
import com.prog7313.sandbox.ui.NotificationDemoScreen
import com.prog7313.sandbox.ui.SettingsScreen
import com.prog7313.sandbox.viewmodel.GadgetViewModel
import com.prog7313.sandbox.ui.FocusLogScreen
import com.prog7313.sandbox.ui.ProfileScreen
import com.prog7313.sandbox.viewmodel.SettingsViewModel
import com.prog7313.sandbox.ui.OpenLibraryDemoScreen
import com.prog7313.sandbox.ui.LocationDemoScreen

@Composable
fun AppNavGraph(
    settingsVm: SettingsViewModel,
    activity: FragmentActivity,
    biometricRepository: BiometricPreferencesRepository,
    onExit: () -> Unit,
    onLogout: () -> Unit
) {
    val personVm: PersonViewModel = viewModel()
    val gadgetVm: GadgetViewModel = viewModel()
    val navController = rememberNavController()
    val currentFirebaseUuid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    AppScaffold(
        navController = navController,
        onExit = onExit,
        onLogout = onLogout
    ) { contentModifier ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = contentModifier
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenForm = { navController.navigate(Routes.FORM) },
                    onOpenGadgets = { navController.navigate(Routes.GADGETS) },
                    onOpenFocusLog = { navController.navigate(Routes.FOCUSLOG) },
                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) },
                    onOpenLibrary = { navController.navigate(Routes.OPEN_LIBRARY) },
                    onOpenLocationDemo = { navController.navigate(Routes.LOCATION_DEMO) },
                    onExit = onExit
                )
            }

            composable(Routes.FORM) {
                FormScreen(
                    personVm = personVm,
                    onContinue = { navController.navigate(Routes.HELLO) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HELLO) {
                HelloScreen(
                    personVm = personVm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.GADGETS) {
                GadgetsScreen(
                    gadgetVm = gadgetVm,
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(Routes.ADD_GADGET) }
                )
            }

            composable(Routes.ADD_GADGET) {
                AddGadgetScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { newGadget: Gadget ->
                        gadgetVm.addGadget(newGadget)
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    settingsVm = settingsVm,
                    activity = activity,
                    biometricRepository = biometricRepository
                )
            }

            composable(Routes.FOCUSLOG) {
                FocusLogScreen(
                    firebaseUuid = currentFirebaseUuid,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.NOTIFICATIONS) {
                NotificationDemoScreen()
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.LOCATION_DEMO) {
                LocationDemoScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.OPEN_LIBRARY) {
                OpenLibraryDemoScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}