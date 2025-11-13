package com.example.uinavegacion.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.local.appointment.AppointmentDao
import com.example.uinavegacion.data.local.appointment.AppointmentEntity
// 1. IMPORTAR LA NUEVA ENTIDAD Y DAO
import com.example.uinavegacion.data.local.specialty.SpecialtyDao
import com.example.uinavegacion.data.local.specialty.SpecialtyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 2. AÑADIR SPECIALTYENTITY A LA LISTA DE ENTIDADES
//    Incrementamos la versión de la DB de 2 a 3
@Database(
    entities = [UserEntity::class, AppointmentEntity::class, SpecialtyEntity::class], // <-- AÑADIDO
    version = 3, // <-- INCREMENTADO
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun appointmentDao(): AppointmentDao

    // 3. EXPONER EL NUEVO DAO
    abstract fun specialtyDao(): SpecialtyDao // <-- AÑADIDO

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
                                // 4. DATOS DE PRUEBA (SEEDING)
                                // Añadimos salarios a los doctores y precargamos especialidades

                                val userDao = getInstance(context).userDao()
                                val specialtyDao = getInstance(context).specialtyDao()

                                // Usuarios (con salarios)
                                val users = listOf(
                                    UserEntity(
                                        name = "Admin", email = "admin@duoc.cl", phone = "+56911111111",
                                        password = "Admin123!", role = "admin"
                                    ),
                                    UserEntity(
                                        name = "Víctor Rosendo", email = "victor@duoc.cl", phone = "+56922222222",
                                        password = "123456", role = "paciente"
                                    ),
                                    UserEntity(
                                        name = "Dr. Juan Pérez", email = "jperez@duoc.cl", phone = "+56933333333",
                                        password = "123456", role = "doctor", specialty = "Cardiología",
                                        salary = 2500000.0 // <-- SALARIO AÑADIDO
                                    ),
                                    UserEntity(
                                        name = "Dra. Ana Gómez", email = "agomez@duoc.cl", phone = "+56944444444",
                                        password = "123456", role = "doctor", specialty = "Dermatología",
                                        salary = 2200000.0 // <-- SALARIO AÑADIDO
                                    )
                                )

                                // Especialidades (con precios)
                                val specialties = listOf(
                                    SpecialtyEntity(name = "Cardiología", price = 35000.0),
                                    SpecialtyEntity(name = "Dermatología", price = 40000.0),
                                    SpecialtyEntity(name = "Medicina General", price = 25000.0),
                                    SpecialtyEntity(name = "Nutrición", price = 30000.0),
                                    SpecialtyEntity(name = "Pediatría", price = 32000.0)
                                )

                                // Insertar todo si la base de datos está vacía
                                if (userDao.count() == 0) {
                                    users.forEach { userDao.insert(it) }
                                    specialties.forEach { specialtyDao.insert(it) }
                                }
                            }
                        }
                    })
                    // Al subir de versión 2 a 3, destruimos y recreamos la DB
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}