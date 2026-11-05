package com.example.mvvmlab.di

import android.app.Application
import com.example.mvvmlab.data.repository.ArchivoNotasRepository
import com.example.mvvmlab.data.repository.NotaRepositoryEnMemoria
import com.example.mvvmlab.domain.repository.NotaRepository

// =====================================================================
// EJERCICIO 11 — Dependencias: ¿quien crea el Repository?
// =====================================================================
//
// Inyeccion de dependencias "a mano" (sin Hilt ni Koin):
//   - Las piezas NO crean sus dependencias: las RECIBEN (por constructor).
//   - Alguien tiene que crearlas: un CONTENEDOR que vive en la capa de
//     aplicacion y existe una sola vez mientras la app esta viva.
//
//   MvvmLabApp (Application)          <- se crea 1 vez al arrancar la app
//     └─ ContenedorApp                <- arma las dependencias
//          └─ notaRepository          <- UNA instancia para toda la app
//                └─ NotasViewModel (lo recibe por la fabrica)
// =====================================================================

// Origenes de datos posibles.
enum class OrigenDatos { MEMORIA, ARCHIVO }

class ContenedorApp(private val app: Application) {

    // ---------------------------------------------------------------
    // UNICA DECISION DE CONFIGURACION (desafio): cambiar esta linea
    // alterna entre el Repository en memoria y el de archivos.
    // Ningun ViewModel ni composable se entera.
    // ---------------------------------------------------------------
    val origenDatos: OrigenDatos = OrigenDatos.ARCHIVO

    // by lazy: se crea la primera vez que alguien lo pide, y despues se
    // devuelve SIEMPRE la misma instancia (no en cada recomposicion, no
    // en cada ViewModel).
    val notaRepository: NotaRepository by lazy {
        when (origenDatos) {
            OrigenDatos.MEMORIA -> NotaRepositoryEnMemoria()
            OrigenDatos.ARCHIVO -> ArchivoNotasRepository(app.filesDir)
        }
    }
}
