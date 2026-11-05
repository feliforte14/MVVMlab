package com.example.mvvmlab

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =====================================================================
// EJERCICIO 14 — Lo que SI va a un archivo de texto
// =====================================================================
// Los registros estructurados (las notas) viven en Room. El archivo de
// texto se usa para algo distinto: un RESUMEN EXPORTABLE, legible por una
// persona, que se genera a pedido a partir de la base. No es una segunda
// copia de la base: nadie lo vuelve a leer para cargar notas, asi que no
// compite como fuente de verdad.
// =====================================================================

// Contrato (fun interface = interfaz con una sola funcion; en los tests se
// puede reemplazar con una lambda).
fun interface ExportadorNotas {
    suspend fun exportar(notas: List<Nota>): String     // devuelve el nombre del archivo
}

class ArchivoExportadorNotas(carpeta: File) : ExportadorNotas {

    private val archivo = File(carpeta, NOMBRE_ARCHIVO)

    override suspend fun exportar(notas: List<Nota>): String = withContext(Dispatchers.IO) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val texto = buildString {
            appendLine("MIS NOTAS — exportado el $fecha")
            appendLine("Total: ${notas.size}")
            appendLine()
            notas.forEachIndexed { i, n ->
                appendLine("${i + 1}. ${n.titulo}")
                n.contenido.lines().forEach { appendLine("   $it") }
                appendLine()
            }
        }
        // Sobrescribe: el resumen siempre refleja el estado actual de la base.
        archivo.writeText(texto, Charsets.UTF_8)
        NOMBRE_ARCHIVO
    }

    companion object {
        const val NOMBRE_ARCHIVO = "notas_exportadas.txt"
    }
}
