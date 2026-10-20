package com.example.ventasapp_ev2.data.model

// Perfiles de usuario del caso
enum class Perfil(val etiqueta: String) {
    VENDEDOR("Vendedor"),
    ENCARGADO("Encargado de gestión");

    companion object {
        fun desde(valor: String): Perfil =
            entries.firstOrNull { it.name == valor } ?: VENDEDOR
    }
}