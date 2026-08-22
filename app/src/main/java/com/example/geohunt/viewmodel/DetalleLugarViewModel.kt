package com.example.geohunt.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.Lugar
import com.example.geohunt.data.repository.AuthRepository
import com.example.geohunt.data.repository.GeoHuntRepository
import com.example.geohunt.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleLugarUiState(
    val lugar: Lugar? = null,
    val visitado: Boolean = false,
    val esPrimero: Boolean = false,
    val distancia: Float? = null,
    val fechaVisita: Long? = null,
    val mensaje: String? = null,
    val error: String? = null,
    val cargando: Boolean = false
)

class DetalleLugarViewModel(private val locationHelper: LocationHelper) : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleLugarUiState())
    val uiState: StateFlow<DetalleLugarUiState> = _uiState.asStateFlow()

    fun loadLugar(eventoId: String, lugarId: String) {
        viewModelScope.launch {
            val currentUid = AuthRepository.getCurrentUid() ?: ""
            val lugar = GeoHuntRepository.getLugarById(eventoId, lugarId)
            val esPrimero = lugar?.primerVisitanteId == currentUid
            val visitado = GeoHuntRepository.esLugarVisitado(currentUid, lugarId)
            val fechaVisita = if (visitado) GeoHuntRepository.getTimestampVisita(currentUid, lugarId) else null
            
            _uiState.value = _uiState.value.copy(
                lugar = lugar,
                visitado = visitado,
                esPrimero = esPrimero,
                fechaVisita = fechaVisita,
                mensaje = if (visitado) lugar?.mensajeOculto else null
            )
        }
    }

    fun comprobarDistancia() {
        val currentUid = AuthRepository.getCurrentUid() ?: return
        val lugar = _uiState.value.lugar ?: return
        if (_uiState.value.visitado) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            
            locationHelper.getCurrentLocation().onSuccess { location ->
                // Comprobación de fecha límite previa para feedback rápido
                val fechaFin = lugar.eventoId.let { id ->
                    // Tendríamos que haber cargado el evento o buscarlo
                    // Por simplicidad, el repositorio lanzará error en la transacción,
                    // pero podemos intentar obtenerlo aquí si lo necesitamos.
                }

                val results = FloatArray(1)
                Location.distanceBetween(location.latitude, location.longitude, lugar.lat, lugar.lng, results)
                val distancia = results[0]

                if (distancia <= lugar.radioMetros) {
                    val exito = GeoHuntRepository.registrarVisita(currentUid, lugar.id, lugar.eventoId)
                    if (exito) {
                        // Recargamos el lugar para saber si fuimos los primeros (atómico)
                        val lugarActualizado = GeoHuntRepository.getLugarById(lugar.eventoId, lugar.id)
                        val fuimosPrimeros = lugarActualizado?.primerVisitanteId == currentUid
                        val ahora = System.currentTimeMillis()
                        
                        _uiState.value = _uiState.value.copy(
                            visitado = true,
                            esPrimero = fuimosPrimeros,
                            distancia = distancia,
                            fechaVisita = ahora,
                            mensaje = lugar.mensajeOculto,
                            cargando = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "No se pudo registrar la visita. El evento puede haber finalizado.",
                            cargando = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        distancia = distancia,
                        cargando = false
                    )
                }
            }.onFailure { exception ->
                val errorMsg = when {
                    exception.message?.contains("GPS") == true -> exception.message!!
                    exception.message?.contains("permission") == true -> "Permiso de ubicación denegado."
                    else -> "No se pudo obtener la ubicación: ${exception.localizedMessage ?: "desconocido"}"
                }
                _uiState.value = _uiState.value.copy(
                    error = errorMsg,
                    cargando = false
                )
            }
        }
    }
}
