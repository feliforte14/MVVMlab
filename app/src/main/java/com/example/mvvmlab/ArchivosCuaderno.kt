package com.example.mvvmlab

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

// --- resumen.txt: texto GENERADO a partir de los registros ---------------
// POLITICA DOCUMENTADA
//  - Cuando se actualiza: SOLO a pedido, cuando el usuario toca
//    "Generar resumen" o "Compartir" en la lista. No se actualiza en cada
//    alta/edicion: es una "foto" para leer o enviar, no una copia viva.
//  - Fuente de verdad: Room. El resumen nunca se vuelve a leer para cargar
//    datos.
//  - Si falla la escritura: se muestra un error; los registros en Room NO
//    se ven afectados y el resumen.txt anterior queda completo (se escribe
//    primero en un temporal y recien despues se reemplaza).
class ArchivoResumen(carpeta: File) : GeneradorResumen {

    private val archivo = File(carpeta, NOMBRE)
    private val temporal = File(carpeta, "$NOMBRE.tmp")

    override suspend fun generar(registros: List<Registro>): String = withContext(Dispatchers.IO) {
        val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val texto = buildString {
            appendLine("CUADERNO DE CAMPO — Resumen")
            appendLine("Generado: $ahora")
            appendLine("Registros: ${registros.size} (${registros.count { it.favorito }} favoritos)")
            appendLine()
            registros.forEach { r ->
                appendLine("${if (r.favorito) "★" else "•"} ${Fechas.formatear(r.fecha)} — ${r.titulo}")
                r.descripcion.lines().forEach { appendLine("    $it") }
                appendLine()
            }
        }
        temporal.writeText(texto, Charsets.UTF_8)
        if (!temporal.renameTo(archivo)) throw IOException("No se pudo escribir $NOMBRE")
        texto
    }

    companion object {
        const val NOMBRE = "resumen.txt"
    }
}

// --- Falla CONTROLADA (Etapa 4: "provocar una falla del Repository") -----
// Envuelve al Repository real: lee normalmente pero toda escritura falla.
// Se activa en ContenedorApp (SIMULAR_FALLA_EN_REGISTROS). No se tocan
// permisos ni archivos del dispositivo.
class FallaControladaRegistroRepository(
    private val real: RegistroRepository
) : RegistroRepository {
    override fun observar(busqueda: String): Flow<List<Registro>> = real.observar(busqueda)
    override suspend fun obtener(id: String): Registro? = real.obtener(id)
    override suspend fun guardar(registro: Registro) = throw IOException("Falla simulada al guardar")
    override suspend fun eliminar(id: String) = throw IOException("Falla simulada al eliminar")
    override suspend fun cambiarFavorito(id: String, favorito: Boolean) =
        throw IOException("Falla simulada al marcar favorito")
}
