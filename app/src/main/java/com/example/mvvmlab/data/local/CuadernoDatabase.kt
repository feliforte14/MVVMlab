package com.example.mvvmlab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RegistroEntity::class], version = 1, exportSchema = false)
abstract class CuadernoDatabase : RoomDatabase() {
    abstract fun registroDao(): RegistroDao
}
