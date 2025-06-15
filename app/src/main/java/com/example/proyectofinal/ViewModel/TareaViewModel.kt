package com.example.proyectofinal.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.DataBase.Tarea.TareaDao
import com.example.proyectofinal.DataBase.Tarea.TareaRepository
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.Tareas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class TareaViewModel(
    private val repo: TareaRepository,
    private val proyectoId: Int,
    private val usuarioActualId: Int,
    private val tareaDao: TareaDao
) : ViewModel() {

    val proyecto = repo.getProyecto(proyectoId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val tareass = repo.getTareas(proyectoId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val todasLasTareass = repo.getTareasPorUsuarioDesdeApi(proyectoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val tareas: StateFlow<List<Tareas>> = tareaDao.getTodasTareas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    var todasLasTareas: StateFlow<List<Tareas>> = MutableStateFlow(emptyList())

    val tareasPropias = repo.getTareasPorUsuario(usuarioActualId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tareasDelProyecto(proyectoId: Int): StateFlow<List<Tareas>> =
        repo.getTareasDelProyecto(proyectoId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val tareasCompletas: StateFlow<List<Tareas>> =
        repo.getTodasLasTareasDelUsuarioIncluyendoUnidos(usuarioActualId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())






    fun sincronizarDesdeServidor() {
        viewModelScope.launch {
            repo.sincronizarDesdeServidor(usuarioActualId)
        }
    }

    fun sincronizarDesdeServidorProyecto(proyectoId: Int) {
        viewModelScope.launch {
            repo.sincronizarDesdeServidorProyecto(proyectoId)
        }
    }




    fun agregarTarea(
        proyectoId: Int,
        descripcion: String,
        prioridad: Int = 1,
        fechaInicio: LocalDate?,
        fechaFin: LocalDate?
    ) = viewModelScope.launch {
        val tarea = Tareas(
            id = 0,
            proyectoId = proyectoId,
            descripcion = descripcion,
            prioridad = prioridad,
            fechaInicio = fechaInicio
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli(),
            fechaFin = fechaFin
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )
        repo.agregarTarea(tarea)

    }

    fun cambiarEstado(t: Tareas) {
        viewModelScope.launch {
            repo.actualizarTarea(t.copy(completada = !t.completada))
        }
    }



    fun actualizarEstado(tarea: Tareas, completada: Boolean) {
        viewModelScope.launch {
            val nuevaTarea = tarea.copy(completada = completada) // crea nueva instancia
            repo.actualizarEstado(nuevaTarea)                    // guarda en Room y servidor
        }
    }



    fun borrarTarea(t: Tareas) {
        viewModelScope.launch {
            repo.borrarTarea(t)
        }
    }


}