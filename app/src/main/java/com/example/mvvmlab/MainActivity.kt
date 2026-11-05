package com.example.mvvmlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mvvmlab.core.theme.MvvmLabTheme
import com.example.mvvmlab.presentation.AppCuaderno

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MvvmLabTheme {
                // Ej 15: la Activity solo aloja la navegacion. Cada pantalla
                // obtiene su ViewModel dentro de su destino (presentation/AppCuaderno.kt).
                AppCuaderno()
            }
        }
    }
}
