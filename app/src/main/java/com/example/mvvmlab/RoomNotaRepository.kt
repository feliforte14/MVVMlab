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

// --- BASE DE DATOS -----------------------------------------------------
@Database(entities = [NotaEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao
}

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

// --- TRANSFORMACIONES Entity <-> dominio ----------------------------------
// Las hace el adaptador (capa de datos). Ni el ViewModel ni la pantalla
// conocen NotaEntity.
fun NotaEntity.aDominio() = Nota(id = id, titulo = titulo, contenido = contenido)

fun Nota.aEntity(actualizadaEn: Long) =
    NotaEntity(id = id, titulo = titulo, contenido = contenido, actualizadaEn = actualizadaEn)
