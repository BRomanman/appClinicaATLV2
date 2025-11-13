package com.example.uinavegacion.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
// 1. IMPORTAR LA NUEVA ENTIDAD Y DAO
import com.example.uinavegacion.data.local.appointment.AppointmentDao
import com.example.uinavegacion.data.local.appointment.AppointmentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 2. AÑADIR APPOINTMENTENTITY A LA LISTA DE ENTIDADES
//    Incrementamos la versión de la DB porque cambiamos el esquema
@Database(
    entities = [UserEntity::class, AppointmentEntity::class], // <-- AÑADIDO
    version = 2, // <-- INCREMENTADO
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // 3. EXPONER EL NUEVO DAO
    abstract fun userDao(): UserDao
    abstract fun appointmentDao(): AppointmentDao // <-- AÑADIDO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).userDao()
                                // 4. AÑADIMOS DATOS DE PRUEBA (SEED) PARA PACIENTES Y DOCTORES
                                val seed = listOf(
                                    // Administrador
                                    UserEntity(
                                        name = "Admin",
                                        email = "admin@duoc.cl",
                                        phone = "+56911111111",
                                        password = "Admin123!",
                                        role = "admin" // <-- ROL AÑADIDO
                                    ),
                                    // Paciente
                                    UserEntity(
                                        name = "Víctor Rosendo",
                                        email = "victor@duoc.cl",
                                        phone = "+56922222222",
                                        password = "123456",
                                        role = "paciente" // <-- ROL AÑADIDO
                                    ),
                                    // Doctores
                                    UserEntity(
                                        name = "Dr. Juan Pérez",
                                        email = "jperez@duoc.cl",
                                        phone = "+56933333333",
                                        password = "123456",
                                        role = "doctor", // <-- ROL AÑADIDO
                                        specialty = "Cardiología"
                                    ),
                                    UserEntity(
                                        name = "Dra. Ana Gómez",
                                        email = "agomez@duoc.cl",
                                        phone = "+56944444444",
                                        password = "123456",
                                        role = "doctor", // <-- ROL AÑADIDO
                                        specialty = "Dermatología"
                                    )
                                )

                                if (dao.count() == 0) {
                                    seed.forEach { dao.insert(it) }
                                }
                            }
                        }
                    })
                    // Al subir de versión 1 a 2, destruimos y recreamos la DB
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}