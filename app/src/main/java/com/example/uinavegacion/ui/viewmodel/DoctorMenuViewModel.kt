package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 1. Importamos el DTO para la lista de citas del doctor (creado en 11a)
import com.example.uinavegacion.data.local.appointment.AppointmentDetailsForDoctor
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla DoctorMenu.
 * Contiene el nombre del doctor Y su lista de citas.
 */
data class DoctorMenuUiState(
    val isLoading: Boolean = true,
    val doctorName: String = "Doctor", // Valor por defecto
    val appointments: List<AppointmentDetailsForDoctor> = emptyList(),
    val errorMsg: String? = null
)

/**
 * ViewModel para el Menú de Doctor.
 * Carga el perfil del doctor y su agenda de citas.
 */
class DoctorMenuViewModel(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // 2. EL ESTADO ÚNICO OBSERVABLE
    private val _uiState = MutableStateFlow(DoctorMenuUiState())
    val uiState: StateFlow<DoctorMenuUiState> = _uiState

    // 3. Carga los datos en cuanto se crea el VM
    init {
        loadDoctorData()
    }

    /**
     * Carga el nombre del doctor y su lista de citas.
     * Esta función puede ser llamada de nuevo para "refrescar".
     */
    fun loadDoctorData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // Obtenemos el ID del doctor logueado
            val doctorId = userPreferences.userIdFlow.firstOrNull()

            if (doctorId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error de sesión. ID de doctor no encontrado."
                    )
                }
                return@launch
            }

            // 4. Realizamos AMBAS consultas
            // (Se podrían optimizar con async/await, pero secuencial es más claro)
            val nameResult = userRepository.getUserById(doctorId)
            val appointmentsResult = appointmentRepository.getAppointmentDetailsForDoctor(doctorId)

            // 5. Actualizamos el estado con los resultados de AMBAS consultas
            val doctorName = if (nameResult.isSuccess) {
                nameResult.getOrNull()?.name ?: "Doctor"
            } else {
                "Doctor" // Usa el default si el nombre falla
            }

            _uiState.update {
                if (appointmentsResult.isSuccess) {
                    // Éxito en citas
                    it.copy(
                        isLoading = false,
                        doctorName = doctorName,
                        appointments = appointmentsResult.getOrNull() ?: emptyList(),
                        errorMsg = if (nameResult.isFailure) nameResult.exceptionOrNull()?.message else null
                    )
                } else {
                    // Error en citas (este es el error más crítico)
                    it.copy(
                        isLoading = false,
                        doctorName = doctorName,
                        errorMsg = appointmentsResult.exceptionOrNull()?.message ?: "Error al cargar agenda"
                    )
                }
            }
        }
    }
}