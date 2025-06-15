package com.example.proyectofinal.DataBase.Tarea

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.Tareas
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {

    @Query("SELECT * FROM tareas WHERE proyectoId = :proyectoId")
    fun getTareas(proyectoId: Int): Flow<List<Tareas>>

    @Query("SELECT * FROM tareas")
    fun getTodasTareas(): Flow<List<Tareas>>

    @Query("SELECT tareas.* FROM tareas INNER JOIN proyectos ON tareas.proyectoId = proyectos.id WHERE proyectos.id_creador = :userId")
    fun getTareasPorUsuario(userId: Int): Flow<List<Tareas>>

    @Query("SELECT * FROM tareas WHERE proyectoId = :proyectoId")
    fun getTareasDelProyecto(proyectoId: Int): Flow<List<Tareas>>




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: Tareas)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(tarea: List<Tareas>)

    @Update
    suspend fun updateTarea(tarea: Tareas)

    @Delete
    suspend fun deleteTarea(tarea: Tareas)
}