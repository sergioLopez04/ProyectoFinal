package com.example.proyectofinal.DataBase

import androidx.room.*
import com.example.proyectofinal.Modelos.Usuario

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun obtenerPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE email = :email AND contraseña = :contraseña LIMIT 1")
    suspend fun verificarCredenciales(email: String, contraseña: String): Usuario?

    @Query("SELECT id FROM usuarios WHERE firebaseUid = :firebaseUid")
    suspend fun obtenerIdPorFirebaseUid(firebaseUid: String): Int?


}
