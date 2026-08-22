package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.Evento
import com.example.geohunt.data.repository.AuthRepository
import com.example.geohunt.data.repository.GeoHuntRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EventoUiState(
    val evento: Evento,
    val visitados: Int,
    val totales: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class EventosViewModel : ViewModel() {
    val currentUser = AuthRepository.currentUserState

    val uiState: StateFlow<List<EventoUiState>> = combine(
        GeoHuntRepository.getEventos(),
        GeoHuntRepository.visitas,
        currentUser
    ) { eventos, visitas, user ->
        val uid = user?.uid ?: ""
        
        // Para cada evento, necesitamos saber el total de lugares y los visitados por este usuario
        eventos.map { evento ->
            // Nota: Aquí lo ideal sería que el repositorio nos diera el conteo. 
            // Por simplicidad en esta fase, el repositorio filtrará los que ya tenemos en memoria
            val visitasEventoUser = visitas.filter { it.eventoId == evento.id && it.usuarioId == uid }.size
            
            // Obtenemos el total de lugares (esto dispara una pequeña suscripción interna)
            // Para no complicar la reactividad, usaremos una aproximación: 
            // Mostramos el progreso basado en las visitas registradas vs lo que el admin defina.
            // MEJORA: El repositorio ahora nos ayudará con un Flow de conteos
            EventoUiState(evento, visitasEventoUser, 0) // El total lo rellenaremos en la UI o con un helper
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Versión mejorada que carga los totales
    val eventosConProgreso: StateFlow<List<EventoUiState>> = GeoHuntRepository.getEventos().flatMapLatest { listaEventos ->
        val flows = listaEventos.map { evento ->
            combine(
                GeoHuntRepository.getLugaresByEvento(evento.id),
                GeoHuntRepository.visitas,
                currentUser
            ) { lugares, visitas, user ->
                val uid = user?.uid ?: ""
                val visitados = visitas.count { it.eventoId == evento.id && it.usuarioId == uid }
                EventoUiState(evento, visitados, lugares.size)
            }
        }
        if (flows.isEmpty()) flowOf(emptyList()) else combine(flows) { it.toList() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun signOut() {
        viewModelScope.launch {
            AuthRepository.signOut()
        }
    }
}
