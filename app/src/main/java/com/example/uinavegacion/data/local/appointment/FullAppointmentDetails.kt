package com.example.uinavegacion.data.local.appointment

/**
 * DTO personalizado para la vista de Administrador.
 * Contiene la información de la cita + el nombre del paciente + el nombre del doctor.
 * Es el resultado de una consulta con doble JOIN.
 */
data class FullAppointmentDetails(
    // Campos de la Cita
    val id: Long,
    val date: String,
    val time: String,
    val status: String,

    // Campos del Paciente (obtenidos del JOIN)
    val patientName: String,

    // Campos del Doctor (obtenidos del JOIN)
    val doctorName: String
)