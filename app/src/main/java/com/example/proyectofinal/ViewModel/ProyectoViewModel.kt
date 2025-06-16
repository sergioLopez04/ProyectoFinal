package com.example.proyectofinal.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.proyectofinal.DataBase.Proyecto.ProyectoRepository
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.ProyectoRequest
import com.example.proyectofinal.Modelos.Usuario
import com.example.proyectofinal.Modelos.UsuarioResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProyectoViewModel(
    private val usuarioActualId: Int,
    private val repository: ProyectoRepository
) : ViewModel() {


    /*val proyectosFlow: Flow<List<Proyecto>> =
        repository.obtenerProyectosPorUsuario(usuarioActualId)*/


    val proyectosFlow: StateFlow<List<Proyecto>> =
        repository.obtenerTodosLosProyectosDelUsuario(usuarioActualId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )





    private val _mensajeUi = MutableStateFlow<String?>(null)
    val mensajeUi: StateFlow<String?> = _mensajeUi




    suspend fun getNombreCreador(id: Int): String {


        // Si no, lo pedimos al servidor y lo guardamos
        return try {
            val usuario = repository.getNombrePorID(id)
            usuario.nombre
        } catch (e: Exception) {
            "Desconocido"
        }
    }

    fun resetMensaje() {
        _mensajeUi.value = null
    }

    private val _proyectos = MutableStateFlow<List<Proyecto>>(emptyList())
    val proyectos = _proyectos.asStateFlow()




    fun insertar(proyecto: Proyecto) {
        viewModelScope.launch {
            if (usuarioActualId <= 0) {
                _mensajeUi.value = "Error: ID del usuario no válido"
                return@launch
            }

            try {
                repository.insertar(proyecto.copy(id_creador = usuarioActualId))
                _mensajeUi.value = "Proyecto creado correctamente"
            } catch (e: Exception) {
                _mensajeUi.value = "Error al insertar proyecto: ${e.localizedMessage}"
            }
        }
    }



    fun eliminar(proyecto: Proyecto) {
        viewModelScope.launch {

            if (proyecto.id_creador == usuarioActualId) {
                repository.eliminar(proyecto)
            }
        }
    }

    fun sincronizarDesdeServidor() {
        viewModelScope.launch {
            repository.sincronizarDesdeServidor(usuarioActualId)
        }
    }

    fun unirseAProyecto(proyectoId: Int, userId: Int) {
        viewModelScope.launch {
            repository.unirseAProyecto(proyectoId, userId)
        }
    }









}

