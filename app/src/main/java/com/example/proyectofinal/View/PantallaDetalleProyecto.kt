package com.example.proyectofinal.View

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.proyectofinal.Modelos.Proyecto
import com.example.proyectofinal.Modelos.Tareas
import com.example.proyectofinal.ViewModel.ActividadViewModel
import com.example.proyectofinal.ViewModel.TareaViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.lazy.grid.items
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.DataBase.Proyecto.ProyectoRepository
import com.example.proyectofinal.ViewModel.ProyectoViewModel
import com.kizitonwose.calendar.core.OutDateStyle


@Composable
fun PantallaDetalleProyecto(
    proyectoId: Int,
    userId: Int,
    actividadViewModel: ActividadViewModel,
    tareaViewModel: TareaViewModel,
    repository: ProyectoRepository
) {

    LaunchedEffect(Unit) {
        tareaViewModel.sincronizarDesdeServidorProyecto(proyectoId)
    }





    // Estados de datos
    val proyecto by tareaViewModel.proyecto.collectAsState()
    val tareas by tareaViewModel.tareass.collectAsState()
    val tareasOrdenadas = tareas.sortedByDescending { it.prioridad }

    // Estados del formulario
    var nuevaDesc by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf(-1) } // -1 = no seleccionada
    var fechaInicio by remember { mutableStateOf<LocalDate?>(null) }
    var fechaFin by remember { mutableStateOf<LocalDate?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currentDateType by remember { mutableStateOf(DateType.INICIO) }

    // Estados de scroll y animación
    val listState = rememberLazyListState()
    val scrollState = rememberScrollState()
    val animatedVisibility by remember { derivedStateOf { showForm } }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val borderWidth = 1.5.dp

    // Validación del formulario
    val isValidForm = remember {
        derivedStateOf {
            nuevaDesc.isNotBlank() &&
                    prioridad != -1 &&
                    fechaInicio != null &&
                    fechaFin != null &&
                    !fechaInicio!!.isAfter(fechaFin!!)
        }
    }

    // Scroll al abrir el formulario
    LaunchedEffect(showForm) {
        if (showForm) listState.animateScrollToItem(0)
    }



    // Registro de actividad al salir
    DisposableEffect(proyectoId) {
        val startTime = System.currentTimeMillis()
        onDispose {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            proyecto?.let {
                actividadViewModel.viewModelScope.launch {
                    actividadViewModel.registrarActividad(
                        it,
                        startTime,
                        endTime,
                        userId,
                        duration
                    )
                }

            }
        }
    }

    Scaffold(
        topBar = { ProjectHeader(proyecto) },
        floatingActionButton = {
            AnimatedFloatingActionButton(
                expanded = !showForm,
                onClick = { showForm = !showForm },
                icon = {
                    Icon(
                        imageVector = if (showForm) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                },
                text = { Text("NUEVA TAREA", fontWeight = FontWeight.Black) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp),
                shape = RoundedCornerShape(24.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                ))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item { Spacer(Modifier.height(16.dp)) }

                // Formulario animado
                if (showForm) {
                    item {
                        AnimatedVisibility(
                            visible = animatedVisibility,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .shadow(24.dp, RoundedCornerShape(32.dp))
                        ) {
                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(
                                    borderWidth,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 600.dp)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Nueva Tarea",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // Descripción mejorada
                                    OutlinedTextField(
                                        value = nuevaDesc,
                                        onValueChange = { nuevaDesc = it },
                                        label = { Text("Descripción") },
                                        placeholder = { Text("Escribe la tarea...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                                        maxLines = 3,
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent
                                        )
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    // Selector de fechas estilo neon
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        DateSelectorPill(
                                            date = fechaInicio,
                                            label = "INICIO",
                                            formatter = dateFormatter,
                                            onClick = {
                                                currentDateType = DateType.INICIO
                                                showDatePicker = true
                                            },
                                            gradient = listOf(Color(0xFF6A1B9A), Color(0xFF9C27B0))
                                        )

                                        Icon(
                                            Icons.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        DateSelectorPill(
                                            date = fechaFin,
                                            label = "FIN",
                                            formatter = dateFormatter,
                                            onClick = {
                                                currentDateType = DateType.FIN
                                                showDatePicker = true
                                            },
                                            gradient = listOf(Color(0xFF009688), Color(0xFF4CAF50))
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    // Prioridad con chips dinámicos
                                    Text(
                                        "Prioridad",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        PriorityChip(
                                            selected = prioridad == HIGH_PRIORITY,
                                            text = "Urgente",
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            onClick = { prioridad = HIGH_PRIORITY }
                                        )
                                        PriorityChip(
                                            selected = prioridad == MEDIUM_PRIORITY,
                                            text = "Importante",
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            onClick = { prioridad = MEDIUM_PRIORITY }
                                        )
                                        PriorityChip(
                                            selected = prioridad == LOW_PRIORITY,
                                            text = "Normal",
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            onClick = { prioridad = LOW_PRIORITY }
                                        )
                                    }

                                    // Botón guardar con gradiente
                                    GradientButton(
                                        text = "CREAR TAREA",
                                        enabled = isValidForm.value,
                                        gradient = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 24.dp)
                                            .height(50.dp)
                                    ) {
                                        tareaViewModel.agregarTarea(
                                            proyectoId = proyectoId,
                                            descripcion = nuevaDesc,
                                            prioridad = prioridad,
                                            fechaInicio = fechaInicio!!,
                                            fechaFin = fechaFin!!
                                        )

                                        showForm = false
                                        nuevaDesc = ""
                                        prioridad = -1
                                        fechaInicio = null
                                        fechaFin = null
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }


                // Lista de tareas existentes
                items(tareasOrdenadas, key = { it.id }) { tarea ->
                    TaskItem(
                        tarea = tarea,
                        formatter = dateFormatter,
                        onCheckedChange = { isChecked ->
                            tareaViewModel.actualizarEstado(tarea, isChecked)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // DatePicker overlay
            // Calendario personalizado como diálogo
            if (showDatePicker) {
                Dialog(onDismissRequest = { showDatePicker = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        CalendarioPersonalizado(
                            selectedDate = when (currentDateType) {
                                DateType.INICIO -> fechaInicio
                                DateType.FIN -> fechaFin
                            },
                            onDateSelected = { date ->
                                when (currentDateType) {
                                    DateType.INICIO -> {
                                        fechaInicio = date
                                        if (fechaFin?.isBefore(date) == true) fechaFin = date
                                    }

                                    DateType.FIN -> {
                                        if (fechaInicio == null || date.isAfter(fechaInicio)) {
                                            fechaFin = date
                                        }
                                    }
                                }
                                showDatePicker = false
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedFloatingActionButton(
    expanded: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    containerColor: Color,
    contentColor: Color,
    elevation: FloatingActionButtonElevation,
    shape: RoundedCornerShape
) {
    AnimatedContent(
        targetState = expanded,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200))
        },
        label = "FAB Expansion"
    ) { isExpanded ->
        if (isExpanded) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                icon = icon,
                text = text,
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = elevation,
                shape = shape
            )
        } else {
            FloatingActionButton(
                onClick = onClick,
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = elevation,
                shape = shape
            ) {
                icon()
            }
        }
    }
}


@Composable
private fun DateSelectorPill(
    date: LocalDate?,
    label: String,
    formatter: DateTimeFormatter,
    onClick: () -> Unit,
    gradient: List<Color>
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (date != null) 1f else 0f,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                Brush.horizontalGradient(
                    colors = if (date != null) gradient else listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(gradient),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = date?.format(formatter) ?: label,
                color = if (date != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.alpha(animatedProgress.coerceAtLeast(0.5f))
            )
        }
    }
}


@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(200)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = gradient,
                    startX = 0f,
                    endX = 500f
                ),
                alpha = animatedAlpha
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun PriorityChip(
    selected: Boolean,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = animatedColor,
        border = BorderStroke(
            1.dp,
            color.copy(alpha = if (selected) 0.8f else color.alpha)
        ),
        shadowElevation = if (selected) 8.dp else 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}


@Composable
fun TaskItem(
    tarea: Tareas,
    formatter: DateTimeFormatter,
    onCheckedChange: (Boolean) -> Unit
) {
    val priorityGradient = when (tarea.prioridad) {
        HIGH_PRIORITY -> listOf(Color(0xFFFF5252), Color(0xFFFF1744))
        MEDIUM_PRIORITY -> listOf(Color(0xFFFFB74D), Color(0xFFFFA726))
        else -> listOf(Color(0xFF81C784), Color(0xFF66BB6A))
    }

    var isHovered by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = if (isHovered) 16.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = priorityGradient.first()
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val hovered = event.type == PointerEventType.Enter
                        isHovered = hovered
                    }
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(priorityGradient))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = onCheckedChange
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (tarea.completada) Color(0xFFE0F2F1) else Color(0xFFFFEBEE), // verde claro o rojo claro
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = if (tarea.completada) "Completada" else "Pendiente",
                        color = if (tarea.completada) Color(0xFF2E7D32) else Color(0xFFC62828), // verde oscuro o rojo oscuro
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }



            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tarea.descripcion,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconTextBadge(
                        icon = Icons.Filled.Add,
                        text = {
                            Text(
                                when (tarea.prioridad) {
                                    HIGH_PRIORITY -> "Urgente"
                                    MEDIUM_PRIORITY -> "Normal"
                                    else -> "Bajo"
                                }
                            )
                        },
                        color = Color.White
                    )


                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {

                        IconTextBadge(
                            icon = Icons.Filled.DateRange,
                            text = {
                                Column {
                                    Text(
                                        text = "Inicio: ",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.width(60.dp) // Asegura mismo ancho para ambos
                                    )
                                    Text(
                                        text = formatter.format(
                                            Instant.ofEpochMilli(tarea.fechaInicio!!)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                        )
                                    )
                                }
                            },
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        IconTextBadge(
                            icon = Icons.Filled.DateRange,
                            text = {
                                Column {
                                    Text(
                                        text = "Fin: ",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.width(60.dp)
                                    )
                                    Text(
                                        text = formatter.format(
                                            Instant.ofEpochMilli(tarea.fechaFin!!)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                        )
                                    )
                                }
                            },
                            color = Color.White.copy(alpha = 0.9f)
                        )

                    }
                }
            }
        }
    }
}

@Composable
private fun IconTextBadge(icon: ImageVector, text: @Composable () -> Unit, color: Color) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )

        Spacer(Modifier.width(4.dp))

        text()
    }
}


private enum class DateType { INICIO, FIN }

const val HIGH_PRIORITY = 3
const val MEDIUM_PRIORITY = 2
const val LOW_PRIORITY = 1


@Composable
private fun ProjectHeader(proyecto: Proyecto?) {
    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                proyecto?.nombre.orEmpty(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                proyecto?.descripcion
                    ?.takeIf { it.isNotBlank() }
                    .orEmpty()
                    .ifEmpty { "Sin descripción" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black
                )
            )
        }
    }
}

@Composable
fun CalendarioPersonalizado(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val startMonth = remember { YearMonth.now().minusYears(50) } // 2 años atrás
    val endMonth = remember { YearMonth.now().plusYears(50) }    // 2 años adelante

    var showMonthSelector by remember { mutableStateOf(false) }
    var showYearSelector by remember { mutableStateOf(false) }

    val daysOfWeek = remember {
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
    }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = YearMonth.now(),
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfGrid
    )

    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // Header con navegación
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Flecha izquierda
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        state.scrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1))
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Mes anterior",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Selectores de mes y año
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = state.firstVisibleMonth.yearMonth.format(
                        DateTimeFormatter.ofPattern("MMMM", Locale("es"))
                    ).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.clickable { showMonthSelector = true }
                )

                Text(
                    text = state.firstVisibleMonth.yearMonth.year.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.clickable { showYearSelector = true }
                )
            }

            // Flecha derecha
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        state.scrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1))
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Mes siguiente",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Selector de mes
        if (showMonthSelector) {
            Dialog(onDismissRequest = { showMonthSelector = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp
                ) {
                    MonthSelector(
                        selectedMonth = state.firstVisibleMonth.yearMonth.month,
                        onMonthSelected = { newMonth ->
                            coroutineScope.launch {
                                state.scrollToMonth(
                                    YearMonth.of(
                                        state.firstVisibleMonth.yearMonth.year,
                                        newMonth
                                    )
                                )
                            }
                            showMonthSelector = false
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Selector de año
        if (showYearSelector) {
            Dialog(onDismissRequest = { showYearSelector = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp
                ) {
                    YearSelector(
                        currentYear = state.firstVisibleMonth.yearMonth.year,
                        minYear = startMonth.year,
                        maxYear = endMonth.year,
                        onYearSelected = { newYear ->
                            coroutineScope.launch {
                                state.scrollToMonth(
                                    YearMonth.of(
                                        newYear,
                                        state.firstVisibleMonth.yearMonth.month
                                    )
                                )
                            }
                            showYearSelector = false
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Días de la semana en español
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    text = when (dayOfWeek) {
                        DayOfWeek.MONDAY -> "LUN"
                        DayOfWeek.TUESDAY -> "MAR"
                        DayOfWeek.WEDNESDAY -> "MIE"
                        DayOfWeek.THURSDAY -> "JUE"
                        DayOfWeek.FRIDAY -> "VIE"
                        DayOfWeek.SATURDAY -> "SAB"
                        DayOfWeek.SUNDAY -> "DOM"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendario principal
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayItem(
                    day = day,
                    today = today,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )
            }
        )
    }
}

@Composable
private fun DayItem(
    day: CalendarDay,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val date = day.date
    val isToday = date == today
    val isSelected = date == selectedDate
    val isInMonth = day.position == DayPosition.MonthDate

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.5.dp else 0.dp,  // Más grueso
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),  // Transparencia
                shape = CircleShape
            )
            .clickable(enabled = isInMonth) { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (isInMonth) 1f else 0.3f
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthSelector(
    selectedMonth: Month,
    onMonthSelected: (Month) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Seleccionar mes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(250.dp)
        ) {
            items(Month.values()) { month ->
                val isSelected = month == selectedMonth
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .clickable { onMonthSelected(month) }
                        .padding(8.dp)
                        .animateItemPlacement(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = month.getDisplayName(TextStyle.FULL, Locale("es")),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun YearSelector(
    currentYear: Int,
    minYear: Int,
    maxYear: Int,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedYear by remember { mutableStateOf(currentYear) }

    Column(modifier = modifier) {
        Text(
            "Seleccionar año",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Selector rápido de años
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp)
        ) {
            items((minYear..maxYear).toList()) { year ->
                val isSelected = year == selectedYear
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { selectedYear = year }
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(0.1f)
                            else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = year.toString(),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Botón de confirmación
        Button(
            onClick = { onYearSelected(selectedYear) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Confirmar año")
        }
    }
}



