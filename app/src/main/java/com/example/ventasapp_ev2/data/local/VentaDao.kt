package com.example.ventasapp_ev2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    @Insert
    suspend fun insertar(venta: VentaEntity): Long

    @Update
    suspend fun actualizar(venta: VentaEntity)

    @Delete
    suspend fun eliminar(venta: VentaEntity)

    @Query("SELECT * FROM ventas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): VentaEntity?

    @Query("SELECT * FROM ventas WHERE id = :id")
    fun observarPorId(id: Long): Flow<VentaEntity?>

    @Query("SELECT * FROM ventas WHERE vendedorId = :vendedorId ORDER BY fechaActualizacion DESC")
    fun ventasDeVendedor(vendedorId: Long): Flow<List<VentaEntity>>


    @Query("SELECT * FROM ventas WHERE estado != 'BORRADOR' ORDER BY fechaActualizacion DESC")
    fun ventasParaRevision(): Flow<List<VentaEntity>>

    @Query("SELECT COUNT(*) FROM ventas WHERE codigoAutorizacion = :codigo AND id != :excluirId")
    suspend fun contarCodigo(codigo: String, excluirId: Long): Int
}