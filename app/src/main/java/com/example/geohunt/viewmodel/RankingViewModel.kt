package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModel : ViewModel() {
    private val _eventoId = MutableStateFlow("")

    val ranking: StateFlow<List<UsuarioRanking>> = _eventoId.flatMapLatest { eventoId ->
        combine(
            GeoHuntRepository.visitas,
            GeoHuntRepository.getLugaresByEvento(eventoId)
        ) { _, lugares ->
            val rankingBase = GeoHuntRepository.getRanking(eventoId, lugares)
            
            coroutineScope {
                rankingBase.map { rankingItem ->
                    async {
                        val nombreReal = AuthRepository.getNombreUsuario(rankingItem.nombre)
                        rankingItem.copy(nombre = nombreReal)
                    }
                }.awaitAll()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setEventoId(id: String) {
        _eventoId.value = id
    }
}
