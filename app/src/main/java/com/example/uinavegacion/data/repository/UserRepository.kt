package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
import java.lang.Exception
import java.util.NoSuchElementException

class UserRepository(
    private val userDao: UserDao
) {

    // (login, register, getDoctorsBySpecialty, getUserById ... sin cambios)
    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.getByEmail(email)
            if (user != null && user.password == password) {
                Result.success(user)
            } else {
                throw IllegalArgumentException("Credenciales inválidas")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, phone: String, password: String, role: String, specialty: String?): Result<Long> {
        return try {
            val exists = userDao.getByEmail(email) != null
            if (exists) {
                throw IllegalStateException("El correo ya está registrado")
            }
            val id = userDao.insert(
                UserEntity(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    role = role,
                    specialty = specialty
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDoctorsBySpecialty(specialty: String): Result<List<UserEntity>> {
        return try {
            val doctors = userDao.getDoctorsBySpecialty(specialty)
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(id: Long): Result<UserEntity> {
        return try {
            val user = userDao.getById(id)
            if (user != null) {
                Result.success(user)
            } else {
                throw NoSuchElementException("Usuario no encontrado con ID $id")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- FUNCIÓN AÑADIDA ---
    /**
     * Obtiene una lista de todos los usuarios (pacientes, doctores, admin).
     * Devuelve Result<List<UserEntity>> o un error.
     */
    suspend fun getAllUsers(): Result<List<UserEntity>> {
        return try {
            val users = userDao.getAllUsers()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}