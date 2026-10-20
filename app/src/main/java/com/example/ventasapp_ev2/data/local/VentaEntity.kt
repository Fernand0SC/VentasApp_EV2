package com.example.ventasapp_ev2.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ventas",
    indices = [Index(value = ["codigoAutorizacion"], unique = true)]
)
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendedorId: Long,
    val tipoServicio: String,
    val fechaOperacion: String,
    val cliente: String,
    val monto: Int,
    val codigoAutorizacion: String,
    val respaldoUri: String? = null,
    val estado: String,
    val observaciones: String = "",
    val fechaCreacion: Long,
    val fechaActualizacion: Long,
    val observacionRevision: String? = null,
    val revisadoPorId: Long? = null,
    val fechaRevision: Long? = null
)