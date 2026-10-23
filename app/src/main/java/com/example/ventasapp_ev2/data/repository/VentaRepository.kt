package com.example.ventasapp_ev2.data.repository

import com.example.ventasapp_ev2.data.local.HistorialDao
import com.example.ventasapp_ev2.data.local.HistorialEntity
import com.example.ventasapp_ev2.data.local.UsuarioDao
import com.example.ventasapp_ev2.data.local.VentaDao
import com.example.ventasapp_ev2.data.local.VentaEntity
import com.example.ventasapp_ev2.data.model.EstadoVenta
import kotlinx.coroutines.flow.Flow

class VentaRepository(
    private val ventaDao: VentaDao,
    private val historialDao: HistorialDao,
    private val usuarioDao: UsuarioDao
) {

    //la lista se actualiza sola cada vez que cambia la tabla
    fun ventasDeVendedor(vendedorId: Long): Flow<List<VentaEntity>> =
        ventaDao.ventasDeVendedor(vendedorId)

    suspend fun obtenerPorId(id: Long): VentaEntity? = ventaDao.obtenerPorId(id)

    // Elimina la venta junto con su historial
    suspend fun eliminar(venta: VentaEntity) {
        historialDao.eliminarDeVenta(venta.id)
        ventaDao.eliminar(venta)
    }

    suspend fun codigoDuplicado(codigo: String, excluirId: Long): Boolean =
        ventaDao.contarCodigo(codigo, excluirId) > 0

    suspend fun guardar(venta: VentaEntity) {
        val anterior = if (venta.id == 0L) null else ventaDao.obtenerPorId(venta.id)

        val ventaId = if (venta.id == 0L) {
            ventaDao.insertar(venta)
        } else {
            ventaDao.actualizar(venta)
            venta.id
        }

        //cada guardado deja un registro en el historial
        val enviada = venta.estado == EstadoVenta.RECIBIDA.name
        val accion = when {
            anterior == null && enviada -> "Venta creada y enviada a revisión"
            anterior == null -> "Venta creada como borrador"
            anterior.estado == EstadoVenta.OBSERVADA.name && enviada -> "Corrección enviada"
            anterior.estado != EstadoVenta.RECIBIDA.name && enviada -> "Venta enviada a revisión"
            else -> "Venta modificada"
        }
        val nombre = usuarioDao.obtenerPorId(venta.vendedorId)?.nombre ?: "Desconocido"

        historialDao.insertar(
            HistorialEntity(
                ventaId = ventaId,
                estadoAnterior = anterior?.estado ?: "-",
                estadoNuevo = venta.estado,
                accion = accion,
                usuarioId = venta.vendedorId,
                usuarioNombre = nombre,
                fecha = venta.fechaActualizacion
            )
        )
    }
}