package com.example.geohunt.data.repository

import com.example.geohunt.data.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var userListener: ListenerRegistration? = null

    private val _currentUserState = MutableStateFlow<Usuario?>(null)
    val currentUserState = _currentUserState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                loadUserProfile(firebaseUser.uid)
                GeoHuntRepository.startListeningVisitas()
            } else {
                userListener?.remove()
                userListener = null
                _currentUserState.value = null
                GeoHuntRepository.stopListeningVisitas()
            }
        }
    }

    private fun loadUserProfile(uid: String) {
        userListener?.remove()
        userListener = db.collection("usuarios").document(uid).addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                _currentUserState.value = snapshot.toObject(Usuario::class.java)
            }
        }
    }

    suspend fun saveUserInFirestore(usuario: Usuario) {
        val userRef = db.collection("usuarios").document(usuario.uid)
        val snapshot = userRef.get().await()
        
        if (snapshot.exists()) {
            // No sobrescribimos el nombre si ya existe uno en Firestore
            // Esto respeta el cambio manual hecho en el perfil
            userRef.update(
                "fotoUrl", usuario.fotoUrl,
                "tipoLogin", usuario.tipoLogin
            ).await()
        } else {
            userRef.set(usuario).await()
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    fun getCurrentUid(): String? = auth.currentUser?.uid

    suspend fun signOut() {
        val user = _currentUserState.value
        val uid = auth.currentUser?.uid
        
        if (user != null && uid != null && user.tipoLogin == "anonimo") {
            try {
                // Si es anónimo, borramos sus datos antes de salir (limpieza de basura)
                GeoHuntRepository.deleteVisitasByUsuario(uid)
                db.collection("usuarios").document(uid).delete().await()
            } catch (e: Exception) { }
        }

        clearLocalData()
        auth.signOut()
    }

    suspend fun deleteAccount() {
        val uid = auth.currentUser?.uid ?: return
        try {
            // Borramos sus visitas y su documento de usuario
            GeoHuntRepository.deleteVisitasByUsuario(uid)
            db.collection("usuarios").document(uid).delete().await()
            
            // Borramos la cuenta de autenticación
            auth.currentUser?.delete()?.await()
        } finally {
            clearLocalData()
        }
    }

    private fun clearLocalData() {
        userListener?.remove()
        userListener = null
        _currentUserState.value = null
        GeoHuntRepository.stopListeningVisitas()
    }

    suspend fun getNombreUsuario(uid: String): String {
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            doc.getString("nombre") ?: "Jugador desconocido"
        } catch (e: Exception) {
            "Error al obtener nombre"
        }
    }
}
