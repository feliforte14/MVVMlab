package com.example.mvvmlab.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// --- DAO: las consultas ------------------------------------------------
@Dao
interface NotaDao {

    // Devuelve Flow: Room vuelve a emitir SOLO cada vez que la tabla cambia.
    // Es la "fuente que ya emite" que anticipaba el Ej 6.
    @Query("SELECT * FROM notas ORDER BY actualizadaEn DESC")
    fun observarTodas(): Flow<List<NotaEntity>>

    @Query("SELECT * FROM notas WHERE id = :id")
    suspend fun obtener(id: String): NotaEntity?

    // Upsert = INSERT si no existe, UPDATE si ya existe (por clave primaria).
    @Upsert
    suspend fun guardar(nota: NotaEntity)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun eliminar(id: String)

    // Consulta que con el archivo JSON habria que programar a mano
    // (leer todo y filtrar): con SQL es una linea.
    @Query("SELECT * FROM notas WHERE titulo LIKE '%' || :texto || '%' ORDER BY titulo")
    fun buscarPorTitulo(texto: String): Flow<List<NotaEntity>>
}
