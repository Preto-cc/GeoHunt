package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.Evento
import com.example.geohunt.data.Lugar
import com.example.geohunt.data.repository.GeoHuntRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    val eventos: StateFlow<List<Evento>> = GeoHuntRepository.getEventosAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun crearEvento(nombre: String, descripcion: String, fechaFin: Long?) {
        viewModelScope.launch {
            val evento = Evento(
                nombre = nombre,
                descripcion = descripcion,
                fechaInicio = System.currentTimeMillis(),
                fechaFin = fechaFin
            )
            GeoHuntRepository.crearEvento(evento)
        }
    }

    fun actualizarEvento(eventoId: String, nombre: String, descripcion: String, activo: Boolean, fechaFin: Long?) {
        viewModelScope.launch {
            GeoHuntRepository.actualizarEvento(eventoId, nombre, descripcion, activo, fechaFin)
        }
    }

    fun moverEvento(desdeIndex: Int, haciaIndex: Int) {
        val lista = eventos.value.toMutableList()
        if (desdeIndex !in lista.indices || haciaIndex !in lista.indices) return
        
        val item = lista.removeAt(desdeIndex)
        lista.add(haciaIndex, item)
        
        viewModelScope.launch {
            GeoHuntRepository.guardarOrdenEventos(lista)
        }
    }

    fun borrarEvento(eventoId: String) {
        viewModelScope.launch {
            GeoHuntRepository.borrarEvento(eventoId)
        }
    }

    fun reiniciarEvento(eventoId: String) {
        viewModelScope.launch {
            GeoHuntRepository.reiniciarEvento(eventoId)
        }
    }

    fun crearLugar(eventoId: String, nombre: String, lat: Double, lng: Double, radio: Float, msg: String, pts: Int, bonus: Int, mostrarCoordenadas: Boolean) {
        viewModelScope.launch {
            val lugar = Lugar(
                eventoId = eventoId,
                nombre = nombre,
                lat = lat,
                lng = lng,
                radioMetros = radio,
                mensajeOculto = msg,
                puntos = pts,
                puntosBonus = bonus,
                mostrarCoordenadas = mostrarCoordenadas
            )
            GeoHuntRepository.crearLugar(eventoId, lugar)
        }
    }

    fun moverLugar(eventoId: String, lugares: List<Lugar>, desdeIndex: Int, haciaIndex: Int) {
        val lista = lugares.toMutableList()
        if (desdeIndex !in lista.indices || haciaIndex !in lista.indices) return
        
        val item = lista.removeAt(desdeIndex)
        lista.add(haciaIndex, item)
        
        viewModelScope.launch {
            GeoHuntRepository.guardarOrdenLugares(eventoId, lista)
        }
    }

    fun actualizarLugar(eventoId: String, lugar: Lugar) {
        viewModelScope.launch {
            GeoHuntRepository.actualizarLugar(eventoId, lugar)
        }
    }

    fun borrarLugar(eventoId: String, lugarId: String) {
        viewModelScope.launch {
            GeoHuntRepository.borrarLugar(eventoId, lugarId)
        }
    }
}
