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
import com.example.mvvmlab.presentation.detalle.DetalleNotaViewModel
import com.example.mvvmlab.presentation.detalle.PantallaDetalle
import com.example.mvvmlab.presentation.lista.ListaNotasViewModel
import com.example.mvvmlab.presentation.lista.PantallaLista

// =====================================================================
// EJERCICIO 12 — Navegacion entre las dos pantallas
// =====================================================================
// Rutas:
//   "lista"          -> PantallaLista
//   "detalle/{id}"   -> PantallaDetalle. {id} es el id de la nota o
//                       "nueva". Se pasa SOLO el identificador, nunca el
//                       objeto Nota entero: el detalle se la pide al
//                       Repository y asi edita la version vigente.
//
// Cada destino tiene SU ViewModel: viewModel(...) dentro de composable{}
// lo asocia a esa entrada de navegacion (vive mientras la pantalla este
// en la pila y se destruye al salir de ella).
// =====================================================================
@Composable
fun AppNotas() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Rutas.LISTA) {

        composable(Rutas.LISTA) {
            val vm: ListaNotasViewModel = viewModel(factory = ListaNotasViewModel.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()
            PantallaLista(
                estado = estado,
                onAbrir = { id -> nav.navigate(Rutas.detalle(id)) },
                onNueva = { nav.navigate(Rutas.detalle(DetalleNotaViewModel.ID_NUEVA)) },
                onEliminar = vm::onEliminar,
                onReintentar = vm::onReintentar
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.StringType })
        ) {
            val vm: DetalleNotaViewModel = viewModel(factory = DetalleNotaViewModel.Factory)
            val estado by vm.uiState.collectAsStateWithLifecycle()
            PantallaDetalle(
                estado = estado,
                onTituloChange = vm::onTituloChange,
                onContenidoChange = vm::onContenidoChange,
                onGuardar = vm::onGuardar,
                onVolver = { nav.popBackStack() }
            )
        }
    }
}
