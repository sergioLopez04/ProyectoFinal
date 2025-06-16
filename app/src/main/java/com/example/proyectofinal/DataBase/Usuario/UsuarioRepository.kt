package com.example.proyectofinal.DataBase.Usuario

import android.util.Log
import com.example.proyectofinal.DataBase.Proyecto.TareasApiService
import com.example.proyectofinal.DataBase.UsuarioDao
import com.example.proyectofinal.Modelos.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao, private val api: ApiService,) {

    suspend fun insertar(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    suspend fun obtenerUsuarioPorEmail(email: String): Usuario? {
        val response = api.getUsuarioPorEmail(email)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }


    suspend fun guardarUsuario(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    suspend fun obtenerIdNumerico(firebaseUid: String): Int {
        
        val remoto = api.obtenerUsuarioPorFirebaseUid(firebaseUid)
        Log.d("usuarioRepo", "Remoto desde API: $remoto")

        usuarioDao.insertar(remoto) 
        return remoto.id
    }

    suspend fun obtenerNombrePorUid(uid: String): String? {
        return api.obtenerUsuarioPorFirebaseUid(uid)?.nombre
    }



}
