package com.example.proyectofinal.DataBase.Usuario

import com.example.proyectofinal.Modelos.Usuario
import com.example.proyectofinal.Modelos.UsuarioApiRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {


    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @GET("usuarios/email/{email}")
    suspend fun getUsuarioPorEmail(@Path("email") email: String): Response<Usuario>


    @POST("registrar-firebase-user")
    suspend fun registrarFirebaseUser(@Body request: UsuarioApiRequest): Response<Unit>

    @GET("usuarios/firebase/{firebase_uid}")
    suspend fun obtenerUsuarioPorFirebaseUid(@Path("firebase_uid") firebaseUid: String): Usuario



}
