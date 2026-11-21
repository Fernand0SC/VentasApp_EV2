package com.example.ventasapp_ev2

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ventasapp_ev2.ui.menu.MenuScreen
import org.junit.Rule
import org.junit.Test

class MenuScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun el_encargado_ve_ventas_recibidas_y_no_el_registro_de_ventas() {

        composeRule.setContent {
            MenuScreen(
                nombre = "Encargado Demo",
                perfil = "ENCARGADO",
                onIrAVentas = {},
                onIrARevision = {},
                onCerrarSesion = {}
            )
        }

        composeRule.onNodeWithText("Ventas recibidas")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Registro de ventas")
            .assertDoesNotExist()
    }
}