package com.example.geohunt.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geohunt.viewmodel.WelcomeViewModel
import kotlinx.coroutines.launch
import com.example.geohunt.R

@Composable
fun WelcomeScreen(
    onLoginSuccess: () -> Unit,
    viewModel: WelcomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    
    val webClientId = "608441159192-i0jbelo7d4vdpb1kau6uhlo8e4v7te4p.apps.googleusercontent.com"

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    if (uiState.showNameDialog) {
        NameDialog(onConfirm = { viewModel.onNameEntered(it) })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo de la App (Usamos el foreground que es el dibujo real)
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logo GeoHunt",
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("GeoHunt", style = MaterialTheme.typography.headlineLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
        Text("La búsqueda del tesoro comienza", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(48.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    
                    scope.launch {
                        try {
                            val result = credentialManager.getCredential(context, request)
                            val idToken = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                            viewModel.onGoogleSignInSuccess(idToken)
                        } catch (e: Exception) {
                            viewModel.onGoogleSignInError(e.message ?: "No se pudo iniciar sesión con Google")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Continuar con Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.onAnonymousSignInClick() },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Continuar solo con nombre")
            }
        }

        uiState.error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun NameDialog(onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
        title = { Text("Introduce tu nombre") },
        text = {
            TextField(value = name, onValueChange = { name = it }, placeholder = { Text("Nombre") })
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Confirmar")
            }
        }
    )
}
