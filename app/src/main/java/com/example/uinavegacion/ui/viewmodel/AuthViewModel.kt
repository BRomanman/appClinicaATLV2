package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.domain.validation.*
import kotlinx.coroutines.delay
// 1. Imports necesarios para el nuevo StateFlow de sesión
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull // Importante
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
// ---
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 2. AÑADIMOS UN ESTADO PARA LA SESIÓN
/**
 * Representa el estado de la sesión del usuario en la app.
 */
sealed class SessionUiState {
    object Loading : SessionUiState() // Estado inicial, esperando a DataStore
    object Unauthenticated : SessionUiState() // Usuario no logueado
    data class Authenticated(val role: String) : SessionUiState() // Usuario logueado
}

// (LoginUiState y RegisterUiState no cambian)
data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val loggedInUserRole: String? = null,
    val loggedInUserId: Long? = null
)

data class RegisterUiState(
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
    ), // Seguiremos arreglando esto después
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
    private val userPreferences: UserPreferences // Recibe la dependencia
) : ViewModel() {

    // --- ESTADOS DE PANTALLA ---
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // 3. NUEVO STATEFLOW PARA EL ESTADO GLOBAL DE LA SESIÓN
    /**
     * Observa el 'isLoggedInFlow' y 'userRoleFlow' de UserPreferences
     * y los convierte en un [SessionUiState] para que la UI principal reaccione.
     */
    val sessionState: StateFlow<SessionUiState> =
        userPreferences.isLoggedInFlow.map { isLoggedIn ->
            if (isLoggedIn) {
                // Si está logueado, busca su rol
                val role = userPreferences.userRoleFlow.firstOrNull() ?: "paciente"
                SessionUiState.Authenticated(role)
            } else {
                SessionUiState.Unauthenticated
            }
        }.stateIn( // Convierte el Flow en un StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Inicia cuando la UI observa
            initialValue = SessionUiState.Loading // Valor inicial mientras lee DataStore
        )


    // ----------------- LOGIN -----------------

    // (onLoginEmailChange, onLoginPassChange, recomputeLoginCanSubmit... sin cambios)
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
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
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
                        // 4. ¡AQUÍ ESTÁ LA MAGIA!
                        // Al llamar a saveSession, UserPreferences se actualiza.
                        // El 'sessionState' (que observa UserPreferences)
                        // se actualizará automáticamente a Authenticated.
                        launch {
                            userPreferences.saveSession(id = user.id, role = user.role)
                        }
                        it.copy(
                            isSubmitting = false,
                            success = true, // Esto es para que la UI de Login navegue
                            loggedInUserRole = user.role,
                            loggedInUserId = user.id
                        )
                    } else {
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

    // 5. LOGOUT
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
            // Al limpiar la sesión, 'sessionState' se actualizará
            // automáticamente a Unauthenticated.
        }
    }

    // ----------------- REGISTRO -----------------
    // (Todas las funciones de registro... sin cambios)

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }
    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }
    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly)) }
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
        _register.update { it.copy(specialty = newSpecialty, specialtyError = null) }
        recomputeRegisterCanSubmit()
    }
    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val specialtyOk = if (s.role == "doctor") s.specialty != null else true
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError, s.specialtyError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled && specialtyOk) }
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