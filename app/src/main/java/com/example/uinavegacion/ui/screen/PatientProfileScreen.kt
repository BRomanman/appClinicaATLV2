package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// 1. Importar el DTO y el VM/State
import com.example.uinavegacion.data.local.appointment.AppointmentDetails
import com.example.uinavegacion.ui.viewmodel.PatientProfileUiState
import com.example.uinavegacion.ui.viewmodel.PatientProfileViewModel

/**
 * Pantalla (conectada al VM) para "Perfil de Paciente".
 * Sigue el patrón de LoginScreenVm.
 */
@Composable
fun PatientProfileScreenVm(
    vm: PatientProfileViewModel, // <-- 2. Recibe el VM
    onGoBack: () -> Unit // Lambda para volver al menú
) {
    // 3. Observamos el ESTADO ÚNICO
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 4. Delegamos la UI a la pantalla presentacional
    PatientProfileScreen(
        state = state,
        onRefresh = vm::loadProfileData, // Exponemos la acción de refrescar
        onGoBack = onGoBack
    )
}

/**
 * Pantalla presentacional para "Perfil de Paciente".
 * Replicamos el diseño de 'app_clinica_atl' original pero con nuestra arquitectura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientProfileScreen(
    state: PatientProfileUiState,
    onRefresh: () -> Unit,
    onGoBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                // 1. Estado de Carga
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // 2. Estado de Error
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

                // 3. Estado con Datos (Éxito)
                else -> {
                    // Contenido del Perfil
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // --- Sección de Info de Usuario ---
                        item {
                            // Placeholder para la imagen de perfil
                            Image(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto de Perfil",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = state.userName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))

                            // Info de Contacto
                            ProfileInfoRow(
                                icon = Icons.Default.Email,
                                text = state.userEmail
                            )
                            ProfileInfoRow(
                                icon = Icons.Default.Phone,
                                text = state.userPhone
                            )

                            Spacer(Modifier.height(24.dp))

                            // --- Sección de Historial de Citas ---
                            Text(
                                text = "Historial de Citas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        // --- Lista de Citas ---
                        if (state.appointments.isEmpty()) {
                            item {
                                Text(
                                    text = "No tienes citas en tu historial.",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(state.appointments) { reservation ->
                                // Reutilizamos la tarjeta de 'MyReservationsScreen'
                                ReservationItemCard(item = reservation)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente para mostrar una línea de info (Icono + Texto)
 */
@Composable
private fun ProfileInfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Un Composable 'Card' (privado) para mostrar un ítem de la lista de citas.
 * Copiado de MyReservationsScreen.kt
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