package com.example.geohunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geohunt.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val nombre: String = "",
    val isLoading: Boolean = false,
    val updateSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val user = AuthRepository.currentUserState.value
        if (user != null) {
            _uiState.value = _uiState.value.copy(nombre = user.nombre)
        }
    }

    fun onNombreChange(nuevoNombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nuevoNombre)
    }

    fun guardarCambios() {
        val uid = AuthRepository.getCurrentUid() ?: return
        val nuevoNombre = _uiState.value.nombre
        if (nuevoNombre.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                    .update("nombre", nuevoNombre).await()
                _uiState.value = _uiState.value.copy(isLoading = false, updateSuccess = true)
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("network") == true) "No hay conexión a internet."
                               else "No se pudo guardar el nombre."
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
            }
        }
    }

    fun borrarCuenta() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                AuthRepository.deleteAccount()
                _uiState.value = _uiState.value.copy(isLoading = false, deleteSuccess = true)
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("recent-login") == true -> "Por seguridad, debes cerrar sesión e iniciarla de nuevo para borrar tu cuenta."
                    e.message?.contains("network") == true -> "No hay conexión a internet."
                    else -> "No se pudo borrar la cuenta: ${e.localizedMessage ?: "inténtelo de nuevo"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
            }
        }
    }
}
