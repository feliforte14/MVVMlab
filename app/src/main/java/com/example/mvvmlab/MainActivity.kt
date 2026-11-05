package com.example.mvvmlab

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.ui.theme.MvvmLabTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MvvmLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaTareas(
                        context = LocalContext.current,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Pantalla "mal hecha a proposito" para el Ejercicio 1 de MVVM:
// todo esta mezclado (UI + validacion + lista + guardado en archivo).
@Composable
fun PantallaTareas(context: Context, modifier: Modifier = Modifier) {
    var texto by remember { mutableStateOf("") }                         // (A)
    var tareas by remember { mutableStateOf(listOf<String>()) }          // (B)
    var error by remember { mutableStateOf<String?>(null) }              // (C)

    Column(modifier.padding(16.dp)) {                                     // (D)
        Text("Mis tareas (${tareas.size})")
        OutlinedTextField(value = texto, onValueChange = { texto = it })

        Button(onClick = {
            if (texto.isBlank()) {                                        // (E)
                error = "La tarea no puede estar vacía"
            } else if (tareas.contains(texto)) {                          // (F)
                error = "Esa tarea ya existe"
            } else {
                tareas = tareas + texto                                   // (G)
                File(context.filesDir, "tareas.txt")                      // (H)
                    .writeText(tareas.joinToString("\n"))
                texto = ""                                                // (I)
                error = null
            }
        }) { Text("Agregar") }

        error?.let { Text(it, color = Color.Red) }                        // (J)
        LazyColumn { items(tareas) { Text(it) } }                         // (K)
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaTareasPreview() {
    MvvmLabTheme {
        PantallaTareas(context = LocalContext.current)
    }
}
