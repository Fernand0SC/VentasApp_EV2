package com.example.ventasapp_ev2.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ventasapp_ev2.data.model.Perfil
import androidx.compose.material.icons.filled.CheckCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    nombre: String,
    perfil: String,
    onIrAVentas: () -> Unit,
    onIrARevision: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val perfilActual = Perfil.desde(perfil)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú principal") },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Hola, $nombre", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Perfil: ${perfilActual.etiqueta}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (perfilActual == Perfil.VENDEDOR) {
                Card(onClick = onIrAVentas, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Column {
                            Text("Registro de ventas", style = MaterialTheme.typography.titleMedium)
                            Text("Crear y consultar tus ventas", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                Card(onClick = onIrARevision, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Column {
                            Text("Ventas recibidas", style = MaterialTheme.typography.titleMedium)
                            Text("Revisar, validar o rechazar las ventas enviadas", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}