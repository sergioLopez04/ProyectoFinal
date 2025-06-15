package com.example.proyectofinal.Modelos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tareas(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val proyectoId: Int,
    val descripcion: String,
    val completada: Boolean = false,
    val prioridad: Int = 1,
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null,
    val isSynced: Boolean = false
)

data class TareaRequest(
    val proyectoId: Int,
    val descripcion: String,
    val completada: Boolean = false,
    val prioridad: Int = 1,
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null,
    val isSynced: Boolean = false
)

data class TareaResponse(
    val id: Int,
    val proyectoId: Int,
    val descripcion: String,
    val completada: Boolean,
    val prioridad: Int,
    val fechaInicio: Long?,
    val fechaFin: Long?
){
    fun toEntity(): Tareas {
        return Tareas(
            id = this.id,
            proyectoId = this.proyectoId,
            descripcion = this.descripcion,
            completada = this.completada,
            prioridad = this.prioridad,
            fechaInicio = this.fechaInicio,
            fechaFin = this.fechaFin
        )
    }
}


