package com.example.proyectofinal.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyectofinal.DataBase.AppDatabase
import com.example.proyectofinal.DataBase.Proyecto.ApiProyecto
import com.example.proyectofinal.DataBase.Proyecto.ProyectoApiService
import com.example.proyectofinal.DataBase.Proyecto.ProyectoRepository

class ProyectoViewModelFactory(
    private val userId: Int,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dao = AppDatabase.getDatabase(application).proyectoDao()
        val api = ApiProyecto.apiService

        val repository = ProyectoRepository(
            dao = dao,
            api = api
        )
        @Suppress("UNCHECKED_CAST")
        return ProyectoViewModel(userId, repository) as T
    }
}