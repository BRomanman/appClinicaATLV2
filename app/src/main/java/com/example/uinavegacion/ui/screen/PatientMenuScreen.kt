package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
// 1. Importar el nuevo ícono
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.ui.viewmodel.PatientMenuUiState
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModel

/**
 * Pantalla (conectada al VM) para el Menú de Paciente.
 */
@Composable
fun PatientMenuScreenVm(
    vm: PatientMenuViewModel,
    onGoBookAppointment: () -> Unit,
    onGoMyReservations: () -> Unit,
    onGoToProfile: () -> Unit, // <-- 2. AÑADIDO
    onLogout: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    PatientMenuScreen(
        state = state,
        onGoBookAppointment = onGoBookAppointment,
        onGoMyReservations = onGoMyReservations,
        onGoToProfile = onGoToProfile, // <-- 3. AÑADIDO
        onLogout = onLogout
    )
}


/**
 * Pantalla presentacional para el Menú de Paciente.
 */
@Composable
private fun PatientMenuScreen(
    state: PatientMenuUiState,
    onGoBookAppointment: () -> Unit,
    onGoMyReservations: () -> Unit,
    onGoToProfile: () -> Unit, // <-- 4. AÑADIDO
    onLogout: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.errorMsg != null) {
            Text(
                text = "Error: ${state.errorMsg}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Bienvenido, ${state.userName}",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Seleccione una opción para comenzar.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))

        val enabled = !state.isLoading

        // --- Botón 1: Agendar Cita ---
        Button(
            onClick = onGoBookAppointment,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.EventAvailable, contentDescription = "Agendar")
            Spacer(Modifier.width(8.dp))
            Text("Agendar Nueva Cita")
        }
        Spacer(Modifier.height(12.dp))

        // --- Botón 2: Mis Citas ---
        Button(
            onClick = onGoMyReservations,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Mis Citas")
            Spacer(Modifier.width(8.dp))
            Text("Ver Mis Citas")
        }
        Spacer(Modifier.height(12.dp))

        // --- Botón 3: Mi Perfil (NUEVO) ---
        Button(
            onClick = onGoToProfile, // <-- 5. AÑADIDO
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = "Mi Perfil")
            Spacer(Modifier.width(8.dp))
            Text("Ver Mi Perfil")
        }
        Spacer(Modifier.height(32.dp))

        // --- Botón 4: Cerrar Sesión ---
        OutlinedButton(
            onClick = onLogout,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión")
            Spacer(Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
    }
}