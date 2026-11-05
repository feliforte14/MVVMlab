package com.example.mvvmlab.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// =====================================================================
// Controlador de corrutinas de prueba (justificacion)
// =====================================================================
// viewModelScope lanza sus corrutinas en Dispatchers.Main, que es el hilo
// de la interfaz de ANDROID. En un test JVM ese hilo no existe y el test
// falla con "Module with the Main dispatcher is missing".
// Esta regla reemplaza Dispatchers.Main por un dispatcher de prueba antes
// de cada test y lo restaura despues.
// UnconfinedTestDispatcher ejecuta las corrutinas en el momento: cuando
// vm.onGuardar() vuelve, el guardado (falso, sin demoras) ya termino y se
// puede verificar el estado.
// =====================================================================
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
