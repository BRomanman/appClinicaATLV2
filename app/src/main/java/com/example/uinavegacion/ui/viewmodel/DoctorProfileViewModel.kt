package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 1. Importamos los DTOs y dependencias
import com.example.uinavegacion.data.local.appointment.AppointmentDetailsForDoctor
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla de Perfil de Doctor.
 * Sigue el patrón estricto de UINavegacion (un solo data class).
 */
data class DoctorProfileUiState(
    val isLoading: Boolean = true,
    val doctor: UserEntity? = null,
    val appointments: List<AppointmentDetailsForDoctor> = emptyList(),
    val errorMsg: String? = null
)

/**
 * ViewModel para el Perfil de Doctor.
 * Carga el perfil de un doctor específico por su ID.
 */
class DoctorProfileViewModel(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    // 2. EL ESTADO ÚNICO OBSERVABLE
    private val _uiState = MutableStateFlow(DoctorProfileUiState())
    val uiState: StateFlow<DoctorProfileUiState> = _uiState

    /**
     * Carga el perfil del doctor y su agenda.
     * Esta función es llamada desde la UI (ScreenVm) cuando recibe el ID.
     */
    fun loadDoctorProfile(doctorId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // 3. Carga de datos en PARALELO
            val userResultDeferred = async { userRepository.getUserById(doctorId) }
            val appointmentsResultDeferred = async { appointmentRepository.getAppointmentDetailsForDoctor(doctorId) }

            // Esperamos a que ambas terminen
            val userResult = userResultDeferred.await()
            val appointmentsResult = appointmentsResultDeferred.await()

            // 4. Procesamos y actualizamos el estado
            if (userResult.isFailure || appointmentsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar el perfil del doctor."
                    )
                }
            } else {
                // ¡Éxito!
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        doctor = userResult.getOrNull(),
                        appointments = appointmentsResult.getOrNull() ?: emptyList(),
                        errorMsg = null
                    )
                }
            }
        }
    }
}