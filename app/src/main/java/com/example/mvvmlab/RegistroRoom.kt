package com.example.mvvmlab

import android.database.sqlite.SQLiteException
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

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

@Database(entities = [RegistroEntity::class], version = 1, exportSchema = false)
abstract class CuadernoDatabase : RoomDatabase() {
    abstract fun registroDao(): RegistroDao
}

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

fun RegistroEntity.aDominio() = Registro(id, titulo, descripcion, fecha, favorito)

fun Registro.aEntity(actualizadoEn: Long) =
    RegistroEntity(id, titulo, descripcion, fecha, favorito, actualizadoEn)
