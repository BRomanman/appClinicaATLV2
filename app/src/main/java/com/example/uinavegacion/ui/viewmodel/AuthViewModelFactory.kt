package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uinavegacion.data.repository.UserRepository
// 1. Importamos la nueva dependencia
import com.example.uinavegacion.data.local.storage.UserPreferences

/**
 * Factory para AuthViewModel.
 * Ahora también necesita UserPreferences.
 */
class AuthViewModelFactory(
    private val repository: UserRepository,
    private val userPreferences: UserPreferences // <-- 2. AÑADIDO
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // 3. Pasamos la dependencia al constructor del VM
            return AuthViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}