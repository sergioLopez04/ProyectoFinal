package com.example.proyectofinal.DataBase.Proyecto

import android.util.Log
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.ProyectoRequest
import com.example.proyectofinal.Modelos.ProyectoResponse
import com.example.proyectofinal.Modelos.UsuarioResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ProyectoRepository(
    private val dao: ProyectoDao,
    private val api: ProyectoApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val proyectos: Flow<List<Proyecto>> = dao.obtenerTodos()

    fun obtenerProyectosPorUsuario(userId: Int): Flow<List<Proyecto>> {
        return dao.obtenerPorUsuario(userId)
    }

    suspend fun insertar(proyecto: Proyecto) {
        withContext(ioDispatcher) {
            try {

                val request = proyecto.toRequest()
                Log.d("TareaRepo", "Enviando tarea al servidor: $request")
                val response = api.crearProyecto(request)
                Log.d("ProyRepo", "Respuesta del servidor: $response")

                val proyectoSincronizado = Proyecto(
                    id = response.id,
                    nombre = response.nombre,
                    descripcion = response.descripcion,
                    id_creador = response.id_creador,
                    fechaCreacion = response.fecha_creacion,
                    tiempoAcumulado = response.tiempo_acumulado,
                    isSynced = true
                )

                Log.d("proyectoSincronizado", proyectoSincronizado.toString())

                dao.insertar(proyectoSincronizado)
            } catch (e: Exception) {
                Log.e("ProyectoRepo", "Error subiendo proyecto a servidor", e)

                val proyectoLocal = proyecto.copy(isSynced = false)
                dao.insertar(proyectoLocal)
            }
        }
    }

    suspend fun unirseAProyecto(proyectoId: Int, userId: Int) {
        val body = mapOf("user_id" to userId)
        val response = api.unirseAProyecto(proyectoId, body)
        if (response.isSuccessful) {
            sincronizarDesdeServidor(userId)
        } else {
            Log.e("Unirse", "Fallo: ${response.code()}")
        }
    }


    suspend fun eliminar(proyecto: Proyecto) {
        withContext(ioDispatcher) {
            dao.eliminar(proyecto)
            try {
                api.eliminarProyecto(proyecto.id)
            } catch (e: Exception) {
                Log.e("ProyectoRepo", "Error eliminando proyecto en servidor", e)
            }
        }
    }

    suspend fun getNombrePorID(id: Int): UsuarioResponse {
        return api.obtenerUsuarioPorId(id)
        //return dao.obtenerNombreUsuario(id)
    }

    suspend fun sincronizarDesdeServidor(userId: Int) {
        withContext(ioDispatcher) {
            try {
                
                val proyectosRemotos = api.obtenerProyectosPorUsuario(userId)
                dao.insertarTodos(proyectosRemotos.map { it.toEntity() })

               
                val proyectoIdsUnidos: List<Int> = api.obtenerProyectoIdsPorUsuario(userId)

                Log.d("proyectoIdsUnidos", proyectoIdsUnidos.toString())

                proyectoIdsUnidos.forEach { proyectoId ->
                    try {
                        val proyectoUnido = api.obtenerProyectoPorId(proyectoId)
                        Log.d("proyectoUnido", proyectoUnido.toString())
                        dao.insertar(proyectoUnido.toEntity())
                    } catch (e: HttpException) {
                        Log.e(
                            "ProyectoRepo",
                            "HTTP al traer proyecto unido $proyectoId: ${e.code()} - ${e.message()}"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "ProyectoRepo",
                            "Error al traer proyecto unido $proyectoId: ${e.message}"
                        )
                    }
                }

            } catch (e: HttpException) {
                Log.e(
                    "ProyectoRepo",
                    "Error HTTP sincronizando proyectos: ${e.code()} - ${e.message()}"
                )
            } catch (e: Exception) {
                Log.e("ProyectoRepo", "Error general sincronizando proyectos: ${e.message}")
            }
        }
    }

    
    fun obtenerProyectosCreadosPorUsuario(userId: Int): Flow<List<Proyecto>> =
        dao.obtenerPorUsuario(userId)

   
    private fun proyectosUnidosDesdeApi(userId: Int): Flow<List<Proyecto>> = flow {
      
        val ids = api.obtenerProyectoIdsPorUsuario(userId)
        
        val lista = ids.mapNotNull { proyectoId ->
            try {
                api.obtenerProyectoPorId(proyectoId)
                    .toEntity()
                    .also { dao.insertar(it) }  
            } catch (e: Exception) {
                null
            }
        }
        emit(lista)
    }

    
    fun obtenerTodosLosProyectosDelUsuario(userId: Int): Flow<List<Proyecto>> =
        combine(
            obtenerProyectosCreadosPorUsuario(userId),
            proyectosUnidosDesdeApi(userId)
        ) { creados, unidos ->
            (creados + unidos)
                .distinctBy { it.id }
                .sortedByDescending { it.fechaCreacion }
        }


    private fun Proyecto.toRequest(): ProyectoRequest {
        return ProyectoRequest(
            nombre = this.nombre,
            descripcion = this.descripcion,
            id_creador = this.id_creador,
            fecha_creacion = this.fechaCreacion,
            tiempo_acumulado = this.tiempoAcumulado
        )
    }


}


