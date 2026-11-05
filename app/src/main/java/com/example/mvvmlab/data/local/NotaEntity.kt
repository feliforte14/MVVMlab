package com.example.mvvmlab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// =====================================================================
// EJERCICIO 14 — Room y archivo no son lo mismo
// =====================================================================
//
// Capa de datos con Room:
//   NotaEntity  -> como se guarda una fila en la tabla "notas" (SQLite)
//   NotaDao     -> consultas (SQL) sobre esa tabla
//   AppDatabase -> la base de datos (archivo notas.db)
//   RoomNotaRepository -> ADAPTADOR: implementa el MISMO contrato
//                         NotaRepository usando el DAO, y transforma
//                         Entity <-> Nota (modelo de dominio).
//
// El ViewModel no cambia: sigue hablando con NotaRepository y no sabe
// que existe SQL.
// =====================================================================

// --- ENTITY: forma de la fila en la tabla ------------------------------
// Es distinta del modelo de dominio Nota a proposito: tiene una columna
// tecnica (actualizadaEn) que la pantalla no necesita. Si mañana cambia
// la tabla, Nota y las pantallas no se enteran.
@Entity(tableName = "notas")
data class NotaEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val contenido: String,
    val actualizadaEn: Long          // milisegundos; para ordenar por ultima edicion
)
