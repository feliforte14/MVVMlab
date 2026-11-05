package com.example.mvvmlab.domain.model

// =====================================================================
// EJERCICIOS 7 y 8 — Leer y guardar un archivo de texto desde el Repository
// =====================================================================

// DECISION DOCUMENTADA: si el archivo no existe se devuelve un RESULTADO
// EXPLICITO (NoExiste), no un texto vacio.
// Motivo: "no existe" y "existe pero esta vacio" son situaciones distintas
// y la pantalla puede querer mostrarlas distinto ("Todavia no hay borrador"
// vs. un borrador en blanco). Con "" no habria forma de diferenciarlas.
//
// Una FALLA REAL de acceso (permiso, disco, archivo corrupto) NO es un
// resultado: se informa con una excepcion (IOException) para que quien
// llama la maneje. Asi se distinguen los tres casos:
//   NoExiste  -> normal, primera vez que se abre la app
//   Leido(..) -> normal, habia un borrador
//   excepcion -> algo salio mal
sealed interface ResultadoLectura {
    data object NoExiste : ResultadoLectura
    data class Leido(val texto: String) : ResultadoLectura
}
