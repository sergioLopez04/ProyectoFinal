package com.example.proyectofinal.Modelos

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "proyectos")
data class Proyecto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val id_creador: Int,
    //var members: List<String> = emptyList(),
    val fechaCreacion: Long = System.currentTimeMillis(),
    val tiempoAcumulado: Long = 0,
    val isSynced: Boolean = false
) {
    fun toEntity(): Proyecto {
        return Proyecto(
            id = this.id,
            nombre = this.nombre,
            descripcion = this.descripcion,
            id_creador = this.id_creador,
            fechaCreacion = this.fechaCreacion,
            tiempoAcumulado = this.tiempoAcumulado
        )
    }

}

data class ProyectoResponse(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val id_creador: Int,
    val members: List<String> = emptyList(),
    val fecha_creacion: Long = System.currentTimeMillis(),
    val tiempo_acumulado: Long = 0
) {
    fun toEntity(): Proyecto {
        return Proyecto(
            id = this.id,
            nombre = this.nombre,
            descripcion = this.descripcion,
            id_creador = this.id_creador,
            fechaCreacion = this.fecha_creacion,
            tiempoAcumulado = this.tiempo_acumulado
        )
    }
}

data class ProyectoRequest(
    val nombre: String,
    val descripcion: String,
    val id_creador: Int,
    val fecha_creacion: Long = System.currentTimeMillis(),
    val tiempo_acumulado: Long = 0
)

