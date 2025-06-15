package com.example.proyectofinal.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyectofinal.DataBase.Tarea.TareaDao
import com.example.proyectofinal.DataBase.Tarea.TareaRepository

class TareaViewModelFactory(
    private val repo: TareaRepository,
    private val proyectoId: Int,
    private val userId: Int,
    private val dao: TareaDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TareaViewModel(repo, proyectoId, userId, dao) as T
    }
}
