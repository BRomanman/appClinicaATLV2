package com.example.uinavegacion.data.local.specialty

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO para el CRUD de Especialidades (incluyendo precio).
 * Sigue el patrón 'suspend' de UINavegacion.
 */
@Dao
interface SpecialtyDao {

    /**
     * Inserta una nueva especialidad.
     * OnConflictStrategy.IGNORE: Si el nombre ya existe (por el índice 'unique'),
     * simplemente no la inserta y no lanza error.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(specialty: SpecialtyEntity): Long

    /**
     * Actualiza una especialidad existente (ej. para cambiar el precio).
     */
    @Update
    suspend fun update(specialty: SpecialtyEntity)

    /**
     * Borra una especialidad.
     */
    @Delete
    suspend fun delete(specialty: SpecialtyEntity)

    /**
     * Obtiene la lista completa de especialidades.
     */
    @Query("SELECT * FROM specialty_table ORDER BY name ASC")
    suspend fun getAll(): List<SpecialtyEntity>
}