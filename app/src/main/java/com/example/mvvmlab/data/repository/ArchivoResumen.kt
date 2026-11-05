package com.example.mvvmlab.data.repository

import com.example.mvvmlab.core.util.Fechas
import com.example.mvvmlab.domain.model.Registro
import com.example.mvvmlab.domain.repository.GeneradorResumen
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
