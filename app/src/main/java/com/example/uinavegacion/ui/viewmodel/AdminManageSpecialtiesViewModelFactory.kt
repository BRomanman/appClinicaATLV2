package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory para crear AdminManageSpecialtiesViewModel con sus dependencias.
 */
class AdminManageSpecialtiesViewModelFactory(
    private val specialtyRepository: SpecialtyRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminManageSpecialtiesViewModel::class.java)) {
            return AdminManageSpecialtiesViewModel(specialtyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}