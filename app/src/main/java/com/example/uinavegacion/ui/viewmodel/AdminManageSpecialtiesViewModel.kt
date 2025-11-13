package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.specialty.SpecialtyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla "Gestionar Especialidades".
 */
data class AdminSpecialtiesUiState(
    val isLoading: Boolean = true,
    val specialties: List<SpecialtyEntity> = emptyList(),
    val newSpecialtyName: String = "", // El texto en el TextField
    val errorMsg: String? = null,
    val successMsg: String? = null
)

/**
 * ViewModel para el CRUD de Especialidades.
 */
class AdminManageSpecialtiesViewModel(
    private val specialtyRepository: SpecialtyRepository
) : ViewModel() {

    // 1. EL ESTADO ÚNICO OBSERVABLE
    private val _uiState = MutableStateFlow(AdminSpecialtiesUiState())
    val uiState: StateFlow<AdminSpecialtiesUiState> = _uiState

    // 2. Carga la lista inicial
    init {
        loadSpecialties()
    }

    /**
     * Carga/Refresca la lista de especialidades desde el repositorio.
     */
    fun loadSpecialties() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            val result = specialtyRepository.getAllSpecialties()
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isLoading = false, specialties = result.getOrNull() ?: emptyList())
                } else {
                    it.copy(isLoading = false, errorMsg = result.exceptionOrNull()?.message)
                }
            }
        }
    }

    /**
     * Actualiza el nombre de la nueva especialidad en el estado.
     */
    fun onNewSpecialtyNameChange(name: String) {
        _uiState.update { it.copy(newSpecialtyName = name, errorMsg = null, successMsg = null) }
    }

    /**
     * Intenta añadir la nueva especialidad al repositorio.
     */
    fun submitNewSpecialty() {
        val name = _uiState.value.newSpecialtyName
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMsg = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = specialtyRepository.addSpecialty(name)

            if (result.isSuccess) {
                // Éxito: Limpia el campo, muestra éxito y recarga la lista
                _uiState.update { it.copy(newSpecialtyName = "", successMsg = "Especialidad '$name' añadida") }
                loadSpecialties() // Recarga la lista
            } else {
                // Error: Muestra el error y deja de cargar
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    /**
     * Intenta borrar una especialidad.
     */
    fun deleteSpecialty(specialty: SpecialtyEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }
            val result = specialtyRepository.deleteSpecialty(specialty)

            if (result.isSuccess) {
                _uiState.update { it.copy(successMsg = "Especialidad eliminada") }
                loadSpecialties() // Recarga la lista
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}