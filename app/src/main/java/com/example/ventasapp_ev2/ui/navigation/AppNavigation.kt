package com.example.ventasapp_ev2.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ventasapp_ev2.ui.login.LoginScreen
import com.example.ventasapp_ev2.ui.menu.MenuScreen
import com.example.ventasapp_ev2.ui.revision.RevisionDetalleScreen
import com.example.ventasapp_ev2.ui.revision.RevisionListaScreen
import com.example.ventasapp_ev2.ui.revision.RevisionViewModel
import com.example.ventasapp_ev2.ui.venta.ListaVentasScreen
import com.example.ventasapp_ev2.ui.venta.VentaFormScreen
import com.example.ventasapp_ev2.ui.venta.VentaViewModel

// Duración (ms) de las transiciones entre pantallas
private const val DURACION_NAVEGACION = 350

// El formulario se anima distinto (sube desde abajo), por eso se detecta aparte
private fun NavBackStackEntry.esFormulario(): Boolean =
    destination.route?.startsWith("form") == true

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val ventaViewModel: VentaViewModel = viewModel()
    val revisionViewModel: RevisionViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",

        // Avanzar: la pantalla nueva entra desde la derecha con fade
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION_NAVEGACION)
            ) + fadeIn(tween(DURACION_NAVEGACION))
        },
        // Avanzar: la pantalla actual se desplaza un poco a la izquierda y se desvanece
        exitTransition = {
            if (targetState.esFormulario()) {
                ExitTransition.None
            } else {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(DURACION_NAVEGACION),
                    targetOffset = { it / 3 }
                ) + fadeOut(tween(DURACION_NAVEGACION))
            }
        },
        // Volver: la pantalla anterior reaparece desde la izquierda
        popEnterTransition = {
            if (initialState.esFormulario()) {
                EnterTransition.None
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(DURACION_NAVEGACION),
                    initialOffset = { it / 3 }
                ) + fadeIn(tween(DURACION_NAVEGACION))
            }
        },
        // Volver: la pantalla que se cierra sale hacia la derecha
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION_NAVEGACION)
            ) + fadeOut(tween(DURACION_NAVEGACION))
        }
    ) {

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
                onIrARevision = { navController.navigate("revision/$usuarioId") },
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        //Lista de ventas (vendedor)
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

        //Formulario (vendedor)
        composable(
            route = "form/{usuarioId}/{ventaId}",
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.LongType },
                navArgument("ventaId") { type = NavType.LongType }
            ),
            // El formulario sube desde abajo, como una hoja
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(DURACION_NAVEGACION)
                ) + fadeIn(tween(DURACION_NAVEGACION))
            },
            // Y al cerrarse vuelve a bajar
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(DURACION_NAVEGACION)
                ) + fadeOut(tween(DURACION_NAVEGACION))
            }
        ) { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getLong("ventaId") ?: 0L

            VentaFormScreen(
                ventaId = ventaId,
                viewModel = ventaViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        //Ventas recibidas (encargado)
        composable(
            route = "revision/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L

            LaunchedEffect(usuarioId) { revisionViewModel.iniciarSesionDe(usuarioId) }

            RevisionListaScreen(
                viewModel = revisionViewModel,
                onVentaClick = { ventaId ->
                    navController.navigate("revision_detalle/$usuarioId/$ventaId")
                },
                onVolver = { navController.popBackStack() }
            )
        }

        //Detalle y revisión de una venta (encargado)
        composable(
            route = "revision_detalle/{usuarioId}/{ventaId}",
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.LongType },
                navArgument("ventaId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L
            val ventaId = backStackEntry.arguments?.getLong("ventaId") ?: 0L

            LaunchedEffect(usuarioId, ventaId) {
                revisionViewModel.iniciarSesionDe(usuarioId)
                revisionViewModel.cargarDetalle(ventaId)
            }

            RevisionDetalleScreen(
                viewModel = revisionViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}