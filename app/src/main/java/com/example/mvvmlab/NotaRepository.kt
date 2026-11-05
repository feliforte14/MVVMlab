package com.example.mvvmlab

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

// =====================================================================
// EJERCICIO 10 — De un texto a una lista persistente
// =====================================================================

// Modelo: una nota guardada. id identifica la nota para editarla o borrarla.
data class Nota(
    val id: String,
    val titulo: String,
    val contenido: String
)

// Contrato (el mismo del Ej 6): el ViewModel solo conoce esto.
interface NotaRepository {
    suspend fun obtenerTodas(): List<Nota>
    suspend fun guardar(nota: Nota)          // alta si el id es nuevo, edicion si ya existe
    suspend fun eliminar(id: String)
}

// El contenido del archivo existe pero no tiene el formato esperado
// (archivo truncado, editado a mano, version vieja...). Es una IOException:
// el ViewModel la trata como cualquier falla de lectura.
class FormatoInvalidoException(mensaje: String, causa: Throwable? = null) : IOException(mensaje, causa)

// =====================================================================
// FORMATO DOCUMENTADO — notas.json (texto JSON, UTF-8)
//
//   {
//     "version": 1,
//     "notas": [
//       { "id": "8f1c...", "titulo": "Parcial", "contenido": "Linea 1\nLinea 2; \"citas\"" }
//     ]
//   }
//
// - Por que JSON y no split(";"): el usuario puede escribir ; , comillas o
//   saltos de linea. JSON define como ESCAPAR esos caracteres (\" \n \\),
//   asi que cualquier texto vuelve exactamente igual. org.json viene
//   incluido en Android: no hace falta agregar dependencias.
// - Nota vacia: un campo vacio se guarda como "" (cadena vacia), nunca se
//   omite ni es null. Igual, el validador no deja guardar notas vacias.
// - "version": permite cambiar el formato en el futuro y seguir leyendo
//   archivos viejos.
// - Archivo inexistente = lista vacia (primera vez que se usa la app).
// =====================================================================
class ArchivoNotasRepository(carpeta: File) : NotaRepository {

    private val archivo = File(carpeta, NOMBRE_ARCHIVO)
    private val temporal = File(carpeta, "$NOMBRE_ARCHIVO.tmp")

    // Mutex: guardar y eliminar son "leer todo -> modificar -> escribir todo".
    // Si dos operaciones se mezclaran, una podria pisar a la otra. El Mutex
    // hace que se ejecuten de a una.
    private val mutex = Mutex()

    override suspend fun obtenerTodas(): List<Nota> = mutex.withLock { leerArchivo() }

    override suspend fun guardar(nota: Nota) = mutex.withLock {
        val notas = leerArchivo().toMutableList()
        val i = notas.indexOfFirst { it.id == nota.id }
        if (i >= 0) notas[i] = nota else notas.add(nota)
        escribirArchivo(notas)
    }

    override suspend fun eliminar(id: String) = mutex.withLock {
        escribirArchivo(leerArchivo().filterNot { it.id == id })
    }

    // --- Lectura -------------------------------------------------------
    private suspend fun leerArchivo(): List<Nota> = withContext(Dispatchers.IO) {
        if (!archivo.exists()) return@withContext emptyList()
        val texto = archivo.readText(Charsets.UTF_8)
        try {
            val raiz = JSONObject(texto)
            val array = raiz.getJSONArray("notas")
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                Nota(
                    id = o.getString("id"),
                    titulo = o.getString("titulo"),
                    contenido = o.getString("contenido")
                )
            }
        } catch (e: JSONException) {
            // Contenido incompleto o corrupto: NO se devuelve lista vacia
            // (eso haria que el proximo guardado BORRE todas las notas).
            // Se informa el problema y el archivo queda intacto.
            throw FormatoInvalidoException("El archivo $NOMBRE_ARCHIVO tiene un formato inválido", e)
        }
    }

    // --- Escritura segura (desafio) -------------------------------------
    // 1. Se escribe la lista COMPLETA en notas.json.tmp.
    // 2. Recien cuando termino bien, se renombra el temporal sobre notas.json.
    // Renombrar dentro de la misma carpeta es atomico: el archivo final es
    // la version vieja completa o la nueva completa, nunca una mezcla.
    // Si la app se corta a mitad de la escritura, solo se pierde el .tmp.
    private suspend fun escribirArchivo(notas: List<Nota>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        notas.forEach { n ->
            array.put(
                JSONObject()
                    .put("id", n.id)
                    .put("titulo", n.titulo)
                    .put("contenido", n.contenido)
            )
        }
        val raiz = JSONObject().put("version", 1).put("notas", array)
        temporal.writeText(raiz.toString(2), Charsets.UTF_8)
        if (!temporal.renameTo(archivo)) {
            throw IOException("No se pudo reemplazar $NOMBRE_ARCHIVO")
        }
    }

    companion object {
        const val NOMBRE_ARCHIVO = "notas.json"
    }
}
