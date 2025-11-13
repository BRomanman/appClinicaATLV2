package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// 1. ELIMINAMOS IMPORTS INNECESARIOS (LocalContext, UserPreferences)
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.ui.viewmodel.AuthViewModel


@Composable
fun LoginScreenVm(
    vm: AuthViewModel,
    onLoginOkNavigate: (String?) -> Unit,
    onGoRegister: () -> Unit
) {

    val state by vm.login.collectAsStateWithLifecycle()

    // 2. ELIMINAMOS 'LocalContext' y 'UserPreferences'.
    // La UI ya no sabe nada sobre el guardado de datos.

    // 3. ACTUALIZAMOS EL LaunchedEffect
    // Ahora reacciona a los 3 campos (success, role, id)
    LaunchedEffect(state.success, state.loggedInUserRole, state.loggedInUserId) {
        // Solo navegamos si los 3 campos están listos
        if (state.success && state.loggedInUserRole != null && state.loggedInUserId != null) {

            // 4. ELIMINAMOS la lógica de 'userPrefs' de aquí.

            val role = state.loggedInUserRole // Obtenemos el rol
            vm.clearLoginResult() // Limpia banderas en el VM
            onLoginOkNavigate(role) // Navega
        }
    }


    LoginScreen(
        email = state.email,
        pass = state.pass,
        emailError = state.emailError,
        passError = state.passError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onEmailChange = vm::onLoginEmailChange,
        onPassChange = vm::onLoginPassChange,
        onSubmit = vm::submitLogin,
        onGoRegister = onGoRegister
    )
}


// (La UI presentacional 'LoginScreen' no cambia)

@Composable
private fun LoginScreen(
    email: String,
    pass: String,
    emailError: String?,
    passError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoRegister: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.secondaryContainer
    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Pantalla de Login (demo). Usa la barra superior, el menú lateral o los botones.",
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError != null) {
                Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = passError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (passError != null) {
                Text(passError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Validando...")
                } else {
                    Text("Entrar")
                }
            }

            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
        }
    }
}