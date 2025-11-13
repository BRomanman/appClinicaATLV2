package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.specialty.SpecialtyDao
import com.example.uinavegacion.data.local.specialty.SpecialtyEntity
import java.lang.Exception

/**
 * Repositorio para gestionar el CRUD de Especialidades.
 * Sigue el patrón estricto de UINavegacion:
 * 1. Depende de su DAO.
 * 2. Todas las funciones públicas 'suspend' devuelven Result<T>.
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
     * Añade una nueva especialidad por su nombre.
     */
    suspend fun addSpecialty(name: String): Result<Long> {
        return try {
            if (name.isBlank()) {
                throw IllegalArgumentException("El nombre no puede estar vacío.")
            }
            val newSpecialty = SpecialtyEntity(name = name.trim())
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
     * Elimina una especialidad existente.
     */
    suspend fun deleteSpecialty(specialty: SpecialtyEntity): Result<Unit> {
        return try {
            specialtyDao.delete(specialty)
            Result.success(Unit) // Éxito significa que no hubo error
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}