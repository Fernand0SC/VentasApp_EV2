package com.example.ventasapp_ev2.ui.revision

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventasapp_ev2.data.local.AppDatabase
import com.example.ventasapp_ev2.data.local.UsuarioEntity
import com.example.ventasapp_ev2.data.local.VentaEntity
import com.example.ventasapp_ev2.data.model.EstadoVenta
import com.example.ventasapp_ev2.data.repository.ReglaNegocioException
import com.example.ventasapp_ev2.data.repository.RevisionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.ventasapp_ev2.data.local.HistorialEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

class RevisionViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)
    private val repository = RevisionRepository(db.ventaDao(), db.usuarioDao(), db.historialDao())
    private val _encargadoId = MutableStateFlow(0L)

    fun iniciarSesionDe(usuarioId: Long) {
        _encargadoId.value = usuarioId
    }

    // Filtros de la lista (buscar por código; filtrar por estado, vendedor y fecha)
    private val _filtros = MutableStateFlow(RevisionFiltros())
    val filtros: StateFlow<RevisionFiltros> = _filtros.asStateFlow()

    // Lista de ventas enviadas, con los filtros aplicados
    val ventas: StateFlow<List<VentaEntity>> =
        combine(repository.ventasParaRevision(), _filtros) { lista, f ->
            lista.filter { venta ->
                (f.codigo.isBlank() ||
                        venta.codigoAutorizacion.contains(f.codigo.trim(), ignoreCase = true)) &&
                        (f.estado == null || venta.estado == f.estado.name) &&
                        (f.vendedorId == null || venta.vendedorId == f.vendedorId) &&
                        (f.fecha.isBlank() || venta.fechaOperacion.contains(f.fecha.trim()))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendedores: StateFlow<List<UsuarioEntity>> = repository.vendedores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onCodigoFiltroChange(valor: String) {
        _filtros.update { it.copy(codigo = valor) }
    }

    fun onEstadoFiltroChange(valor: EstadoVenta?) {
        _filtros.update { it.copy(estado = valor) }
    }

    fun onVendedorFiltroChange(valor: Long?) {
        _filtros.update { it.copy(vendedorId = valor) }
    }

    fun onFechaFiltroChange(valor: String) {
        _filtros.update { it.copy(fecha = valor) }
    }

    private val _detalle = MutableStateFlow(RevisionDetalleUiState())
    val detalle: StateFlow<RevisionDetalleUiState> = _detalle.asStateFlow()


    // Historial de la venta que se está viendo
    private val _ventaIdActual = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val historial: StateFlow<List<HistorialEntity>> = _ventaIdActual
        .flatMapLatest { repository.historialDeVenta(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var jobDetalle: Job? = null

    fun cargarDetalle(ventaId: Long) {
        _ventaIdActual.value = ventaId
        jobDetalle?.cancel()
        _detalle.value = RevisionDetalleUiState()
        jobDetalle = viewModelScope.launch {
            repository.observarVenta(ventaId).collect { venta ->
                if (venta == null) {
                    _detalle.update { it.copy(cargando = false, venta = null) }
                } else {
                    val nombre = repository.nombreUsuario(venta.vendedorId)
                    _detalle.update {
                        it.copy(
                            cargando = false,
                            venta = venta,
                            vendedorNombre = nombre,
                            esPropia = venta.vendedorId == _encargadoId.value
                        )
                    }
                }
            }
        }
    }

    fun onObservacionChange(valor: String) {
        _detalle.update { it.copy(observacion = valor, observacionError = null, errorGeneral = null) }
    }

    fun validar() = ejecutar(EstadoVenta.VALIDADA)

    fun rechazar() = ejecutar(EstadoVenta.RECHAZADA)

    fun solicitarCorreccion() = ejecutar(EstadoVenta.OBSERVADA)

    fun consumirMensaje() {
        _detalle.update { it.copy(mensajeExito = null) }
    }

    private fun ejecutar(nuevoEstado: EstadoVenta) {
        val actual = _detalle.value
        val venta = actual.venta ?: return
        if (actual.procesando) return

        if (actual.esPropia) {
            _detalle.update { it.copy(errorGeneral = "No puedes revisar tus propias ventas") }
            return
        }
        if (nuevoEstado != EstadoVenta.VALIDADA && actual.observacion.trim().length < 5) {
            _detalle.update {
                it.copy(observacionError = "Indica el motivo (mínimo 5 caracteres)")
            }
            return
        }

        viewModelScope.launch {
            _detalle.update { it.copy(procesando = true, errorGeneral = null) }
            try {
                repository.cambiarEstado(
                    ventaId = venta.id,
                    nuevoEstado = nuevoEstado,
                    responsableId = _encargadoId.value,
                    observacion = actual.observacion
                )
                _detalle.update {
                    it.copy(
                        procesando = false,
                        observacion = "",
                        mensajeExito = when (nuevoEstado) {
                            EstadoVenta.VALIDADA -> "Venta validada correctamente"
                            EstadoVenta.RECHAZADA -> "Venta rechazada"
                            else -> "Corrección solicitada al vendedor"
                        }
                    )
                }
            } catch (e: ReglaNegocioException) {
                _detalle.update { it.copy(procesando = false, errorGeneral = e.message) }
            }
        }
    }
}