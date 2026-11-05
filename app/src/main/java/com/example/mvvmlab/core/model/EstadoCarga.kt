package com.example.mvvmlab.core.model

// =====================================================================
// EJERCICIO 9 — Cargando, listo o fallo
// =====================================================================
// En vez de banderas sueltas (cargando=true, error!=null, ...) que se
// pueden combinar de forma ambigua, cada operacion tiene un tipo SELLADO
// con sus estados posibles. Un "when" sobre ellos es exhaustivo.
// =====================================================================

// Estado de la LECTURA inicial del borrador.
sealed interface EstadoCarga {
    data object Cargando : EstadoCarga
    data object Listo : EstadoCarga
    data class Error(val mensaje: String) : EstadoCarga
}
