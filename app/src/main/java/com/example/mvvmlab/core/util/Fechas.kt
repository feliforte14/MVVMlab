package com.example.mvvmlab.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
