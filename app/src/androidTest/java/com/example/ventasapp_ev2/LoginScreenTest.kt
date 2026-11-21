package com.example.ventasapp_ev2

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ventasapp_ev2.ui.login.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun al_ingresar_con_campos_vacios_se_muestran_los_errores() {

        composeRule.setContent {
            LoginScreen(onLoginExitoso = {})
        }

        composeRule.onNodeWithText("Registro de Ventas")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Ingresar")
            .performClick()

        composeRule.onNodeWithText("Ingrese su usuario")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Ingrese su contraseña")
            .assertIsDisplayed()
    }
}