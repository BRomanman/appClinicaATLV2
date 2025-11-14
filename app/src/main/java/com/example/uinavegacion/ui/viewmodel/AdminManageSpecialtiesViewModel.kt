package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.specialty.SpecialtyEntity
// 1. AÑADIMOS EL IMPORT QUE FALTABA
import com.example.uinavegacion.data.repository.SpecialtyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI (CORREGIDO) para "Gestionar Especialidades".
 */
data class AdminSpecialtiesUiState(
    val isLoading: Boolean = true,
    val specialties: List<SpecialtyEntity> = emptyList(),
    val newSpecialtyName: String = "", // El texto en el TextField de Nombre
    val newSpecialtyPrice: String = "", // El texto en el TextField de Precio
    val errorMsg: String? = null,
    val successMsg: String? = null
)

/**
 * ViewModel (CORREGIDO) para el CRUD de Especialidades.
 */
class AdminManageSpecialtiesViewModel(
    private val specialtyRepository: SpecialtyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSpecialtiesUiState())
    val uiState: StateFlow<AdminSpecialtiesUiState> = _uiState

    init {
        loadSpecialties()
    }

    /**
     * Carga/Refresca la lista de especialidades.
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

    // --- Handlers para los TextFields ---

    fun onNewSpecialtyNameChange(name: String) {
        _uiState.update { it.copy(newSpecialtyName = name, errorMsg = null, successMsg = null) }
    }

    fun onNewSpecialtyPriceChange(price: String) {
        // Filtra para que solo sean números
        if (price.all { it.isDigit() }) {
            _uiState.update { it.copy(newSpecialtyPrice = price, errorMsg = null, successMsg = null) }
        }
    }

    // --- Acciones de CRUD ---

    fun submitNewSpecialty() {
        val name = _uiState.value.newSpecialtyName
        val price = _uiState.value.newSpecialtyPrice.toDoubleOrNull()

        // Validaciones
        if (name.isBlank() || price == null) {
            _uiState.update { it.copy(errorMsg = "Nombre y precio no pueden estar vacíos") }
            return
        }
        if (price <= 0) {
            _uiState.update { it.copy(errorMsg = "El precio debe ser mayor a 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Llama a la función del repo que SÍ incluye el precio
            val result = specialtyRepository.addSpecialty(name, price)

            if (result.isSuccess) {
                // Éxito: Limpia campos y recarga
                _uiState.update { it.copy(newSpecialtyName = "", newSpecialtyPrice = "", successMsg = "Especialidad '$name' añadida") }
                loadSpecialties()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun deleteSpecialty(specialty: SpecialtyEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }
            val result = specialtyRepository.deleteSpecialty(specialty)

            if (result.isSuccess) {
                _uiState.update { it.copy(successMsg = "Especialidad eliminada") }
                loadSpecialties()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = result.exceptionOrNull()?.message) }
            }
        }
    }

    /**
     * Actualiza una especialidad (usado para cambiar el precio).
     */
    fun updateSpecialty(specialty: SpecialtyEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }
            val result = specialtyRepository.updateSpecialty(specialty)

            if (result.isSuccess) {
                _uiState.update { it.copy(successMsg = "Precio de '${specialty.name}' actualizado") }
                loadSpecialties()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = result.exceptionOrNull()?.message) }
            }
        }
    }

    /**
     * Limpia los mensajes de la UI después de que se muestran (para el Snackbar).
     */
    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}