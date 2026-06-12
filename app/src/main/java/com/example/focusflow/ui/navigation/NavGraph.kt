package com.example.focusflow.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focusflow.ui.auth.LoginScreen
import com.example.focusflow.ui.settings.SettingsScreen
import com.example.focusflow.ui.splash.SplashScreen
import com.example.focusflow.ui.tasks.MapPickerScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val MAP_PICKER = "map_picker"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onAuthChecked = { isLoggedIn ->
                    if (isLoggedIn) {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = Routes.LOGIN,
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 3 } },
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
            )
        }

        composable(
            route = Routes.SETTINGS,
            enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it } + fadeOut(tween(200)) },
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MAP_PICKER) {
            MapPickerScreen(
                onLocationSelected = { address ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("picked_location", address)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
