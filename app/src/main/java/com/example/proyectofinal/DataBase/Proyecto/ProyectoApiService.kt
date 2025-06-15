package com.example.proyectofinal.DataBase.Proyecto

import android.media.tv.TsRequest
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.ProyectoRequest
import com.example.proyectofinal.Modelos.ProyectoResponse
import com.example.proyectofinal.Modelos.Usuario
import com.example.proyectofinal.Modelos.UsuarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProyectoApiService {

    @GET("proyectos/usuario/{id_creador}")
    suspend fun obtenerProyectosPorUsuario(@Path("id_creador") userId: Int): List<ProyectoResponse>

    @POST("proyectos")
    suspend fun crearProyecto(@Body proyectoRequest: ProyectoRequest): ProyectoResponse

    @DELETE("proyectos/{id}")
    suspend fun eliminarProyecto(@Path("id") id: Int)

    @POST("proyectos/{id}/unirse")
    suspend fun unirseAProyecto(
        @Path("id") id: Int,
        @Body body: Map<String, Int>
    ): Response<Void>

    @GET("proyectos/{id}")
    suspend fun obtenerProyecto(@Path("id") id: Int): ProyectoResponse

    // IDs de la tabla usuarios_proyectos
    @GET("usuario_proyecto/usuario/{userId}/proyectoIds")
    suspend fun obtenerProyectoIdsPorUsuario(@Path("userId") userId: Int): List<Int>

    // Proyecto individual por ID
    @GET("proyectos/{id}")
    suspend fun obtenerProyectoPorId(@Path("id") proyectoId: Int): ProyectoResponse

    @GET("usuarios/{id}/nombre")
    suspend fun obtenerUsuarioPorId(@Path("id") id: Int): UsuarioResponse

    @GET("usuarios/{uid}/nombre")
    suspend fun obtenerUsuarioPorUId(@Path("uid") uid: String): UsuarioResponse


}
