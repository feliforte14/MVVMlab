package com.example.mvvmlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mvvmlab.ui.theme.MvvmLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MvvmLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Ejercicio actual: 2 (pantalla de notas con un unico estado).
                    // El Ejercicio 1 sigue disponible en Ej01PantallaTareas.kt.
                    PantallaNotaConEstado(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
