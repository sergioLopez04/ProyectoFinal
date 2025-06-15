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
        // ❌ Ignorar local si es el primer login, o si estás sincronizando
        val remoto = api.obtenerUsuarioPorFirebaseUid(firebaseUid)
        Log.d("usuarioRepo", "Remoto desde API: $remoto")

        usuarioDao.insertar(remoto) // 🔁 Esto también actualiza el local si ya existe
        return remoto.id
    }

    suspend fun obtenerNombrePorUid(uid: String): String? {
        // Llama a tu API o base de datos local/remota
        // Ejemplo si usás Retrofit o Room:
        return api.obtenerUsuarioPorFirebaseUid(uid)?.nombre
    }



}
