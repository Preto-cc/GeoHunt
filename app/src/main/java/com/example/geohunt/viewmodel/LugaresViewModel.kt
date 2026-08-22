package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.Lugar
import com.example.geohunt.data.repository.AuthRepository
import com.example.geohunt.data.repository.GeoHuntRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class LugarUiState(
    val lugar: Lugar,
    val visitado: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class LugaresViewModel : ViewModel() {
    private val _eventoId = MutableStateFlow("")
    
    val uiState: StateFlow<List<LugarUiState>> = _eventoId.flatMapLatest { eventoId ->
        combine(
            GeoHuntRepository.getLugaresByEvento(eventoId),
            GeoHuntRepository.visitas
        ) { lugares, visitas ->
            val currentUid = AuthRepository.getCurrentUid() ?: ""
            lugares.map { lugar ->
                val visitado = visitas.any { it.lugarId == lugar.id && it.usuarioId == currentUid }
                LugarUiState(lugar, visitado)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setEventoId(id: String) {
        _eventoId.value = id
    }
}
