package com.example.mvvmlab.domain.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Ej 5 — Desafio: probar espacios en blanco y una nota valida.
// Como las reglas estan en Kotlin puro, se prueban SIN emulador:
// click derecho sobre el archivo -> Run 'ValidadorNotaTest'.
class ValidadorNotaTest {

    @Test
    fun tituloSoloEspacios_esInvalido() {
        val errores = ValidadorNota.validar(titulo = "     ", contenido = "Contenido suficiente")
        assertNotNull(errores.titulo)
        assertNull(errores.contenido)
    }

    @Test
    fun contenidoConEspaciosAlrededor_noCuentaLosEspacios() {
        // "   corto   " tiene 11 caracteres, pero solo 5 "reales"
        val errores = ValidadorNota.validar(titulo = "Parcial", contenido = "   corto   ")
        assertNotNull(errores.contenido)
    }

    @Test
    fun contenidoDeExactamente10Caracteres_esValido() {
        val errores = ValidadorNota.validar(titulo = "Parcial", contenido = "1234567890")
        assertNull(errores.contenido)
    }

    @Test
    fun notaValida_noTieneErrores() {
        val errores = ValidadorNota.validar(titulo = "Parcial DA1", contenido = "Repasar MVVM y StateFlow")
        assertFalse(errores.hayErrores)
        assertEquals(ErroresNota(), errores)
    }

    @Test
    fun ambosCamposMal_informaAmbosErrores() {
        val errores = ValidadorNota.validar(titulo = "", contenido = "")
        assertTrue(errores.hayErrores)
        assertNotNull(errores.titulo)
        assertNotNull(errores.contenido)
    }
}
