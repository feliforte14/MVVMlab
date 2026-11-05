package com.example.mvvmlab

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// =====================================================================
// EJERCICIO 13 — Pruebas de los ViewModels
// Sin emulador, sin archivos, sin pantalla: JUnit + Repository falso.
// Ejecutar: click derecho sobre el archivo -> Run 'NotasViewModelTest'
//           o ./gradlew :app:testDebugUnitTest
// Estructura de cada test: PREPARAR -> ACTUAR -> VERIFICAR.
// =====================================================================
class NotasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Ej 14: el exportador tambien se reemplaza por uno falso (una lambda).
    private val exportadorFalso = ExportadorNotas { "falso.txt" }

    private val parcial = Nota("1", "Parcial DA1", "Repasar MVVM y Repository")
    private val compras = Nota("2", "Compras", "Yerba, café y galletitas")

    private fun detalle(repo: NotaRepository, id: String = DetalleNotaViewModel.ID_NUEVA) =
        DetalleNotaViewModel(repo, SavedStateHandle(mapOf(DetalleNotaViewModel.ARG_ID to id)))

    // --- 1. CARGA INICIAL ----------------------------------------------

    @Test
    fun lista_cargaInicial_muestraLasNotasDelRepository() = runTest {
        val repo = FakeNotaRepository(listOf(parcial, compras))

        val vm = ListaNotasViewModel(repo, exportadorFalso)

        val estado = vm.uiState.value
        assertEquals(EstadoCarga.Listo, estado.carga)
        assertEquals(listOf(parcial, compras), estado.notas)
    }

    @Test
    fun lista_siLaLecturaFalla_muestraErrorYNoSeCierra() = runTest {
        val repo = FakeNotaRepository(listOf(parcial)).apply { fallarLectura = true }

        val vm = ListaNotasViewModel(repo, exportadorFalso)

        assertTrue(vm.uiState.value.carga is EstadoCarga.Error)
    }

    @Test
    fun detalle_conId_cargaLaNotaEnElFormulario() = runTest {
        val vm = detalle(FakeNotaRepository(listOf(parcial)), id = "1")

        val estado = vm.uiState.value
        assertEquals("Parcial DA1", estado.titulo)
        assertFalse(estado.esNueva)
        assertFalse(estado.hayCambiosSinGuardar)
    }

    // --- 2. VALIDACION -------------------------------------------------

    @Test
    fun detalle_tituloVacio_muestraErrorDelCampo() = runTest {
        val vm = detalle(FakeNotaRepository())
        vm.onTituloChange("   ")
        vm.onContenidoChange("Contenido suficientemente largo")

        vm.onGuardar()

        assertNotNull(vm.uiState.value.errorTitulo)
        assertNull(vm.uiState.value.errorContenido)
    }

    // Desafio: una solicitud invalida NUNCA invoca guardar.
    @Test
    fun detalle_solicitudInvalida_nuncaInvocaGuardar() = runTest {
        val repo = FakeNotaRepository()
        val vm = detalle(repo)
        vm.onTituloChange("")
        vm.onContenidoChange("corto")

        vm.onGuardar()
        vm.onGuardar()                      // aunque se insista

        assertTrue(repo.guardadas.isEmpty())
        assertFalse(vm.uiState.value.guardadoOk)
    }

    // --- 3. GUARDADO CORRECTO ------------------------------------------

    @Test
    fun detalle_notaValida_seGuardaYAvisaQueTermino() = runTest {
        val repo = FakeNotaRepository()
        val vm = detalle(repo)
        vm.onTituloChange("  Nueva nota  ")
        vm.onContenidoChange("Contenido de prueba")

        vm.onGuardar()

        assertEquals(1, repo.guardadas.size)
        assertEquals("Nueva nota", repo.guardadas.first().titulo)   // se guarda sin espacios extremos
        assertTrue(vm.uiState.value.guardadoOk)
        assertFalse(vm.uiState.value.guardando)
    }

    @Test
    fun guardarEnElDetalle_actualizaLaLista_unaSolaFuenteDeVerdad() = runTest {
        val repo = FakeNotaRepository(listOf(parcial))
        val lista = ListaNotasViewModel(repo, exportadorFalso)
        val editor = detalle(repo, id = "1")

        editor.onContenidoChange("Repasar MVVM, Repository y tests")
        editor.onGuardar()

        assertEquals("Repasar MVVM, Repository y tests", lista.uiState.value.notas.single().contenido)
    }

    // --- 4. ERROR DE ESCRITURA -----------------------------------------

    @Test
    fun detalle_siFallaLaEscritura_muestraErrorYConservaLoEscrito() = runTest {
        val repo = FakeNotaRepository().apply { fallarEscritura = true }
        val vm = detalle(repo)
        vm.onTituloChange("Parcial")
        vm.onContenidoChange("Contenido de prueba")

        vm.onGuardar()

        val estado = vm.uiState.value
        assertNotNull(estado.errorOperacion)
        assertFalse(estado.guardadoOk)          // no se navega como si hubiera guardado
        assertFalse(estado.guardando)
        assertEquals("Parcial", estado.titulo)  // el usuario no pierde lo escrito
    }

    @Test
    fun lista_siFallaEliminar_muestraErrorYConservaLaNota() = runTest {
        val repo = FakeNotaRepository(listOf(parcial)).apply { fallarEscritura = true }
        val vm = ListaNotasViewModel(repo, exportadorFalso)

        vm.onEliminar("1")

        assertNotNull(vm.uiState.value.errorOperacion)
        assertEquals(1, vm.uiState.value.notas.size)
    }
}
