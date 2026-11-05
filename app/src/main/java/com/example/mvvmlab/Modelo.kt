package com.example.mvvmlab

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// =====================================================================
// EJERCICIO 15 — Cuaderno de campo: MODELO DE DOMINIO Y CONTRATOS
// =====================================================================
// Estos tipos no conocen Room, archivos ni Compose. Las pantallas y los
// ViewModels solo hablan con esto.
// =====================================================================

// Una observacion de una salida, recorrido, visita o actividad.
data class Registro(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: Long,            // dia de la observacion: medianoche UTC en milisegundos
    val favorito: Boolean
)

// --- Contrato de los REGISTROS (implementado con Room) -----------------
interface RegistroRepository {
    // Emite los registros cuyo titulo contiene "busqueda" ("" = todos),
    // y vuelve a emitir cada vez que cambian.
    fun observar(busqueda: String): Flow<List<Registro>>
    suspend fun obtener(id: String): Registro?
    suspend fun guardar(registro: Registro)          // alta o edicion
    suspend fun eliminar(id: String)
    suspend fun cambiarFavorito(id: String, favorito: Boolean)
}

// --- Contrato de la BITACORA libre (implementado con bitacora.txt) -----
interface BitacoraRepository {
    suspend fun leer(): String?                      // null = todavia no existe
    suspend fun guardar(texto: String)
}

// --- Generacion del RESUMEN (implementado con resumen.txt) -------------
// Devuelve el texto generado (para mostrarlo o compartirlo).
fun interface GeneradorResumen {
    suspend fun generar(registros: List<Registro>): String
}

// --- Fechas -------------------------------------------------------------
// Se guardan como "medianoche UTC" (lo mismo que devuelve el DatePicker de
// Material 3) y se muestran siempre en UTC, asi no "cambian de dia" por
// la zona horaria.
object Fechas {
    fun hoy(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun formatear(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))
}
