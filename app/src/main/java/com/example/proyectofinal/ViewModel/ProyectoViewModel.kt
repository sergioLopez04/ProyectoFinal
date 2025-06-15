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

/*

class ProyectoViewModel(application: Application) : AndroidViewModel(application) {
    private val _proyectos = MutableStateFlow<List<ProyectoResponse>>(emptyList())
    val proyectosFlow: StateFlow<List<ProyectoResponse>> = _proyectos

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun cargarProyectos(userId: Int) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerProyectosPorUsuario(userId)
                _proyectos.value = response.map { it.toDomainModel() }
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al cargar proyectos", e)
            }
        }
    }

    fun insertar(proyecto: ProyectoResponse) {
        coroutineScope.launch {
            try {
                val request = ProyectoRequest(
                    nombre = proyecto.nombre,
                    descripcion = proyecto.descripcion,
                    id_creador = proyecto.id_creador
                )
                val response = RetrofitClient.instance.crearProyecto(request)
                cargarProyectos(proyecto.id_creador)
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al crear proyecto", e)
            }
        }
    }

    fun eliminar(proyecto: ProyectoResponse) {
        coroutineScope.launch {
            try {
                RetrofitClient.instance.eliminarProyecto(proyecto.id ?: 0)
                cargarProyectos(proyecto.id_creador)
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al eliminar proyecto", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }

    private fun ProyectoResponse.toDomainModel(): ProyectoResponse {
        return ProyectoResponse(
            id = this.id,
            nombre = this.nombre,
            descripcion = this.descripcion,
            id_creador = this.id_creador,
            members = this.members,
            firestore_id = this.firestore_id ?: "",
            fecha_creacion = this.fecha_creacion,
            tiempo_acumulado = this.tiempo_acumulado
        )
    }
}*/