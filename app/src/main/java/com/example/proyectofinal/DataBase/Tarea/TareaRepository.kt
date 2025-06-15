package com.example.proyectofinal.DataBase.Tarea

import android.util.Log
import com.example.proyectofinal.DataBase.Proyecto.ProyectoApiService
import com.example.proyectofinal.DataBase.Proyecto.ProyectoDao
import com.example.proyectofinal.DataBase.Proyecto.TareasApiService
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.TareaRequest
import com.example.proyectofinal.Modelos.Tareas
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class TareaRepository(
    private val proyectoDao: ProyectoDao,
    private val tareaDao: TareaDao,
    private val api: TareasApiService,
    private val apiP: ProyectoApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getProyecto(id: Int): Flow<Proyecto?> = proyectoDao.getProyecto(id)
    fun getTareas(proyectoId: Int): Flow<List<Tareas>> = tareaDao.getTareas(proyectoId)
    fun getTodasTareas(): Flow<List<Tareas>> = tareaDao.getTodasTareas()
    fun getTareasPorUsuario(userId: Int): Flow<List<Tareas>> {
        return tareaDao.getTareasPorUsuario(userId)
    }

    fun getTareasPorUsuarioDesdeApi(userId: Int): Flow<List<Tareas>> = flow {
        val response = api.obtenerTareasDelUsuario(userId)
        emit(response.map { it.toEntity() }) // conversión aquí
    }


    fun getTareasDelProyecto(proyectoId: Int): Flow<List<Tareas>> {
        return tareaDao.getTareasDelProyecto(proyectoId)
    }





    suspend fun agregarTarea(tarea: Tareas) {
        withContext(ioDispatcher) {
            try {
                val request = tarea.toRequest()
                Log.d("TareaRepo", "Enviando tarea al servidor: $request")
                val response = api.crearTarea(request)
                Log.d("TareaRepo", "Respuesta del servidor: $response")


                val tareaSincronizada = Tareas(
                    id = response.id,
                    proyectoId = response.proyectoId,
                    descripcion = response.descripcion,
                    completada = response.completada,
                    prioridad = response.prioridad,
                    fechaInicio = response.fechaInicio,
                    fechaFin = response.fechaFin,
                    isSynced = true
                )

                tareaDao.insertTarea(tareaSincronizada)

            } catch (e: Exception) {
                Log.e("TareaRepo", "Error subiendo tarea al servidor", e)

                val tareaLocal = tarea.copy(isSynced = false)
                tareaDao.insertTarea(tareaLocal)
            }
        }
    }

    fun Tareas.toRequest(): TareaRequest {
        return TareaRequest(
            proyectoId = proyectoId,
            descripcion = descripcion,
            prioridad = prioridad,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )
    }

    suspend fun sincronizarDesdeServidor(userId: Int) {
        withContext(ioDispatcher) {
            try {
                // 1️⃣ Tareas de proyectos propios
                val tareasPropias = api.obtenerTareasDelUsuario(userId)
                tareaDao.insertarTodos(tareasPropias.map { it.toEntity() })

                // 2️⃣ Obtener IDs de proyectos unidos
                val proyectoIdsUnidos = apiP.obtenerProyectoIdsPorUsuario(userId)

                // 3️⃣ Tareas de cada proyecto unido
                proyectoIdsUnidos.forEach { proyectoId ->
                    try {
                        val tareasProyectoUnido = api.obtenerTareasDelProyecto(proyectoId)
                        Log.d("tareasProyectoUnido", tareasProyectoUnido.toString())
                        tareaDao.insertarTodos(tareasProyectoUnido.map { it.toEntity() })
                    } catch (e: Exception) {
                        Log.e("TareaRepo", "Error al obtener tareas del proyecto unido $proyectoId: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                Log.e("TareaRepo", "Error al sincronizar tareas: ${e.message}")
            }
        }
    }


    fun getTodasLasTareasDelUsuarioIncluyendoUnidos(userId: Int): Flow<List<Tareas>> {
        val tareasPropiasFlow = tareaDao.getTareasPorUsuario(userId)

        // Usamos flow builder para convertir la lista en un flow
        val tareasUnidasFlows = flow {
            val proyectoIdsUnidos = apiP.obtenerProyectoIdsPorUsuario(userId)
            val flows = proyectoIdsUnidos.map { id -> tareaDao.getTareasDelProyecto(id) }

            // Combinamos todas las tareas
            emitAll(
                combine(listOf(tareasPropiasFlow) + flows) { listas ->
                    listas.flatMap { it }.distinctBy { it.id }
                }
            )
        }

        return tareasUnidasFlows
    }





    suspend fun sincronizarDesdeServidorProyecto(proyectoId: Int) {
        withContext(ioDispatcher) {
            try {
                val tareasRemotas = api.obtenerTareasDelProyecto(proyectoId)
                tareaDao.insertarTodos(tareasRemotas.map { it.toEntity() })
            } catch (e: HttpException) {
                Log.e("TareaRepo", "Error HTTP sincronizando por proyecto: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                Log.e("TareaRepo", "Error general: ${e.message}")
            }
        }
    }

    suspend fun actualizarEstado(tarea: Tareas) {
        withContext(ioDispatcher) {
            tareaDao.updateTarea(tarea)  // tarea ya viene con el campo actualizado

            try {
                api.actualizarEstadoTarea(tarea.id, TareasApiService.EstadoRequest(tarea.completada))
            } catch (e: Exception) {
                Log.e("TareaRepo", "Error al actualizar estado remoto: ${e.message}")
            }
        }
    }






    suspend fun actualizarTarea(t: Tareas) = tareaDao.updateTarea(t)
    suspend fun borrarTarea(t: Tareas) = tareaDao.deleteTarea(t)
}