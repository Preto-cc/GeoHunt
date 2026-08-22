package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.Usuario
import com.example.geohunt.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class WelcomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showNameDialog: Boolean = false,
    val isAuthenticated: Boolean = false
)

class WelcomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onGoogleSignInSuccess(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    val usuario = Usuario(
                        uid = firebaseUser.uid,
                        nombre = firebaseUser.displayName ?: "Jugador de Google",
                        tipoLogin = "google",
                        fotoUrl = firebaseUser.photoUrl?.toString()
                    )
                    AuthRepository.saveUserInFirestore(usuario)
                    _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("network") == true -> "No hay conexión a internet."
                    e.message?.contains("canceled") == true -> "Inicio de sesión cancelado."
                    else -> "Error al entrar con Google: ${e.localizedMessage ?: "inténtelo de nuevo"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
            }
        }
    }

    fun onAnonymousSignInClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                FirebaseAuth.getInstance().signInAnonymously().await()
                _uiState.value = _uiState.value.copy(isLoading = false, showNameDialog = true)
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("network") == true) "No hay conexión a internet."
                               else "Error al iniciar sesión anónima."
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
            }
        }
    }

    fun onNameEntered(nombre: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val usuario = Usuario(
                    uid = uid,
                    nombre = nombre,
                    tipoLogin = "anonimo"
                )
                AuthRepository.saveUserInFirestore(usuario)
                _uiState.value = _uiState.value.copy(isLoading = false, showNameDialog = false, isAuthenticated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = message)
    }
}
