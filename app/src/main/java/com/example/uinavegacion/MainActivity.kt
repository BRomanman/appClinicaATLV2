package com.example.uinavegacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.navigation.AppNavGraph
import com.example.uinavegacion.ui.theme.UINavegacionTheme
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.local.storage.UserPreferences
// 1. Importar todos los VMs y Factories
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModel
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModelFactory
import com.example.uinavegacion.ui.viewmodel.AppointmentViewModel
import com.example.uinavegacion.ui.viewmodel.AppointmentViewModelFactory
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModelFactory
import com.example.uinavegacion.ui.viewmodel.DoctorMenuViewModel
import com.example.uinavegacion.ui.viewmodel.DoctorMenuViewModelFactory
import com.example.uinavegacion.ui.viewmodel.MyReservationsViewModel
import com.example.uinavegacion.ui.viewmodel.MyReservationsViewModelFactory
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModel
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModelFactory

class MainActivity : ComponentActivity() {

    // Instancias de dependencias
    private val database by lazy { AppDatabase.getInstance(this) }
    private val userPreferences by lazy { UserPreferences(this) }
    private val userRepository by lazy { UserRepository(database.userDao()) }
    private val appointmentRepository by lazy {
        AppointmentRepository(database.appointmentDao())
    }

    // --- ViewModels ---
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(userRepository, userPreferences)
    }
    private val appointmentViewModel: AppointmentViewModel by viewModels {
        AppointmentViewModelFactory(userRepository, appointmentRepository, userPreferences)
    }
    private val patientMenuViewModel: PatientMenuViewModel by viewModels {
        PatientMenuViewModelFactory(userRepository, userPreferences)
    }
    private val myReservationsViewModel: MyReservationsViewModel by viewModels {
        MyReservationsViewModelFactory(appointmentRepository, userPreferences)
    }
    private val doctorMenuViewModel: DoctorMenuViewModel by viewModels {
        DoctorMenuViewModelFactory(userRepository, appointmentRepository, userPreferences)
    }

    // 2. INSTANCIAR EL NUEVO VIEWMODEL DE ADMIN
    private val adminMenuViewModel: AdminMenuViewModel by viewModels {
        AdminMenuViewModelFactory(userRepository, appointmentRepository, userPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UINavegacionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(
                        authViewModel = authViewModel,
                        appointmentViewModel = appointmentViewModel,
                        patientMenuViewModel = patientMenuViewModel,
                        myReservationsViewModel = myReservationsViewModel,
                        doctorMenuViewModel = doctorMenuViewModel,
                        adminMenuViewModel = adminMenuViewModel // <-- 3. Pasar el VM
                    )
                }
            }
        }
    }
}

@Composable
fun AppRoot(
    authViewModel: AuthViewModel,
    appointmentViewModel: AppointmentViewModel,
    patientMenuViewModel: PatientMenuViewModel,
    myReservationsViewModel: MyReservationsViewModel,
    doctorMenuViewModel: DoctorMenuViewModel,
    adminMenuViewModel: AdminMenuViewModel // <-- 4. Recibir el VM
) {
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        authViewModel = authViewModel,
        appointmentViewModel = appointmentViewModel,
        patientMenuViewModel = patientMenuViewModel,
        myReservationsViewModel = myReservationsViewModel,
        doctorMenuViewModel = doctorMenuViewModel,
        adminMenuViewModel = adminMenuViewModel // <-- 5. Pasar el VM al NavGraph
    )
}