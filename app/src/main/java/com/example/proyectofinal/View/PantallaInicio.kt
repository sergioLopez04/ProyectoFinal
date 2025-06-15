package com.example.proyectofinal.View

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.ViewModel.ProyectoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.Usuario
import com.example.proyectofinal.ViewModel.ProyectoViewModelFactory
import kotlinx.coroutines.delay


@Composable
fun InicioScreen(
    userId: Int,
    viewModelo: ProyectoViewModel = viewModel(
        factory = ProyectoViewModelFactory(
            userId = userId,
            application = LocalContext.current.applicationContext as Application
        )
    ),
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    val navBackStackEntry = remember { navController.currentBackStackEntry!! }


    val viewModel: ProyectoViewModel = viewModel(
        viewModelStoreOwner = navBackStackEntry,
        factory = ProyectoViewModelFactory(
            userId = userId,
            application = context.applicationContext as Application
        )
    )

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        isLoading = true
        viewModel.sincronizarDesdeServidor()
        delay(500)
        val keys = prefs.all.keys
        val proyectosPendientes = keys.filter { it.startsWith("proyecto_unido_") }

        for (key in proyectosPendientes) {
            val id = key.removePrefix("proyecto_unido_").toIntOrNull()
            if (id != null) {
                viewModel.sincronizarDesdeServidor()
                prefs.edit().remove(key).apply()
            }
        }

        isLoading = false

        val proyectoActualId = prefs.getString("proyecto_actual_id", null)
        if (proyectoActualId != null) {
            navController.navigate("main/$userId/proyecto/$proyectoActualId") {
                popUpTo("main/$userId/inicio") { inclusive = true }
            }
        }


    }



    val proyectos by viewModel.proyectosFlow.collectAsState(initial = emptyList())
    var proyectoAEliminar by remember { mutableStateOf<Proyecto?>(null) }
    var mostrarDialogCrear by remember { mutableStateOf(false) }
    val nuevoProyectoState = remember { mutableStateOf(NuevoProyectoState()) }

    if (isLoading) {
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
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp
            )
        }
    } else {


        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                    )
                )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { mostrarDialogCrear = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Nuevo Proyecto") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation()
                    )
                }
            ) { innerPadding ->

                if (proyectos.isEmpty()) {
                    EmptyProjectsState(modifier = Modifier.padding(innerPadding))
                } else {
                    ProjectsList(
                        proyectos = proyectos,
                        userId = userId,
                        onDeleteClick = { proyectoAEliminar = it },
                        navController = navController,
                        contentPadding = innerPadding
                    )
                }

            }


            if (mostrarDialogCrear) {
                CrearProyectoDialog(
                    state = nuevoProyectoState.value,
                    idUsuario = userId,
                    onDismiss = {
                        mostrarDialogCrear = false
                        nuevoProyectoState.value = NuevoProyectoState()
                    },
                    onConfirm = {
                        viewModel.insertar(it)
                        Log.d("pro", "${it}")
                        mostrarDialogCrear = false
                        nuevoProyectoState.value = NuevoProyectoState()
                    }
                )
            }

            proyectoAEliminar?.let { proyecto ->
                ConfirmarEliminacionDialog(
                    proyecto = proyecto,
                    onDismiss = { proyectoAEliminar = null },
                    onConfirm = {
                        viewModel.eliminar(proyecto)
                        proyectoAEliminar = null
                    }
                )
            }
        }
    }
}


@Composable
private fun ProjectsList(
    proyectos: List<Proyecto>,
    userId: Int,
    onDeleteClick: (Proyecto) -> Unit,
    contentPadding: PaddingValues,
    navController: NavHostController
) {

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        items(
            items = proyectos,
            key = { it.id ?: 0 }
        ) { proyecto ->
            ProyectoCard(
                proyecto = proyecto,
                userId = userId,
                onDeleteClick = onDeleteClick,
                navController = navController
            )
        }
    }
}

@Composable
private fun EmptyProjectsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Clear,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}


@Composable
private fun CrearProyectoDialog(
    state: NuevoProyectoState,
    idUsuario: Int,
    onDismiss: () -> Unit,
    onConfirm: (Proyecto) -> Unit
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Create, null) },
        title = { Text("Nuevo Proyecto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del proyecto") },
                    isError = showError && nombre.isBlank(),
                    supportingText = {
                        if (showError && nombre.isBlank()) {
                            Text("El nombre es requerido")
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    singleLine = false,
                    maxLines = 3,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(
                            Proyecto(
                                nombre = nombre,
                                descripcion = descripcion,
                                id_creador = idUsuario,
                                fechaCreacion = System.currentTimeMillis()
                            )
                        )
                    } else {
                        showError = true
                    }
                }
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ConfirmarEliminacionDialog(
    proyecto: Proyecto,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null) },
        title = { Text("Confirmar eliminación") },
        text = { Text("¿Eliminar permanentemente '${proyecto.nombre}'?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Immutable
private data class NuevoProyectoState(
    var nombre: String = "",
    var descripcion: String = "",

    )

