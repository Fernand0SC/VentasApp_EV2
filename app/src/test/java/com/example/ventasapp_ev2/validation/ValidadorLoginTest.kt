package com.example.ventasapp_ev2.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorLoginTest {

    @Test
    fun usuario_vacio_devuelveError() {
        assertEquals("Ingrese su usuario", ValidadorLogin.validarUsuario(""))
    }

    @Test
    fun usuario_soloEspacios_devuelveError() {
        assertEquals("Ingrese su usuario", ValidadorLogin.validarUsuario("   "))
    }

    @Test
    fun usuario_conTexto_esValido() {
        assertNull(ValidadorLogin.validarUsuario("vendedor"))
    }

    @Test
    fun clave_vacia_devuelveError() {
        assertEquals("Ingrese su contraseña", ValidadorLogin.validarClave(""))
    }

    @Test
    fun clave_conMenosDeCuatroCaracteres_devuelveError() {
        assertEquals(
            "La contraseña debe tener al menos 4 caracteres",
            ValidadorLogin.validarClave("123")
        )
    }

    @Test
    fun clave_conCuatroCaracteres_esValida() {
        assertNull(ValidadorLogin.validarClave("1234"))
    }

    @Test
    fun validar_camposVacios_marcaAmbosCampos() {
        val errores = ValidadorLogin.validar("", "")
        assertFalse(errores.esValido)
        assertEquals("Ingrese su usuario", errores.usuario)
        assertEquals("Ingrese su contraseña", errores.clave)
    }

    @Test
    fun validar_datosCorrectos_esValido() {
        assertTrue(ValidadorLogin.validar("vendedor", "1234").esValido)
    }

    @Test
    fun validar_soloClaveCorta_marcaSoloLaClave() {
        val errores = ValidadorLogin.validar("vendedor", "12")
        assertFalse(errores.esValido)
        assertNull(errores.usuario)
        assertEquals("La contraseña debe tener al menos 4 caracteres", errores.clave)
    }
}
