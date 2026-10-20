package com.example.ventasapp_ev2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ventaId: Long,
    val estadoAnterior: String,
    val estadoNuevo: String,
    val accion: String,
    val usuarioId: Long,
    val usuarioNombre: String,
    val fecha: Long,
    val observacion: String? = null
)