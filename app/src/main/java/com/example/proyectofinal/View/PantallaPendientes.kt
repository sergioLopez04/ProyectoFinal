package com.example.proyectofinal.View


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.Modelos.Tareas
import com.example.proyectofinal.ViewModel.TareaViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(tareaViewModel: TareaViewModel, userId: Int) {
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        isLoading = true
        tareaViewModel.sincronizarDesdeServidor()
        delay(500)
        isLoading = false
    }

    val tareas by tareaViewModel.tareasCompletas.collectAsState(initial = emptyList())


    // Filtrar las tareas pendientes (no completadas)
    val tareasPendientes = tareas.filter { !it.completada }

    val tareasOrdenadas = tareasPendientes.sortedByDescending { it.prioridad }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                    )
                ), // o el fondo que uses
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {


        if (tareasPendientes.isNotEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                        )
                    )
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)

                ) {
                    items(tareasOrdenadas, key = { it.id }) { tarea ->
                        TaskItem(
                            tarea = tarea,
                            formatter = DateTimeFormatter.ofPattern("dd MMM yyyy"),
                            onCheckedChange = { isChecked ->
                                tareaViewModel.actualizarEstado(tarea, isChecked)
                            }
                        )
                    }
                }
            }


        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                        )
                    )
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
                            text = "Sin Tareas Pendientes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                    }
                }
            }
        }
    }
}