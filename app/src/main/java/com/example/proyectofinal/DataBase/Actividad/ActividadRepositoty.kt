package com.example.proyectofinal.DataBase.Actividad

import android.util.Log
import com.example.proyectofinal.DataBase.Proyecto.TareasApiService
import com.example.proyectofinal.Modelos.Actividad
import com.example.proyectofinal.Modelos.ActividadResponse
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.TiempoRequest
import com.example.proyectofinal.Modelos.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.Instant
import java.util.Calendar


class ActividadRepositoty(
    private val dao: ActividadDao,
    private val api: ActividadApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {


    suspend fun registrarActividad(actividad: Actividad) {
        val duracion = actividad.tiempoFin - actividad.tiempoInicio

        // Guarda localmente
        dao.actualizarTiempo(actividad.proyectoId, duracion)
        dao.insertarActividad(
            Actividad(
                proyectoId = actividad.proyectoId,
                userId = actividad.userId,
                tiempoInicio = actividad.tiempoInicio,
                tiempoFin = actividad.tiempoFin,
                duracion = duracion
            )
        )

        // Ahora envía al servidor
        try {
            val response = api.actualizarTiempoAcumulado(
                actividad.proyectoId,
                TiempoRequest(tiempo_acumulado = duracion)
            )
            if (response.isSuccessful) {
                Log.d("ActividadRepo", "Tiempo actualizado en servidor")
            } else {
                Log.e("ActividadRepo", "Error al actualizar tiempo: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ActividadRepo", "Error actualizando tiempo en servidor", e)
        }
    }

    suspend fun obtenerActividadesConsolidadas(userId: Int): List<Actividad> {
        return withContext(ioDispatcher) {
            // Primero sincronizar
            sincronizarDesdeServidor(userId)

            // Luego obtener de BD local (que ahora está actualizada)
            dao.obtenerActividadesPorUsuario(userId)
        }
    }

    suspend fun sincronizarDesdeServidor(userId: Int) {
        withContext(ioDispatcher) {
            try {
                val actividadesRemotas: List<ActividadResponse> = api.obtenerActividadesDelUsuario(userId)
                val entidades: List<Actividad> = actividadesRemotas.map { it.toEntity() }


                dao.insertarTodos(entidades)
                Log.i("ActividadRepo", "Sincronización exitosa: ${entidades.size} actividades actualizadas")
            } catch (e: HttpException) {
                Log.e("ActividadRepo", "Error HTTP sincronizando: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                Log.e("ActividadRepo", "Error general: ${e.message}")
            }
        }
    }

    suspend fun sincronizarDesdeServidore(userId: Int) {
        withContext(ioDispatcher) {
            try {
                val actividadesRemotas = api.obtenerActividadesDelUsuario(userId)
                val actividadesLocales = dao.obtenerActividadesPorUsuario(userId)

                // Convertir a mapas para acceso rápido
                val mapaRemotas = actividadesRemotas.associateBy { it.id }
                val mapaLocales = actividadesLocales.associateBy { it.id }

                // 1. Actualizar registros existentes
                actividadesLocales.forEach { local ->
                    mapaRemotas[local.id]?.let { remota ->
                        // Solo actualizar si son diferentes
                        if (local != remota.toEntity()) {
                            dao.actualizarActividad(remota.toEntity())
                        }
                    }
                }

                val nuevasActividades = actividadesRemotas.filter { !mapaLocales.containsKey(it.id) }
                dao.insertarTodos(nuevasActividades.map { it.toEntity() })


            } catch (e: Exception) {
                Log.e("ActividadRepo", "Error sincronizando: ${e.message}")
                // Considera relanzar el error o manejar reintentos
            }
        }
    }



    fun obtenerResumenDiariooo(fecha: Long, userId: Int): Flow<List<Pair<Proyecto, Long>>> {
        val inicio = fecha.millisToStartOfDay()
        val fin = fecha.millisToEndOfDay()
        return dao.obtenerResumenDiario(userId, inicio, fin)
            .map { list -> list.map { Pair(it.proyecto, it.tiempo) } }
    }

    fun obtenerResumenDiario(fecha: Long, userId: Int): Flow<List<Pair<Proyecto, Long>>> {
        val inicio = fecha.millisToStartOfDay()
        val fin = fecha.millisToEndOfDay()
        return dao.obtenerResumenDiario(userId, inicio, fin)
            .map { list -> list.map { it.proyecto to it.tiempo } } // <- aquí está bien
    }




    fun obtenerTiempoPorProyectoYDia(proyectoId: Int, diaTimestamp: Long): Flow<Long> {
        val inicio = diaTimestamp.millisToStartOfDay()
        val fin = diaTimestamp.millisToEndOfDay()
        return dao.obtenerTiempoProyectoDia(proyectoId, inicio, fin)
    }

   /* fun obtenerResumenPorDia(fecha: Long): Flow<List<ActividadDao.ProyectoConTiempo>> {
        val inicio = fecha.millisToStartOfDay()
        val fin = fecha.millisToEndOfDay()
        return dao.obtenerResumenDiario(inicio, fin)
    }*/


}



// Extensión para fechas
fun Long.millisToStartOfDay(): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@millisToStartOfDay }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun Long.millisToEndOfDay(): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@millisToEndOfDay }
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}