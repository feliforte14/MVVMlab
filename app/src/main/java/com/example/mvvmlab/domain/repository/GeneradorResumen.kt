package com.example.mvvmlab.domain.repository

import com.example.mvvmlab.domain.model.Registro

// --- Generacion del RESUMEN (implementado con resumen.txt) -------------
// Devuelve el texto generado (para mostrarlo o compartirlo).
fun interface GeneradorResumen {
    suspend fun generar(registros: List<Registro>): String
}
