package com.example.mvvmlab

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// =====================================================================
// EJERCICIOS 7 y 8 — Leer y guardar un archivo de texto desde el Repository
// =====================================================================

// DECISION DOCUMENTADA: si el archivo no existe se devuelve un RESULTADO
// EXPLICITO (NoExiste), no un texto vacio.
// Motivo: "no existe" y "existe pero esta vacio" son situaciones distintas
// y la pantalla puede querer mostrarlas distinto ("Todavia no hay borrador"
// vs. un borrador en blanco). Con "" no habria forma de diferenciarlas.
//
// Una FALLA REAL de acceso (permiso, disco, archivo corrupto) NO es un
// resultado: se informa con una excepcion (IOException) para que quien
// llama la maneje. Asi se distinguen los tres casos:
//   NoExiste  -> normal, primera vez que se abre la app
//   Leido(..) -> normal, habia un borrador
//   excepcion -> algo salio mal
sealed interface ResultadoLectura {
    data object NoExiste : ResultadoLectura
    data class Leido(val texto: String) : ResultadoLectura
}

// Contrato: el ViewModel solo conoce esto.
interface BorradorRepository {
    suspend fun leer(): ResultadoLectura
    suspend fun guardar(texto: String)       // Ej 8
}

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
