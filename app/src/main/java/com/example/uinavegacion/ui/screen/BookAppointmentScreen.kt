package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.ui.viewmodel.AppointmentViewModel
import com.example.uinavegacion.ui.viewmodel.BookAppointmentUiState

/**
 * Pantalla para Agendar Cita (conectada al VM).
 */
@Composable
fun BookAppointmentScreenVm(
    vm: AppointmentViewModel,
    onBookingSuccessNavigate: () -> Unit,
    onGoToDoctorProfile: (Long) -> Unit // <-- 1. AÑADIDA NUEVA LAMBDA
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.bookingSuccess) {
        if (state.bookingSuccess) {
            vm.clearBookingResult()
            onBookingSuccessNavigate()
        }
    }

    BookAppointmentScreen(
        state = state,
        onSpecialtyChange = vm::onSpecialtyChange,
        onDoctorChange = vm::onDoctorChange,
        onDateChange = vm::onDateChange,
        onTimeChange = vm::onTimeChange,
        onSubmit = vm::submitBooking,
        onViewProfile = {
            // El botón solo está activo si el ID no es nulo
            state.selectedDoctorId?.let { id ->
                onGoToDoctorProfile(id) // <-- 2. LLAMAR A LA NAVEGACIÓN
            }
        }
    )
}

/**
 * Pantalla presentacional para Agendar Cita.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookAppointmentScreen(
    state: BookAppointmentUiState,
    onSpecialtyChange: (String) -> Unit,
    onDoctorChange: (UserEntity) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onViewProfile: () -> Unit // <-- 3. AÑADIDA NUEVA LAMBDA
) {
    val bg = MaterialTheme.colorScheme.tertiaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Agendar Cita",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(12.dp))

        Text(
            text = "Sigue el patrón de UI/VM de UINavegacion.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        // --- 1. Dropdown de Especialidades ---
        DropdownMenuField(
            label = "Especialidad",
            options = state.specialties,
            selectedOptionText = state.selectedSpecialty,
            onOptionSelected = { onSpecialtyChange(it) },
            enabled = !state.isBooking
        )
        Spacer(Modifier.height(8.dp))

        // --- 2. Dropdown de Doctores ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                DropdownMenuField(
                    label = "Doctor",
                    options = state.doctors.map { it.name },
                    selectedOptionText = state.selectedDoctorName,
                    onOptionSelected = { name ->
                        val doctor = state.doctors.first { it.name == name }
                        onDoctorChange(doctor)
                    },
                    enabled = !state.isBooking && state.doctors.isNotEmpty(),
                    isLoading = state.isLoadingDoctors
                )
            }
            // 4. AÑADIDO BOTÓN "VER PERFIL"
            TextButton(
                onClick = onViewProfile,
                // Solo activo si un doctor está seleccionado y no se está agendando
                enabled = !state.isBooking && state.selectedDoctorId != null,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Ver Perfil")
            }
        }
        Spacer(Modifier.height(8.dp))

        // --- 3. Selector de Fecha ---
        OutlinedTextField(
            value = state.selectedDate,
            onValueChange = onDateChange,
            label = { Text("Fecha (YYYY-MM-DD)") },
            placeholder = { Text("Ej: 2025-12-25") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBooking && state.selectedDoctorId != null,
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        // --- 4. Dropdown de Horas ---
        DropdownMenuField(
            label = "Hora",
            options = state.availableTimes,
            selectedOptionText = state.selectedTime,
            onOptionSelected = { onTimeChange(it) },
            enabled = !state.isBooking && state.availableTimes.isNotEmpty(),
            isLoading = state.isLoadingTimes
        )
        Spacer(Modifier.height(16.dp))

        // --- 5. Botón de Enviar ---
        Button(
            onClick = onSubmit,
            enabled = !state.isBooking && state.selectedTime.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isBooking) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Agendando...")
            } else {
                Text("Confirmar Cita")
            }
        }

        // --- 6. Mensaje de Error Global ---
        if (state.errorMsg != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMsg, color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * Componente reutilizable para un Dropdown Menu (Selector).
 * (Sin cambios)
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
    isError: Boolean = false
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
            isError = isError
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