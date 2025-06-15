package com.example.proyectofinal.View

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.DataBase.Usuario.ApiCliente
import com.example.proyectofinal.Modelos.Usuario
import com.example.proyectofinal.Modelos.UsuarioApiRequest
import com.example.proyectofinal.R
import com.example.proyectofinal.ViewModel.UsuarioViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegistroScreen(
    onBackToLogin: () -> Unit,
    viewModel: UsuarioViewModel = viewModel(),
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val usuarioApi = ApiCliente.apiService

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Registro", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Warning else Icons.Default.Info
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            if (password.length < 6) {
                                Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            } else {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val firebaseUser = auth.currentUser
                                            firebaseUser?.let {
                                                val nuevoUsuario = Usuario(
                                                    nombre = name,
                                                    email = email,
                                                    contraseña = password,
                                                    firebaseUid = it.uid
                                                )

                                                viewModel.guardarUsuario(nuevoUsuario)

                                                val usuarioApiRequest = UsuarioApiRequest(
                                                    nombre = name,
                                                    email = email,
                                                    contraseña = password,
                                                    firebase_uid = it.uid
                                                )

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val response = usuarioApi.registrarFirebaseUser(usuarioApiRequest)
                                                    withContext(Dispatchers.Main) {
                                                        if (response.isSuccessful) {
                                                            Toast.makeText(context, "Usuario registrado", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }

                                                onBackToLogin()
                                            }
                                        } else {
                                            Toast.makeText(context, "Error en la autenticación", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Por favor ingresa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registrar")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { onBackToLogin() }) {
                    Text("¿Ya tienes cuenta? Inicia sesión", color = Color.Gray)
                }
            }
        }
    }
}

