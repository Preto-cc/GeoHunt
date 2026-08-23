package com.example.geohunt.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geohunt.viewmodel.LugarUiState
import com.example.geohunt.viewmodel.LugaresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoLugaresScreen(
    eventoId: String,
    onLugarClick: (String) -> Unit,
    onVerRanking: () -> Unit,
    onBack: () -> Unit,
    viewModel: LugaresViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventoId) {
        viewModel.setEventoId(eventoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lugares", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onVerRanking) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Ver Ranking")
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
            items(uiState.lugares) { state ->
                LugarItem(state = state, onClick = { 
                    if (!state.bloqueado) onLugarClick(state.lugar.id) 
                })
            }
        }
    }
}

@Composable
fun LugarItem(state: LugarUiState, onClick: () -> Unit) {
    val alpha = if (state.bloqueado) 0.5f else 1f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !state.bloqueado, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f * alpha)),
        colors = CardDefaults.cardColors(
            containerColor = if (state.bloqueado)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else if (!state.visitado) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.lugar.nombre, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = if (state.bloqueado) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else if (!state.visitado) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.lugar.puntos} pts", 
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.bloqueado) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
            
            StatusBadge(visitado = state.visitado, bloqueado = state.bloqueado)
        }
    }
}

@Composable
fun StatusBadge(visitado: Boolean, bloqueado: Boolean) {
    val containerColor = when {
        visitado -> Color(0xFFE8F5E9)
        bloqueado -> Color(0xFFFFEBEE)
        else -> Color(0xFFF5F5F5)
    }
    val contentColor = when {
        visitado -> Color(0xFF2E7D32)
        bloqueado -> Color(0xFFC62828)
        else -> Color(0xFF757575)
    }
    val icon = when {
        visitado -> Icons.Default.Done
        bloqueado -> Icons.Default.Lock
        else -> Icons.Default.Lock
    }
    val text = when {
        visitado -> "Visitado"
        bloqueado -> "Bloqueado"
        else -> "Pendiente"
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
