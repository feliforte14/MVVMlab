package com.example.mvvmlab.domain.repository

import com.example.mvvmlab.domain.model.Nota

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
