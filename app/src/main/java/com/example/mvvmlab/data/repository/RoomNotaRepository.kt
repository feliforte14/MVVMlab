package com.example.mvvmlab.data.repository

import android.database.sqlite.SQLiteException
import com.example.mvvmlab.data.local.NotaDao
import com.example.mvvmlab.data.mapper.aDominio
import com.example.mvvmlab.data.mapper.aEntity
import com.example.mvvmlab.domain.model.Nota
import com.example.mvvmlab.domain.repository.NotaRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// --- ADAPTADOR: el contrato de siempre, implementado con el DAO ---------
class RoomNotaRepository(
    private val dao: NotaDao,
    private val reloj: () -> Long = System::currentTimeMillis
) : NotaRepository {

    // Room ya emite cuando la tabla cambia: no hace falta un StateFlow
    // propio ni "emitir a mano" despues de guardar (como en el Ej 12).
    override fun observarTodas(): Flow<List<Nota>> =
        dao.observarTodas()
            .map { filas -> filas.map { it.aDominio() } }
            .catch { e -> throw traducir(e) }

    override suspend fun obtener(id: String): Nota? = conErrores { dao.obtener(id)?.aDominio() }

    override suspend fun guardar(nota: Nota) = conErrores { dao.guardar(nota.aEntity(reloj())) }

    override suspend fun eliminar(id: String) = conErrores { dao.eliminar(id) }

    // Room informa fallas con SQLiteException. El contrato (y los ViewModels)
    // trabajan con IOException: el ADAPTADOR traduce, asi el ViewModel no
    // necesita conocer nada de SQL ni de Room.
    private inline fun <T> conErrores(bloque: () -> T): T =
        try { bloque() } catch (e: SQLiteException) { throw traducir(e) }

    private fun traducir(e: Throwable): Throwable =
        if (e is SQLiteException) IOException("Error de base de datos: ${e.message}", e) else e
}
