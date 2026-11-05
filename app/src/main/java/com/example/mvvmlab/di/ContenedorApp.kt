package com.example.mvvmlab.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.room.Room
import com.example.mvvmlab.MvvmLabApp
import com.example.mvvmlab.data.local.CuadernoDatabase
import com.example.mvvmlab.data.repository.ArchivoBitacoraRepository
import com.example.mvvmlab.data.repository.ArchivoResumen
import com.example.mvvmlab.data.repository.FallaControladaRegistroRepository
import com.example.mvvmlab.data.repository.RoomRegistroRepository
import com.example.mvvmlab.domain.repository.BitacoraRepository
import com.example.mvvmlab.domain.repository.GeneradorResumen
import com.example.mvvmlab.domain.repository.RegistroRepository

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

fun CreationExtras.contenedor() = (checkNotNull(this[APPLICATION_KEY]) as MvvmLabApp).contenedor
