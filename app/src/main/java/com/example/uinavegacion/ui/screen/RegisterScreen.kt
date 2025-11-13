package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Import para scroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll // Import para scroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
// 1. Importamos el DropdownMenuField que creamos en el Paso 4
import com.example.uinavegacion.ui.screen.DropdownMenuField
// 2. Importamos el estado que modificamos en el Paso 3
import com.example.uinavegacion.ui.viewmodel.RegisterUiState


/**
 * Pantalla de Registro (conectada al VM).
 * Sigue el patrón de LoginScreenVm.
 */
@Composable
fun RegisterScreenVm(
    vm: AuthViewModel,
    onRegisteredNavigateLogin: () -> Unit, // Navega a Login si el registro es OK
    onGoLogin: () -> Unit // Navega a Login
) {
    // 3. Observamos el StateFlow de REGISTRO
    val state by vm.register.collectAsStateWithLifecycle()

    // 4. Reaccionamos al éxito (igual que en Login)
    LaunchedEffect(state.success) {
        if (state.success) {
            vm.clearRegisterResult() // Limpia bandera
            onRegisteredNavigateLogin() // Navega
        }
    }

    // 5. Delegamos a la UI presentacional
    RegisterScreen(
        state = state, // Pasamos el estado completo
        onNameChange = vm::onNameChange,
        onEmailChange = vm::onRegisterEmailChange,
        onPhoneChange = vm::onPhoneChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onRoleChange = vm::onRoleChange, // <-- NUEVO HANDLER
        onSpecialtyChange = vm::onSpecialtyChange, // <-- NUEVO HANDLER
        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}

/**
 * Pantalla presentacional de Registro (solo UI).
 * Ahora recibe el RegisterUiState completo.
 */
@Composable
private fun RegisterScreen(
    state: RegisterUiState, // <-- RECIBE EL ESTADO COMPLETO
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onRoleChange: (String) -> Unit, // <-- NUEVO
    onSpecialtyChange: (String) -> Unit, // <-- NUEVO
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.tertiaryContainer // Fondo distinto
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            // 6. Añadimos scroll vertical por si el formulario no cabe
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Registro",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Crea tu cuenta para la Clínica.",
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            // --- CAMPO NOMBRE ---
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Nombre Completo") },
                singleLine = true,
                isError = state.nameError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.nameError != null) {
                Text(state.nameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))

            // --- CAMPO EMAIL ---
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                isError = state.emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.emailError != null) {
                Text(state.emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))

            // --- CAMPO TELÉFONO ---
            OutlinedTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono") },
                singleLine = true,
                isError = state.phoneError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.phoneError != null) {
                Text(state.phoneError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))

            // 7. --- CAMPO ROL (NUEVO) ---
            DropdownMenuField(
                label = "Rol de Usuario",
                options = listOf("paciente", "doctor", "admin"), // Opciones de rol
                selectedOptionText = state.role, // Valor del state
                onOptionSelected = onRoleChange, // Handler del VM
                enabled = !state.isSubmitting
            )
            Spacer(Modifier.height(8.dp))

            // 8. --- CAMPO ESPECIALIDAD (NUEVO Y CONDICIONAL) ---
            // Solo se muestra si el rol seleccionado es "doctor"
            if (state.role == "doctor") {
                DropdownMenuField(
                    label = "Especialidad",
                    options = state.availableSpecialties, // Lista del state
                    selectedOptionText = state.specialty ?: "", // Maneja nulo
                    onOptionSelected = onSpecialtyChange, // Handler del VM
                    enabled = !state.isSubmitting,
                    isError = state.specialtyError != null // Marca error
                )
                if (state.specialtyError != null) {
                    Text(state.specialtyError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(8.dp))
            }

            // --- CAMPO CONTRASEÑA ---
            OutlinedTextField(
                value = state.pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña") },
                singleLine = true,
                isError = state.passError != null,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPass) "Ocultar" else "Mostrar"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (state.passError != null) {
                Text(state.passError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))

            // --- CAMPO CONFIRMAR CONTRASEÑA ---
            OutlinedTextField(
                value = state.confirm,
                onValueChange = onConfirmChange,
                label = { Text("Confirmar Contraseña") },
                singleLine = true,
                isError = state.confirmError != null,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showConfirm) "Ocultar" else "Mostrar"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (state.confirmError != null) {
                Text(state.confirmError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(16.dp))

            // --- BOTÓN REGISTRAR ---
            Button(
                onClick = onSubmit,
                enabled = state.canSubmit && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Registrando...")
                } else {
                    Text("Crear Cuenta")
                }
            }

            if (state.errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.errorMsg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            // --- BOTÓN IR A LOGIN ---
            OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Ya tengo cuenta (Ir a Login)")
            }
        }
    }
}

/**
 * 9. COPIAMOS EL HELPER DROPDOWN
 * Componente reutilizable para un Dropdown Menu (Selector).
 * Lo copiamos de BookAppointmentScreen para usarlo aquí también.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuField(
    label: String,
    options: List<String>,
    selectedOptionText: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isError: Boolean = false // <-- Añadido para marcar error
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled && !isLoading) expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedOptionText,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            enabled = enabled,
            isError = isError // <-- Pasamos el error
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}