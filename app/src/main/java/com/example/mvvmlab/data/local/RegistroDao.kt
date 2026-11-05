package com.example.mvvmlab.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    // Busqueda por titulo (LIKE) + orden: favoritos primero, despues los
    // mas recientes. Con un archivo de texto esto seria leer todo y filtrar
    // a mano; con SQL es una consulta.
    @Query(
        """SELECT * FROM registros
           WHERE titulo LIKE '%' || :busqueda || '%'
           ORDER BY favorito DESC, fecha DESC, actualizadoEn DESC"""
    )
    fun observar(busqueda: String): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros WHERE id = :id")
    suspend fun obtener(id: String): RegistroEntity?

    @Upsert
    suspend fun guardar(registro: RegistroEntity)

    @Query("DELETE FROM registros WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("UPDATE registros SET favorito = :favorito, actualizadoEn = :ahora WHERE id = :id")
    suspend fun cambiarFavorito(id: String, favorito: Boolean, ahora: Long)
}
