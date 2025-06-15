package com.example.proyectofinal.DataBase.Proyecto

import androidx.room.*
import com.example.proyectofinal.Modelos.Proyecto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProyectoDao {
    @Query("SELECT * FROM proyectos ORDER BY fechaCreacion DESC")
    fun obtenerTodos(): Flow<List<Proyecto>>

    @Query("SELECT * FROM proyectos WHERE id_creador = :userId ORDER BY fechaCreacion DESC")
    fun obtenerPorUsuario(userId: Int): Flow<List<Proyecto>>


    @Query("SELECT nombre FROM usuarios WHERE id = :userId")
    fun obtenerNombreUsuario(userId: Int): Flow<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(proyecto: Proyecto)

    @Delete
    suspend fun eliminar(proyecto: Proyecto)

    @Query("SELECT * FROM proyectos WHERE id = :id")
    suspend fun obtenerProyectoPorId(id: Int): Proyecto?

    @Query("SELECT * FROM proyectos WHERE id_creador = :userId /*OR members LIKE '%' || :userId || '%'*/")
    fun getAll(userId: Int): Flow<List<Proyecto>>

    @Query("SELECT * FROM proyectos WHERE id = :id")
    fun getProyecto(id: Int): Flow<Proyecto?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(proyectos: List<Proyecto>)

    @Query("UPDATE proyectos SET isSynced = :estado WHERE id = :id")
    suspend fun actualizarSync(id: Int, estado: Boolean)

    @Query("SELECT * FROM proyectos WHERE isSynced = 0")
    suspend fun obtenerNoSincronizados(): List<Proyecto>




}
