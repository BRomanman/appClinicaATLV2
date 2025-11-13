package com.example.uinavegacion.data.local.appointment

/**
 * Esta data class NO es una tabla (@Entity).
 * Es un DTO (Data Transfer Object) personalizado que Room
 * usará para devolver el resultado de una consulta JOIN.
 *
 * Contiene los campos de AppointmentEntity + campos de UserEntity (el doctor).
 */
data class AppointmentDetails(
    // Campos de la Cita
    val id: Long,
    val date: String,
    val time: String,
    val status: String,

    // Campos del Doctor (obtenidos del JOIN)
    val doctorName: String,
    val doctorSpecialty: String?
)