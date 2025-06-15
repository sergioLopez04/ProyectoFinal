package com.example.proyectofinal.DataBase.Actividad

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinal.Modelos.Actividad
import com.example.proyectofinal.Modelos.Proyecto
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividad(actividad: Actividad)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(actividades: List<Actividad>)

    @Query("UPDATE proyectos SET tiempoAcumulado = tiempoAcumulado + :duracion WHERE id = :proyectoId")
    suspend fun actualizarTiempo(proyectoId: Int, duracion: Long)

    @Query(
        "SELECT proyectos.*, SUM(actividades.duracion) as tiempo " +
                "FROM proyectos " +
                "INNER JOIN actividades ON proyectos.id = actividades.proyectoId " +
                "WHERE actividades.fecha BETWEEN :inicio AND :fin " +
                "AND actividades.userId = :userId " +  // <-- Añade esta línea
                "GROUP BY proyectos.id"
    )
    fun obtenerResumenDiario(userId: Int, inicio: Long, fin: Long): Flow<List<ProyectoConTiempo>>

    @Update
    suspend fun actualizarActividad(actividad: Actividad)

    @Query("SELECT SUM(tiempoFin - tiempoInicio) FROM actividades WHERE proyectoId = :proyectoId AND tiempoInicio BETWEEN :inicio AND :fin")
    fun obtenerTiempoProyectoDia(proyectoId: Int, inicio: Long, fin: Long): Flow<Long>

    @Query("SELECT * FROM actividades WHERE userId = :userId ORDER BY fecha DESC, tiempoInicio DESC")
    suspend fun obtenerActividadesPorUsuario(userId: Int): List<Actividad>

    // Clase para mapear el resultado de la consulta
    data class ProyectoConTiempo(
        @Embedded val proyecto: Proyecto,
        val tiempo: Long
    )





}