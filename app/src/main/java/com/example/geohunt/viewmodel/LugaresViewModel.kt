package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.Lugar
import com.example.geohunt.data.repository.AuthRepository
import com.example.geohunt.data.repository.GeoHuntRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LugarUiState(
    val lugar: Lugar,
    val visitado: Boolean,
    val bloqueado: Boolean = false
)

data class EventoLugaresUiState(
    val lugares: List<LugarUiState> = emptyList(),
    val mostrarBloqueados: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class LugaresViewModel : ViewModel() {
    private val _eventoId = MutableStateFlow("")
    
    val uiState: StateFlow<EventoLugaresUiState> = _eventoId.flatMapLatest { eventoId ->
        if (eventoId.isEmpty()) return@flatMapLatest flowOf(EventoLugaresUiState())

        combine(
            GeoHuntRepository.getLugaresByEvento(eventoId),
            GeoHuntRepository.visitas,
            GeoHuntRepository.getEventos().map { list -> list.find { it.id == eventoId } }
        ) { lugares, visitas, evento ->
            val currentUid = AuthRepository.getCurrentUid() ?: ""
            
            val idsVisitadas = visitas
                .filter { it.usuarioId == currentUid && it.eventoId == eventoId }
                .map { it.lugarId }
                .toSet()

            val listaLugarUi = lugares.map { lugar ->
                val visitado = idsVisitadas.contains(lugar.id)
                val bloqueado = lugar.desbloqueadoPorLugarId != null && 
                               !idsVisitadas.contains(lugar.desbloqueadoPorLugarId)
                
                LugarUiState(lugar, visitado, bloqueado)
            }

            val mostrarBloqueados = evento?.mostrarLugaresBloqueados ?: true
            
            // Si mostrarBloqueados es false, filtramos los que estén bloqueados
            val listaFiltrada = if (mostrarBloqueados) {
                listaLugarUi
            } else {
                listaLugarUi.filter { !it.bloqueado }
            }

            EventoLugaresUiState(listaFiltrada, mostrarBloqueados)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventoLugaresUiState())

    fun setEventoId(id: String) {
        _eventoId.value = id
    }
}
