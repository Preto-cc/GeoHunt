package com.example.geohunt.data.repository

import com.example.geohunt.data.Evento
import com.example.geohunt.data.Lugar
import com.example.geohunt.data.UsuarioRanking
import com.example.geohunt.data.Visita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object GeoHuntRepository {
    private val db = FirebaseFirestore.getInstance()
    private var visitasListener: ListenerRegistration? = null

    private val _visitas = MutableStateFlow<List<Visita>>(emptyList())
    val visitas: StateFlow<List<Visita>> = _visitas.asStateFlow()

    fun startListeningVisitas() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        visitasListener?.remove()
        visitasListener = db.collection("visitas")
            .whereEqualTo("usuarioId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || FirebaseAuth.getInstance().currentUser == null) return@addSnapshotListener
                if (snapshot != null) {
                    _visitas.value = snapshot.toObjects(Visita::class.java)
                }
            }
    }

    fun stopListeningVisitas() {
        visitasListener?.remove()
        visitasListener = null
        _visitas.value = emptyList()
    }

    fun getEventos(): Flow<List<Evento>> = callbackFlow {
        if (FirebaseAuth.getInstance().currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = db.collection("eventos")
            .whereEqualTo("activo", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || FirebaseAuth.getInstance().currentUser == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Evento::class.java)
                    trySend(list.sortedBy { it.orden })
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getEventosAdmin(): Flow<List<Evento>> = callbackFlow {
        if (FirebaseAuth.getInstance().currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = db.collection("eventos")
            .addSnapshotListener { snapshot, error ->
                if (error != null || FirebaseAuth.getInstance().currentUser == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Evento::class.java)
                    trySend(list.sortedBy { it.orden })
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getLugaresByEvento(eventoId: String): Flow<List<Lugar>> = callbackFlow {
        if (FirebaseAuth.getInstance().currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = db.collection("eventos").document(eventoId).collection("lugares")
            .addSnapshotListener { snapshot, error ->
                if (error != null || FirebaseAuth.getInstance().currentUser == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Lugar::class.java)
                    trySend(list.sortedBy { it.orden })
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getLugarById(eventoId: String, lugarId: String): Lugar? {
        return try {
            db.collection("eventos").document(eventoId).collection("lugares").document(lugarId)
                .get().await().toObject(Lugar::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun registrarVisita(usuarioId: String, lugarId: String, eventoId: String): Boolean {
        val visitaId = "${eventoId}_${lugarId}_${usuarioId}"
        val visitaRef = db.collection("visitas").document(visitaId)
        val lugarRef = db.collection("eventos").document(eventoId).collection("lugares").document(lugarId)
        val eventoRef = db.collection("eventos").document(eventoId)
        val rankingRef = db.collection("eventos").document(eventoId).collection("ranking").document(usuarioId)
        val userRef = db.collection("usuarios").document(usuarioId)

        return try {
            db.runTransaction { transaction ->
                // 1. Lecturas
                val eventoDoc = transaction.get(eventoRef)
                val fechaFin = eventoDoc.getLong("fechaFin")
                if (fechaFin != null && fechaFin > 0 && System.currentTimeMillis() > fechaFin) {
                    throw Exception("El evento ha finalizado")
                }

                val visitaDoc = transaction.get(visitaRef)
                if (visitaDoc.exists()) return@runTransaction false

                val lugarDoc = transaction.get(lugarRef)
                val puntosLugar = (lugarDoc.getLong("puntos") ?: 0L).toInt()
                val puntosBonusLugar = (lugarDoc.getLong("puntosBonus") ?: 0L).toInt()

                val primerId = lugarDoc.getString("primerVisitanteId")
                val esPrimero = primerId.isNullOrEmpty()

                val rankingDoc = transaction.get(rankingRef)
                val userDoc = transaction.get(userRef)
                val nombreUsuario = userDoc.getString("nombre") ?: "Usuario"

                // 2. Escrituras
                if (esPrimero) {
                    transaction.update(lugarRef, "primerVisitanteId", usuarioId)
                }

                val timestamp = System.currentTimeMillis()
                val nuevaVisita = Visita(usuarioId, lugarId, eventoId, timestamp, esPrimero)
                transaction.set(visitaRef, nuevaVisita)

                val puntosGanados = puntosLugar + if (esPrimero) puntosBonusLugar else 0
                val puntosActuales = (rankingDoc.getLong("puntosTotales") ?: 0L).toInt()
                val nuevosPuntos = puntosActuales + puntosGanados

                val rankingData = mapOf(
                    "uid" to usuarioId,
                    "nombre" to nombreUsuario,
                    "puntosTotales" to nuevosPuntos,
                    "ultimaVisita" to timestamp,
                    "ultimoLugarId" to lugarId
                )
                transaction.set(rankingRef, rankingData)

                true
            }.await()
        } catch (e: Exception) { false }
    }

    fun getRankingByEvento(eventoId: String): Flow<List<UsuarioRanking>> = callbackFlow {
        if (FirebaseAuth.getInstance().currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = db.collection("eventos").document(eventoId).collection("ranking")
            .orderBy("puntosTotales", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || FirebaseAuth.getInstance().currentUser == null) {
                    android.util.Log.e("GeoHuntRepository", "Error cargando ranking: ${error?.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(UsuarioRanking::class.java)
                        .sortedWith(compareByDescending<UsuarioRanking> { it.puntosTotales }.thenBy { it.ultimaVisita })
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun crearEvento(evento: Evento) {
        val ref = db.collection("eventos").document()
        val query = db.collection("eventos").orderBy("orden", Query.Direction.DESCENDING).limit(1).get().await()
        val nuevoOrden = if (query.isEmpty) 0 else (query.documents[0].getLong("orden") ?: 0L).toInt() + 1
        
        db.collection("eventos").document(ref.id).set(evento.copy(id = ref.id, orden = nuevoOrden)).await()
    }

    suspend fun actualizarEvento(eventoId: String, nombre: String, descripcion: String, activo: Boolean, fechaFin: Long?, rankingCercano: Boolean, mostrarLugaresBloqueados: Boolean) {
        val updates = mutableMapOf<String, Any?>(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "activo" to activo,
            "fechaFin" to fechaFin,
            "rankingCercano" to rankingCercano,
            "mostrarLugaresBloqueados" to mostrarLugaresBloqueados
        )
        db.collection("eventos").document(eventoId).update(updates).await()
    }

    suspend fun guardarOrdenEventos(eventos: List<Evento>) {
        val batch = db.batch()
        eventos.forEachIndexed { index, evento ->
            batch.update(db.collection("eventos").document(evento.id), "orden", index)
        }
        batch.commit().await()
    }

    suspend fun borrarEvento(eventoId: String) {
        try {
            val rankingQuery = db.collection("eventos").document(eventoId).collection("ranking").get().await()
            if (!rankingQuery.isEmpty) {
                val batchRanking = db.batch()
                rankingQuery.documents.forEach { batchRanking.delete(it.reference) }
                batchRanking.commit().await()
            }
            val visitasQuery = db.collection("visitas").whereEqualTo("eventoId", eventoId).get().await()
            if (!visitasQuery.isEmpty) {
                val batchVisitas = db.batch()
                visitasQuery.documents.forEach { batchVisitas.delete(it.reference) }
                batchVisitas.commit().await()
            }
            val lugaresRef = db.collection("eventos").document(eventoId).collection("lugares")
            val lugaresSnapshot = lugaresRef.get().await()
            if (!lugaresSnapshot.isEmpty) {
                val batchLugares = db.batch()
                lugaresSnapshot.documents.forEach { batchLugares.delete(it.reference) }
                batchLugares.commit().await()
            }
            db.collection("eventos").document(eventoId).delete().await()
        } catch (e: Exception) { }
    }

    suspend fun reiniciarEvento(eventoId: String) {
        try {
            val rankingQuery = db.collection("eventos").document(eventoId).collection("ranking").get().await()
            if (!rankingQuery.isEmpty) {
                val batchRanking = db.batch()
                rankingQuery.documents.forEach { batchRanking.delete(it.reference) }
                batchRanking.commit().await()
            }
            val visitasQuery = db.collection("visitas").whereEqualTo("eventoId", eventoId).get().await()
            if (!visitasQuery.isEmpty) {
                val batch = db.batch()
                visitasQuery.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
            val lugaresSnapshot = db.collection("eventos").document(eventoId).collection("lugares").get().await()
            if (!lugaresSnapshot.isEmpty) {
                val batch = db.batch()
                lugaresSnapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "primerVisitanteId", FieldValue.delete())
                }
                batch.commit().await()
            }
        } catch (e: Exception) { }
    }

    suspend fun crearLugar(eventoId: String, lugar: Lugar) {
        val ref = db.collection("eventos").document(eventoId).collection("lugares").document()
        val query = db.collection("eventos").document(eventoId).collection("lugares")
            .orderBy("orden", Query.Direction.DESCENDING).limit(1).get().await()
        val nuevoOrden = if (query.isEmpty) 0 else (query.documents[0].getLong("orden") ?: 0L).toInt() + 1

        db.collection("eventos").document(eventoId).collection("lugares").document(ref.id)
            .set(lugar.copy(id = ref.id, eventoId = eventoId, orden = nuevoOrden)).await()
    }

    suspend fun actualizarLugar(eventoId: String, lugar: Lugar) {
        db.collection("eventos").document(eventoId).collection("lugares").document(lugar.id).update(
            "nombre", lugar.nombre,
            "lat", lugar.lat,
            "lng", lugar.lng,
            "radioMetros", lugar.radioMetros,
            "mensajeOculto", lugar.mensajeOculto,
            "puntos", lugar.puntos,
            "puntosBonus", lugar.puntosBonus,
            "mostrarCoordenadas", lugar.mostrarCoordenadas,
            "desbloqueadoPorLugarId", lugar.desbloqueadoPorLugarId
        ).await()
    }

    suspend fun guardarOrdenLugares(eventoId: String, lugares: List<Lugar>) {
        val batch = db.batch()
        lugares.forEachIndexed { index, lugar ->
            batch.update(db.collection("eventos").document(eventoId).collection("lugares").document(lugar.id), "orden", index)
        }
        batch.commit().await()
    }

    suspend fun borrarLugar(eventoId: String, lugarId: String) {
        db.collection("eventos").document(eventoId).collection("lugares").document(lugarId).delete().await()
    }

    suspend fun deleteVisitasByUsuario(usuarioId: String) {
        try {
            val query = db.collection("visitas").whereEqualTo("usuarioId", usuarioId).get().await()
            if (!query.isEmpty) {
                val batch = db.batch()
                query.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        } catch (e: Exception) { }
    }

    suspend fun updateNombreEnRankings(usuarioId: String, nuevoNombre: String) {
        try {
            val eventosQuery = db.collection("eventos").get().await()
            eventosQuery.documents.forEach { eventoDoc ->
                val rankingDocRef = eventoDoc.reference.collection("ranking").document(usuarioId)
                val rankingDoc = rankingDocRef.get().await()
                if (rankingDoc.exists()) {
                    rankingDocRef.update("nombre", nuevoNombre).await()
                }
            }
        } catch (e: Exception) { }
    }

    fun esLugarVisitado(usuarioId: String, lugarId: String): Boolean {
        return _visitas.value.any { it.usuarioId == usuarioId && it.lugarId == lugarId }
    }

    fun getTimestampVisita(usuarioId: String, lugarId: String): Long? {
        return _visitas.value.find { it.usuarioId == usuarioId && it.lugarId == lugarId }?.timestamp
    }
}
