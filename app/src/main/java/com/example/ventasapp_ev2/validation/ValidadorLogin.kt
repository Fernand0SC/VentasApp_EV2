package com.example.ventasapp_ev2.validation

data class ErroresLogin(
    val usuario: String? = null,
    val clave: String? = null
) {
    val esValido: Boolean
        get() = usuario == null && clave == null
}

/** Reglas de validación del inicio de sesión (Kotlin puro, sin Android). */
object ValidadorLogin {

    const val CLAVE_MIN_CARACTERES = 4

    fun validarUsuario(valor: String): String? =
        if (valor.isBlank()) "Ingrese su usuario" else null

    fun validarClave(valor: String): String? = when {
        valor.isBlank() -> "Ingrese su contraseña"
        valor.length < CLAVE_MIN_CARACTERES ->
            "La contraseña debe tener al menos $CLAVE_MIN_CARACTERES caracteres"
        else -> null
    }

    fun validar(usuario: String, clave: String): ErroresLogin = ErroresLogin(
        usuario = validarUsuario(usuario),
        clave = validarClave(clave)
    )
}
