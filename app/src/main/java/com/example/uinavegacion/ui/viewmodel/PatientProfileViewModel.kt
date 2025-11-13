package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 1. Importamos las dependencias y DTOs necesarios
import com.example.uinavegacion.data.local.appointment.AppointmentDetails
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla PatientProfile.
 * Contiene TODOS los datos que la UI necesita para dibujarse.
 */
data class PatientProfileUiState(
    val isLoading: Boolean = true,
    // Detalles del usuario
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    // Historial de citas
    val appointments: List<AppointmentDetails> = emptyList(),
    val errorMsg: String? = null
)

/**
 * ViewModel para el Perfil de Paciente.
 * Carga los datos del usuario y su historial de citas.
 */
class PatientProfileViewModel(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // 2. EL ESTADO ÚNICO OBSERVABLE
    private val _uiState = MutableStateFlow(PatientProfileUiState())
    val uiState: StateFlow<PatientProfileUiState> = _uiState

    // 3. Carga los datos en cuanto se crea el VM
    init {
        loadProfileData()
    }

    /**
     * Carga los datos del perfil (info + citas) en paralelo.
     * Esta función puede ser llamada de nuevo para "refrescar".
     */
    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // Obtenemos el ID del paciente logueado
            val patientId = userPreferences.userIdFlow.firstOrNull()
            if (patientId == null) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Error de sesión de usuario.") }
                return@launch
            }

            // 4. Carga de datos en PARALELO
            val userResultDeferred = async { userRepository.getUserById(patientId) }
            val appointmentsResultDeferred = async { appointmentRepository.getAppointmentDetailsForUser(patientId) }

            // Esperamos a que ambas terminen
            val userResult = userResultDeferred.await()
            val appointmentsResult = appointmentsResultDeferred.await()

            // 5. Procesamos los resultados y actualizamos el estado
            if (userResult.isFailure || appointmentsResult.isFailure) {
                val userError = userResult.exceptionOrNull()?.message ?: ""
                val apptError = appointmentsResult.exceptionOrNull()?.message ?: ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar datos: $userError $apptError"
                    )
                }
            } else {
                // ¡Éxito!
                val user = userResult.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userName = user?.name ?: "N/A",
                        userEmail = user?.email ?: "N/A",
                        userPhone = user?.phone ?: "N/A",
                        appointments = appointmentsResult.getOrNull() ?: emptyList(),
                        errorMsg = null
                    )
                }
            }
        }
    }
}