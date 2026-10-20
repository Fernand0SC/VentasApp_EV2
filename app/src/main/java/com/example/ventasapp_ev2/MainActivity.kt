package com.example.ventasapp_ev2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ventasapp_ev2.ui.navigation.AppNavigation
import com.example.ventasapp_ev2.ui.theme.VentasApp_EV2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VentasApp_EV2Theme {
                AppNavigation()
            }
        }
    }
}