package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository

/**
 * Factory para crear DoctorProfileViewModel con sus dependencias.
 */
class DoctorProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorProfileViewModel::class.java)) {
            return DoctorProfileViewModel(
                userRepository,
                appointmentRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}