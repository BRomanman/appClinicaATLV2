package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// 1. Importar el DTO y el VM/State
import com.example.uinavegacion.R
import com.example.uinavegacion.data.local.appointment.AppointmentDetailsForDoctor
import com.example.uinavegacion.ui.viewmodel.DoctorProfileUiState
import com.example.uinavegacion.ui.viewmodel.DoctorProfileViewModel
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.ui.viewmodel.DoctorMenuUiState

/**
 * Pantalla (conectada al VM) para "Perfil de Doctor".
 * Sigue el patrón Vm/Screen, pero también recibe un 'doctorId' de la navegación.
 */
@Composable
fun DoctorProfileScreenVm(
    vm: DoctorProfileViewModel,
    doctorId: Long?, // El ID viene de la ruta de navegación
    onGoBack: () -> Unit
) {
    // 2. Observamos el ESTADO ÚNICO
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 3. Efecto de carga
    // Cuando la pantalla aparece, si el doctorId es válido,
    // le decimos al ViewModel que cargue los datos de ESE doctor.
    LaunchedEffect(doctorId) {
        if (doctorId != null) {
            vm.loadDoctorProfile(doctorId)
        }
    }

    // 4. Delegamos la UI a la pantalla presentacional
    DoctorProfileScreen(
        state = state,
        onRefresh = {
            if (doctorId != null) {
                vm.loadDoctorProfile(doctorId)
            }
        },
        onGoBack = onGoBack
    )
}

/**
 * Pantalla presentacional para "Perfil de Doctor".
 * Recrea el diseño de 'app_clinica_atl' original.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorProfileScreen(
    state: DoctorProfileUiState,
    onRefresh: () -> Unit,
    onGoBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.doctor?.name ?: "Perfil de Doctor") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
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

                // 2. Estado de Error (o ID nulo)
                state.errorMsg != null || state.doctor == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.errorMsg ?: "No se pudo cargar el doctor.",
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
                        // --- Sección de Info de Doctor ---
                        item {
                            DoctorProfileHeader(doctor = state.doctor)
                            Spacer(Modifier.height(24.dp))

                            // --- Sección de Agenda de Citas ---
                            Text(
                                text = "Agenda del Doctor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        // --- Lista de Citas ---
                        if (state.appointments.isEmpty()) {
                            item {
                                Text(
                                    text = "Este doctor no tiene citas agendadas.",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(state.appointments) { appointment ->
                                // Reutilizamos la tarjeta (copiándola) de 'DoctorMenuScreen'
                                DoctorAppointmentCard(item = appointment)
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
 * Componente para el encabezado del perfil del doctor.
 */
@Composable
private fun DoctorProfileHeader(doctor: UserEntity?) {
    if (doctor == null) return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Asignamos una imagen de placeholder basada en la especialidad
        val imageRes = when (doctor.specialty) {
            "Cardiología" -> R.drawable.doctor_cardio_1
            "Dermatología" -> R.drawable.doctor_derma_1
            "Medicina General" -> R.drawable.doctor_medgen_1
            "Nutrición" -> R.drawable.doctor_nutri_1
            else -> R.drawable.logo_clean // Imagen por defecto
        }

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Foto de ${doctor.name}",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = doctor.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = doctor.specialty ?: "Especialidad no definida",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = doctor.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/**
 * Un Composable 'Card' (privado) para mostrar la cita (vista Doctor).
 * Copiado de DoctorMenuScreen.kt
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