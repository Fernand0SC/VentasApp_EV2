package com.example.ventasapp_ev2.ui.venta

import com.example.ventasapp_ev2.data.model.EstadoVenta

data class VentaUiState(

    val ventaId: Long = 0,

    // Campos del formulario
    val tipoServicio: String = "",
    val fechaOperacion: String = "",
    val cliente: String = "",
    val monto: String = "",
    val codigoAutorizacion: String = "",
    val respaldoUri: String? = null,
    val estado: String = EstadoVenta.BORRADOR.name,
    val observaciones: String = "",

    // Errores por campo (null = campo válido)
    val tipoServicioError: String? = null,
    val fechaError: String? = null,
    val clienteError: String? = null,
    val montoError: String? = null,
    val codigoError: String? = null,

    // Estado de la pantalla
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val editable: Boolean = true,
    val errorGeneral: String? = null
)