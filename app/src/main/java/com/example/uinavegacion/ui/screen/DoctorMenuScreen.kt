package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Importar el DTO y el VM/State
import com.example.uinavegacion.data.local.appointment.AppointmentDetailsForDoctor
import com.example.uinavegacion.ui.viewmodel.DoctorMenuUiState
import com.example.uinavegacion.ui.viewmodel.DoctorMenuViewModel

/**
 * Pantalla (conectada al VM) para "Menú de Doctor".
 * Sigue el patrón de LoginScreenVm.
 */
@Composable
fun DoctorMenuScreenVm( // <-- Esta es la única definición pública de esta función
    vm: DoctorMenuViewModel,
    onLogout: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    DoctorMenuScreen(
        state = state,
        onRefresh = vm::loadDoctorData,
        onLogout = onLogout
    )
}

/**
 * Pantalla presentacional (privada) para "Menú de Doctor".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorMenuScreen(
    state: DoctorMenuUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Agenda") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    ) { innerPadding ->

        // Contenido de la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                // Estado de Carga
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Estado de Error
                state.errorMsg != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${state.errorMsg}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Estado con Datos (Éxito)
                else -> {
                    // Saludo + Lista
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Bienvenido, ${state.doctorName}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Estas son sus citas agendadas:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    if (state.appointments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No tienes citas en tu agenda.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        // Lista de Citas
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.appointments) { appointment ->
                                DoctorAppointmentCard(item = appointment)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Un Composable 'Card' (privado) para mostrar la cita (vista Doctor).
 */
@Composable
private fun DoctorAppointmentCard(item: AppointmentDetailsForDoctor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Paciente: ${item.patientName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Teléfono: ${item.patientPhone ?: "No disponible"}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fecha: ${item.date}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Hora: ${item.time}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Estado: ${item.status.uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}