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
 * Sigue el patrón de LoginScreenVm.
 */
@Composable
fun BookAppointmentScreenVm(
    vm: AppointmentViewModel,
    onBookingSuccessNavigate: () -> Unit // Acción para navegar tras éxito
) {
    // 1. Observamos el ESTADO ÚNICO
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 2. Reaccionamos a los eventos de éxito (igual que en LoginScreen)
    LaunchedEffect(state.bookingSuccess) {
        if (state.bookingSuccess) {
            vm.clearBookingResult() // Limpia la bandera en el VM
            onBookingSuccessNavigate() // Navega
        }
    }

    // 3. Delegamos la UI a la pantalla presentacional
    BookAppointmentScreen(
        state = state,
        onSpecialtyChange = vm::onSpecialtyChange,
        onDoctorChange = vm::onDoctorChange,
        onDateChange = vm::onDateChange,
        onTimeChange = vm::onTimeChange,
        onSubmit = vm::submitBooking
    )
}

/**
 * Pantalla presentacional para Agendar Cita.
 * Solo recibe estado y lambdas.
 */
@OptIn(ExperimentalMaterial3Api::class) // Necesario para ExposedDropdownMenuBox
@Composable
private fun BookAppointmentScreen(
    state: BookAppointmentUiState,
    onSpecialtyChange: (String) -> Unit,
    onDoctorChange: (UserEntity) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.tertiaryContainer // Fondo distinto

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Para pantallas pequeñas
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

        // --- 2. Dropdown de Doctores (depende de especialidad) ---
        DropdownMenuField(
            label = "Doctor",
            options = state.doctors.map { it.name }, // Mostramos nombres
            selectedOptionText = state.selectedDoctorName,
            onOptionSelected = { name ->
                // Buscamos el UserEntity completo por el nombre
                val doctor = state.doctors.first { it.name == name }
                onDoctorChange(doctor)
            },
            enabled = !state.isBooking && state.doctors.isNotEmpty(),
            isLoading = state.isLoadingDoctors
        )
        Spacer(Modifier.height(8.dp))

        // --- 3. Selector de Fecha (simplificado como TextField) ---
        // TODO: Reemplazar con un DatePicker Dialog
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

        // --- 4. Dropdown de Horas (depende de fecha) ---
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuField(
    label: String,
    options: List<String>,
    selectedOptionText: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled && !isLoading) expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // Importante
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
            enabled = enabled
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