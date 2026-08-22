package com.example.geohunt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geohunt.data.Evento
import com.example.geohunt.ui.components.ConfirmDialog
import com.example.geohunt.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onEventoClick: (String) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val eventos by viewModel.eventos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var eventoParaEditar by remember { mutableStateOf<Evento?>(null) }
    var eventoParaBorrar by remember { mutableStateOf<Evento?>(null) }
    var eventoParaReiniciar by remember { mutableStateOf<Evento?>(null) }

    if (showAddDialog) {
        AddEventoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, desc, fechaFin ->
                viewModel.crearEvento(nombre, desc, fechaFin)
                showAddDialog = false
            }
        )
    }

    eventoParaEditar?.let { evento ->
        EditEventoDialog(
            evento = evento,
            onDismiss = { eventoParaEditar = null },
            onConfirm = { nombre, desc, activo, fechaFin ->
                viewModel.actualizarEvento(evento.id, nombre, desc, activo, fechaFin)
                eventoParaEditar = null
            }
        )
    }

    eventoParaBorrar?.let { evento ->
        ConfirmDialog(
            title = "Borrar Evento",
            message = "¿Estás seguro de que quieres borrar el evento '${evento.nombre}'? Se borrarán todos sus lugares y visitas permanentemente.",
            onConfirm = {
                viewModel.borrarEvento(evento.id)
                eventoParaBorrar = null
            },
            onDismiss = { eventoParaBorrar = null }
        )
    }

    eventoParaReiniciar?.let { evento ->
        ConfirmDialog(
            title = "Reiniciar Evento",
            message = "¿Quieres borrar todas las visitas y puntos del evento '${evento.nombre}'? Los lugares se mantendrán, pero nadie los habrá visitado aún.",
            onConfirm = {
                viewModel.reiniciarEvento(evento.id)
                eventoParaReiniciar = null
            },
            onDismiss = { eventoParaReiniciar = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Eventos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Evento")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(eventos) { index, evento ->
                AdminEventoItem(
                    evento = evento,
                    index = index,
                    totalItems = eventos.size,
                    onClick = { onEventoClick(evento.id) },
                    onEdit = { eventoParaEditar = evento },
                    onReset = { eventoParaReiniciar = evento },
                    onDelete = { eventoParaBorrar = evento },
                    onMoveUp = { viewModel.moverEvento(index, index - 1) },
                    onMoveDown = { viewModel.moverEvento(index, index + 1) }
                )
            }
        }
    }
}

@Composable
fun AdminEventoItem(
    evento: Evento, 
    index: Int,
    totalItems: Int,
    onClick: () -> Unit, 
    onEdit: () -> Unit, 
    onReset: () -> Unit, 
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Controles de Ordenación
            Column {
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
                }
                IconButton(onClick = onMoveDown, enabled = index < totalItems - 1) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(evento.nombre, style = MaterialTheme.typography.titleMedium)
                Text(if (evento.activo) "Activo" else "Inactivo", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventoDialog(onDismiss: () -> Unit, onConfirm: (String, String, Long?) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf<Long?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (date != null) {
                        fechaFin = date
                        showTimePicker = true
                    }
                    showDatePicker = false
                }) { Text("Siguiente") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = fechaFin ?: System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    fechaFin = cal.timeInMillis
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            title = { Text("Seleccionar hora") },
            text = { TimePicker(state = timePickerState) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        title = { Text("Nuevo Evento") },
        text = {
            Column {
                TextField(
                    value = nombre, 
                    onValueChange = { nombre = it }, 
                    label = { Text("Nombre", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Descripción", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Finalización:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val label = if (fechaFin == null) "Sin fecha límite" 
                               else SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(fechaFin!!))
                    
                    Text(label, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Cambiar fecha")
                    }
                    if (fechaFin != null) {
                        IconButton(onClick = { fechaFin = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar fecha")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, desc, fechaFin) }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventoDialog(evento: Evento, onDismiss: () -> Unit, onConfirm: (String, String, Boolean, Long?) -> Unit) {
    var nombre by remember { mutableStateOf(evento.nombre) }
    var desc by remember { mutableStateOf(evento.descripcion) }
    var activo by remember { mutableStateOf(evento.activo) }
    var fechaFin by remember { mutableStateOf(evento.fechaFin) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaFin ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (date != null) {
                        fechaFin = date
                        showTimePicker = true
                    }
                    showDatePicker = false
                }) { Text("Siguiente") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = fechaFin ?: System.currentTimeMillis() }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val resCal = Calendar.getInstance().apply {
                        timeInMillis = fechaFin ?: System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    fechaFin = resCal.timeInMillis
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            title = { Text("Seleccionar hora") },
            text = { TimePicker(state = timePickerState) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        title = { Text("Editar Evento") },
        text = {
            Column {
                TextField(
                    value = nombre, 
                    onValueChange = { nombre = it }, 
                    label = { Text("Nombre", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Descripción", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Finalización:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val label = if (fechaFin == null || fechaFin!! <= 0) "Sin fecha límite" 
                               else SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(fechaFin!!))
                    
                    Text(label, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Cambiar fecha")
                    }
                    if (fechaFin != null && fechaFin!! > 0) {
                        IconButton(onClick = { fechaFin = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar fecha")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activo, onCheckedChange = { activo = it })
                    Text("Evento Activo")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, desc, activo, fechaFin) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
