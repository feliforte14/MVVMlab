package com.example.mvvmlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mvvmlab.core.theme.MvvmLabTheme
import com.example.mvvmlab.presentation.nota.NotaViewModel
import com.example.mvvmlab.presentation.nota.PantallaNotaConViewModel

class MainActivity : ComponentActivity() {

    // Mecanismo apropiado para obtener el ViewModel (Ej 3):
    // "by viewModels()" NO crea uno nuevo cada vez. Le pide a Android el
    // ViewModel asociado a esta Activity: la primera vez lo crea, y si la
    // Activity se recrea (rotacion) devuelve EL MISMO.
    // Nunca hacer NotaViewModel() a mano: se crearia uno nuevo en cada
    // recreacion y se perderia el estado.
    private val notaViewModel: NotaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MvvmLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // La Activity no valida ni guarda: solo conecta el VM con la pantalla.
                    PantallaNotaConViewModel(
                        viewModel = notaViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
