package com.example.ventasapp_ev2.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorRevisionTest {

    @Test
    fun validarVenta_noExigeObservacion() {
        assertNull(ValidadorRevision.validarObservacion("", obligatoria = false))
    }

    @Test
    fun rechazarOCorregir_sinObservacion_devuelveError() {
        assertEquals(
            "Indica el motivo (mínimo 5 caracteres)",
            ValidadorRevision.validarObservacion("", obligatoria = true)
        )
    }

    @Test
    fun rechazarOCorregir_conObservacionCorta_devuelveError() {
        assertEquals(
            "Indica el motivo (mínimo 5 caracteres)",
            ValidadorRevision.validarObservacion("  abcd  ", obligatoria = true)
        )
    }

    @Test
    fun rechazarOCorregir_conCincoCaracteres_esValido() {
        assertNull(ValidadorRevision.validarObservacion("abcde", obligatoria = true))
    }
}
