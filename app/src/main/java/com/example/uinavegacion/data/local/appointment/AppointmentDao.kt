package com.example.uinavegacion.data.local.appointment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.uinavegacion.data.local.user.UserEntity

// (DTO AppointmentDetailsForDoctor sin cambios)
data class AppointmentDetailsForDoctor(
    val id: Long,
    val date: String,
    val time: String,
    val status: String,
    val patientName: String,
    val patientPhone: String?
)

@Dao
interface AppointmentDao {

    // (insert, getAppointmentsForUser, getAppointmentsForDoctor... sin cambios)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM appointment_table WHERE patientId = :userId")
    suspend fun getAppointmentsForUser(userId: Long): List<AppointmentEntity>

    @Query("SELECT * FROM appointment_table WHERE doctorId = :doctorId")
    suspend fun getAppointmentsForDoctor(doctorId: Long): List<AppointmentEntity>

    @Query("SELECT * FROM appointment_table WHERE doctorId = :doctorId AND date = :date AND time = :time")
    suspend fun getAppointmentByDoctorDateTime(doctorId: Long, date: String, time: String): AppointmentEntity?

    @Query("SELECT time FROM appointment_table WHERE doctorId = :doctorId AND date = :date AND status = 'agendada'")
    suspend fun getBookedTimesForDoctorOnDate(doctorId: Long, date: String): List<String>

    @Query("""
        SELECT 
            a.id, a.date, a.time, a.status,
            u.name AS doctorName, 
            u.specialty AS doctorSpecialty
        FROM appointment_table AS a
        INNER JOIN user_table AS u ON a.doctorId = u.id
        WHERE a.patientId = :patientId
        ORDER BY a.date ASC, a.time ASC
    """)
    suspend fun getAppointmentDetailsForPatient(patientId: Long): List<AppointmentDetails>

    @Query("""
        SELECT 
            a.id, a.date, a.time, a.status,
            u.name AS patientName, 
            u.phone AS patientPhone
        FROM appointment_table AS a
        INNER JOIN user_table AS u ON a.patientId = u.id
        WHERE a.doctorId = :doctorId
        ORDER BY a.date ASC, a.time ASC
    """)
    suspend fun getAppointmentDetailsForDoctor(doctorId: Long): List<AppointmentDetailsForDoctor>

    // --- FUNCIÓN AÑADIDA ---
    /**
     * Obtiene TODAS las citas con los nombres del paciente y del doctor.
     * Esta consulta es para el Administrador.
     */
    @Query("""
        SELECT 
            a.id, a.date, a.time, a.status,
            p.name AS patientName, 
            d.name AS doctorName
        FROM appointment_table AS a
        INNER JOIN user_table AS p ON a.patientId = p.id
        INNER JOIN user_table AS d ON a.doctorId = d.id
        ORDER BY a.date DESC, a.time DESC
    """)
    suspend fun getAllAppointmentDetails(): List<FullAppointmentDetails>
}