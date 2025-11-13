package com.example.uinavegacion.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. FUSIONAMOS LOS CAMPOS
// Tomamos la estructura limpia de UINavegacion y añadimos
// los campos clave de app_clinica_atl (role, specialty, profileImageUrl)

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String, // Campo que estaba en UINavegacion
    val password: String,

    // --- Campos añadidos desde app_clinica_atl ---
    val role: String, // "paciente", "doctor", "admin"
    val specialty: String? = null, // "Cardiología", "Dermatología", etc. (Nulo para pacientes/admin)
    val profileImageUrl: String? = null // URL o path a la imagen de perfil
)