package com.example.proyectofinal

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.proyectofinal.DataBase.Actividad.ActividadRepositoty
import com.example.proyectofinal.DataBase.AppDatabase
import com.example.proyectofinal.DataBase.Proyecto.ProyectoRepository
import com.example.proyectofinal.DataBase.RetrofitInstance
import com.example.proyectofinal.DataBase.Tarea.TareaRepository
import com.example.proyectofinal.DataBase.Usuario.UsuarioRepository
import com.example.proyectofinal.View.ActividadPorDiaScreen
import com.example.proyectofinal.View.ActividadScreen
import com.example.proyectofinal.View.ChatScreen
import com.example.proyectofinal.View.InicioScreen
import com.example.proyectofinal.View.InvitacionesScreen
import com.example.proyectofinal.View.LoginScreen
import com.example.proyectofinal.View.PantallaDetalleProyecto
import com.example.proyectofinal.View.Pantallas
import com.example.proyectofinal.View.RegistroScreen
import com.example.proyectofinal.View.TareasScreen
import com.example.proyectofinal.View.UserAvatar
import com.example.proyectofinal.ViewModel.ActividadViewModel
import com.example.proyectofinal.ViewModel.ProyectoViewModel
import com.example.proyectofinal.ViewModel.TareaViewModel
import com.example.proyectofinal.ViewModel.TareaViewModelFactory
import com.example.proyectofinal.ui.theme.ProyectoFinalTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            ProyectoFinalTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                var yaNavego by rememberSaveable { mutableStateOf(false) }

                // Repositorio de usuario
                val apiUsuario = RetrofitInstance.apiUsuarioService
                val usuarioRepo = UsuarioRepository(
                    AppDatabase.getDatabase(context).usuarioDao(),
                    apiUsuario
                )

                val db = AppDatabase.getDatabase(context)
                val apiProyecto = RetrofitInstance.apiProyectoService
                val proyectoRepo = ProyectoRepository(
                    dao = db.proyectoDao(),
                    api = apiProyecto
                )

                // 👉 Dynamic Link
                LaunchedEffect(Unit) {
                    Log.d("DeepLink", "Verificando si hay deep link...")
                    FirebaseDynamicLinks.getInstance()
                        .getDynamicLink(intent)
                        .addOnSuccessListener { pendingDynamicLinkData ->
                            val deepLink: Uri? = pendingDynamicLinkData?.link
                            Log.d("DeepLink", "Enlace recibido: $deepLink")
                            val pathSegments = deepLink?.pathSegments
                            Log.d("Auth", "${pathSegments?.firstOrNull()}")
                            if (deepLink != null && pathSegments?.firstOrNull() == "chat") {

                                val projectId = pathSegments.getOrNull(1)

                                if (auth.currentUser != null && projectId != null && !yaNavego) {
                                    Log.d("aqui llega??", "${yaNavego}")
                                    yaNavego = true
                                    scope.launch {
                                        try {
                                            val userId =
                                                usuarioRepo.obtenerIdNumerico(auth.currentUser!!.uid)
                                            Log.d("Authh", "Usuario: ${userId}")

                                            // Guardar como pendiente
                                            val prefs = context.getSharedPreferences("prefs", MODE_PRIVATE)
                                            prefs.edit()
                                                .putInt("user_numeric_id", userId)
                                                .putBoolean("viene_de_enlace", true)
                                                .putString("proyecto_actual_id", projectId)
                                                .apply()

                                            proyectoRepo.unirseAProyecto(projectId.toInt(), userId)

                                            // Navegar a main y luego al chat
                                            navController.navigate("main/$userId/chat/$projectId") {
                                                popUpTo("login") { inclusive = true }
                                            }


                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Error uniendo al proyecto", e)
                                            auth.signOut()
                                            navController.navigate("login")
                                        }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Log.e("MainActivity", "No se pudo obtener el enlace dinámico", it)
                        }
                }

                // 👉 Si ya está logueado, navegar directamente
                LaunchedEffect(auth.currentUser) {
                    delay(50)
                    val user = auth.currentUser
                    if (user != null && !yaNavego) {
                        yaNavego = true
                        try {
                            val userId = usuarioRepo.obtenerIdNumerico(user.uid)
                            val prefs = context.getSharedPreferences("prefs", MODE_PRIVATE)
                            prefs.edit().putInt("user_numeric_id", userId).apply()

                            navController.navigate("main/$userId/inicio") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            auth.signOut()
                            navController.navigate("login")
                        }
                    }
                }



                // 👉 UI
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHostApp(navController)
                }
            }
        }

    }
}


@Composable
fun NavHostApp(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Login con posible redirección a chat
        composable("login")
        {
            val api = RetrofitInstance.apiUsuarioService
            LoginScreen(navController, api)

        }

        composable("registro")
        {
            RegistroScreen(
                onBackToLogin = { navController.navigate("login") }
            )
        }

        composable(
            "main/{userId}/inicio?reload={reload}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("reload") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo = ActividadRepositoty(db.actividadDao(), apiActividad)
            MainScreen(userId, repo, navController, "inicio")
        }

        composable("main/{userId}/proyecto/{projectId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull()

            if (userId != null && projectId != null) {
                val userId = backStackEntry.arguments!!.getInt("userId")
                val context = LocalContext.current
                val db = AppDatabase.getDatabase(context)
                val apiActividad = RetrofitInstance.apiActividadService
                val repo = ActividadRepositoty(db.actividadDao(), apiActividad)
                MainScreen(userId, repo, navController, "inicio")
            }
        }


        // Pantallas principales
        composable(
            "main/{userId}/inicio",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            val userId = it.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo = ActividadRepositoty(db.actividadDao(), apiActividad)
            MainScreen(userId, repo, navController, "inicio")
        }

        composable(
            "main/{userId}/tareas",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            val userId = it.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo = ActividadRepositoty(db.actividadDao(), apiActividad)
            MainScreen(userId, repo, navController, "tareas")
        }

        composable(
            "main/{userId}/chat",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            val userId = it.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo = ActividadRepositoty(db.actividadDao(), apiActividad)
            MainScreen(userId, repo, navController, "chat")
        }

        composable(
            "main/{userId}/actividad",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            val userId = it.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo = ActividadRepositoty(db.actividadDao(), apiActividad)
            MainScreen(userId, repo, navController, "actividad")
        }

        composable(
            "detalleProyecto/{userId}/{proyectoId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("proyectoId") { type = NavType.IntType }
            )
        )
        {
            val proyectoId = it.arguments!!.getInt("proyectoId")
            val userId = it.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo = ActividadRepositoty(db.actividadDao(), apiActividad)
            val apiTarea = RetrofitInstance.apiTareasService
            val apiProyecto = RetrofitInstance.apiProyectoService
            val repo3 = ProyectoRepository(db.proyectoDao(), apiProyecto)

            val repo2 = TareaRepository(db.proyectoDao(), db.tareaDao(), apiTarea, apiProyecto)
            PantallaDetalleProyecto(
                proyectoId,
                userId,
                ActividadViewModel(repo, db.actividadDao(), apiActividad, userId),
                TareaViewModel(repo2, proyectoId, userId, db.tareaDao()), repo3
            )
        }

        composable(
            "actividad/{proyectoId}/{userId}",
            arguments = listOf(
                navArgument("proyectoId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType }
            )
        ) {
            val proyectoId = it.arguments!!.getInt("proyectoId")
            val userId = it.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo =
                ActividadRepositoty(AppDatabase.getDatabase(context).actividadDao(), apiActividad)
            Log.d("userrrr", userId.toString());
            ActividadPorDiaScreen(repo, db.actividadDao(), apiActividad, userId)
        }

        composable(
            route = "main/{userId}/invitaciones",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            val userId = it.arguments!!.getInt("userId")
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val apiProyecto = RetrofitInstance.apiProyectoService
            val repo3 = ProyectoRepository(db.proyectoDao(), apiProyecto)
            InvitacionesScreen(currentUserId = userId, navController = navController, ProyectoViewModel(userId, repo3))
        }

        // ChatScreen desde dynamic link
        composable(
            route = "chat/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "myapp://chat/{projectId}" }
            )
        ) { back ->
            val projectId = back.arguments!!.getString("projectId")!!
            val context = LocalContext.current
            val apiUsuario = RetrofitInstance.apiUsuarioService
            val usuarioRepo = UsuarioRepository(
                AppDatabase.getDatabase(context).usuarioDao(),
                apiUsuario
            )
            ChatScreen(projectId = projectId, navController = navController, repo = usuarioRepo)
        }

        // ChatScreen desde app (usuario ya logueado)
        composable(
            route = "main/{userId}/chat/{proyectoId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("proyectoId") { type = NavType.StringType }
            )
        ) { back ->
            val context = LocalContext.current
            val apiUsuario = RetrofitInstance.apiUsuarioService
            val usuarioRepo = UsuarioRepository(
                AppDatabase.getDatabase(context).usuarioDao(),
                apiUsuario
            )
            ChatScreen(projectId = back.arguments!!.getString("proyectoId")!!, navController = navController, repo = usuarioRepo)
        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userId: Int,
    actividadRepository: ActividadRepositoty,
    navController: NavHostController,
    currentRoute: String
) {
    val items = listOf(
        Pantallas("inicio", "Inicio", Icons.Default.List),
        Pantallas("tareas", "Tareas", Icons.Default.CheckCircle),
        Pantallas("chat", "Chat", Icons.Default.AccountBox),
        Pantallas("actividad", "Actividad", Icons.Default.Info)
    )

    val currentScreen = items.find { currentRoute.endsWith(it.route) } ?: items[0]

    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var showDialog by remember { mutableStateOf(false) }
    var mensajeDialogo by remember { mutableStateOf("") }
    var dndActive by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    if (auth.currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") { popUpTo(0) }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentScreen.title) },
                actions = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val initial = user?.displayName
                        ?.trim()
                        ?.firstOrNull()
                        ?.uppercaseChar()
                        ?.toString()
                        ?: user?.email
                            ?.firstOrNull()
                            ?.uppercaseChar()
                            ?.toString()
                        ?: "U"

                    UserAvatar(initial, { showMenu = true })
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") { popUpTo(0) }
                            },
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )


                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                title = { Text("Estado actualizado") },
                                text = { Text(mensajeDialogo) },
                                confirmButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("OK")
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute.endsWith(screen.route),
                        onClick = {
                            navController.navigate("main/$userId/${screen.route}") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("main/$userId/${screen.route}") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(Modifier.padding(innerPadding)) {
            val context = LocalContext.current
            val api = RetrofitInstance.apiTareasService
            val apiPro = RetrofitInstance.apiProyectoService
            val db = AppDatabase.getDatabase(context)
            val apiActividad = RetrofitInstance.apiActividadService
            val repo = TareaRepository(AppDatabase.getDatabase(context).proyectoDao(), AppDatabase.getDatabase(context).tareaDao(), api, apiPro)
            val apiProyecto = RetrofitInstance.apiProyectoService
            val repo3 = ProyectoRepository(db.proyectoDao(), apiProyecto)

            when (currentScreen.route) {
                "inicio" -> InicioScreen(userId = userId, navController = navController)
                "tareas" -> {
                    val tareaViewModel: TareaViewModel = viewModel(
                        factory = TareaViewModelFactory(
                            repo,
                            0,
                            userId,
                            AppDatabase.getDatabase(context).tareaDao()
                        )
                    )
                    TareasScreen(tareaViewModel, userId)
                }

                "chat" -> InvitacionesScreen(currentUserId = userId, navController = navController,ProyectoViewModel(userId, repo3))
                "actividad" -> ActividadScreen(
                    repository = actividadRepository,
                    navController = navController,
                    dao = db.actividadDao(),
                    api = apiActividad,
                    userId = userId
                )
            }
        }
    }
}