package com.example.proyectofinal.View


import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Paint
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.ViewModel.ActividadViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyectofinal.DataBase.Actividad.ActividadRepositoty
import com.example.proyectofinal.ViewModel.ActividadViewModelFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import java.time.*
import java.time.format.*
import java.time.temporal.*
import java.util.*
import androidx.compose.material3.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import com.example.proyectofinal.DataBase.Actividad.ActividadApiService
import com.example.proyectofinal.DataBase.Actividad.ActividadDao
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun ActividadScreen(
    repository: ActividadRepositoty,
    navController: NavController,
    dao: ActividadDao,
    api: ActividadApiService,
    userId: Int
) {

    val factory = remember { ActividadViewModelFactory(repository, dao, api, userId) }
    val viewModel: ActividadViewModel = viewModel(factory = factory)
    var isLoading by remember { mutableStateOf(true) }

    // En ActividadScreen
    LaunchedEffect(Unit) {
        isLoading = true
        repository.sincronizarDesdeServidor(userId)
        delay(500)
        isLoading = false
    }


    val actividades by viewModel.actividadesDiarias.collectAsState()

    val ordenadas = actividades.sortedByDescending { it.second }

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
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                        )
                    )
            ) {

                if (ordenadas.isEmpty()) {
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
                                    contentDescription = "Sin actividad",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(bottom = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                // Mensaje principal
                                Text(
                                    text = "Nada que mostrar",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(4.dp))

                                // Mensaje secundario
                                Text(
                                    text = "Todavía no has registrado actividad para este día.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))

                                // Botón principal
                                Button(
                                    onClick = { navController.navigate("actividad/0/$userId") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Ver actividad reciente",
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(10.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GraficoCircular(
                            actividades = ordenadas,
                            size = 275.dp,
                            onSliceClick = { proyecto ->
                                navController.navigate("actividad/${proyecto.id}/$userId")
                            }
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    ListaDetalladaActividad(
                        actividades = ordenadas,
                        onItemClick = { proyecto ->
                            navController.navigate("actividad/${proyecto.id}/$userId")
                        }
                    )
                }
            }

        }
    }


}


@Composable
private fun GraficoCircular(
    actividades: List<Pair<Proyecto, Long>>,
    size: Dp = 250.dp,
    onSliceClick: (Proyecto) -> Unit
) {
    val total = actividades.sumOf { it.second }.toFloat()
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .size(size)
            .pointerInput(actividades) {
                detectTapGestures { offset ->
                    val center = this.size.center
                    val theta = (atan2(offset.y - center.y, offset.x - center.x)
                        .toDegrees() + 360f + 90f) % 360f
                    var start = 0f
                    actividades.forEach { (proyecto, tiempo) ->
                        val sweep = (tiempo / total) * 360f
                        if (theta in start..(start + sweep)) {
                            onSliceClick(proyecto)
                            return@detectTapGestures
                        }
                        start += sweep
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasSize = drawContext.size
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val radius = min(canvasSize.width, canvasSize.height) / 2f
            val textSizePx = with(density) { 18.sp.toPx() }

            var startAngle = 0f
            actividades.forEach { (proyecto, tiempo) ->
                val sweepAngle = (tiempo / total) * 360f
                var color = colorForProject(proyecto.id)

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    style = Fill // Esto rellena el trozo
                )

                drawArc(
                    color = Color.Black,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    style = Stroke(
                        width = with(density) { 1.5.dp.toPx() },
                        cap = StrokeCap.Butt,
                        join = StrokeJoin.Miter
                    )
                )


                // 2) Posición de label usando midAngle directamente
                val midAngle = startAngle + sweepAngle / 2f
                val angleRad = Math.toRadians(midAngle.toDouble())
                val labelRad = radius + with(density) { 12.dp.toPx() }
                val x = center.x + (labelRad * cos(angleRad)).toFloat()
                val y = center.y + (labelRad * sin(angleRad)).toFloat()

                // 3) Texto horizontal centrado
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        proyecto.nombre,
                        x,
                        y,
                        Paint().apply {
                            textSize = textSizePx
                            color = Color.Black
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }

                startAngle += sweepAngle
            }
        }

    }
}


// para conversión de radianes a grados
private fun Float.toDegrees() = this * 180f / PI.toFloat()


@Composable
private fun ListaDetalladaActividad(
    actividades: List<Pair<Proyecto, Long>>,
    onItemClick: (Proyecto) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(actividades) { (proyecto, tiempo) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onItemClick(proyecto) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // círculo de color
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorForProject(proyecto.id), CircleShape)
                )
                Spacer(Modifier.width(20.dp))
                // nombre del proyecto
                Text(
                    text = proyecto.nombre,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.weight(1f))
                // duración formateada
                Text(
                    text = formatDuration(tiempo),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadPorDiaScreen(
    repository: ActividadRepositoty,
    dao: ActividadDao,
    api: ActividadApiService,
    userId: Int
) {
    val vm: ActividadViewModel = viewModel(
        factory = remember { ActividadViewModelFactory(repository, dao, api, userId) }
    )

    // 1) Día seleccionado + diálogo de calendario
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // 2) Carga el resumen cada vez que cambie el día
    LaunchedEffect(fechaSeleccionada) {
        vm.setFecha(
            fechaSeleccionada.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }

    val resumen by vm.actividadesDiarias.collectAsState(emptyList())

    Log.d("ACTIVIDADES_RESUMEN", "Resumen: ${resumen}")

    // 3) Calcula inicio de semana en base al día seleccionado
    val fechaInicioSemana = remember(fechaSeleccionada) {
        fechaSeleccionada.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val diasSemana = remember(fechaInicioSemana) {
        (0..6).map { fechaInicioSemana.plusDays(it.toLong()) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ───── Semana y Mês ─────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.minusWeeks(1) }) {
                Icon(Icons.Default.ArrowBack, "Semana anterior")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Rango días (al pulsar, abre calendario)
                Text(
                    text = "${fechaInicioSemana.dayOfMonth} - ${fechaInicioSemana.plusDays(6).dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { showDatePickerDialog = true }
                )
                // Mes y año
                Text(
                    text = fechaInicioSemana
                        .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES")))
                        .replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }


            IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.plusWeeks(1) }) {
                Icon(Icons.Default.ArrowForward, "Semana siguiente")
            }
        }

        // ───── Calendario para elegir fecha cualquiera ─────
        if (showDatePickerDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = fechaSeleccionada
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            fechaSeleccionada = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePickerDialog = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }


        Spacer(Modifier.height(8.dp))

        // ───── Selector de días de la semana ─────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            diasSemana.forEach { dia ->
                val esSel = dia == fechaSeleccionada
                val esHoy = dia == LocalDate.now()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                esSel -> MaterialTheme.colorScheme.primary
                                esHoy -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else -> Color.Transparent
                            }
                        )
                        .clickable { fechaSeleccionada = dia },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")),
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                esSel -> MaterialTheme.colorScheme.onPrimary
                                esHoy -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            dia.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                esSel -> MaterialTheme.colorScheme.onPrimary
                                esHoy -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ───── Lista de actividades de ese día ─────
        if (resumen.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay actividades este día")
            }
        } else {
            LazyColumn {
                items(resumen) { (proyecto, tiempo) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .background(colorForProject(proyecto.id), CircleShape)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(proyecto.nombre)
                        Spacer(Modifier.weight(1f))
                        Text(formatDuration(tiempo))
                    }
                    Divider()
                }
            }

        }
    }
}


@Composable
fun SelectorSemanas(
    semanaOffset: Int,
    onSemanaChange: (Int) -> Unit,
    fechaInicioSemana: LocalDate,
    onRangoClick: () -> Unit,
    diaSeleccionado: LocalDate,
    onDiaSeleccionado: (LocalDate) -> Unit
) {
    val fechaFinSemana = fechaInicioSemana.plusDays(6)
    val mesAnno = fechaInicioSemana.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fila con botones y rango de fechas
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onSemanaChange(semanaOffset + 1) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ArrowBack, "Semana anterior")
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rango de fechas clickeable
                Text(
                    text = "Del ${fechaInicioSemana.dayOfMonth} al ${fechaFinSemana.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { onRangoClick() },
                    textAlign = TextAlign.Center
                )

                // Mes y año
                Text(
                    text = mesAnno.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = { if (semanaOffset > 0) onSemanaChange(semanaOffset - 1) },
                modifier = Modifier.size(48.dp),
                enabled = semanaOffset > 0
            ) {
                Icon(Icons.Default.ArrowForward, "Semana siguiente")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Selector de días de la semana
        SemanaSelectorDias(
            diasSemana = (0..6).map { fechaInicioSemana.plusDays(it.toLong()) },
            diaSeleccionado = diaSeleccionado,
            onDiaSeleccionado = onDiaSeleccionado
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    if (showDialog) {
        val datePickerState = rememberDatePickerState()

        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = { Text("Seleccionar fecha") },
            text = {
                DatePicker(
                    state = datePickerState,
                    title = { Text("Elige una fecha") },
                    showModeToggle = false
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val fecha = Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(fecha)
                        }
                        onDismiss()
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SemanaSelectorDias(
    diasSemana: List<LocalDate>,
    diaSeleccionado: LocalDate,
    onDiaSeleccionado: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        diasSemana.forEach { fecha ->
            val esHoy = fecha == LocalDate.now()
            val esSeleccionado = fecha == diaSeleccionado

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            esSeleccionado -> MaterialTheme.colorScheme.primary
                            esHoy -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                    )
                    .clickable { onDiaSeleccionado(fecha) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            esSeleccionado -> MaterialTheme.colorScheme.onPrimary
                            esHoy -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = fecha.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            esSeleccionado -> MaterialTheme.colorScheme.onPrimary
                            esHoy -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "$hours h ${minutes} min"
        minutes > 0 -> "$minutes min"
        else -> "${seconds % 60} seg"
    }
}

/*fun colorForProject(id: Int): Color {
    val colors = listOf(
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
        Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5)
    )
    return colors[id.absoluteValue % colors.size]
}*/

fun colorForProject(id: Int): Color {
    val colors = listOf(
        Color(0xFFEF5350),  // Rojo
        Color(0xFFFFA726),  // Naranja
        Color(0xFFFF7043),  // Naranja oscuro
        Color(0xFF26A69A),  // Verde azulado (más oscuro que el fondo)
        Color(0xFF7E57C2),  // Violeta
        Color(0xFFEC407A)   // Rosa
    )
    return colors[id.absoluteValue % colors.size]
}
