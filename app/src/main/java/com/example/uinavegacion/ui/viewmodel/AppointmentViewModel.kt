package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.appointment.AppointmentEntity
import com.example.uinavegacion.data.local.user.UserEntity
// 1. Importamos las dependencias necesarias
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull // <-- Importante
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// (El Data Class 'BookAppointmentUiState' no cambia)
data class BookAppointmentUiState(
    val specialties: List<String> = listOf(
        "Cardiología",
        "Dermatología",
        "Medicina General",
        "Nutrición"
    ),
    val doctors: List<UserEntity> = emptyList(),
    val availableTimes: List<String> = emptyList(),
    val selectedSpecialty: String = "",
    val selectedDoctorId: Long? = null,
    val selectedDoctorName: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val isLoadingDoctors: Boolean = false,
    val isLoadingTimes: Boolean = false,
    val isBooking: Boolean = false,
    val bookingSuccess: Boolean = false,
    val errorMsg: String? = null
)

class AppointmentViewModel(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences // <-- 2. RECIBIMOS LA DEPENDENCIA
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookAppointmentUiState())
    val uiState: StateFlow<BookAppointmentUiState> = _uiState

    private val allDaySlots = listOf(
        "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
    )

    // (Todos los handlers 'on...Change' y las funciones 'load...' no cambian)
    // ...
    fun onSpecialtyChange(specialty: String) {
        _uiState.update {
            it.copy(
                selectedSpecialty = specialty,
                doctors = emptyList(),
                selectedDoctorId = null,
                selectedDoctorName = "",
                availableTimes = emptyList(),
                selectedDate = "",
                selectedTime = ""
            )
        }
        loadDoctorsBySpecialty(specialty)
    }

    fun onDoctorChange(doctor: UserEntity) {
        _uiState.update {
            it.copy(
                selectedDoctorId = doctor.id,
                selectedDoctorName = doctor.name,
                availableTimes = emptyList(),
                selectedDate = "",
                selectedTime = ""
            )
        }
    }

    fun onDateChange(date: String) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                availableTimes = emptyList(),
                selectedTime = ""
            )
        }
        val doctorId = _uiState.value.selectedDoctorId
        if (doctorId != null) {
            loadAvailableTimes(doctorId, date)
        }
    }

    fun onTimeChange(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    private fun loadDoctorsBySpecialty(specialty: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDoctors = true, errorMsg = null) }
            val result = userRepository.getDoctorsBySpecialty(specialty)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isLoadingDoctors = false,
                        doctors = result.getOrNull() ?: emptyList()
                    )
                } else {
                    it.copy(
                        isLoadingDoctors = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al cargar doctores"
                    )
                }
            }
        }
    }

    private fun loadAvailableTimes(doctorId: Long, date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTimes = true, errorMsg = null) }
            val result = appointmentRepository.getBookedTimes(doctorId, date)
            _uiState.update {
                if (result.isSuccess) {
                    val bookedTimes = result.getOrNull() ?: emptyList()
                    val availableSlots = allDaySlots.filter { it !in bookedTimes }
                    it.copy(
                        isLoadingTimes = false,
                        availableTimes = availableSlots
                    )
                } else {
                    it.copy(
                        isLoadingTimes = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al cargar horas"
                    )
                }
            }
        }
    }

    /**
     * Acción final: Enviar la reserva a la base de datos.
     */
    fun submitBooking() {
        val s = _uiState.value

        if (s.isBooking || s.selectedDoctorId == null || s.selectedDate.isBlank() || s.selectedTime.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Debe seleccionar especialidad, doctor, fecha y hora.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBooking = true, errorMsg = null, bookingSuccess = false) }

            // 3. OBTENEMOS EL ID REAL DEL DATASTORE
            // Reemplazamos: val patientId = 2L
            val patientId = userPreferences.userIdFlow.firstOrNull()

            // 4. VALIDAMOS QUE EL ID EXISTA
            if (patientId == null) {
                _uiState.update {
                    it.copy(
                        isBooking = false,
                        errorMsg = "No se pudo identificar al usuario. Por favor, inicie sesión de nuevo."
                    )
                }
                return@launch // Detenemos la corrutina
            }

            val newAppointment = AppointmentEntity(
                patientId = patientId, // <-- 5. USAMOS EL ID REAL
                doctorId = s.selectedDoctorId,
                date = s.selectedDate,
                time = s.selectedTime,
                status = "agendada"
            )

            val result = appointmentRepository.bookAppointment(newAppointment)

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isBooking = false, bookingSuccess = true)
                } else {
                    it.copy(
                        isBooking = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al agendar"
                    )
                }
            }
        }
    }

    /**
     * Limpia las banderas de resultado después de que la UI ha navegado.
     */
    fun clearBookingResult() {
        _uiState.update { it.copy(bookingSuccess = false, errorMsg = null) }
    }
}