package com.example.ventasapp_ev2.validation


object ValidadorRevision {

    const val MOTIVO_MIN_CARACTERES = 5


    fun validarObservacion(observacion: String, obligatoria: Boolean): String? =
        if (obligatoria && observacion.trim().length < MOTIVO_MIN_CARACTERES)
            "Indica el motivo (mínimo $MOTIVO_MIN_CARACTERES caracteres)"
        else null
}
