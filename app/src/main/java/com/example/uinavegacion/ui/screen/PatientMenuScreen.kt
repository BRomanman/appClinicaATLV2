package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
// 1. Importar el VM y el State
import com.example.uinavegacion.ui.viewmodel.PatientMenuUiState
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModel

/**
 * Pantalla (conectada al VM) para el Menú de Paciente.
 * Sigue el patrón de LoginScreenVm.
 */
@Composable
fun PatientMenuScreenVm(
    vm: PatientMenuViewModel, // <-- 2. Recibe el VM
    onGoBookAppointment: () -> Unit,
    onGoMyReservations: () -> Unit,
    onLogout: () -> Unit
) {
    // 3. Observamos el ESTADO ÚNICO
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 4. Delegamos la UI a la pantalla presentacional
    PatientMenuScreen(
        state = state, // <-- Pasamos el estado
        onGoBookAppointment = onGoBookAppointment,
        onGoMyReservations = onGoMyReservations,
        onLogout = onLogout
    )
}


/**
 * Pantalla presentacional para el Menú de Paciente.
 * Ahora recibe el State y es "tonta".
 */
@Composable
private fun PatientMenuScreen(
    state: PatientMenuUiState, // <-- 5. Recibe el UiState
    onGoBookAppointment: () -> Unit,
    onGoMyReservations: () -> Unit,
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
        // 6. Lógica de UI basada en el estado
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.errorMsg != null) {
            Text(
                text = "Error: ${state.errorMsg}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        } else {
            // 7. ¡AQUÍ ESTÁ LA MAGIA! Usamos el nombre del estado
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

        // Botones (sin cambios, pero se desactivarían si está cargando)
        val enabled = !state.isLoading

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

        Button(
            onClick = onGoMyReservations,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Mis Citas")
            Spacer(Modifier.width(8.dp))
            Text("Ver Mis Citas")
        }
        Spacer(Modifier.height(32.dp))

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