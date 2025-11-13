package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.uinavegacion.domain.validation.*
import com.example.uinavegacion.data.repository.UserRepository
// 1. Importamos UserPreferences
import com.example.uinavegacion.data.local.storage.UserPreferences

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,

    // 2. AHORA GUARDAMOS ROL E ID
    val loggedInUserRole: String? = null,
    val loggedInUserId: Long? = null // <-- AÑADIDO
)

data class RegisterUiState(
    // (Sin cambios aquí)
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val role: String = "paciente",
    val specialty: String? = null,
    val availableSpecialties: List<String> = listOf(
        "Cardiología",
        "Dermatología",
        "Medicina General",
        "Nutrición"
    ),
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val specialtyError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)


class AuthViewModel(
    private val repository: UserRepository,
    private val userPreferences: UserPreferences // <-- 3. RECIBIMOS LA DEPENDENCIA
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // ----------------- LOGIN: handlers y envío -----------------

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null &&
                s.email.isNotBlank() &&
                s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false, loggedInUserRole = null, loggedInUserId = null) }
            delay(500)

            val result = repository.login(s.email.trim(), s.pass)

            _login.update {
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        // 4. ¡AQUÍ ESTÁ LA LÓGICA!
                        // Guardamos la sesión en DataStore ANTES de notificar a la UI
                        launch {
                            userPreferences.saveSession(id = user.id, role = user.role)
                        }
                        // Notificamos a la UI con los datos del usuario
                        it.copy(
                            isSubmitting = false,
                            success = true,
                            errorMsg = null,
                            loggedInUserRole = user.role, // <-- ROL
                            loggedInUserId = user.id      // <-- ID
                        )
                    } else {
                        // Caso raro: éxito pero usuario nulo
                        it.copy(isSubmitting = false, success = false, errorMsg = "Usuario no encontrado")
                    }
                } else {
                    it.copy(isSubmitting = false, success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación")
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null, loggedInUserRole = null, loggedInUserId = null) }
    }

    // 5. FUNCIÓN DE LOGOUT
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
            // (Opcional: podrías navegar a Home/Login desde el NavGraph observando un StateFlow de "sesión cerrada")
        }
    }

    // ----------------- REGISTRO: handlers y envío -----------------
    // (Sin cambios en las funciones de Registro)

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update {
            it.copy(name = filtered, nameError = validateNameLettersOnly(filtered))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update {
            it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    fun onRoleChange(newRole: String) {
        _register.update {
            it.copy(
                role = newRole,
                specialty = if (newRole != "doctor") null else it.specialty,
                specialtyError = if (newRole == "doctor" && it.specialty == null) "Debe elegir especialidad" else null
            )
        }
        recomputeRegisterCanSubmit()
    }

    fun onSpecialtyChange(newSpecialty: String) {
        _register.update {
            it.copy(
                specialty = newSpecialty,
                specialtyError = null
            )
        }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val specialtyOk = if (s.role == "doctor") s.specialty != null else true
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null } && specialtyOk
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(700)

            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                password = s.pass,
                role = s.role,
                specialty = s.specialty
            )

            _register.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(isSubmitting = false, success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar")
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}