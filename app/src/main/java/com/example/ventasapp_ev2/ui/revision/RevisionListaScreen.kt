package com.example.ventasapp_ev2.ui.revision

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ventasapp_ev2.data.local.UsuarioEntity
import com.example.ventasapp_ev2.data.local.VentaEntity
import com.example.ventasapp_ev2.data.model.EstadoVenta
import com.example.ventasapp_ev2.ui.venta.EstadoChip
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionListaScreen(
    viewModel: RevisionViewModel,
    onVentaClick: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val ventas by viewModel.ventas.collectAsState()
    val filtros by viewModel.filtros.collectAsState()
    val vendedores by viewModel.vendedores.collectAsState()
    val hayFiltros = filtros != RevisionFiltros()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas recibidas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            FiltrosVentas(
                filtros = filtros,
                vendedores = vendedores,
                onCodigoChange = viewModel::onCodigoFiltroChange,
                onEstadoChange = viewModel::onEstadoFiltroChange,
                onVendedorChange = viewModel::onVendedorFiltroChange,
                onFechaChange = viewModel::onFechaFiltroChange
            )

            if (ventas.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (hayFiltros) "No hay ventas que coincidan" else "Aún no hay ventas recibidas",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ventas, key = { it.id }) { venta ->
                        VentaRevisionCard(venta = venta, onClick = { onVentaClick(venta.id) })
                    }
                }
            }
        }
    }
}

// Buscar por código y filtrar por estado, vendedor y fecha
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosVentas(
    filtros: RevisionFiltros,
    vendedores: List<UsuarioEntity>,
    onCodigoChange: (String) -> Unit,
    onEstadoChange: (EstadoVenta?) -> Unit,
    onVendedorChange: (Long?) -> Unit,
    onFechaChange: (String) -> Unit
) {
    var menuVendedor by remember { mutableStateOf(false) }
    val vendedorSeleccionado = vendedores.firstOrNull { it.id == filtros.vendedorId }?.nombre ?: "Todos"
    val estadosFiltro = listOf(
        EstadoVenta.RECIBIDA,
        EstadoVenta.OBSERVADA,
        EstadoVenta.VALIDADA,
        EstadoVenta.RECHAZADA
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = filtros.codigo,
            onValueChange = onCodigoChange,
            label = { Text("Buscar por código") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Estado
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filtros.estado == null,
                onClick = { onEstadoChange(null) },
                label = { Text("Todos") }
            )
            estadosFiltro.forEach { estado ->
                FilterChip(
                    selected = filtros.estado == estado,
                    onClick = { onEstadoChange(estado) },
                    label = { Text(estado.etiqueta) }
                )
            }
        }

        // Vendedor y fecha
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                FilterChip(
                    selected = filtros.vendedorId != null,
                    onClick = { menuVendedor = true },
                    label = { Text("Vendedor: $vendedorSeleccionado") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                )
                DropdownMenu(
                    expanded = menuVendedor,
                    onDismissRequest = { menuVendedor = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos") },
                        onClick = {
                            onVendedorChange(null)
                            menuVendedor = false
                        }
                    )
                    vendedores.forEach { vendedor ->
                        DropdownMenuItem(
                            text = { Text(vendedor.nombre) },
                            onClick = {
                                onVendedorChange(vendedor.id)
                                menuVendedor = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = filtros.fecha,
                onValueChange = onFechaChange,
                label = { Text("Fecha") },
                placeholder = { Text("dd/MM/aaaa") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VentaRevisionCard(venta: VentaEntity, onClick: () -> Unit) {
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
                Text(venta.codigoAutorizacion, style = MaterialTheme.typography.titleMedium)
                EstadoChip(EstadoVenta.desde(venta.estado))
            }
            Text(
                venta.tipoServicio,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$ ${formatoMonto.format(venta.monto)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    venta.fechaOperacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}