package com.example.proyectofinal.DataBase.Actividad

import com.example.proyectofinal.Modelos.ActividadResponse
import com.example.proyectofinal.Modelos.ProyectoResponse
import com.example.proyectofinal.Modelos.TareaRequest
import com.example.proyectofinal.Modelos.TareaResponse
import com.example.proyectofinal.Modelos.TiempoRequest
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ActividadApiService {

    @POST("proyectos/{id}/actualizar-tiempo")
    suspend fun actualizarTiempoAcumulado(
        @Path("id") proyectoId: Int,
        @Body tiempo: TiempoRequest
    ): Response<Unit>

    @POST("actividades")
    suspend fun registrarActividad(@Body actividad: ActividadRequest): Response<Unit>

    data class ActividadRequest(
        @SerializedName("proyecto_id") val proyectoId: Int,
        val user_id: Int,
        val fecha: String,
        val duracion: Long,
        @SerializedName("tiempo_inicio") val tiempoInicio: Long,
        @SerializedName("tiempo_fin") val tiempoFin: Long
    )

    @GET("actividades")
    suspend fun obtenerActividadesDelUsuario(
        @Query("userId") userId: Int
    ): List<ActividadResponse>






}