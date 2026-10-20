package com.example.ventasapp_ev2.ui.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ventasapp_ev2.ui.login.LoginScreen
import com.example.ventasapp_ev2.ui.menu.MenuScreen
import com.example.ventasapp_ev2.ui.venta.ListaVentasScreen
import com.example.ventasapp_ev2.ui.venta.VentaFormScreen
import com.example.ventasapp_ev2.ui.venta.VentaViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()


    val ventaViewModel: VentaViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        //Login
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

        //Menu
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

        //Lista de ventas
        composable(
            route = "lista/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L

            // Le dice al ViewModel qué vendedor está usando la app
            LaunchedEffect(usuarioId) { ventaViewModel.iniciarSesionDe(usuarioId) }

            ListaVentasScreen(
                viewModel = ventaViewModel,
                onNuevaVenta = { navController.navigate("form/$usuarioId/0") },
                onVentaClick = { ventaId -> navController.navigate("form/$usuarioId/$ventaId") },
                onVolver = { navController.popBackStack() }
            )
        }

        //Formulario
        composable(
            route = "form/{usuarioId}/{ventaId}",
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.LongType },
                navArgument("ventaId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getLong("ventaId") ?: 0L

            VentaFormScreen(
                ventaId = ventaId,
                viewModel = ventaViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}