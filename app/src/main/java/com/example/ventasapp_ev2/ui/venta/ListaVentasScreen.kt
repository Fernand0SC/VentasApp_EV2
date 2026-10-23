package com.example.ventasapp_ev2.ui.venta

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ventasapp_ev2.data.local.VentaEntity
import com.example.ventasapp_ev2.data.model.EstadoVenta
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVentasScreen(
    viewModel: VentaViewModel,
    onNuevaVenta: () -> Unit,
    onVentaClick: (Long) -> Unit,
    onVolver: () -> Unit
) {
    //La lista viene del ViewModel
    val ventas by viewModel.ventas.collectAsState()

    // Venta que el usuario quiere eliminar (null = ningún diálogo abierto)
    var ventaAEliminar by remember { mutableStateOf<VentaEntity?>(null) }

    ventaAEliminar?.let { venta ->
        AlertDialog(
            onDismissRequest = { ventaAEliminar = null },
            title = { Text("Eliminar venta") },
            text = { Text("¿Eliminar la venta ${venta.codigoAutorizacion}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarVenta(venta.id)
                    ventaAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { ventaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis ventas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaVenta,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva venta") }
            )
        }
    ) { padding ->
        if (ventas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Aún no has registrado ventas",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Pulsa \"Nueva venta\" para crear la primera",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ventas, key = { it.id }) { venta ->
                    VentaItem(
                        venta = venta,
                        onClick = { onVentaClick(venta.id) },
                        onEliminar = { ventaAEliminar = venta }
                    )
                }
            }
        }
    }
}

@Composable
private fun VentaItem(venta: VentaEntity, onClick: () -> Unit, onEliminar: () -> Unit) {
    val estado = EstadoVenta.desde(venta.estado)
    val formatoMonto = NumberFormat.getIntegerInstance(Locale("es", "CL"))

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = venta.codigoAutorizacion,
                    style = MaterialTheme.typography.titleMedium
                )
                EstadoChip(estado)
            }
            Text(
                text = venta.tipoServicio,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ${formatoMonto.format(venta.monto)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = venta.fechaOperacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Una venta validada no se puede eliminar
                    if (estado != EstadoVenta.VALIDADA) {
                        IconButton(onClick = onEliminar) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar venta",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            if (estado == EstadoVenta.VALIDADA) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "  Validada: solo lectura",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (estado == EstadoVenta.OBSERVADA || estado == EstadoVenta.RECHAZADA) {
                Text(
                    text = if (estado == EstadoVenta.OBSERVADA)
                        "Corrección solicitada: toca para ver el detalle"
                    else "Rechazada: toca para ver el motivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

        }
    }
}


@Composable
fun EstadoChip(estado: EstadoVenta) {
    val colorFondo by animateColorAsState(
        targetValue = when (estado) {
            EstadoVenta.BORRADOR -> MaterialTheme.colorScheme.surfaceVariant
            EstadoVenta.RECIBIDA -> MaterialTheme.colorScheme.primaryContainer
            EstadoVenta.OBSERVADA -> MaterialTheme.colorScheme.tertiaryContainer
            EstadoVenta.VALIDADA -> MaterialTheme.colorScheme.secondaryContainer
            EstadoVenta.RECHAZADA -> MaterialTheme.colorScheme.errorContainer
        },
        label = "colorEstado"
    )
    Surface(color = colorFondo, shape = RoundedCornerShape(50)) {
        Text(
            text = estado.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}