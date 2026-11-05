package com.example.mvvmlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mvvmlab.ui.theme.MvvmLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MvvmLabTheme {
                // Ej 12: la Activity solo aloja la navegacion. Cada pantalla
                // obtiene su ViewModel dentro de su destino (NavegacionApp.kt).
                AppNotas()
            }
        }
    }
}
