package com.example.proyectofinal.DataBase.Proyecto

import android.media.tv.TsRequest
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.ProyectoRequest
import com.example.proyectofinal.Modelos.ProyectoResponse
import com.example.proyectofinal.Modelos.TareaRequest
import com.example.proyectofinal.Modelos.TareaResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TareasApiService {

    @GET("proyectos/{id}/tareas")
    suspend fun obtenerTareasDelProyecto(@Path("id") proyectoId: Int): List<TareaResponse>

    @GET("usuarios/{userId}/tareas")
    suspend fun obtenerTareasDelUsuario(@Path("userId") userId: Int): List<TareaResponse>

    @PUT("tareas/{id}/estado")
    suspend fun actualizarEstadoTarea(
        @Path("id") id: Int,
        @Body estado: EstadoRequest
    )

    data class EstadoRequest(val completada: Boolean)



    @GET("tareas/{id_creador}")
    suspend fun obtenerTareasPorProyecto(@Path("id_creador") userId: Int): List<TareaResponse>

    @POST("tareas")
    suspend fun crearTarea(@Body tareaRequest: TareaRequest): TareaResponse

    @DELETE("tareas/{id}")
    suspend fun eliminarProyecto(@Path("id") id: Int)
}
