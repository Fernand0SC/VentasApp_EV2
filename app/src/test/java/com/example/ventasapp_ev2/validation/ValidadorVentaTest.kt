package com.example.ventasapp_ev2.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ValidadorVentaTest {

    // "Hoy" fijo para que los tests de fecha den siempre el mismo resultado
    private fun fecha(texto: String): Date =
        SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).parse(texto)!!

    private val hoy = fecha("15/06/2026")

    // ---------- Tipo de convenio o servicio ----------

    @Test
    fun tipoServicio_vacio_devuelveError() {
        assertEquals(
            "Seleccione el tipo de convenio o servicio",
            ValidadorVenta.validarTipoServicio("")
        )
    }

    @Test
    fun tipoServicio_soloEspacios_devuelveError() {
        assertEquals(
            "Seleccione el tipo de convenio o servicio",
            ValidadorVenta.validarTipoServicio("   ")
        )
    }

    @Test
    fun tipoServicio_seleccionado_esValido() {
        assertNull(ValidadorVenta.validarTipoServicio("Clínica dental"))
    }

    // ---------- Fecha de operación ----------

    @Test
    fun fecha_vacia_devuelveError() {
        assertEquals("Ingrese la fecha de la operación", ValidadorVenta.validarFecha("", hoy))
    }

    @Test
    fun fecha_conFormatoIncorrecto_devuelveError() {
        val formato = "Use el formato dd/MM/aaaa"
        assertEquals(formato, ValidadorVenta.validarFecha("2026-06-10", hoy))
        assertEquals(formato, ValidadorVenta.validarFecha("10/6/2026", hoy))
        assertEquals(formato, ValidadorVenta.validarFecha("10/06/26", hoy))
        assertEquals(formato, ValidadorVenta.validarFecha("ab/cd/efgh", hoy))
    }

    @Test
    fun fecha_inexistente_devuelveError() {
        val formato = "Use el formato dd/MM/aaaa"
        assertEquals(formato, ValidadorVenta.validarFecha("31/02/2026", hoy))
        assertEquals(formato, ValidadorVenta.validarFecha("32/01/2026", hoy))
        assertEquals(formato, ValidadorVenta.validarFecha("10/13/2026", hoy))
    }

    @Test
    fun fecha_futura_devuelveError() {
        assertEquals("La fecha no puede ser futura", ValidadorVenta.validarFecha("16/06/2026", hoy))
    }

    @Test
    fun fecha_deHoy_esValida() {
        assertNull(ValidadorVenta.validarFecha("15/06/2026", hoy))
    }

    @Test
    fun fecha_pasada_esValida() {
        assertNull(ValidadorVenta.validarFecha("01/01/2026", hoy))
    }

    @Test
    fun formatearFecha_generaFormatoDiaMesAnio() {
        assertEquals("05/03/2026", ValidadorVenta.formatearFecha(fecha("05/03/2026")))
    }

    // ---------- Cliente ficticio ----------

    @Test
    fun cliente_vacio_devuelveError() {
        assertEquals("Ingrese el nombre del cliente ficticio", ValidadorVenta.validarCliente(""))
    }

    @Test
    fun cliente_conMenosDeTresCaracteres_devuelveError() {
        assertEquals(
            "El nombre debe tener al menos 3 caracteres",
            ValidadorVenta.validarCliente("Al")
        )
    }

    @Test
    fun cliente_ignoraEspaciosAlrededorAlContarCaracteres() {
        assertEquals(
            "El nombre debe tener al menos 3 caracteres",
            ValidadorVenta.validarCliente("  Al  ")
        )
    }

    @Test
    fun cliente_conTresCaracteres_esValido() {
        assertNull(ValidadorVenta.validarCliente("Ana"))
    }

    // ---------- Monto ----------

    @Test
    fun monto_vacio_devuelveError() {
        assertEquals("Ingrese el monto", ValidadorVenta.validarMonto(""))
    }

    @Test
    fun monto_noNumerico_devuelveError() {
        assertEquals("El monto debe ser numérico", ValidadorVenta.validarMonto("abc"))
        assertEquals("El monto debe ser numérico", ValidadorVenta.validarMonto("12.5"))
    }

    @Test
    fun monto_cero_devuelveError() {
        assertEquals("El monto debe ser mayor que 0", ValidadorVenta.validarMonto("0"))
    }

    @Test
    fun monto_negativo_devuelveError() {
        assertEquals("El monto debe ser mayor que 0", ValidadorVenta.validarMonto("-5"))
    }

    @Test
    fun monto_unPeso_esValido() {
        assertNull(ValidadorVenta.validarMonto("1"))
    }

    @Test
    fun monto_enElLimiteMaximo_esValido() {
        assertNull(ValidadorVenta.validarMonto("10000000"))
    }

    @Test
    fun monto_sobreElMaximo_devuelveError() {
        assertEquals(
            "El monto no puede superar \$10.000.000",
            ValidadorVenta.validarMonto("10000001")
        )
    }

    @Test
    fun monto_enormeQueNoCabeEnInt_dicenQueSuperaElMaximo() {
        assertEquals(
            "El monto no puede superar \$10.000.000",
            ValidadorVenta.validarMonto("99999999999")
        )
    }

    // ---------- Código de autorización ----------

    @Test
    fun codigo_vacio_devuelveError() {
        assertEquals("Ingrese el código de autorización", ValidadorVenta.validarCodigo(""))
    }

    @Test
    fun codigo_conMenosDeCuatroCaracteres_devuelveError() {
        assertEquals(
            "El código debe tener al menos 4 caracteres",
            ValidadorVenta.validarCodigo("AB1")
        )
    }

    @Test
    fun codigo_ignoraEspaciosAlrededorAlContarCaracteres() {
        assertEquals(
            "El código debe tener al menos 4 caracteres",
            ValidadorVenta.validarCodigo("  AB1  ")
        )
    }

    @Test
    fun codigo_conCuatroCaracteres_esValido() {
        assertNull(ValidadorVenta.validarCodigo("AB12"))
    }

    // ---------- Validación completa del formulario ----------

    @Test
    fun validar_formularioVacio_marcaLosCincoCampos() {
        val errores = ValidadorVenta.validar("", "", "", "", "", hoy)
        assertFalse(errores.esValido)
        assertEquals("Seleccione el tipo de convenio o servicio", errores.tipoServicio)
        assertEquals("Ingrese la fecha de la operación", errores.fecha)
        assertEquals("Ingrese el nombre del cliente ficticio", errores.cliente)
        assertEquals("Ingrese el monto", errores.monto)
        assertEquals("Ingrese el código de autorización", errores.codigo)
    }

    @Test
    fun validar_formularioCorrecto_esValido() {
        val errores = ValidadorVenta.validar(
            tipoServicio = "Servicio veterinario",
            fecha = "10/06/2026",
            cliente = "Cliente Prueba",
            monto = "25000",
            codigo = "AUT-001",
            hoy = hoy
        )
        assertTrue(errores.esValido)
    }

    @Test
    fun validar_conUnSoloCampoMalo_soloMarcaEseCampo() {
        val errores = ValidadorVenta.validar(
            tipoServicio = "Clínica dental",
            fecha = "10/06/2026",
            cliente = "Cliente Prueba",
            monto = "0",
            codigo = "AUT-001",
            hoy = hoy
        )
        assertFalse(errores.esValido)
        assertEquals("El monto debe ser mayor que 0", errores.monto)
        assertNull(errores.tipoServicio)
        assertNull(errores.fecha)
        assertNull(errores.cliente)
        assertNull(errores.codigo)
    }
}
