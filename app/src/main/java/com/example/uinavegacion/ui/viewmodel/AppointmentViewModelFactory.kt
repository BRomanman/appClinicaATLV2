package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository
// 1. Importamos la nueva dependencia
import com.example.uinavegacion.data.local.storage.UserPreferences

/**
 * Factory para crear AppointmentViewModel con sus dependencias.
 * Ahora también inyecta UserPreferences.
 */
class AppointmentViewModelFactory(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences // <-- 2. AÑADIDO
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            // 3. Pasamos la dependencia al constructor del VM
            return AppointmentViewModel(
                userRepository,
                appointmentRepository,
                userPreferences // <-- AÑADIDO
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}