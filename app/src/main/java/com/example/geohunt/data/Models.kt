package com.example.geohunt.data

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Evento(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val fechaInicio: Long = 0L,
    val fechaFin: Long? = null,
    val activo: Boolean = true,
    val orden: Int = 0,
    val rankingCercano: Boolean = false
)

@Keep
data class Lugar(
    val id: String = "",
    val eventoId: String = "",
    val nombre: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radioMetros: Float = 0f,
    val mensajeOculto: String = "",
    val puntos: Int = 0,
    val puntosBonus: Int = 0,
    val primerVisitanteId: String? = null,
    val orden: Int = 0,
    val mostrarCoordenadas: Boolean = true
)

@Keep
data class Visita(
    val usuarioId: String = "",
    val lugarId: String = "",
    val eventoId: String = "",
    val timestamp: Long = 0L,
    @get:PropertyName("esPrimero") @set:PropertyName("esPrimero")
    var esPrimero: Boolean = false
)

@Keep
data class UsuarioRanking(
    val uid: String = "",
    val nombre: String = "",
    val puntosTotales: Int = 0,
    val ultimaVisita: Long = 0L
)
