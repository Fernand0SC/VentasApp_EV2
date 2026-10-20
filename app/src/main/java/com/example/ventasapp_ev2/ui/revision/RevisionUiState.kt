package com.example.ventasapp_ev2.ui.revision

import com.example.ventasapp_ev2.data.local.VentaEntity

data class RevisionDetalleUiState(
    val cargando: Boolean = true,
    val venta: VentaEntity? = null,
    val vendedorNombre: String = "",
    val esPropia: Boolean = false,
    val observacion: String = "",
    val observacionError: String? = null,
    val procesando: Boolean = false,
    val errorGeneral: String? = null,
    val mensajeExito: String? = null
)