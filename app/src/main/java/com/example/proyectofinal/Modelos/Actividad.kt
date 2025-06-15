package com.example.proyectofinal.Modelos

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.OffsetDateTime

@Entity(tableName = "actividades")
data class Actividad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val proyectoId: Int,
    val userId: Int,
    val fecha: Long = System.currentTimeMillis(),
    val tiempoInicio: Long,
    val tiempoFin: Long,
    val duracion: Long
)

data class TiempoRequest(
    val tiempo_acumulado: Long
)

data class ActividadResponse(
    val id: Int,
    @SerializedName("proyecto_id") val proyectoId: Int,
    @SerializedName("user_id") val userId: Int, // <-- Añade esto
    val fecha: String,
    @SerializedName("tiempo_inicio") val tiempoInicio: Long,
    @SerializedName("tiempo_fin") val tiempoFin: Long,
    val duracion: Long
)

fun ActividadResponse.toEntity(): Actividad {
    return Actividad(
        id = id,
        proyectoId = proyectoId,
        userId = userId,
        fecha = OffsetDateTime.parse(fecha).toInstant().toEpochMilli(),
        tiempoInicio = tiempoInicio,
        tiempoFin = tiempoFin,
        duracion = duracion
    )
}


