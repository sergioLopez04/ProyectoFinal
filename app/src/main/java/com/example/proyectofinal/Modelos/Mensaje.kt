package com.example.proyectofinal.Modelos

data class Mensaje(
    val id: String = "",
    val authorId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

