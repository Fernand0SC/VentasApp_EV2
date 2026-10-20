package com.example.ventasapp_ev2.ui.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ventasapp_ev2.ui.login.LoginScreen
import com.example.ventasapp_ev2.ui.menu.MenuScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginExitoso = { usuario ->
                    val nombre = Uri.encode(usuario.nombre)
                    navController.navigate("menu/${usuario.id}/$nombre/${usuario.perfil}") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "menu/{usuarioId}/{nombre}/{perfil}",
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.LongType },
                navArgument("nombre") { type = NavType.StringType },
                navArgument("perfil") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val perfil = backStackEntry.arguments?.getString("perfil") ?: ""

            MenuScreen(
                nombre = nombre,
                perfil = perfil,
                onIrAVentas = { navController.navigate("lista/$usuarioId") },
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }


        composable(
            route = "lista/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) {
            Text("Lista de ventas (próximamente)")
        }
    }
}