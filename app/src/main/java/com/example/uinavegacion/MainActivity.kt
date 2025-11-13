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
// 1. Importar el nuevo Repositorio
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.local.storage.UserPreferences
// 2. Importar todos los VMs y Factories
import com.example.uinavegacion.ui.viewmodel.AdminManageSpecialtiesViewModel
import com.example.uinavegacion.ui.viewmodel.AdminManageSpecialtiesViewModelFactory
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModel
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModelFactory
import com.example.uinavegacion.ui.viewmodel.AppointmentViewModel
import com.example.uinavegacion.ui.viewmodel.AppointmentViewModelFactory
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModelFactory
import com.example.uinavegacion.ui.viewmodel.DoctorMenuViewModel
import com.example.uinavegacion.ui.viewmodel.DoctorMenuViewModelFactory
import com.example.uinavegacion.ui.viewmodel.DoctorProfileViewModel
import com.example.uinavegacion.ui.viewmodel.DoctorProfileViewModelFactory
import com.example.uinavegacion.ui.viewmodel.MyReservationsViewModel
import com.example.uinavegacion.ui.viewmodel.MyReservationsViewModelFactory
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModel
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModelFactory
import com.example.uinavegacion.ui.viewmodel.PatientProfileViewModel
import com.example.uinavegacion.ui.viewmodel.PatientProfileViewModelFactory

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
    private val adminMenuViewModel: AdminMenuViewModel by viewModels {
        AdminMenuViewModelFactory(userRepository, appointmentRepository, userPreferences)
    }
    private val patientProfileViewModel: PatientProfileViewModel by viewModels {
        PatientProfileViewModelFactory(userRepository, appointmentRepository, userPreferences)
    }
    private val doctorProfileViewModel: DoctorProfileViewModel by viewModels {
        DoctorProfileViewModelFactory(userRepository, appointmentRepository)
    }

    // 4. INSTANCIAR EL NUEVO VIEWMODEL DE GESTIÓN


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
                        adminMenuViewModel = adminMenuViewModel,
                        patientProfileViewModel = patientProfileViewModel,
                        doctorProfileViewModel = doctorProfileViewModel
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
    adminMenuViewModel: AdminMenuViewModel,
    patientProfileViewModel: PatientProfileViewModel,
    doctorProfileViewModel: DoctorProfileViewModel,
    adminManageSpecialtiesViewModel: AdminManageSpecialtiesViewModel // <-- 6. Recibir el VM
) {
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        authViewModel = authViewModel,
        appointmentViewModel = appointmentViewModel,
        patientMenuViewModel = patientMenuViewModel,
        myReservationsViewModel = myReservationsViewModel,
        doctorMenuViewModel = doctorMenuViewModel,
        adminMenuViewModel = adminMenuViewModel,
        patientProfileViewModel = patientProfileViewModel,
        doctorProfileViewModel = doctorProfileViewModel,
        adminManageSpecialtiesViewModel = adminManageSpecialtiesViewModel // <-- 7. Pasar el VM al NavGraph
    )
}