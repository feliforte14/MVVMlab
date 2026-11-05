package com.example.mvvmlab

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

data class Nota(
    val id: String,
    val titulo: String,
    val contenido: String
)

// =====================================================================
// EJERCICIO 12 — Contrato con Flow: UNA fuente de verdad para 2 pantallas
// =====================================================================
// En el Ej 6 elegimos "valor puntual" (obtenerTodas) porque habia UNA
// pantalla. Ahora hay dos (lista y detalle): si cada una pidiera su
// "foto" por separado, la lista no se enteraria de lo que guardo el
// detalle. Con observarTodas(): Flow, el Repository EMITE la lista cada
// vez que cambia, y la lista se actualiza sola al volver.
// =====================================================================
interface NotaRepository {
    fun observarTodas(): Flow<List<Nota>>    // emite la lista actual y cada cambio
    suspend fun obtener(id: String): Nota?   // una nota puntual (para el detalle)
    suspend fun guardar(nota: Nota)          // alta o edicion
    suspend fun eliminar(id: String)
}

class FormatoInvalidoException(mensaje: String, causa: Throwable? = null) : IOException(mensaje, causa)

// Formato de notas.json: el mismo del Ej 10 (JSON con version, escape de
// caracteres, escritura en temporal + renombrado atomico).
class ArchivoNotasRepository(carpeta: File) : NotaRepository {

    private val archivo = File(carpeta, NOMBRE_ARCHIVO)
    private val temporal = File(carpeta, "$NOMBRE_ARCHIVO.tmp")
    private val mutex = Mutex()

    // Copia en memoria de lo que hay en disco. null = todavia no se leyo.
    // Es lo que se emite a quienes observan.
    private val notas = MutableStateFlow<List<Nota>?>(null)

    // onStart: antes de empezar a emitir, se asegura de haber leido el
    // archivo. Si la lectura falla, el Flow termina con la excepcion y
    // quien observa (el ViewModel) la maneja.
    override fun observarTodas(): Flow<List<Nota>> =
        notas.onStart { cargarSiHaceFalta() }.filterNotNull()

    override suspend fun obtener(id: String): Nota? = mutex.withLock {
        actualesLocked().find { it.id == id }
    }

    override suspend fun guardar(nota: Nota) = mutex.withLock {
        val lista = actualesLocked().toMutableList()
        val i = lista.indexOfFirst { it.id == nota.id }
        if (i >= 0) lista[i] = nota else lista.add(nota)
        escribirArchivo(lista)
        notas.value = lista            // primero disco, despues se emite
    }

    override suspend fun eliminar(id: String) = mutex.withLock {
        val lista = actualesLocked().filterNot { it.id == id }
        escribirArchivo(lista)
        notas.value = lista
    }

    private suspend fun cargarSiHaceFalta() = mutex.withLock { actualesLocked(); Unit }

    // Debe llamarse con el mutex tomado.
    private suspend fun actualesLocked(): List<Nota> =
        notas.value ?: leerArchivo().also { notas.value = it }

    private suspend fun leerArchivo(): List<Nota> = withContext(Dispatchers.IO) {
        if (!archivo.exists()) return@withContext emptyList()
        val texto = archivo.readText(Charsets.UTF_8)
        try {
            val array = JSONObject(texto).getJSONArray("notas")
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                Nota(o.getString("id"), o.getString("titulo"), o.getString("contenido"))
            }
        } catch (e: JSONException) {
            throw FormatoInvalidoException("El archivo $NOMBRE_ARCHIVO tiene un formato inválido", e)
        }
    }

    private suspend fun escribirArchivo(lista: List<Nota>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        lista.forEach { n ->
            array.put(JSONObject().put("id", n.id).put("titulo", n.titulo).put("contenido", n.contenido))
        }
        temporal.writeText(JSONObject().put("version", 1).put("notas", array).toString(2), Charsets.UTF_8)
        if (!temporal.renameTo(archivo)) throw IOException("No se pudo reemplazar $NOMBRE_ARCHIVO")
    }

    companion object {
        const val NOMBRE_ARCHIVO = "notas.json"
    }
}
