package com.example.mvvmlab

import android.app.Application
import androidx.room.Room

// =====================================================================
// Contenedor de dependencias (Ej 11) del Cuaderno de campo
//
//   MvvmLabApp (Application)
//     └─ ContenedorApp
//          ├─ CuadernoDatabase (Room, cuaderno.db) ─► RegistroRepository
//          ├─ ArchivoBitacoraRepository (bitacora.txt) ─► BitacoraRepository
//          └─ ArchivoResumen (resumen.txt) ─► GeneradorResumen
// =====================================================================
class ContenedorApp(private val app: Application) {

    // Etapa 4 — experimento "falla controlada del Repository":
    // cambiar a true para que toda escritura de registros falle.
    private val simularFallaEnRegistros = false

    private val baseDeDatos: CuadernoDatabase by lazy {
        Room.databaseBuilder(app, CuadernoDatabase::class.java, "cuaderno.db").build()
    }

    val registroRepository: RegistroRepository by lazy {
        val real = RoomRegistroRepository(baseDeDatos.registroDao())
        if (simularFallaEnRegistros) FallaControladaRegistroRepository(real) else real
    }

    val bitacoraRepository: BitacoraRepository by lazy { ArchivoBitacoraRepository(app.filesDir) }

    val generadorResumen: GeneradorResumen by lazy { ArchivoResumen(app.filesDir) }
}

class MvvmLabApp : Application() {
    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
    }
}
