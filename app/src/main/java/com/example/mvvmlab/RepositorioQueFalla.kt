package com.example.mvvmlab

import kotlinx.coroutines.delay
import java.io.IOException

// =====================================================================
// EJERCICIO 9 — Repository de PRUEBA que falla a proposito
// =====================================================================
// Para comprobar la ruta de error NO se tocan permisos del dispositivo:
// se usa temporalmente esta implementacion del mismo contrato, que lanza
// una excepcion controlada.
//
// - fallasDeLectura: cuantas lecturas fallan antes de empezar a funcionar.
//   Con 1, la primera carga falla y "Reintentar" funciona.
// - fallarEscritura: si true, todo guardar() falla.
// - demoraMs: para ver el estado "Cargando..." / "Guardando..." en pantalla.
//
// Se activa desde BorradorViewModel.Factory (USAR_REPOSITORIO_QUE_FALLA).
// =====================================================================
class RepositorioQueFalla(
    private var fallasDeLectura: Int = 1,
    private val fallarEscritura: Boolean = true,
    private val demoraMs: Long = 1200
) : BorradorRepository {

    private var texto: String? = "Texto de prueba del repositorio que falla"

    override suspend fun leer(): ResultadoLectura {
        delay(demoraMs)
        if (fallasDeLectura > 0) {
            fallasDeLectura--
            throw IOException("Falla de lectura simulada")
        }
        return texto?.let { ResultadoLectura.Leido(it) } ?: ResultadoLectura.NoExiste
    }

    override suspend fun guardar(texto: String) {
        delay(demoraMs)
        if (fallarEscritura) throw IOException("Falla de escritura simulada")
        this.texto = texto
    }
}
