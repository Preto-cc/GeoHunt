package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.Evento
import com.example.geohunt.data.UsuarioRanking
import com.example.geohunt.data.repository.AuthRepository
import com.example.geohunt.data.repository.GeoHuntRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RankingUiState(
    val items: List<UsuarioRanking> = emptyList(),
    val rankingCercano: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModel : ViewModel() {
    private val _eventoId = MutableStateFlow("")

    val uiState: StateFlow<RankingUiState> = _eventoId.flatMapLatest { eventoId ->
        if (eventoId.isEmpty()) return@flatMapLatest flowOf(RankingUiState())
        
        combine(
            GeoHuntRepository.visitas,
            GeoHuntRepository.getLugaresByEvento(eventoId),
            GeoHuntRepository.getEventosAdmin().map { list -> list.find { it.id == eventoId } }
        ) { _, lugares, evento ->
            val rankingBase = GeoHuntRepository.getRanking(eventoId, lugares)
            
            val itemsConNombre = coroutineScope {
                rankingBase.map { rankingItem ->
                    async {
                        val nombreReal = AuthRepository.getNombreUsuario(rankingItem.uid)
                        rankingItem.copy(nombre = nombreReal)
                    }
                }.awaitAll()
            }
            
            RankingUiState(
                items = itemsConNombre,
                rankingCercano = evento?.rankingCercano ?: false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RankingUiState())

    fun setEventoId(id: String) {
        _eventoId.value = id
    }
}
