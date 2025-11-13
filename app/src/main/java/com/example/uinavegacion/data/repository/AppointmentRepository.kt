package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.appointment.AppointmentDao
import com.example.uinavegacion.data.local.appointment.AppointmentEntity
import com.example.uinavegacion.data.local.appointment.AppointmentDetails
import com.example.uinavegacion.data.local.appointment.AppointmentDetailsForDoctor
// 1. Importar el nuevo DTO
import com.example.uinavegacion.data.local.appointment.FullAppointmentDetails
import java.lang.Exception

class AppointmentRepository(
    private val appointmentDao: AppointmentDao
) {

    // (bookAppointment, getAppointmentsForUser, getAppointmentsForDoctor... sin cambios)
    suspend fun bookAppointment(appointment: AppointmentEntity): Result<Long> {
        return try {
            val existingAppointment = appointmentDao.getAppointmentByDoctorDateTime(
                appointment.doctorId,
                appointment.date,
                appointment.time
            )
            if (existingAppointment != null) {
                throw IllegalStateException("La hora seleccionada ya no está disponible.")
            }
            val newId = appointmentDao.insert(appointment)
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointmentsForUser(userId: Long): Result<List<AppointmentEntity>> {
        return try {
            val appointments = appointmentDao.getAppointmentsForUser(userId)
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointmentsForDoctor(doctorId: Long): Result<List<AppointmentEntity>> {
        return try {
            val appointments = appointmentDao.getAppointmentsForDoctor(doctorId)
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBookedTimes(doctorId: Long, date: String): Result<List<String>> {
        return try {
            val bookedTimes = appointmentDao.getBookedTimesForDoctorOnDate(doctorId, date)
            Result.success(bookedTimes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointmentDetailsForUser(userId: Long): Result<List<AppointmentDetails>> {
        return try {
            val appointments = appointmentDao.getAppointmentDetailsForPatient(userId)
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointmentDetailsForDoctor(doctorId: Long): Result<List<AppointmentDetailsForDoctor>> {
        return try {
            val appointments = appointmentDao.getAppointmentDetailsForDoctor(doctorId)
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- FUNCIÓN AÑADIDA ---
    /**
     * Obtiene TODAS las citas con detalles completos (paciente y doctor) para el Admin.
     * Devuelve Result<List<FullAppointmentDetails>> o un error.
     */
    suspend fun getAllAppointmentDetails(): Result<List<FullAppointmentDetails>> {
        return try {
            val appointments = appointmentDao.getAllAppointmentDetails()
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}