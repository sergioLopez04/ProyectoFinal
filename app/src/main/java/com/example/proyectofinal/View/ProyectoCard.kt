package com.example.proyectofinal.View

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.ViewModel.ProyectoViewModel
import com.example.proyectofinal.ViewModel.ProyectoViewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*@Composable
fun ProyectoCard(
    proyecto: Proyecto,
    userId: Int,
    onDeleteClick: (Proyecto) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProyectoViewModel = viewModel(
        factory = ProyectoViewModelFactory(
            userId = proyecto.id_creador,
            application = LocalContext.current.applicationContext as Application
        )
    ),
) {
    val nombreCreador by viewModel.getNombreCreador(proyecto.id_creador).collectAsState(initial = "")

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    val fechaFormateada = proyecto.fechaCreacion
        .takeIf { it != 0L }
        ?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(formatter)
        } ?: "Sin fecha"


    Card(
        modifier = modifier
            .clickable { navController.navigate("detalleProyecto/$userId/${proyecto.id}") },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = proyecto.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = { onDeleteClick(proyecto) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = proyecto.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Creado por: ${nombreCreador}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}*/

@Composable
fun ProyectoCard(
    proyecto: Proyecto,
    userId: Int,
    onDeleteClick: (Proyecto) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProyectoViewModel = viewModel(
        factory = ProyectoViewModelFactory(
            userId = proyecto.id_creador,
            application = LocalContext.current.applicationContext as Application
        )
    ),
) {
    var nombreCreador by remember { mutableStateOf("Cargando...") }
    LaunchedEffect(proyecto.id_creador) {
        nombreCreador = viewModel.getNombreCreador(proyecto.id_creador)
    }

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    val fechaFormateada = proyecto.fechaCreacion
        .takeIf { it != 0L }
        ?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(formatter)
        } ?: "Sin fecha"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { navController.navigate("detalleProyecto/$userId/${proyecto.id}") },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f) // fondo claro semitransparente
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = proyecto.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0D47A1) // Azul oscuro para resaltar
                )

                IconButton(
                    onClick = { onDeleteClick(proyecto) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (proyecto.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = proyecto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Creador: $nombreCreador",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
