package com.example.mvvmlab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// =====================================================================
// REGISTROS EN ROOM (fuente de verdad de los registros estructurados)
// =====================================================================

@Entity(tableName = "registros")
data class RegistroEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: Long,
    val favorito: Boolean,
    val actualizadoEn: Long                 // columna tecnica, no llega a la UI
)
