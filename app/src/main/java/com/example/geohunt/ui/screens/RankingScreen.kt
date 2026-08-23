package com.example.geohunt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.example.geohunt.data.repository.AuthRepository
import com.example.geohunt.viewmodel.RankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    eventoId: String,
    onBack: () -> Unit,
    viewModel: RankingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val usuarioActual by AuthRepository.currentUserState.collectAsState()
    val currentUid = usuarioActual?.uid ?: ""
    val esAdmin = usuarioActual?.admin == true

    LaunchedEffect(eventoId) {
        viewModel.setEventoId(eventoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking del Evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        val ranking = uiState.items
        val indexUsuarioActual = ranking.indexOfFirst { it.uid == currentUid }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(ranking) { index, usuario ->
                val esPropio = usuario.uid == currentUid
                val esVecino = indexUsuarioActual != -1 && 
                               (index == indexUsuarioActual - 1 || index == indexUsuarioActual + 1)
                
                val mostrarPuntos = !uiState.rankingCercano || esAdmin || esPropio || esVecino

                RankingItem(
                    posicion = index + 1, 
                    usuario = usuario,
                    mostrarPuntos = mostrarPuntos
                )
            }
        }
    }
}

@Composable
fun RankingItem(
    posicion: Int, 
    usuario: com.example.geohunt.data.UsuarioRanking,
    mostrarPuntos: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$posicion. ",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            if (posicion <= 3) {
                val trophyColor = when (posicion) {
                    1 -> Color(0xFFFFD700) // Oro
                    2 -> Color(0xFFC0C0C0) // Plata
                    else -> Color(0xFFCD7F32) // Bronce
                }
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trofeo",
                    tint = trophyColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (mostrarPuntos) "${usuario.puntosTotales} pts" else "Puntos: Oculto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (mostrarPuntos) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
