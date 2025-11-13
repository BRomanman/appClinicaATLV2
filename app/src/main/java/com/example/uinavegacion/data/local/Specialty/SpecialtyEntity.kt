package com.example.uinavegacion.data.local.specialty

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Nueva entidad para almacenar las especialidades médicas y su precio.
 * Añadimos un índice 'unique' al nombre para evitar duplicados.
 */
@Entity(
    tableName = "specialty_table",
    indices = [Index(value = ["name"], unique = true)] // Evita nombres duplicados
)
data class SpecialtyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double // <-- AÑADIDO para calcular "cuánto generan"
)