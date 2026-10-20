package com.example.ventasapp_ev2.data.model

enum class EstadoVenta(val etiqueta: String) {
    BORRADOR("Borrador"),
    RECIBIDA("Recibida"),
    OBSERVADA("Observada"),
    VALIDADA("Validada"),
    RECHAZADA("Rechazada");

    companion object {
        fun desde(valor: String): EstadoVenta =
            entries.firstOrNull { it.name == valor } ?: BORRADOR
    }
}