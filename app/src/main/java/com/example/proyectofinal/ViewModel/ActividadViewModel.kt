package com.example.proyectofinal.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectofinal.DataBase.Actividad.ActividadRepositoty
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import com.example.proyectofinal.DataBase.Actividad.ActividadApiService
import com.example.proyectofinal.DataBase.Actividad.ActividadDao
import com.example.proyectofinal.DataBase.Actividad.millisToEndOfDay
import com.example.proyectofinal.DataBase.Actividad.millisToStartOfDay
import com.example.proyectofinal.Modelos.Actividad
import com.example.proyectofinal.Modelos.Proyecto
import kotlinx.coroutines.launch
import java.util.Date


class ActividadViewModel(
    private val repository: ActividadRepositoty,
    private val actividadDao: ActividadDao,
    private val api: ActividadApiService,
    userId: Int
) : ViewModel() {


    private val _fechaSeleccionada = MutableStateFlow(System.currentTimeMillis())

    val actividadesDiarias: StateFlow<List<Pair<Proyecto, Long>>> = _fechaSeleccionada
        .flatMapLatest { fecha ->
            repository.obtenerResumenDiario(fecha, userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Método para cambiar la fecha
    fun setFecha(fecha: Long) {
        _fechaSeleccionada.value = fecha
    }

    private val _userId = MutableStateFlow(0)

    fun setUserId(userId: Int) {
        _userId.value = userId
    }

    val actividadesDiariass: StateFlow<List<Pair<Proyecto, Long>>> = combine(
        _userId,
        _fechaSeleccionada
    ) { userId, fecha ->
        Triple(userId, fecha.millisToStartOfDay(), fecha.millisToEndOfDay())
    }.flatMapLatest { (userId, fecha) ->
        repository.obtenerResumenDiario(fecha, userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    suspend fun registrarActividad(
        proyecto: Proyecto,
        inicio: Long,
        fin: Long,
        userId: Int,
        duration: Long
    ) {
        if (fin <= inicio) return

        val actividad = Actividad(
            proyectoId = proyecto.id,
            userId = userId,
            tiempoInicio = inicio,
            tiempoFin = fin,
            duracion = duration
        )

        actividadDao.insertarActividad(actividad)

        Log.d("aaaaaaaaaaaaaaaaaa",actividad.toString());

        try {
            api.registrarActividad(
                ActividadApiService.ActividadRequest(
                    proyectoId = actividad.proyectoId,
                    user_id = userId,
                    fecha = Date(actividad.fecha).toInstant().toString(),
                    tiempoInicio = inicio,
                    tiempoFin = fin,
                    duracion = duration
                )
            )
        } catch (e: Exception) {
            Log.e("ActividadRepo", "Error al enviar actividad a servidor: ${e.message}")
        }
    }

}