package com.example.ventasapp_ev2.data.repository

import com.example.ventasapp_ev2.data.local.UsuarioDao
import com.example.ventasapp_ev2.data.local.VentaDao
import com.example.ventasapp_ev2.data.local.VentaEntity
import com.example.ventasapp_ev2.data.model.EstadoVenta
import com.example.ventasapp_ev2.data.model.Perfil
import kotlinx.coroutines.flow.Flow

class ReglaNegocioException(mensaje: String) : Exception(mensaje)

class RevisionRepository(
    private val ventaDao: VentaDao,
    private val usuarioDao: UsuarioDao
) {

    fun ventasParaRevision(): Flow<List<VentaEntity>> = ventaDao.ventasParaRevision()

    fun observarVenta(id: Long): Flow<VentaEntity?> = ventaDao.observarPorId(id)

    suspend fun nombreUsuario(id: Long): String =
        usuarioDao.obtenerPorId(id)?.nombre ?: "Desconocido"


    suspend fun cambiarEstado(
        ventaId: Long,
        nuevoEstado: EstadoVenta,
        responsableId: Long,
        observacion: String?
    ) {
        val responsable = usuarioDao.obtenerPorId(responsableId)
            ?: throw ReglaNegocioException("Usuario no válido")
        exigir(
            Perfil.desde(responsable.perfil) == Perfil.ENCARGADO,
            "Solo el encargado puede revisar ventas"
        )

        val venta = ventaDao.obtenerPorId(ventaId)
            ?: throw ReglaNegocioException("La venta no existe")
        exigir(venta.vendedorId != responsable.id, "No puedes revisar tus propias ventas")
        exigir(
            venta.estado == EstadoVenta.RECIBIDA.name,
            "Solo se pueden revisar ventas en estado Recibida"
        )

        val motivo = observacion?.trim().orEmpty()
        if (nuevoEstado != EstadoVenta.VALIDADA) {
            exigir(motivo.isNotEmpty(), "Debes indicar el motivo o la observación")
        }

        val ahora = System.currentTimeMillis()
        ventaDao.actualizar(
            venta.copy(
                estado = nuevoEstado.name,
                observacionRevision = motivo.ifEmpty { null },
                revisadoPorId = responsable.id,
                fechaRevision = ahora,
                fechaActualizacion = ahora
            )
        )
    }

    private fun exigir(condicion: Boolean, mensaje: String) {
        if (!condicion) throw ReglaNegocioException(mensaje)
    }
}