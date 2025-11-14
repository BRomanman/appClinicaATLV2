package com.example.uinavegacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.local.storage.UserPreferences
import com.example.uinavegacion.data.repository.AppointmentRepository
import com.example.uinavegacion.data.repository.SpecialtyRepository
import com.example.uinavegacion.data.repository.UserRepository
// 1. Importaremos estos 2 archivos nuevos en el siguiente paso
import com.example.uinavegacion.navigation.AuthNavGraph
import com.example.uinavegacion.navigation.MainNavGraph
import com.example.uinavegacion.ui.theme.UINavegacionTheme
import com.example.uinavegacion.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    // --- Instancias de Dependencias ---
    private val database by lazy { AppDatabase.getInstance(this) }
    private val userPreferences by lazy { UserPreferences(this) }
    private val userRepository by lazy { UserRepository(database.userDao()) }
    private val appointmentRepository by lazy {
        AppointmentRepository(database.appointmentDao())
    }
    private val specialtyRepository by lazy {
        SpecialtyRepository(database.specialtyDao())
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
    // Esta es la instanciación que arregla tu error de compilación
    private val adminManageSpecialtiesViewModel: AdminManageSpecialtiesViewModel by viewModels {
        AdminManageSpecialtiesViewModelFactory(specialtyRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UINavegacionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. AppRoot ahora es el "Vigilante"
                    AppRoot(
                        authViewModel = authViewModel,
                        // Agrupamos todos los VMs de la app principal
                        mainAppViewModels = MainAppViewModels(
                            authViewModel,
                            appointmentViewModel,
                            patientMenuViewModel,
                            myReservationsViewModel,
                            doctorMenuViewModel,
                            adminMenuViewModel,
                            patientProfileViewModel,
                            doctorProfileViewModel,
                            adminManageSpecialtiesViewModel
                        )
                    )
                }
            }
        }
    }
}

/**
 * Data class para agrupar todos los ViewModels que
 * el "Mundo Principal" (logueado) necesita.
 */
data class MainAppViewModels(
    val authViewModel: AuthViewModel,
    val appointmentViewModel: AppointmentViewModel,
    val patientMenuViewModel: PatientMenuViewModel,
    val myReservationsViewModel: MyReservationsViewModel,
    val doctorMenuViewModel: DoctorMenuViewModel,
    val adminMenuViewModel: AdminMenuViewModel,
    val patientProfileViewModel: PatientProfileViewModel,
    val doctorProfileViewModel: DoctorProfileViewModel,
    val adminManageSpecialtiesViewModel: AdminManageSpecialtiesViewModel
)

/**
 * El Composable raíz que decide qué "Mundo" mostrar.
 * Observa el [SessionUiState] del AuthViewModel.
 */
@Composable
fun AppRoot(
    authViewModel: AuthViewModel,
    mainAppViewModels: MainAppViewModels
) {
    // 3. Observamos el estado de la sesión (del 'AuthViewModel' del paso anterior)
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()

    // 4. Decidimos qué "Mundo" mostrar
    when (sessionState) {
        // Mundo 1: Cargando (esperando a DataStore)
        is SessionUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Mundo 2: No Autenticado
        is SessionUiState.Unauthenticated -> {
            // Mostramos el gráfico de navegación de Login/Registro
            // (Dará error ahora, lo creamos en el siguiente paso)
            AuthNavGraph(authViewModel = authViewModel)
        }

        // Mundo 3: Autenticado
        is SessionUiState.Authenticated -> {
            // Mostramos la App principal (con el menú lateral)
            // (Dará error ahora, lo creamos en el siguiente paso)
            MainNavGraph(viewModels = mainAppViewModels)
        }
    }
}