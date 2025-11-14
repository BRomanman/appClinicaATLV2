package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.specialty.SpecialtyDao
import com.example.uinavegacion.data.local.specialty.SpecialtyEntity
import java.lang.Exception

/**
 * Repositorio para gestionar el CRUD de Especialidades (CORREGIDO).
 * Ahora maneja 'price' en sus funciones.
 */
class SpecialtyRepository(
    private val specialtyDao: SpecialtyDao
) {

    /**
     * Obtiene la lista de todas las especialidades.
     */
    suspend fun getAllSpecialties(): Result<List<SpecialtyEntity>> {
        return try {
            val specialties = specialtyDao.getAll()
            Result.success(specialties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Añade una nueva especialidad (con nombre y precio).
     */
    suspend fun addSpecialty(name: String, price: Double): Result<Long> {
        return try {
            if (name.isBlank()) {
                throw IllegalArgumentException("El nombre no puede estar vacío.")
            }
            if (price <= 0) {
                throw IllegalArgumentException("El precio debe ser mayor a 0.")
            }
            val newSpecialty = SpecialtyEntity(name = name.trim(), price = price)
            val newId = specialtyDao.insert(newSpecialty)

            if (newId == -1L) { // -1L es devuelto por Room si OnConflictStrategy.IGNORE lo previene
                throw IllegalStateException("La especialidad '$name' ya existe.")
            }
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza una especialidad existente (para cambiar el precio).
     */
    suspend fun updateSpecialty(specialty: SpecialtyEntity): Result<Unit> {
        return try {
            specialtyDao.update(specialty)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una especialidad existente.
     */
    suspend fun deleteSpecialty(specialty: SpecialtyEntity): Result<Unit> {
        return try {
            specialtyDao.delete(specialty)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}