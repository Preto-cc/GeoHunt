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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geohunt.data.Lugar
import com.example.geohunt.data.repository.GeoHuntRepository
import com.example.geohunt.ui.components.ConfirmDialog
import com.example.geohunt.ui.theme.SuccessGreenLight
import com.example.geohunt.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLugaresScreen(
    eventoId: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val lugares by GeoHuntRepository.getLugaresByEvento(eventoId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var lugarParaEditar by remember { mutableStateOf<Lugar?>(null) }
    var lugarParaBorrar by remember { mutableStateOf<Lugar?>(null) }

    if (showAddDialog) {
        LugarDialog(
            title = "Nuevo Lugar",
            lugaresDelEvento = lugares,
            onDismiss = { showAddDialog = false },
            onConfirm = { lugar ->
                viewModel.crearLugar(
                    eventoId, lugar.nombre, lugar.lat, lugar.lng, 
                    lugar.radioMetros, lugar.mensajeOculto, lugar.puntos, lugar.puntosBonus,
                    lugar.mostrarCoordenadas, lugar.desbloqueadoPorLugarId
                )
                showAddDialog = false
            }
        )
    }

    lugarParaEditar?.let { lugar ->
        LugarDialog(
            title = "Editar Lugar",
            lugar = lugar,
            lugaresDelEvento = lugares.filter { it.id != lugar.id },
            onDismiss = { lugarParaEditar = null },
            onConfirm = { nuevoLugar ->
                viewModel.actualizarLugar(eventoId, nuevoLugar.copy(id = lugar.id))
                lugarParaEditar = null
            }
        )
    }

    lugarParaBorrar?.let { lugar ->
        ConfirmDialog(
            title = "Borrar Lugar",
            message = "¿Estás seguro de que quieres borrar el lugar '${lugar.nombre}'? Esta acción no se puede deshacer.",
            onConfirm = {
                viewModel.borrarLugar(eventoId, lugar.id)
                lugarParaBorrar = null
            },
            onDismiss = { lugarParaBorrar = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Lugares", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Default.Add, contentDescription = "Nuevo Lugar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(lugares) { index, lugar ->
                AdminLugarItem(
                    lugar = lugar,
                    index = index,
                    totalItems = lugares.size,
                    onEdit = { lugarParaEditar = lugar },
                    onDelete = { lugarParaBorrar = lugar },
                    onMoveUp = { viewModel.moverLugar(eventoId, lugares, index, index - 1) },
                    onMoveDown = { viewModel.moverLugar(eventoId, lugares, index, index + 1) }
                )
            }
        }
    }
}

@Composable
fun AdminLugarItem(
    lugar: Lugar, 
    index: Int,
    totalItems: Int,
    onEdit: () -> Unit, 
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(lugar.nombre, style = MaterialTheme.typography.titleMedium)
                    if (!lugar.mostrarCoordenadas) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.VisibilityOff, 
                            contentDescription = "Coordenadas Ocultas",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text("${lugar.lat}, ${lugar.lng}", style = MaterialTheme.typography.bodySmall)
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
fun LugarDialog(
    title: String,
    lugar: Lugar? = null,
    lugaresDelEvento: List<Lugar> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Lugar) -> Unit
) {
    var nombre by remember { mutableStateOf(lugar?.nombre ?: "") }
    var lat by remember { mutableStateOf(lugar?.lat?.toString() ?: "") }
    var lng by remember { mutableStateOf(lugar?.lng?.toString() ?: "") }
    var radio by remember { mutableStateOf(lugar?.radioMetros?.toInt()?.toString() ?: "30") }
    var msg by remember { mutableStateOf(lugar?.mensajeOculto ?: "") }
    var pts by remember { mutableStateOf(lugar?.puntos?.toString() ?: "100") }
    var bonus by remember { mutableStateOf(lugar?.puntosBonus?.toString() ?: "50") }
    var mostrarCoordenadas by remember { mutableStateOf(lugar?.mostrarCoordenadas ?: true) }
    var desbloqueadoPor by remember { mutableStateOf(lugar?.desbloqueadoPorLugarId) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    var expanded by remember { mutableStateOf(false) }
    val lugarSeleccionado = lugaresDelEvento.find { it.id == desbloqueadoPor }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        title = { Text(title) },
        text = {
            LazyColumn {
                item {
                    val fieldColors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                    
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    TextField(
                        value = nombre, onValueChange = { nombre = it }, 
                        label = { Text("Nombre", fontWeight = FontWeight.Bold) },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = lat, onValueChange = { lat = it; errorMsg = null }, 
                        label = { Text("Latitud", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Ej: 40.4167 o 40,4167") },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = lng, onValueChange = { lng = it; errorMsg = null }, 
                        label = { Text("Longitud", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Ej: -3.7033 o -3,7033") },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = radio, onValueChange = { radio = it; errorMsg = null }, 
                        label = { Text("Radio (m) - Entero", fontWeight = FontWeight.Bold) },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = msg, onValueChange = { msg = it }, 
                        label = { Text("Mensaje Oculto", fontWeight = FontWeight.Bold) },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = pts, onValueChange = { pts = it; errorMsg = null }, 
                        label = { Text("Puntos", fontWeight = FontWeight.Bold) },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = bonus, onValueChange = { bonus = it; errorMsg = null }, 
                        label = { Text("Puntos Bonus", fontWeight = FontWeight.Bold) },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = mostrarCoordenadas,
                            onCheckedChange = { mostrarCoordenadas = it }
                        )
                        Text("Mostrar coordenadas a los usuarios", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = lugarSeleccionado?.nombre ?: "Nadie (Libre)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Desbloqueado por", fontWeight = FontWeight.Bold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = fieldColors,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nadie (Libre)") },
                                onClick = {
                                    desbloqueadoPor = null
                                    expanded = false
                                }
                            )
                            lugaresDelEvento.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.nombre) },
                                    onClick = {
                                        desbloqueadoPor = item.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (nombre.isBlank()) {
                    errorMsg = "El nombre es obligatorio"
                    return@Button
                }

                // Normalizar latitud y longitud (cambiar comas por puntos)
                val cleanLat = lat.replace(',', '.')
                val cleanLng = lng.replace(',', '.')
                
                val latNum = cleanLat.toDoubleOrNull()
                val lngNum = cleanLng.toDoubleOrNull()
                val radioNum = radio.toIntOrNull()
                val ptsNum = pts.toIntOrNull()
                val bonusNum = bonus.toIntOrNull()

                if (latNum == null || lngNum == null) {
                    errorMsg = "Latitud o Longitud inválidas (usa punto o coma)"
                } else if (radioNum == null) {
                    errorMsg = "El radio debe ser un número entero"
                } else if (ptsNum == null || bonusNum == null) {
                    errorMsg = "Los puntos deben ser números enteros"
                } else {
                    onConfirm(
                        Lugar(
                            nombre = nombre,
                            lat = latNum,
                            lng = lngNum,
                            radioMetros = radioNum.toFloat(),
                            mensajeOculto = msg,
                            puntos = ptsNum,
                            puntosBonus = bonusNum,
                            mostrarCoordenadas = mostrarCoordenadas,
                            desbloqueadoPorLugarId = desbloqueadoPor
                        )
                    )
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
