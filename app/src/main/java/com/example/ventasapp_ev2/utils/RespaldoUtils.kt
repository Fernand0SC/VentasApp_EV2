package com.example.ventasapp_ev2.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ResultadoRespaldo(val uri: String? = null, val error: String? = null)

object RespaldoUtils {

    // Restricción del caso: solo imágenes y con tamaño máximo
    const val MAX_BYTES = 8L * 1024 * 1024
    private val TIPOS_PERMITIDOS = listOf("image/jpeg", "image/png", "image/webp")

    // Carpeta privada de la app donde se guardan los respaldos
    private fun carpeta(context: Context): File =
        File(context.filesDir, "respaldos").apply { mkdirs() }

    fun crearArchivo(context: Context): File =
        File(carpeta(context), "respaldo_${System.currentTimeMillis()}.jpg")

    // Uri segura (FileProvider) para que la cámara pueda escribir en nuestro archivo
    fun uriParaCamara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    // Valida una foto recién tomada y devuelve un mensaje de error o null si está bien
    fun validarArchivo(archivo: File): String? = when {
        !archivo.exists() || archivo.length() == 0L -> "No se pudo guardar la imagen"
        archivo.length() > MAX_BYTES -> "La imagen supera el máximo de 8 MB"
        else -> null
    }

    // Copia la imagen elegida en la galería a la carpeta privada de la app
    suspend fun copiarDesdeGaleria(context: Context, origen: Uri): ResultadoRespaldo =
        withContext(Dispatchers.IO) {
            val tipo = context.contentResolver.getType(origen)
            if (tipo == null || tipo !in TIPOS_PERMITIDOS) {
                return@withContext ResultadoRespaldo(error = "Solo se permiten imágenes JPG, PNG o WebP")
            }
            val destino = crearArchivo(context)
            try {
                context.contentResolver.openInputStream(origen)?.use { entrada ->
                    destino.outputStream().use { salida -> entrada.copyTo(salida) }
                }
                val error = validarArchivo(destino)
                if (error != null) {
                    destino.delete()
                    ResultadoRespaldo(error = error)
                } else {
                    ResultadoRespaldo(uri = Uri.fromFile(destino).toString())
                }
            } catch (e: Exception) {
                destino.delete()
                ResultadoRespaldo(error = "No se pudo leer la imagen seleccionada")
            }
        }
}