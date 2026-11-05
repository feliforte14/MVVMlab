package com.example.mvvmlab.data.repository

import android.database.sqlite.SQLiteException
import androidx.room.Entity
import com.example.mvvmlab.data.local.RegistroDao
import com.example.mvvmlab.data.mapper.aDominio
import com.example.mvvmlab.data.mapper.aEntity
import com.example.mvvmlab.domain.model.Registro
import com.example.mvvmlab.domain.repository.RegistroRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Adaptador: implementa el contrato con el DAO, traduce Entity <-> dominio
// y SQLiteException -> IOException.
class RoomRegistroRepository(
    private val dao: RegistroDao,
    private val reloj: () -> Long = System::currentTimeMillis
) : RegistroRepository {

    override fun observar(busqueda: String): Flow<List<Registro>> =
        dao.observar(busqueda.trim())
            .map { filas -> filas.map { it.aDominio() } }
            .catch { e -> throw traducir(e) }

    override suspend fun obtener(id: String): Registro? = conErrores { dao.obtener(id)?.aDominio() }

    override suspend fun guardar(registro: Registro) = conErrores { dao.guardar(registro.aEntity(reloj())) }

    override suspend fun eliminar(id: String) = conErrores { dao.eliminar(id) }

    override suspend fun cambiarFavorito(id: String, favorito: Boolean) =
        conErrores { dao.cambiarFavorito(id, favorito, reloj()) }

    private inline fun <T> conErrores(bloque: () -> T): T =
        try { bloque() } catch (e: SQLiteException) { throw traducir(e) }

    private fun traducir(e: Throwable): Throwable =
        if (e is SQLiteException) IOException("Error de base de datos: ${e.message}", e) else e
}
