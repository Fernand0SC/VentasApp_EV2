package com.example.ventasapp_ev2.validation

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class ErroresVenta(
    val tipoServicio: String? = null,
    val fecha: String? = null,
    val cliente: String? = null,
    val monto: String? = null,
    val codigo: String? = null
) {
    val esValido: Boolean
        get() = tipoServicio == null && fecha == null && cliente == null &&
                monto == null && codigo == null
}


object ValidadorVenta {

    const val FORMATO_FECHA = "dd/MM/yyyy"
    const val MONTO_MAXIMO = 10_000_000
    const val CLIENTE_MIN_CARACTERES = 3
    const val CODIGO_MIN_CARACTERES = 4

    const val ERROR_CODIGO_DUPLICADO = "Este código de autorización ya está registrado"

    private val patronFecha = Regex("""\d{2}/\d{2}/\d{4}""")

    // SimpleDateFormat no es seguro entre hilos, por eso se crea uno nuevo cada vez
    private fun nuevoFormato() =
        SimpleDateFormat(FORMATO_FECHA, Locale.ROOT).apply { isLenient = false }

    fun formatearFecha(fecha: Date = Date()): String = nuevoFormato().format(fecha)

    fun validarTipoServicio(valor: String): String? =
        if (valor.isBlank()) "Seleccione el tipo de convenio o servicio" else null

    fun validarFecha(valor: String, hoy: Date = Date()): String? = when {
        valor.isBlank() -> "Ingrese la fecha de la operación"
        !esFechaValida(valor) -> "Use el formato dd/MM/aaaa"
        esFechaFutura(valor, hoy) -> "La fecha no puede ser futura"
        else -> null
    }

    fun validarCliente(valor: String): String? = when {
        valor.isBlank() -> "Ingrese el nombre del cliente ficticio"
        valor.trim().length < CLIENTE_MIN_CARACTERES ->
            "El nombre debe tener al menos $CLIENTE_MIN_CARACTERES caracteres"
        else -> null
    }

    fun validarMonto(valor: String): String? {
        if (valor.isBlank()) return "Ingrese el monto"
        val monto = valor.toIntOrNull()
        return when {
            // solo dígitos pero demasiado grande para un Int: supera el máximo
            monto == null && valor.all { it.isDigit() } -> mensajeMontoMaximo()
            monto == null -> "El monto debe ser numérico"
            monto <= 0 -> "El monto debe ser mayor que 0"
            monto > MONTO_MAXIMO -> mensajeMontoMaximo()
            else -> null
        }
    }

    fun validarCodigo(valor: String): String? = when {
        valor.isBlank() -> "Ingrese el código de autorización"
        valor.trim().length < CODIGO_MIN_CARACTERES ->
            "El código debe tener al menos $CODIGO_MIN_CARACTERES caracteres"
        else -> null
    }


    fun validar(
        tipoServicio: String,
        fecha: String,
        cliente: String,
        monto: String,
        codigo: String,
        hoy: Date = Date()
    ): ErroresVenta = ErroresVenta(
        tipoServicio = validarTipoServicio(tipoServicio),
        fecha = validarFecha(fecha, hoy),
        cliente = validarCliente(cliente),
        monto = validarMonto(monto),
        codigo = validarCodigo(codigo)
    )

    private fun mensajeMontoMaximo() = "El monto no puede superar \$10.000.000"

    private fun esFechaValida(texto: String): Boolean {
        if (!patronFecha.matches(texto)) return false
        return try {
            nuevoFormato().parse(texto) != null
        } catch (e: ParseException) {
            false
        }
    }

    private fun esFechaFutura(texto: String, hoy: Date): Boolean {
        val fecha = nuevoFormato().parse(texto) ?: return false
        return fecha.after(hoy)
    }
}
