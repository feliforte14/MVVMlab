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

// Pruebas del integrador: reglas y ViewModels con Repository falso.
class CuadernoTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val hoy = 1_762_300_800_000L                       // un dia fijo (medianoche UTC)
    private val unDia = 86_400_000L
    private val reserva = Registro("1", "Reserva Costanera Sur", "Garzas y un carpincho", hoy, favorito = false)
    private val feria = Registro("2", "Feria de San Telmo", "Mucha gente", hoy - unDia, favorito = true)

    private val generadorFalso = GeneradorResumen { regs -> "Resumen de ${regs.size}" }

    private fun editor(repo: RegistroRepository, id: String = EditarRegistroViewModel.ID_NUEVO) =
        EditarRegistroViewModel(repo, SavedStateHandle(mapOf(EditarRegistroViewModel.ARG_ID to id)), hoy = { hoy })

    // --- Reglas --------------------------------------------------------
    @Test
    fun validador_tituloVacio_descripcionVacia_fechaFutura() {
        val e = ValidadorRegistro.validar("  ", "", hoy + unDia, hoy)
        assertNotNull(e.titulo); assertNotNull(e.descripcion); assertNotNull(e.fecha)
    }

    @Test
    fun validador_registroValido_sinErrores() {
        assertFalse(ValidadorRegistro.validar("Salida", "Observé aves", hoy, hoy).hayErrores)
    }

    @Test
    fun validador_tituloMuyLargo_esInvalido() {
        assertNotNull(ValidadorRegistro.validar("x".repeat(61), "ok", hoy, hoy).titulo)
    }

    // --- Lista ---------------------------------------------------------
    @Test
    fun lista_buscaPorTitulo() = runTest {
        val vm = ListaRegistrosViewModel(FakeRegistroRepository(listOf(reserva, feria)), generadorFalso)
        vm.onBusquedaChange("feria")
        assertEquals(listOf(feria), vm.uiState.value.registros)
    }

    @Test
    fun lista_errorDeLectura_muestraEstadoDeError() = runTest {
        val repo = FakeRegistroRepository(listOf(reserva)).apply { fallarLectura = true }
        val vm = ListaRegistrosViewModel(repo, generadorFalso)
        assertTrue(vm.uiState.value.carga is EstadoCarga.Error)
    }

    @Test
    fun lista_favorito_seInvierte() = runTest {
        val vm = ListaRegistrosViewModel(FakeRegistroRepository(listOf(reserva)), generadorFalso)
        vm.onFavorito(reserva)
        assertTrue(vm.uiState.value.registros.single().favorito)
    }

    @Test
    fun lista_compartir_generaResumenConTodosAunqueHayaBusqueda() = runTest {
        val vm = ListaRegistrosViewModel(FakeRegistroRepository(listOf(reserva, feria)), generadorFalso)
        vm.onBusquedaChange("feria")
        vm.onGenerarResumen(compartir = true)
        assertEquals("Resumen de 2", vm.uiState.value.textoParaCompartir)
    }

    // --- Edicion -------------------------------------------------------
    @Test
    fun editar_tituloVacio_nuncaGuarda() = runTest {
        val repo = FakeRegistroRepository()
        val vm = editor(repo)
        vm.onDescripcionChange("Algo")
        vm.onGuardar()
        assertTrue(repo.guardados.isEmpty())
        assertNotNull(vm.uiState.value.errores.titulo)
    }

    @Test
    fun editar_registroValido_seGuardaConLaFechaDeHoy() = runTest {
        val repo = FakeRegistroRepository()
        val vm = editor(repo)
        vm.onTituloChange(" Salida al río ")
        vm.onDescripcionChange("Patos")
        vm.onGuardar()
        assertEquals("Salida al río", repo.guardados.single().titulo)
        assertEquals(hoy, repo.guardados.single().fecha)
        assertTrue(vm.uiState.value.guardadoOk)
    }

    @Test
    fun editar_falloDeEscritura_conservaLosDatos() = runTest {
        val repo = FakeRegistroRepository(listOf(reserva)).apply { fallarEscritura = true }
        val vm = editor(repo, id = "1")
        vm.onTituloChange("Reserva (editada)")
        vm.onGuardar()
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.guardadoOk)
        assertEquals("Reserva (editada)", vm.uiState.value.formulario.titulo)
    }

    @Test
    fun editar_detectaCambiosSinGuardar() = runTest {
        val vm = editor(FakeRegistroRepository(listOf(reserva)), id = "1")
        assertFalse(vm.uiState.value.hayCambiosSinGuardar)
        vm.onFavoritoChange(true)
        assertTrue(vm.uiState.value.hayCambiosSinGuardar)
        assertNull(vm.uiState.value.error)
    }
}
