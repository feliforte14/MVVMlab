package com.example.mvvmlab

import android.app.Application
import com.example.mvvmlab.di.ContenedorApp

// Application propia: Android la crea ANTES que cualquier Activity y la
// mantiene mientras viva el proceso. Es el lugar natural del contenedor.
// Se registra en AndroidManifest.xml con android:name=".MvvmLabApp".
class MvvmLabApp : Application() {

    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
    }
}
