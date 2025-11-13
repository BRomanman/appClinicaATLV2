package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla PatientMenu.
 * Sigue el patrón estricto de UINavegacion (un solo data class).
 */
data class PatientMenuUiState(
    val userName: String = "Paciente", // Valor por defecto
    val isLoading: Boolean = true,
    val errorMsg: String? = null
)

/**
 * ViewModel para el Menú de Paciente.
 * Su única función es cargar el nombre del usuario logueado.
 */
class PatientMenuViewModel(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // 1. EL ESTADO ÚNICO OBSERVABLE
    private val _uiState = MutableStateFlow(PatientMenuUiState())
    val uiState: StateFlow<PatientMenuUiState> = _uiState

    // 2. Bloque de inicialización: carga los datos en cuanto el VM se crea
    init {
        loadUserName()
    }

    /**
     * Carga el nombre del usuario desde el repositorio usando el ID guardado
     * en UserPreferences.
     */
    private fun loadUserName() {
        viewModelScope.launch {
            // Se asegura de que isLoading sea true al empezar
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // Obtenemos el ID guardado en el Paso 6
            val userId = userPreferences.userIdFlow.firstOrNull()

            if (userId == null) {
                // Si no hay ID, actualiza el estado con error
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error de sesión. ID no encontrado."
                    )
                }
                return@launch
            }

            // Usamos la función del repositorio que creamos en el paso 9.2
            val result = userRepository.getUserById(userId)

            // Actualizamos el estado con el resultado
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isLoading = false,
                        // ¡Éxito! Usamos el nombre del usuario
                        userName = result.getOrNull()?.name ?: "Usuario"
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}