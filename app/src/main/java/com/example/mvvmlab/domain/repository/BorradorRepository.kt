package com.example.mvvmlab.domain.repository

import com.example.mvvmlab.domain.model.ResultadoLectura

// Contrato: el ViewModel solo conoce esto.
interface BorradorRepository {
    suspend fun leer(): ResultadoLectura
}
