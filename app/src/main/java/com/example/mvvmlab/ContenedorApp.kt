package com.example.mvvmlab

import android.app.Application
import androidx.room.Room

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
enum class OrigenDatos { MEMORIA, ARCHIVO, ROOM }

class ContenedorApp(private val app: Application) {

    // ---------------------------------------------------------------
    // UNICA DECISION DE CONFIGURACION (desafio): cambiar esta linea
    // alterna entre el Repository en memoria y el de archivos.
    // Ningun ViewModel ni composable se entera.
    // ---------------------------------------------------------------
    val origenDatos: OrigenDatos = OrigenDatos.ROOM

    // by lazy: se crea la primera vez que alguien lo pide, y despues se
    // devuelve SIEMPRE la misma instancia (no en cada recomposicion, no
    // en cada ViewModel).
    val notaRepository: NotaRepository by lazy {
        when (origenDatos) {
            OrigenDatos.MEMORIA -> NotaRepositoryEnMemoria()
            OrigenDatos.ARCHIVO -> ArchivoNotasRepository(app.filesDir)
            OrigenDatos.ROOM -> RoomNotaRepository(baseDeDatos.notaDao())
        }
    }

    // Ej 14: UNA instancia de la base para toda la app (abrirla es costoso
    // y dos instancias sobre el mismo archivo podrian pisarse).
    private val baseDeDatos: AppDatabase by lazy {
        Room.databaseBuilder(app, AppDatabase::class.java, "notas.db").build()
    }

    // Ej 14: el archivo de texto queda para el resumen exportable.
    val exportadorNotas: ExportadorNotas by lazy { ArchivoExportadorNotas(app.filesDir) }
}

// Application propia: Android la crea ANTES que cualquier Activity y la
// mantiene mientras viva el proceso. Es el lugar natural del contenedor.
// Se registra en AndroidManifest.xml con android:name=".MvvmLabApp".
class MvvmLabApp : Application() {

    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
    }
}
