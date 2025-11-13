package com.example.uinavegacion.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String,
    val specialty: String? = null,
    val profileImageUrl: String? = null,

    // --- CAMPO AÑADIDO (para tu solicitud de "saldos") ---
    /**
     * Salario del doctor (nullable, ya que pacientes y admins no lo tienen).
     */
    val salary: Double? = null
)