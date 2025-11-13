package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
// 1. Importar el DTO y el VM/State
import com.example.uinavegacion.data.local.appointment.AppointmentDetails
import com.example.uinavegacion.ui.viewmodel.MyReservationsUiState
import com.example.uinavegacion.ui.viewmodel.MyReservationsViewModel

/**
 * Pantalla (conectada al VM) para "Mis Citas".
 * Sigue el patrón de LoginScreenVm.
 */
@Composable
fun MyReservationsScreenVm(
    vm: MyReservationsViewModel, // <-- 2. Recibe el VM
    onGoBack: () -> Unit // Lambda para volver al menú
) {
    // 3. Observamos el ESTADO ÚNICO
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 4. Delegamos la UI a la pantalla presentacional
    MyReservationsScreen(
        state = state,
        onRefresh = vm::loadReservations, // Exponemos la acción de refrescar
        onGoBack = onGoBack
    )
}

/**
 * Pantalla presentacional para "Mis Citas".
 * Muestra una lista de 'AppointmentDetails'.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyReservationsScreen(
    state: MyReservationsUiState,
    onRefresh: () -> Unit,
    onGoBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Citas") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->

        // Contenido de la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp), // Padding lateral para las tarjetas
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. Estado de Carga
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                // 2. Estado de Error
                state.errorMsg != null -> {
                    Text(
                        text = "Error: ${state.errorMsg}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // 3. Estado Vacío
                state.reservations.isEmpty() -> {
                    Text(
                        text = "No tienes citas agendadas.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // 4. Estado con Datos (Éxito)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.reservations) { reservation ->
                            ReservationItemCard(item = reservation)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Un Composable 'Card' para mostrar un ítem de la lista de citas.
 */
@Composable
private fun ReservationItemCard(item: AppointmentDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Dr(a). ${item.doctorName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.doctorSpecialty ?: "Medicina General",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
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
                fontWeight = FontWeight.SemiBold,
                color = when(item.status) {
                    "agendada" -> MaterialTheme.colorScheme.tertiary
                    "cancelada" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}