package com.example.mvvmlab

import android.app.Application
import com.example.mvvmlab.di.ContenedorApp
import com.example.mvvmlab.di.contenedor

class MvvmLabApp : Application() {
    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
    }
}
