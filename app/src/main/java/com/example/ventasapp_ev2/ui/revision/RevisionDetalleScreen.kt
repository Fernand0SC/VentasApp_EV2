package com.example.ventasapp_ev2.ui.revision

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ventasapp_ev2.data.model.EstadoVenta
import com.example.ventasapp_ev2.ui.venta.EstadoChip
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionDetalleScreen(
    viewModel: RevisionViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.detalle.collectAsState()
    val context = LocalContext.current

    // Confirmación visual y regreso a la lista
    LaunchedEffect(estado.mensajeExito) {
        estado.mensajeExito?.let { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            viewModel.consumirMensaje()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisar venta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val venta = estado.venta

        when {
            estado.cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            venta == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("La venta no existe") }

            else -> {
                val formatoMonto = NumberFormat.getIntegerInstance(Locale("es", "CL"))
                val estadoVenta = EstadoVenta.desde(venta.estado)
                val puedeRevisar = estadoVenta == EstadoVenta.RECIBIDA && !estado.esPropia

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Antecedentes de la venta
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(venta.codigoAutorizacion, style = MaterialTheme.typography.titleLarge)
                                EstadoChip(estadoVenta)
                            }
                            Spacer(Modifier.height(8.dp))
                            DetalleFila("Vendedor", estado.vendedorNombre)
                            DetalleFila("Tipo de convenio o servicio", venta.tipoServicio)
                            DetalleFila("Cliente (ficticio)", venta.cliente)
                            DetalleFila("Monto", "$ ${formatoMonto.format(venta.monto)}")
                            DetalleFila("Fecha de operación", venta.fechaOperacion)
                            DetalleFila(
                                "Observaciones del vendedor",
                                venta.observaciones.ifBlank { "Sin observaciones" }
                            )
                        }
                    }

                    // Respaldo fotográfico
                    Text("Respaldo fotográfico", style = MaterialTheme.typography.titleMedium)
                    if (venta.respaldoUri != null) {
                        AsyncImage(
                            model = venta.respaldoUri,
                            contentDescription = "Respaldo de la venta",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text(
                            "Esta venta no tiene respaldo adjunto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Resultado de una revisión anterior
                    AnimatedVisibility(visible = venta.observacionRevision != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Observación de la revisión", style = MaterialTheme.typography.labelLarge)
                                Text(venta.observacionRevision.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Avisos
                    AnimatedVisibility(visible = estado.esPropia) {
                        AvisoError("No puedes revisar tus propias ventas")
                    }
                    if (!estado.esPropia && estadoVenta != EstadoVenta.RECIBIDA) {
                        Text(
                            "Esta venta ya fue revisada. Estado actual: ${estadoVenta.etiqueta}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Acciones de revisión
                    AnimatedVisibility(visible = puedeRevisar) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = estado.observacion,
                                onValueChange = viewModel::onObservacionChange,
                                label = { Text("Observación o motivo") },
                                supportingText = {
                                    Text(
                                        estado.observacionError
                                            ?: "Obligatoria para rechazar o solicitar corrección"
                                    )
                                },
                                trailingIcon = {
                                    if (estado.observacionError != null) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                isError = estado.observacionError != null,
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            AnimatedVisibility(visible = estado.errorGeneral != null) {
                                AvisoError(estado.errorGeneral.orEmpty())
                            }

                            Button(
                                onClick = viewModel::validar,
                                enabled = !estado.procesando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Validar venta")
                            }
                            FilledTonalButton(
                                onClick = viewModel::solicitarCorreccion,
                                enabled = !estado.procesando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Solicitar corrección")
                            }
                            OutlinedButton(
                                onClick = viewModel::rechazar,
                                enabled = !estado.procesando,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Rechazar venta")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleFila(titulo: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AvisoError(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Error, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(mensaje, style = MaterialTheme.typography.bodyMedium)
        }
    }
}