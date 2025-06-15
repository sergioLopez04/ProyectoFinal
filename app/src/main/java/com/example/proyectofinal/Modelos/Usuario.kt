package com.example.proyectofinal.Modelos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String = "",
    val email: String = "",
    val contraseña: String = "",
    val firebaseUid: String = ""
)

data class UsuarioApiRequest(
    val nombre: String,
    val email: String,
    val contraseña: String,
    val firebase_uid: String
)

data class UsuarioResponse(
    val nombre: String
)

