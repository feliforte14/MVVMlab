package com.example.mvvmlab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// --- BASE DE DATOS -----------------------------------------------------
@Database(entities = [NotaEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao
}
