package com.example.mvvmlab.data.mapper

import com.example.mvvmlab.data.local.RegistroEntity
import com.example.mvvmlab.domain.model.Registro

fun RegistroEntity.aDominio() = Registro(id, titulo, descripcion, fecha, favorito)

fun Registro.aEntity(actualizadoEn: Long) =
    RegistroEntity(id, titulo, descripcion, fecha, favorito, actualizadoEn)
