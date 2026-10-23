package com.example.ventasapp_ev2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {

    @Insert
    suspend fun insertar(registro: HistorialEntity)

    @Query("DELETE FROM historial WHERE ventaId = :ventaId")
    suspend fun eliminarDeVenta(ventaId: Long)

    @Query("SELECT * FROM historial WHERE ventaId = :ventaId ORDER BY fecha DESC")
    fun historialDeVenta(ventaId: Long): Flow<List<HistorialEntity>>
}