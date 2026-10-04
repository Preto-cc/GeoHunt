package com.example.geohunt.data

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val tipoLogin: String = "", // "google" o "anonimo"
    val fotoUrl: String? = null,
    @get:PropertyName("admin") @set:PropertyName("admin")
    var admin: Boolean = false
)
