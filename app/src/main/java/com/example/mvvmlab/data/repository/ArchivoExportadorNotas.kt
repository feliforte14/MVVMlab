package com.example.mvvmlab.data.repository

import com.example.mvvmlab.domain.model.Nota
import com.example.mvvmlab.domain.repository.ExportadorNotas
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
