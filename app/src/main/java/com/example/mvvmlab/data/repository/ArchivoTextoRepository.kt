package com.example.mvvmlab.data.repository

import com.example.mvvmlab.domain.model.ResultadoLectura
import com.example.mvvmlab.domain.repository.BorradorRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Implementacion con un archivo de texto en el almacenamiento INTERNO
// PRIVADO de la app (filesDir).
//
// Recibe la CARPETA (un File), no un Context ni una Activity: es lo unico
// que necesita, y asi no retiene referencias a pantallas que Android
// puede destruir (evita fugas de memoria).
class ArchivoTextoRepository(carpeta: File) : BorradorRepository {

    private val archivo = File(carpeta, NOMBRE_ARCHIVO)

    override suspend fun leer(): ResultadoLectura =
        // Dispatchers.IO: hilos pensados para trabajo de disco/red.
        // Nunca leer archivos en el hilo principal (congela la pantalla).
        withContext(Dispatchers.IO) {
            if (!archivo.exists()) {
                ResultadoLectura.NoExiste
            } else {
                // readText puede lanzar IOException: se deja propagar.
                ResultadoLectura.Leido(archivo.readText(Charsets.UTF_8))
            }
        }

    // Ej 8 — DECISION DOCUMENTADA: se SOBRESCRIBE todo el archivo.
    // Un borrador representa "el texto actual", no un historial: si se
    // agregara al final (appendText), cada guardado duplicaria el contenido.
    // writeText crea el archivo si no existe y reemplaza lo que tenia.
    //
    // UTF-8 explicito: la misma codificacion al leer y al escribir; asi las
    // tildes, la ñ y los saltos de linea vuelven exactamente igual.
    override suspend fun guardar(texto: String) {
        withContext(Dispatchers.IO) {                 // fuera del hilo principal
            archivo.writeText(texto, Charsets.UTF_8)  // puede lanzar IOException
        }
    }

    companion object {
        const val NOMBRE_ARCHIVO = "borrador.txt"
    }
}
