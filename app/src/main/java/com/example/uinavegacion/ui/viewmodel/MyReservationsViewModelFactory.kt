package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.AppointmentRepository

/**
 * Factory para crear MyReservationsViewModel con sus dependencias.
 * Sigue el mismo patrón de las otras factories.
 */
class MyReservationsViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyReservationsViewModel::class.java)) {
            return MyReservationsViewModel(appointmentRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}