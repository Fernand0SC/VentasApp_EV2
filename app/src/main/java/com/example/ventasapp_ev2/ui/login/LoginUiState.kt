package com.example.ventasapp_ev2.ui.login

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val usuarioError: String? = null,
    val claveError: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false
)