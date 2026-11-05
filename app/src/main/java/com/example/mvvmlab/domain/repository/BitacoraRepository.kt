package com.example.mvvmlab.domain.repository

// --- Contrato de la BITACORA libre (implementado con bitacora.txt) -----
interface BitacoraRepository {
    suspend fun leer(): String?                      // null = todavia no existe
    suspend fun guardar(texto: String)
}
