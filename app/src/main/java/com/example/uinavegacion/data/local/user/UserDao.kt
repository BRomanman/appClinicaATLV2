package com.example.uinavegacion.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    // (getByEmail, insert, count, getDoctorsBySpecialty, getById ... sin cambios)
    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM user_table")
    suspend fun count(): Int

    @Query("SELECT * FROM user_table WHERE role = 'doctor' AND specialty = :specialty")
    suspend fun getDoctorsBySpecialty(specialty: String): List<UserEntity>

    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    // --- FUNCIÓN AÑADIDA ---
    /**
     * Obtiene todos los usuarios de la base de datos, ordenados por rol.
     */
    @Query("SELECT * FROM user_table ORDER BY role, name ASC")
    suspend fun getAllUsers(): List<UserEntity>
}