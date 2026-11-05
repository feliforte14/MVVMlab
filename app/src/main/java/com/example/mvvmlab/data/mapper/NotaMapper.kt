package com.example.mvvmlab.data.mapper

import androidx.room.Entity
import com.example.mvvmlab.data.local.NotaEntity
import com.example.mvvmlab.domain.model.Nota

// --- TRANSFORMACIONES Entity <-> dominio ----------------------------------
// Las hace el adaptador (capa de datos). Ni el ViewModel ni la pantalla
// conocen NotaEntity.
fun NotaEntity.aDominio() = Nota(id = id, titulo = titulo, contenido = contenido)

fun Nota.aEntity(actualizadaEn: Long) =
    NotaEntity(id = id, titulo = titulo, contenido = contenido, actualizadaEn = actualizadaEn)
