package com.example.proyectofinal.View

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.proyectofinal.R
import com.example.proyectofinal.ViewModel.UsuarioViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.google.android.gms.tasks.Task
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.draw.clip
import com.example.proyectofinal.DataBase.Proyecto.ProyectoApiService
import com.example.proyectofinal.DataBase.Usuario.ApiCliente
import com.example.proyectofinal.DataBase.Usuario.ApiService
import com.example.proyectofinal.Modelos.Usuario
import com.example.proyectofinal.Modelos.UsuarioApiRequest


@Composable
fun LoginScreen(
    navController: NavHostController,
    api: ApiService,
    viewModel: UsuarioViewModel = viewModel(),
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        prefs.edit()
            .remove("viene_de_enlace")
            .remove("proyecto_actual_id")
            .apply()
    }

    val auth = FirebaseAuth.getInstance()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.uid?.let { uid ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val usuarioExistente = viewModel.obtenerUsuarioPorEmail2(uid)

                            withContext(Dispatchers.Main) {
                                Log.d("usuarioExistente", usuarioExistente?.toString() ?: "null")

                                if (usuarioExistente != null) {
                                    navController.navigate("main/${usuarioExistente}/inicio") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    Log.d("llga","llga")
                                    val usuario = Usuario(
                                        nombre = (firebaseUser.displayName ?: firebaseUser.email).toString(),
                                        email = firebaseUser.email ?: "",
                                        contraseña = "google",
                                        firebaseUid = uid
                                    )

                                    viewModel.guardarUsuario(usuario)

                                    val nuevoUsuario = UsuarioApiRequest(
                                        nombre = (firebaseUser.displayName ?: firebaseUser.email).toString(),
                                        email = firebaseUser.email ?: "",
                                        contraseña = "google",
                                        firebase_uid = uid
                                    )



                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            ApiCliente.apiService.registrarFirebaseUser(nuevoUsuario)
                                        }

                                        Log.d("response_code", response.code().toString())
                                        Log.d("response_body", response.body().toString())

                                        if (response.isSuccessful) {
                                            val usuarioRegistrado = response.body()
                                            Toast.makeText(context, "Bienvenido ${firebaseUser.displayName}", Toast.LENGTH_SHORT).show()

                                            val idnuevoUsuario = api.obtenerUsuarioPorFirebaseUid(nuevoUsuario.firebase_uid)
                                            navController.navigate("main/${idnuevoUsuario.id}/inicio") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            val errorMsg = response.errorBody()?.string()
                                            Log.e("API_ERROR", "Error ${response.code()}: $errorMsg")
                                            Toast.makeText(context, "Error registrando usuario", Toast.LENGTH_SHORT).show()
                                        }

                                    } catch (e: Exception) {
                                        Log.e("EXCEPTION", "Excepción al registrar usuario", e)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Error de red o configuración", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Error autenticando con Google", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: ApiException) {
            Toast.makeText(context, "Google falló", Toast.LENGTH_LONG).show()
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("98202319289-k2rlf8rhhltvug8vbpo0054ubfkm4a46.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            launcher.launch(googleSignInClient.signInIntent)
        }
        Log.d("googleSignInClient", googleSignInClient.toString())
    }


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


                Image(
                    painter = painterResource(id = R.drawable.gestiondeproyectos),
                    contentDescription = "Logo del chat",
                    modifier = Modifier
                        .size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Login", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    if (email.isNotEmpty()) {
                        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Introduce tu correo", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("¿Olvidaste tu contraseña?", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = auth.currentUser
                                    firebaseUser?.let {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val usuario = viewModel.obtenerUsuarioPorEmail(email)
                                            withContext(Dispatchers.Main) {
                                                if (usuario != null) {
                                                    navController.navigate("main/${usuario.id}/inicio") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val errorMsg = task.exception?.localizedMessage ?: "Error de autenticación"
                                    Toast.makeText(context, "Inicio de sesión fallido", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Email y contraseña obligatorios", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iniciar sesión")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { launchGoogleSignIn() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Logo del chat",
                        modifier = Modifier
                            .size(30.dp)

                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar sesión con Google", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { navController.navigate("registro") }) {
                    Text("¿No tienes cuenta? Regístrate", color = Color.Gray)
                }
            }
        }
    }
}