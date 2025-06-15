package com.example.proyectofinal.DataBase


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.proyectofinal.DataBase.Actividad.ActividadDao
import com.example.proyectofinal.DataBase.Proyecto.ProyectoDao
import com.example.proyectofinal.DataBase.Tarea.TareaDao
import com.example.proyectofinal.Modelos.Actividad
import com.example.proyectofinal.Modelos.Mensaje
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.Tareas
import com.example.proyectofinal.Modelos.Usuario


@Database(entities = [Proyecto::class, Usuario::class, Actividad::class, Tareas::class], version = 70, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun proyectoDao(): ProyectoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun actividadDao(): ActividadDao
    abstract fun tareaDao(): TareaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "proyectos_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

