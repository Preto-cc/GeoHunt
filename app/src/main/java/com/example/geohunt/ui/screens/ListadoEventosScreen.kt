package com.example.geohunt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geohunt.viewmodel.EventoUiState
import com.example.geohunt.viewmodel.EventosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoEventosScreen(
    onEventoClick: (String) -> Unit,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: EventosViewModel = viewModel()
) {
    val eventosState by viewModel.eventosConProgreso.collectAsState()
    val usuario by viewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        val esAnonimo = usuario?.tipoLogin == "anonimo"
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            title = { Text("Cerrar sesión") },
            text = {
                Text(
                    if (esAnonimo) 
                        "¿Estás seguro? Al ser una cuenta anónima, perderás todo tu progreso y puntos para siempre. Esta acción no se puede deshacer."
                    else 
                        "¿Estás seguro de que quieres cerrar sesión?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.signOut()
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GeoHunt - Eventos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                    if (usuario?.admin == true) {
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Panel Admin")
                        }
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(eventosState) { item ->
                EventoItem(item = item, onClick = { onEventoClick(item.evento.id) })
            }
        }
    }
}

@Composable
fun EventoItem(item: EventoUiState, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.evento.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.evento.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Círculo de progreso
            CircularProgressWithText(
                visitados = item.visitados,
                totales = item.totales
            )
        }
    }
}

@Composable
fun CircularProgressWithText(visitados: Int, totales: Int) {
    val progress = if (totales > 0) visitados.toFloat() / totales else 0f
    
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(64.dp),
            strokeWidth = 6.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(
            text = "$visitados/$totales",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
