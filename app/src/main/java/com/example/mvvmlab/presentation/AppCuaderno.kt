package com.example.mvvmlab.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mvvmlab.core.navigation.Rutas
import com.example.mvvmlab.presentation.bitacora.BitacoraViewModel
import com.example.mvvmlab.presentation.bitacora.PantallaBitacora
import com.example.mvvmlab.presentation.editar.EditarRegistroViewModel
import com.example.mvvmlab.presentation.editar.PantallaEditar
import com.example.mvvmlab.presentation.lista.ListaRegistrosViewModel
import com.example.mvvmlab.presentation.lista.PantallaLista

// Rutas (core/navigation/Rutas.kt): lista · editar/{id} (id o "nuevo") · bitacora
// Cada destino obtiene SU ViewModel; todos comparten las mismas
// instancias de Repository (ContenedorApp).
@Composable
fun AppCuaderno() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Rutas.LISTA) {

        composable(Rutas.LISTA) {
            val vm: ListaRegistrosViewModel = viewModel(factory = ListaRegistrosViewModel.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()
            PantallaLista(
                estado = estado,
                onBusquedaChange = vm::onBusquedaChange,
                onAbrir = { id -> nav.navigate(Rutas.editar(id)) },
                onNuevo = { nav.navigate(Rutas.editar(EditarRegistroViewModel.ID_NUEVO)) },
                onFavorito = vm::onFavorito,
                onEliminar = vm::onEliminar,
                onGenerarResumen = vm::onGenerarResumen,
                onCompartido = vm::onCompartido,
                onBitacora = { nav.navigate(Rutas.BITACORA) },
                onReintentar = vm::onReintentar
            )
        }

        composable(
            Rutas.EDITAR,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.StringType })
        ) {
            val vm: EditarRegistroViewModel = viewModel(factory = EditarRegistroViewModel.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()
            PantallaEditar(
                estado = estado,
                onTituloChange = vm::onTituloChange,
                onDescripcionChange = vm::onDescripcionChange,
                onFechaChange = vm::onFechaChange,
                onFavoritoChange = vm::onFavoritoChange,
                onGuardar = vm::onGuardar,
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.BITACORA) {
            val vm: BitacoraViewModel = viewModel(factory = BitacoraViewModel.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()
            PantallaBitacora(
                estado = estado,
                onTextoChange = vm::onTextoChange,
                onGuardar = vm::onGuardar,
                onDescartar = vm::onDescartar,
                onReintentar = vm::onReintentar,
                onVolver = { nav.popBackStack() }
            )
        }
    }
}
