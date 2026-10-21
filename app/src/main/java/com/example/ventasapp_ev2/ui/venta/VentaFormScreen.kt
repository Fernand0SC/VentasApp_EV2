package com.example.ventasapp_ev2.ui.venta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ventasapp_ev2.data.model.EstadoVenta
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ventasapp_ev2.utils.RespaldoUtils
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material.icons.filled.Info
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaFormScreen(
    ventaId: Long,
    viewModel: VentaViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ruta del archivo donde la cámara guardará la foto
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    //cámara
    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val archivo = rutaFotoPendiente?.let { File(it) }
        rutaFotoPendiente = null
        if (archivo != null) {
            if (!exito) {
                archivo.delete()
            } else {
                val error = RespaldoUtils.validarArchivo(archivo)
                if (error != null) {
                    archivo.delete()
                    viewModel.onRespaldoError(error)
                } else {
                    viewModel.onRespaldoChange(Uri.fromFile(archivo).toString())
                }
            }
        }
    }

    fun abrirCamara() {
        val archivo = RespaldoUtils.crearArchivo(context)
        rutaFotoPendiente = archivo.absolutePath
        camaraLauncher.launch(RespaldoUtils.uriParaCamara(context, archivo))
    }

    // Permiso de cámara en tiempo de ejecución
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else viewModel.onRespaldoError("Permiso de cámara denegado. Puedes usar la galería.")
    }

    fun onCamaraClick() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (tienePermiso) abrirCamara() else permisoLauncher.launch(Manifest.permission.CAMERA)
    }

    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val resultado = RespaldoUtils.copiarDesdeGaleria(context, uri)
                if (resultado.error != null) viewModel.onRespaldoError(resultado.error)
                else viewModel.onRespaldoChange(resultado.uri)
            }
        }
    }

    LaunchedEffect(ventaId) {
        if (ventaId == 0L) viewModel.nuevaVenta() else viewModel.cargarVenta(ventaId)
    }

    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) {
            viewModel.consumirGuardado()
            onVolver()
        }
    }

    val titulo = when {
        ventaId == 0L -> "Nueva venta"
        !estado.editable -> "Detalle de venta"
        else -> "Editar venta"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {


            // Observación del encargado cuando la venta fue observada o rechazada
            val hayObservacion = estado.observacionRevision != null &&
                    (estado.estado == EstadoVenta.OBSERVADA.name ||
                            estado.estado == EstadoVenta.RECHAZADA.name)

            AnimatedVisibility(visible = hayObservacion) {
                val rechazada = estado.estado == EstadoVenta.RECHAZADA.name
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (rechazada) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                if (rechazada) "El encargado rechazó esta venta"
                                else "El encargado solicitó una corrección",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                estado.observacionRevision.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!rechazada) {
                                Text(
                                    "Corrige los datos y vuelve a enviarla.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }


            // Aviso animado cuando la venta ya está validada
            AnimatedVisibility(visible = !estado.editable) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Text(
                            "  Esta venta está validada y no se puede modificar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            //Tipo de convenio o servicio (lista desplegable)
            var menuAbierto by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = estado.tipoServicio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de convenio o servicio") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    isError = estado.tipoServicioError != null,
                    supportingText = { Text(estado.tipoServicioError ?: "") },
                    enabled = estado.editable,
                    modifier = Modifier.fillMaxWidth()
                )
                // Capa transparente que abre el menú al tocar el campo
                if (estado.editable) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(bottom = 20.dp)
                            .clickable { menuAbierto = true }
                    )
                }
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    viewModel.tiposServicio.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                viewModel.onTipoServicioChange(tipo)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            //Fecha de operacion
            OutlinedTextField(
                value = estado.fechaOperacion,
                onValueChange = viewModel::onFechaChange,
                label = { Text("Fecha de operación (dd/MM/aaaa)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                singleLine = true,
                isError = estado.fechaError != null,
                supportingText = { Text(estado.fechaError ?: "") },
                enabled = estado.editable,
                modifier = Modifier.fillMaxWidth()
            )

            //Cliente ficticio
            OutlinedTextField(
                value = estado.cliente,
                onValueChange = viewModel::onClienteChange,
                label = { Text("Cliente (ficticio)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                isError = estado.clienteError != null,
                supportingText = { Text(estado.clienteError ?: "") },
                enabled = estado.editable,
                modifier = Modifier.fillMaxWidth()
            )

            //Monto
            OutlinedTextField(
                value = estado.monto,
                onValueChange = viewModel::onMontoChange,
                label = { Text("Monto (ficticio)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = estado.montoError != null,
                supportingText = { Text(estado.montoError ?: "") },
                enabled = estado.editable,
                modifier = Modifier.fillMaxWidth()
            )

            //Código de autorización
            OutlinedTextField(
                value = estado.codigoAutorizacion,
                onValueChange = viewModel::onCodigoChange,
                label = { Text("Código de autorización (ficticio)") },
                leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                singleLine = true,
                isError = estado.codigoError != null,
                supportingText = { Text(estado.codigoError ?: "") },
                enabled = estado.editable,
                modifier = Modifier.fillMaxWidth()
            )


            OutlinedTextField(
                value = estado.observaciones,
                onValueChange = viewModel::onObservacionesChange,
                label = { Text("Observaciones (opcional)") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                minLines = 2,
                maxLines = 4,
                enabled = estado.editable,
                modifier = Modifier.fillMaxWidth()
            )


            // Respaldo fotográfico (cámara o galería)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Respaldo fotográfico", style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (estado.respaldoUri == null) "Sin respaldo adjunto" else "Respaldo adjunto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Vista previa animada de la imagen adjunta
                    AnimatedVisibility(visible = estado.respaldoUri != null) {
                        AsyncImage(
                            model = estado.respaldoUri,
                            contentDescription = "Respaldo de la venta",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    if (estado.editable) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onCamaraClick() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("  Cámara")
                            }
                            OutlinedButton(
                                onClick = {
                                    galeriaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("  Galería")
                            }
                        }
                        if (estado.respaldoUri != null) {
                            TextButton(onClick = { viewModel.onRespaldoChange(null) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("  Quitar respaldo")
                            }
                        }
                    }

                    AnimatedVisibility(visible = estado.respaldoError != null) {
                        Text(
                            text = estado.respaldoError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            //Mensaje de error general
            AnimatedVisibility(visible = estado.errorGeneral != null) {
                Text(
                    text = estado.errorGeneral ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //Botones de accion
            if (estado.editable) {
                // El borrador solo se ofrece mientras la venta aún es un borrador
                if (estado.estado == EstadoVenta.BORRADOR.name) {
                    OutlinedButton(
                        onClick = { viewModel.guardar(enviar = false) },
                        enabled = !estado.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Guardar borrador")
                    }
                }
                Button(
                    onClick = { viewModel.guardar(enviar = true) },
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Enviar a revisión")
                    }
                }
            }
        }
    }
}