package com.example.proyectofinal.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.DataBase.Chat.ChatRepository
import com.example.proyectofinal.DataBase.Usuario.UsuarioRepository
import com.example.proyectofinal.Modelos.Mensaje
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    repo: ChatRepository,
    projectId: String,
    usuarioRepo: UsuarioRepository
) : ViewModel() {

    val messages = repo.getMessages(projectId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNames: StateFlow<Map<String, String>> = _userNames

    init {
        viewModelScope.launch {
            messages.collectLatest { msgList ->
                val uids = msgList.map { it.authorId }.distinct()
                val names = mutableMapOf<String, String>()
                uids.forEach { uid ->
                    val nombre = usuarioRepo.obtenerNombrePorUid(uid) ?: "Desconocido"
                    names[uid] = nombre
                }
                _userNames.value = names
            }
        }
    }


    val repo = repo
    val projectId = projectId

    fun send(text: String) = viewModelScope.launch {
        repo.sendMessage(projectId, text)
    }

    val members = repo.getMembers(projectId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


}

class ChatVMFactory(
    private val projectId: String,
    private val usuarioRepo: UsuarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(ChatRepository(), projectId, usuarioRepo) as T
        }
        throw IllegalArgumentException("Unknown VM")
    }
}
