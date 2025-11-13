package com.example.uinavegacion.data.local.appointment // <-- Package corregido

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.uinavegacion.data.local.user.UserEntity

// Esta es la entidad de 'app_clinica_atl' adaptada para 'UINavegacion'
// Define las relaciones entre un Paciente (UserEntity) y un Doctor (UserEntity)
@Entity(
    tableName = "appointment_table",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"]), Index(value = ["doctorId"])]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val doctorId: Long,
    val date: String, // "YYYY-MM-DD"
    val time: String, // "HH:MM"
    val status: String // "agendada", "cancelada", "completada"
)