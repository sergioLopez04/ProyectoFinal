package com.example.proyectofinal.ViewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.example.proyectofinal.DataBase.AppDatabase
import com.example.proyectofinal.DataBase.RetrofitInstance
import com.example.proyectofinal.DataBase.Usuario.UsuarioRepository
import com.example.proyectofinal.Modelos.Usuario
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).usuarioDao()
    val apiUsuario = RetrofitInstance.apiUsuarioService
    private val repo = UsuarioRepository(dao, apiUsuario)


    fun insertar(usuario: Usuario) {
        viewModelScope.launch {
            repo.insertar(usuario)
        }
    }

    suspend fun obtenerUsuarioPorEmail(email: String): Usuario? {

        val remoto = repo.obtenerUsuarioPorEmail(email)

        return remoto
    }

    suspend fun obtenerUsuarioPorEmail2(firebaseUid: String): Int? {
        return try {
            val remoto = apiUsuario.obtenerUsuarioPorFirebaseUid(firebaseUid)
            remoto.id
        } catch (e: HttpException) {
            if (e.code() == 404) {
                null // Usuario no existe aún
            } else {
                throw e // Otros errores los relanzamos
            }
        }
    }



    fun guardarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repo.guardarUsuario(usuario)
        }
    }

}
