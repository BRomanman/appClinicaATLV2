package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository

/**
 * Factory para crear AdminMenuViewModel con sus dependencias.
 */
class AdminMenuViewModelFactory(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminMenuViewModel::class.java)) {
            // Inyectamos las 3 dependencias
            return AdminMenuViewModel(
                userRepository,
                appointmentRepository,
                userPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}