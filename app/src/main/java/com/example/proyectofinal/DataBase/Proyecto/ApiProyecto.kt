package com.example.proyectofinal.DataBase.Proyecto

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiProyecto {
    private const val BASE_URL = "https://apiproyectofinal.onrender.com/api/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ProyectoApiService by lazy {
        retrofit.create(ProyectoApiService::class.java)
    }
}