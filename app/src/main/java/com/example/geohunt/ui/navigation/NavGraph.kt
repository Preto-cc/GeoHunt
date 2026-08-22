package com.example.geohunt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.geohunt.data.repository.AuthRepository
import com.example.geohunt.ui.screens.*

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Eventos : Screen("eventos")
    object Admin : Screen("admin")
    object Profile : Screen("profile")
    object AdminLugares : Screen("admin_lugares/{eventoId}") {
        fun createRoute(eventoId: String) = "admin_lugares/$eventoId"
    }
    object Lugares : Screen("lugares/{eventoId}") {
        fun createRoute(eventoId: String) = "lugares/$eventoId"
    }
    object DetalleLugar : Screen("detalle/{eventoId}/{lugarId}") {
        fun createRoute(eventoId: String, lugarId: String) = "detalle/$eventoId/$lugarId"
    }
    object Ranking : Screen("ranking/{eventoId}") {
        fun createRoute(eventoId: String) = "ranking/$eventoId"
    }
}

// Extensión para navegar de forma segura evitando duplicados por clics rápidos
fun NavHostController.navigateSafe(route: String, popUpToRoute: String? = null, inclusive: Boolean = false) {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.navigate(route) {
            if (popUpToRoute != null) {
                popUpTo(popUpToRoute) { this.inclusive = inclusive }
            }
            launchSingleTop = true
        }
    }
}

// Extensión para volver atrás de forma segura
fun NavHostController.popSafe() {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.popBackStack()
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    val startDest = if (AuthRepository.isUserLoggedIn()) Screen.Eventos.route else Screen.Welcome.route

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginSuccess = {
                    navController.navigateSafe(Screen.Eventos.route, Screen.Welcome.route, true)
                }
            )
        }
        composable(Screen.Eventos.route) {
            ListadoEventosScreen(
                onEventoClick = { eventoId ->
                    navController.navigateSafe(Screen.Lugares.createRoute(eventoId))
                },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAdminClick = {
                    navController.navigateSafe(Screen.Admin.route)
                },
                onProfileClick = {
                    navController.navigateSafe(Screen.Profile.route)
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popSafe() },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Admin.route) {
            AdminScreen(
                onBack = { navController.popSafe() },
                onEventoClick = { eventoId ->
                    navController.navigateSafe(Screen.AdminLugares.createRoute(eventoId))
                }
            )
        }
        composable(Screen.AdminLugares.route) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            AdminLugaresScreen(
                eventoId = eventoId,
                onBack = { navController.popSafe() }
            )
        }
        composable(Screen.Lugares.route) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            ListadoLugaresScreen(
                eventoId = eventoId,
                onLugarClick = { lugarId ->
                    navController.navigateSafe(Screen.DetalleLugar.createRoute(eventoId, lugarId))
                },
                onVerRanking = {
                    navController.navigateSafe(Screen.Ranking.createRoute(eventoId))
                },
                onBack = { navController.popSafe() }
            )
        }
        composable(Screen.DetalleLugar.route) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: ""
            DetalleLugarScreen(
                eventoId = eventoId,
                lugarId = lugarId,
                onBack = { navController.popSafe() }
            )
        }
        composable(Screen.Ranking.route) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            RankingScreen(
                eventoId = eventoId,
                onBack = { navController.popSafe() }
            )
        }
    }
}
