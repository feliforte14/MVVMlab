package com.example.mvvmlab.domain.repository

import com.example.mvvmlab.domain.model.Nota

// Contrato (el mismo del Ej 6): el ViewModel solo conoce esto.
interface NotaRepository {
    suspend fun obtenerTodas(): List<Nota>
    suspend fun guardar(nota: Nota)          // alta si el id es nuevo, edicion si ya existe
    suspend fun eliminar(id: String)
}
