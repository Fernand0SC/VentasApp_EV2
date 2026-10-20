package com.example.ventasapp_ev2.data.repository

import com.example.ventasapp_ev2.data.local.VentaDao
import com.example.ventasapp_ev2.data.local.VentaEntity
import kotlinx.coroutines.flow.Flow

class VentaRepository(private val ventaDao: VentaDao) {

    //la lista se actualiza sola cada vez que cambia la tabla
    fun ventasDeVendedor(vendedorId: Long): Flow<List<VentaEntity>> =
        ventaDao.ventasDeVendedor(vendedorId)

    suspend fun obtenerPorId(id: Long): VentaEntity? = ventaDao.obtenerPorId(id)


    suspend fun codigoDuplicado(codigo: String, excluirId: Long): Boolean =
        ventaDao.contarCodigo(codigo, excluirId) > 0

    suspend fun guardar(venta: VentaEntity) {
        if (venta.id == 0L) ventaDao.insertar(venta) else ventaDao.actualizar(venta)
    }
}