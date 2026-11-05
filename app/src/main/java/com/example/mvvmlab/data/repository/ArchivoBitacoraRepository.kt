package com.example.mvvmlab.data.repository

import com.example.mvvmlab.domain.repository.BitacoraRepository
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// =====================================================================
// ARCHIVOS DE TEXTO del Cuaderno de campo (en filesDir, privados, sin
// permisos). Son INDEPENDIENTES de Room: no guardan registros.
// =====================================================================

// --- bitacora.txt: texto libre del usuario -----------------------------
// Se lee y se escribe ENTERO en UTF-8, asi se conservan parrafos (\n\n) y
// tildes. Escritura segura: temporal + renombrado (como el Ej 10).
class ArchivoBitacoraRepository(carpeta: File) : BitacoraRepository {

    private val archivo = File(carpeta, NOMBRE)
    private val temporal = File(carpeta, "$NOMBRE.tmp")

    override suspend fun leer(): String? = withContext(Dispatchers.IO) {
        if (archivo.exists()) archivo.readText(Charsets.UTF_8) else null
    }

    override suspend fun guardar(texto: String) = withContext(Dispatchers.IO) {
        temporal.writeText(texto, Charsets.UTF_8)
        if (!temporal.renameTo(archivo)) throw IOException("No se pudo guardar $NOMBRE")
    }

    companion object {
        const val NOMBRE = "bitacora.txt"
    }
}
