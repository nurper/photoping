package com.photoping.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.photoping.ui.screens.CameraScreen
import com.photoping.ui.screens.HomeScreen
import com.photoping.ui.screens.LoginScreen
import com.photoping.ui.screens.RegisterScreen
import com.photoping.ui.screens.SubmitScreen
import com.photoping.ui.viewmodel.AppViewModel

@Composable
fun PhotoPingNavGraph(appViewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {
        composable(Routes.Login) {
            LoginScreen(
                appViewModel = appViewModel,
                onLoggedIn = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(Routes.Register) }
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                appViewModel = appViewModel,
                onRegistered = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                appViewModel = appViewModel,
                onOpenCamera = { navController.navigate(Routes.Camera) }
            )
        }

        composable(Routes.Camera) {
            CameraScreen(
                onPhotoCaptured = { path ->
                    navController.navigate("${Routes.Submit}?path=${Uri.encode(path)}")
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.Submit}?path={path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { entry ->
            val path = entry.arguments?.getString("path")?.let { Uri.decode(it) }
            SubmitScreen(
                appViewModel = appViewModel,
                imagePath = path,
                onDone = {
                    navController.popBackStack(Routes.Home, inclusive = false)
                }
            )
        }
    }
}
