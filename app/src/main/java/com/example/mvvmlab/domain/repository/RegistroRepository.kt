package com.example.mvvmlab.domain.repository

import com.example.mvvmlab.domain.model.Registro
import kotlinx.coroutines.flow.Flow

// --- Contrato de los REGISTROS (implementado con Room) -----------------
interface RegistroRepository {
    // Emite los registros cuyo titulo contiene "busqueda" ("" = todos),
    // y vuelve a emitir cada vez que cambian.
    fun observar(busqueda: String): Flow<List<Registro>>
    suspend fun obtener(id: String): Registro?
    suspend fun guardar(registro: Registro)          // alta o edicion
    suspend fun eliminar(id: String)
    suspend fun cambiarFavorito(id: String, favorito: Boolean)
}
