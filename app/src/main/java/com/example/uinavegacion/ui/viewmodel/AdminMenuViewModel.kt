package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 1. Importamos los DTOs y Entidades que necesitamos
import com.example.uinavegacion.data.local.appointment.FullAppointmentDetails
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.async // <-- Importante para carga en paralelo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla AdminMenu.
 * Contiene el nombre del admin, la lista de TODOS los usuarios
 * y la lista de TODAS las citas.
 */
data class AdminMenuUiState(
    val isLoading: Boolean = true,
    val adminName: String = "Admin", // Valor por defecto
    val allUsers: List<UserEntity> = emptyList(),
    val allAppointments: List<FullAppointmentDetails> = emptyList(),
    val errorMsg: String? = null
)

/**
 * ViewModel para el Menú de Administrador.
 * Carga todos los datos de la clínica.
 */
class AdminMenuViewModel(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // 2. EL ESTADO ÚNICO OBSERVABLE
    private val _uiState = MutableStateFlow(AdminMenuUiState())
    val uiState: StateFlow<AdminMenuUiState> = _uiState

    // 3. Carga los datos en cuanto se crea el VM
    init {
        loadAdminDashboard()
    }

    /**
     * Carga todos los datos del panel de admin.
     * Esta función puede ser llamada de nuevo para "refrescar".
     */
    fun loadAdminDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // Obtenemos el ID del admin logueado
            val adminId = userPreferences.userIdFlow.firstOrNull()
            if (adminId == null) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Error de sesión de admin.") }
                return@launch
            }

            // 4. Carga de datos en PARALELO
            // Lanzamos las 3 tareas al mismo tiempo
            val nameResultDeferred = async { userRepository.getUserById(adminId) }
            val usersResultDeferred = async { userRepository.getAllUsers() }
            val appointmentsResultDeferred = async { appointmentRepository.getAllAppointmentDetails() }

            // Esperamos a que todas terminen
            val nameResult = nameResultDeferred.await()
            val usersResult = usersResultDeferred.await()
            val appointmentsResult = appointmentsResultDeferred.await()

            // 5. Procesamos los resultados y actualizamos el estado UNA VEZ
            val adminName = nameResult.getOrNull()?.name ?: "Admin"

            // Verificamos si alguna de las consultas principales falló
            if (usersResult.isFailure || appointmentsResult.isFailure) {
                val userError = usersResult.exceptionOrNull()?.message ?: ""
                val apptError = appointmentsResult.exceptionOrNull()?.message ?: ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        adminName = adminName,
                        errorMsg = "Error al cargar datos: $userError $apptError"
                    )
                }
            } else {
                // ¡Éxito!
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        adminName = adminName,
                        allUsers = usersResult.getOrNull() ?: emptyList(),
                        allAppointments = appointmentsResult.getOrNull() ?: emptyList(),
                        errorMsg = null
                    )
                }
            }
        }
    }
}