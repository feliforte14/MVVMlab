package com.example.mvvmlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Rutas: "lista" · "editar/{id}" (id o "nuevo") · "bitacora"
// Cada destino obtiene SU ViewModel; todos comparten las mismas
// instancias de Repository (ContenedorApp).
@Composable
fun AppCuaderno() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "lista") {

        composable("lista") {
            val vm: ListaRegistrosViewModel = viewModel(factory = ListaRegistrosViewModel.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()
            PantallaLista(
                estado = estado,
                onBusquedaChange = vm::onBusquedaChange,
                onAbrir = { id -> nav.navigate("editar/$id") },
                onNuevo = { nav.navigate("editar/${EditarRegistroViewModel.ID_NUEVO}") },
                onFavorito = vm::onFavorito,
                onEliminar = vm::onEliminar,
                onGenerarResumen = vm::onGenerarResumen,
                onCompartido = vm::onCompartido,
                onBitacora = { nav.navigate("bitacora") },
                onReintentar = vm::onReintentar
            )
        }

        composable(
            "editar/{${EditarRegistroViewModel.ARG_ID}}",
            arguments = listOf(navArgument(EditarRegistroViewModel.ARG_ID) { type = NavType.StringType })
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

        composable("bitacora") {
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
