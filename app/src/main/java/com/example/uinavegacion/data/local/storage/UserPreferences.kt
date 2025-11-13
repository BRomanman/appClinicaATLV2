package com.example.uinavegacion.data.local.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.* // Importamos todos los tipos de keys
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creamos la instancia del DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    // 1. DEFINIMOS LAS LLAVES
    // Ya no es un objeto 'companion' porque las usaremos en varias funciones
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_ID = longPreferencesKey("user_id")
    private val USER_ROLE = stringPreferencesKey("user_role")

    // 2. OBSERVABLES (Flows) PARA LEER LOS DATOS
    // La app puede observar si el usuario está logueado
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    // Observable para el ID del usuario
    val userIdFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    // Observable para el Rol del usuario
    val userRoleFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ROLE]
        }

    // 3. FUNCIONES DE ESCRITURA (suspend)

    /**
     * Guarda la sesión completa del usuario (ID y Rol) y marca como logueado.
     */
    suspend fun saveSession(id: Long, role: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = id
            preferences[USER_ROLE] = role
        }
    }

    /**
     * Limpia todos los datos de la sesión (Logout).
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear() // Borra todas las preferencias
        }
    }

    // (Eliminamos la función antigua setLoggedIn)
}