package com.example.geohunt.ui.screens

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geohunt.ui.components.PermissionRequest
import com.example.geohunt.utils.LocationHelper
import com.example.geohunt.viewmodel.DetalleLugarViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.random.Random

@Suppress("UNCHECKED_CAST")
class DetalleLugarViewModelFactory(private val locationHelper: LocationHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetalleLugarViewModel(locationHelper) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLugarScreen(
    eventoId: String,
    lugarId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val viewModel: DetalleLugarViewModel = viewModel(factory = DetalleLugarViewModelFactory(locationHelper))
    
    val uiState by viewModel.uiState.collectAsState()
    var showPermissionRequest by remember { mutableStateOf(false) }

    LaunchedEffect(eventoId, lugarId) {
        viewModel.loadLugar(eventoId, lugarId)
    }

    if (showPermissionRequest) {
        PermissionRequest(permission = Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
            showPermissionRequest = false
            if (granted) {
                viewModel.comprobarDistancia()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(uiState.lugar?.nombre ?: "Detalle", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val lugar = uiState.lugar
                if (lugar != null) {
                    Text(text = lugar.nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (lugar.mostrarCoordenadas || uiState.visitado) {
                        Text(text = "Coordenadas: ${lugar.lat}, ${lugar.lng}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val basePuntosText = "Radio: ${lugar.radioMetros}m | Puntos: ${lugar.puntos}"
                        Text(text = basePuntosText, style = MaterialTheme.typography.bodyMedium)
                        
                        if (uiState.esPrimero) {
                            val infiniteTransition = rememberInfiniteTransition(label = "shimmerPoints")
                            val offset by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1000f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "offsetPoints"
                            )

                            val goldenBrush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD4AF37),
                                    Color(0xFFFFD700),
                                    Color(0xFFFFFACD),
                                    Color(0xFFFFD700),
                                    Color(0xFFD4AF37)
                                ),
                                start = Offset(offset, offset),
                                end = Offset(offset + 100f, offset + 100f)
                            )
                            
                            Text(
                                text = " +${lugar.puntosBonus}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    brush = goldenBrush
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.bloqueado) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    text = "Este lugar está bloqueado. Debes visitar el lugar requerido para desbloquearlo.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else if (uiState.visitado) {
                        if (uiState.esPrimero) {
                            GoldenShimmerText("¡HAS SIDO EL PRIMERO EN VISITAR EL LUGAR!")
                        } else {
                            Text(
                                text = "¡LUGAR VISITADO!",
                                color = Color(0xFF4CAF50),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        uiState.fechaVisita?.let { timestamp ->
                            val sdf = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
                            val horaFormateada = sdf.format(java.util.Date(timestamp))
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Visitado a las $horaFormateada",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                text = uiState.mensaje ?: "",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Button(
                            onClick = { showPermissionRequest = true },
                            enabled = !uiState.cargando
                        ) {
                            if (uiState.cargando) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Comprobar distancia")
                            }
                        }

                        uiState.distancia?.let { dist ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Distancia actual: ${"%.2f".format(dist)} metros", style = MaterialTheme.typography.bodyLarge)
                        }

                        uiState.error?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = err, color = Color.Red)
                        }
                    }
                }
            }
        }
        
        // Capa de confeti (solo si es el primero)
        if (uiState.esPrimero) {
            ConfettiEfecto()
        }
    }
}

@Composable
fun GoldenShimmerText(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD4AF37), // Oro viejo
            Color(0xFFFFD700), // Oro brillante
            Color(0xFFFFFACD), // Oro pálido (reflejo)
            Color(0xFFFFD700),
            Color(0xFFD4AF37)
        ),
        start = Offset(offset, offset),
        end = Offset(offset + 300f, offset + 300f)
    )

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            brush = brush
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ConfettiEfecto() {
    val particles = remember { List(80) { ConfettiParticle() } }
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(4000, easing = LinearOutSlowInEasing)
        )
    }

    val progress = animatable.value
    if (progress < 1f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val y = particle.startY + (progress * size.height * 1.5f * particle.speed)
                val x = particle.startX + (Math.sin(progress.toDouble() * 10 * particle.drift).toFloat() * 50f)
                
                if (y < size.height) {
                    drawCircle(
                        color = particle.color,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

class ConfettiParticle {
    val startX = Random.nextFloat() * 1000f
    val startY = -Random.nextFloat() * 1000f
    val speed = 0.5f + Random.nextFloat()
    val drift = Random.nextFloat()
    val color = listOf(
        Color(0xFFFFD700), Color(0xFFFF4081), Color(0xFF00E5FF), 
        Color(0xFF76FF03), Color(0xFFFFEA00)
    ).random()
}
