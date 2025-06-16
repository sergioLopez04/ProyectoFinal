package com.example.proyectofinal.View

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.proyectofinal.DataBase.AppDatabase
import com.example.proyectofinal.DataBase.Chat.ChatRepository
import com.example.proyectofinal.DataBase.RetrofitInstance
import com.example.proyectofinal.DataBase.Usuario.UsuarioRepository
import com.example.proyectofinal.ViewModel.ChatVMFactory
import com.example.proyectofinal.ViewModel.ChatViewModel
import com.example.proyectofinal.ViewModel.ProyectoViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.shortLinkAsync
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(projectId: String, navController: NavController, repo: UsuarioRepository) {
    val vm: ChatViewModel = viewModel(factory = ChatVMFactory(projectId, repo))
    val msgs by vm.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userNames by vm.userNames.collectAsState()



    BackHandler {
       
        navController.popBackStack()

        
        val firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUserId != null) {

            scope.launch {
                val user = repo.obtenerIdNumerico(firebaseUserId)

               
                val timestamp = System.currentTimeMillis()
                navController.navigate("main/$user/inicio?reload=$timestamp") {
                    launchSingleTop = true
                }


                prefs.edit().remove("viene_de_enlace").apply()
            }
        }
    }







    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat del proyecto") },
                actions = {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        
                    }
                }
            )
            /*if (miembros.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    miembros.forEach { miembro ->
                        Text(
                            text = miembro,
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }*/
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                    )
                )
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(msgs) { msg ->
                    val isMine = msg.authorId == FirebaseAuth.getInstance().currentUser?.uid
                    val authorName = userNames[msg.authorId] ?: "Desconocido"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                    ) {
                        // Nombre encima del mensaje
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                msg.text,
                                Modifier.padding(8.dp)
                            )
                        }
                    }
                }

            }

            // Input
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") }
                )
                IconButton(onClick = {
                    if (text.isNotBlank()) {
                        scope.launch {
                            vm.send(text)
                            text = ""
                        }
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}


@Composable
fun InvitacionesScreen(
    currentUserId: Int,
    navController: NavHostController,
    viewModel: ProyectoViewModel
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    // Flow<List<Proyecto>>
    /*val proyectos by db.proyectoDao()
        .getAll(currentUserId)
        .collectAsState(initial = emptyList())*/

    val proyectos by viewModel.proyectosFlow.collectAsState(initial = emptyList())

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(proyectos) {
        delay(500)
            isLoading = false

    }


    Column(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                )
            )
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isLoading) {

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
        }else{



            if (proyectos.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icono representativo
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Sin Tareas Pendientes",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            // Mensaje principal
                            Text(
                                text = "Sin Chats",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }

            } else {


                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(proyectos) { proyecto ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("main/$currentUserId/chat/${proyecto.id.toString()}")
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = proyecto.nombre,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                FirebaseDynamicLinks.getInstance()
                                    .createDynamicLink()
                                    .setLink(Uri.parse("https://gestorproyectos.page.link/chat/${proyecto.id}"))
                                    .setDomainUriPrefix("https://gestorproyectos.page.link")
                                    .setAndroidParameters(
                                        DynamicLink.AndroidParameters.Builder().build()
                                    )
                                    .buildShortDynamicLink()
                                    .addOnSuccessListener { shortDynamicLink ->
                                        val inviteUrl = shortDynamicLink.shortLink.toString()
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "¡Únete al chat del proyecto “${proyecto.nombre}”!\n$inviteUrl"
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                sendIntent,
                                                "Compartir enlace"
                                            )
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Invitaciones", "Error creando Dynamic Link", e)
                                    }
                            }) {
                                Icon(Icons.Default.Person, contentDescription = "Compartir enlace")
                            }

                        }
                        Divider()
                    }

                }
            }
        }
    }
}
