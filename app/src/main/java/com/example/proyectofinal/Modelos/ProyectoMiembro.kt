package com.example.proyectofinal.Modelos

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "proyecto_miembro",
    primaryKeys = ["proyectoId", "userId"],
    foreignKeys = [
        ForeignKey(entity = Proyecto::class, parentColumns = ["id"], childColumns = ["proyectoId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["userId"])
    ]
)
data class ProyectoMiembro(
    val proyectoId: Int,
    val userId: Int
)
