package com.example.proyectofinal.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyectofinal.DataBase.Actividad.ActividadApiService
import com.example.proyectofinal.DataBase.Actividad.ActividadDao
import com.example.proyectofinal.DataBase.Actividad.ActividadRepositoty

class ActividadViewModelFactory(
    private val repository: ActividadRepositoty,
    private val dao: ActividadDao,
    private val api: ActividadApiService,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActividadViewModel::class.java)) {
            return ActividadViewModel(repository, dao, api, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
