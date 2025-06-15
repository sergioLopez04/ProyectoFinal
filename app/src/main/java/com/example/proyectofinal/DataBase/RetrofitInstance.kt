package com.example.proyectofinal.DataBase

import com.example.proyectofinal.DataBase.Actividad.ActividadApiService
import com.example.proyectofinal.DataBase.Proyecto.ApiProyecto
import com.example.proyectofinal.DataBase.Proyecto.ProyectoApiService
import com.example.proyectofinal.DataBase.Proyecto.TareasApiService
import com.example.proyectofinal.DataBase.Usuario.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://apiproyectofinal.onrender.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiTareasService: TareasApiService by lazy {
        retrofit.create(TareasApiService::class.java)
    }

    val apiActividadService: ActividadApiService by lazy {
        retrofit.create(ActividadApiService::class.java)
    }

    val apiUsuarioService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val apiProyectoService: ProyectoApiService by lazy {
        retrofit.create(ProyectoApiService::class.java)
    }

}
