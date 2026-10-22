package com.example.ventasapp_ev2.ui.venta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventasapp_ev2.data.local.AppDatabase
import com.example.ventasapp_ev2.data.local.VentaEntity
import com.example.ventasapp_ev2.data.model.EstadoVenta
import com.example.ventasapp_ev2.data.repository.VentaRepository
import com.example.ventasapp_ev2.validation.ValidadorVenta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VentaViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = AppDatabase.getDatabase(app).let { db ->
        VentaRepository(db.ventaDao(), db.historialDao(), db.usuarioDao())
    }

    // Opciones del campo tipo de convenio o servicio
    val tiposServicio = listOf(
        "Clínica dental",
        "Servicio automotriz",
        "Servicio veterinario",
        "Otros beneficios"
    )

    private val _usuarioId = MutableStateFlow(0L)

    fun iniciarSesionDe(usuarioId: Long) {
        _usuarioId.value = usuarioId
    }

    // Lista de ventas
    @OptIn(ExperimentalCoroutinesApi::class)
    val ventas: StateFlow<List<VentaEntity>> = _usuarioId
        .flatMapLatest { repository.ventasDeVendedor(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado del formulario
    private val _estado = MutableStateFlow(VentaUiState())
    val estado: StateFlow<VentaUiState> = _estado.asStateFlow()

    // Prepara el formulario para registrar una venta nueva
    fun nuevaVenta() {
        _estado.value = VentaUiState(fechaOperacion = ValidadorVenta.formatearFecha())
    }

    // Carga una venta existente para consultarla o modificarla
    fun cargarVenta(id: Long) {
        _estado.value = VentaUiState()
        viewModelScope.launch {
            val venta = repository.obtenerPorId(id) ?: return@launch
            // un vendedor solo puede abrir sus propias ventas
            if (venta.vendedorId != _usuarioId.value) return@launch
            _estado.value = VentaUiState(
                ventaId = venta.id,
                tipoServicio = venta.tipoServicio,
                fechaOperacion = venta.fechaOperacion,
                cliente = venta.cliente,
                monto = venta.monto.toString(),
                codigoAutorizacion = venta.codigoAutorizacion,
                respaldoUri = venta.respaldoUri,
                estado = venta.estado,
                observaciones = venta.observaciones,
                observacionRevision = venta.observacionRevision,
                // Una venta validada ya no se puede modificar
                editable = venta.estado != EstadoVenta.VALIDADA.name
            )
        }
    }

    // Eventos de los campos
    fun onTipoServicioChange(valor: String) {
        _estado.update { it.copy(tipoServicio = valor, tipoServicioError = null) }
    }

    fun onFechaChange(valor: String) {
        _estado.update { it.copy(fechaOperacion = valor, fechaError = null) }
    }

    fun onClienteChange(valor: String) {
        _estado.update { it.copy(cliente = valor, clienteError = null) }
    }

    fun onMontoChange(valor: String) {
        // Solo se aceptan dígitos
        if (valor.all { it.isDigit() }) {
            _estado.update { it.copy(monto = valor, montoError = null) }
        }
    }

    fun onCodigoChange(valor: String) {
        _estado.update { it.copy(codigoAutorizacion = valor, codigoError = null) }
    }

    fun onObservacionesChange(valor: String) {
        _estado.update { it.copy(observaciones = valor) }
    }

    fun onRespaldoChange(uri: String?) {
        _estado.update { it.copy(respaldoUri = uri, respaldoError = null) }
    }

    fun onRespaldoError(mensaje: String?) {
        _estado.update { it.copy(respaldoError = mensaje) }
    }

    // Validaciones: las reglas viven en ValidadorVenta, el ViewModel solo las aplica al estado
    private fun validarCampos(): Boolean {
        val e = _estado.value

        val errores = ValidadorVenta.validar(
            tipoServicio = e.tipoServicio,
            fecha = e.fechaOperacion,
            cliente = e.cliente,
            monto = e.monto,
            codigo = e.codigoAutorizacion
        )

        _estado.update {
            it.copy(
                tipoServicioError = errores.tipoServicio,
                fechaError = errores.fecha,
                clienteError = errores.cliente,
                montoError = errores.monto,
                codigoError = errores.codigo
            )
        }

        return errores.esValido
    }

    // Guardar
    fun guardar(enviar: Boolean) {
        if (!_estado.value.editable) return
        if (!validarCampos()) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }
            val e = _estado.value
            val codigo = e.codigoAutorizacion.trim().uppercase()

            // no se permiten códigos de autorización duplicados
            if (repository.codigoDuplicado(codigo, e.ventaId)) {
                _estado.update {
                    it.copy(guardando = false, codigoError = ValidadorVenta.ERROR_CODIGO_DUPLICADO)
                }
                return@launch
            }

            val existente = if (e.ventaId != 0L) repository.obtenerPorId(e.ventaId) else null

            // no modificar ventas ajenas ni ventas ya validadas
            if (existente != null &&
                (existente.vendedorId != _usuarioId.value ||
                        existente.estado == EstadoVenta.VALIDADA.name)
            ) {
                _estado.update {
                    it.copy(guardando = false, errorGeneral = "Esta venta no se puede modificar")
                }
                return@launch
            }

            val ahora = System.currentTimeMillis()
            val venta = VentaEntity(
                id = e.ventaId,
                vendedorId = _usuarioId.value,
                tipoServicio = e.tipoServicio,
                fechaOperacion = e.fechaOperacion,
                cliente = e.cliente.trim(),
                monto = e.monto.toInt(),
                codigoAutorizacion = codigo,
                respaldoUri = e.respaldoUri,
                estado = if (enviar) EstadoVenta.RECIBIDA.name else EstadoVenta.BORRADOR.name,
                observaciones = e.observaciones.trim(),
                fechaCreacion = existente?.fechaCreacion ?: ahora,
                fechaActualizacion = ahora
            )
            repository.guardar(venta)
            _estado.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }

    fun consumirGuardado() {
        _estado.update { it.copy(guardadoExitosamente = false) }
    }
}