package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 1. Importamos el DTO de detalles que creamos en el paso 10a
import com.example.uinavegacion.data.local.appointment.AppointmentDetails
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla "Mis Citas".
 * Contiene la lista de citas con detalles, el estado de carga y errores.
 */
data class MyReservationsUiState(
    val isLoading: Boolean = true,
    val reservations: List<AppointmentDetails> = emptyList(),
    val errorMsg: String? = null
)

/**
 * ViewModel para la pantalla "Mis Citas".
 * Obtiene el ID del usuario y carga su lista de citas.
 */
class MyReservationsViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // 2. EL ESTADO ÚNICO OBSERVABLE
    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState

    // 3. Bloque de inicialización: carga las citas en cuanto se crea el VM
    init {
        loadReservations()
    }

    /**
     * Carga la lista de citas del usuario.
     * Esta función puede ser llamada de nuevo para "refrescar" la lista.
     */
    fun loadReservations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // Obtenemos el ID del usuario logueado
            val userId = userPreferences.userIdFlow.firstOrNull()

            if (userId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "No se pudo identificar al usuario. Inicie sesión."
                    )
                }
                return@launch
            }

            // 4. Llama a la nueva función del repositorio (creada en 10a)
            val result = appointmentRepository.getAppointmentDetailsForUser(userId)

            // Actualiza el estado con el éxito o fracaso
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isLoading = false,
                        reservations = result.getOrNull() ?: emptyList()
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al cargar citas"
                    )
                }
            }
        }
    }
}