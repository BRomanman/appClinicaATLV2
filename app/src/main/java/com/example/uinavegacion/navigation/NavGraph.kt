package com.example.uinavegacion.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
// 1. Importar el Snackbar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import com.example.uinavegacion.ui.components.AppDrawer
import com.example.uinavegacion.ui.components.AppTopBar
import com.example.uinavegacion.ui.components.DrawerItem
// 2. Importar todas las pantallas Vm
import com.example.uinavegacion.ui.screen.AdminManageSpecialtiesScreenVm
import com.example.uinavegacion.ui.screen.AdminMenuScreenVm
import com.example.uinavegacion.ui.screen.DoctorMenuScreenVm
import com.example.uinavegacion.ui.screen.DoctorProfileScreenVm
import com.example.uinavegacion.ui.screen.MyReservationsScreenVm
import com.example.uinavegacion.ui.screen.PatientMenuScreenVm
import com.example.uinavegacion.ui.screen.PatientProfileScreenVm
import com.example.uinavegacion.ui.screen.BookAppointmentScreenVm
import com.example.uinavegacion.ui.screen.HomeScreen
import com.example.uinavegacion.ui.screen.LoginScreenVm
import com.example.uinavegacion.ui.screen.RegisterScreenVm
// 3. Importar todos los ViewModels
import com.example.uinavegacion.ui.viewmodel.AdminManageSpecialtiesViewModel
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModel
import com.example.uinavegacion.ui.viewmodel.AppointmentViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.DoctorMenuViewModel
import com.example.uinavegacion.ui.viewmodel.DoctorProfileViewModel
import com.example.uinavegacion.ui.viewmodel.MyReservationsViewModel
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModel
import com.example.uinavegacion.ui.viewmodel.PatientProfileViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    appointmentViewModel: AppointmentViewModel,
    patientMenuViewModel: PatientMenuViewModel,
    myReservationsViewModel: MyReservationsViewModel,
    doctorMenuViewModel: DoctorMenuViewModel,
    adminMenuViewModel: AdminMenuViewModel,
    patientProfileViewModel: PatientProfileViewModel,
    doctorProfileViewModel: DoctorProfileViewModel,
    adminManageSpecialtiesViewModel: AdminManageSpecialtiesViewModel // <-- 4. Recibir el nuevo VM
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // 5. Añadir SnackbarHostState para los mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // --- Helpers de Navegación ---
    val goHome: () -> Unit = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goPatientMenu: () -> Unit = { navController.navigate(Route.PatientMenu.path) }
    val goDoctorMenu: () -> Unit = { navController.navigate(Route.DoctorMenu.path) }
    val goAdminMenu: () -> Unit = { navController.navigate(Route.AdminMenu.path) }
    val goBookAppointment: () -> Unit = { navController.navigate(Route.BookAppointment.path) }
    val goMyReservations: () -> Unit = { navController.navigate(Route.MyReservations.path) }
    val goPatientProfile: () -> Unit = { navController.navigate(Route.PatientProfile.path) }
    val goDoctorProfile: (Long) -> Unit = { doctorId ->
        navController.navigate(Route.DoctorProfile.createRoute(doctorId))
    }
    // 6. AÑADIDA NUEVA LAMBDA
    val goManageSpecialties: () -> Unit = { navController.navigate(Route.AdminManageSpecialties.path) }

    val doLogout: () -> Unit = {
        scope.launch { drawerState.close() }
        authViewModel.logout()
        goHome()
    }

    val onLoginOkNavigate: (String?) -> Unit = { role ->
        when (role) {
            "admin" -> goAdminMenu()
            "doctor" -> goDoctorMenu()
            "paciente" -> goPatientMenu()
            else -> goHome()
        }
    }

    // 7. AÑADIR "GESTIÓN" AL DRAWER (solo para admin, pero lo ponemos visible por ahora)
    val drawerItems = mutableListOf(
        DrawerItem("Home", Icons.Default.Home, { scope.launch { drawerState.close() }; goHome() }),
        DrawerItem(
            "Mi Perfil",
            Icons.Default.AccountCircle,
            { scope.launch { drawerState.close() }; goPatientProfile() }),
        DrawerItem("Agendar Cita", Icons.Default.Event, { scope.launch { drawerState.close() }; goBookAppointment() }),
        DrawerItem("Mis Citas", Icons.Default.CalendarMonth, { scope.launch { drawerState.close() }; goMyReservations() }),
        DrawerItem("(Admin) Especialidades", Icons.Default.Assignment, { scope.launch { drawerState.close() }; goManageSpecialties() }), // <-- NUEVO
        DrawerItem("Login", Icons.Default.Login, { scope.launch { drawerState.close() }; goLogin() }),
        DrawerItem("Registro", Icons.Default.PersonAdd, { scope.launch { drawerState.close() }; goRegister() }),
        DrawerItem("Cerrar Sesión", Icons.Default.Logout, doLogout)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer(null, drawerItems) }
    ) {
        Scaffold(
            // 8. Añadir el SnackbarHost
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AppTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onHome = goHome,
                    onLogin = goLogin,
                    onRegister = goRegister
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // (Todas las rutas anteriores sin cambios)
                composable(Route.Home.path) { HomeScreen(goLogin, goRegister, goBookAppointment) }
                composable(Route.Login.path) { LoginScreenVm(authViewModel, onLoginOkNavigate, goRegister) }
                composable(Route.Register.path) { RegisterScreenVm(authViewModel, goLogin, goLogin) }
                composable(Route.BookAppointment.path) {
                    BookAppointmentScreenVm(appointmentViewModel, goMyReservations, goDoctorProfile)
                }
                composable(Route.PatientMenu.path) {
                    PatientMenuScreenVm(patientMenuViewModel, goBookAppointment, goMyReservations, goPatientProfile, doLogout)
                }
                composable(Route.MyReservations.path) {
                    MyReservationsScreenVm(myReservationsViewModel, goPatientMenu)
                }
                composable(Route.DoctorMenu.path) {
                    DoctorMenuScreenVm(doctorMenuViewModel, doLogout)
                }

                // 9. ACTUALIZAR RUTA 'AdminMenu'
                composable(Route.AdminMenu.path) {
                    AdminMenuScreenVm(
                        vm = adminMenuViewModel,
                        onGoToManageSpecialties = goManageSpecialties, // <-- Pasar la lambda
                        onLogout = doLogout
                    )
                }

                composable(Route.PatientProfile.path) {
                    PatientProfileScreenVm(patientProfileViewModel, goPatientMenu)
                }
                composable(
                    route = Route.DoctorProfile.path,
                    arguments = listOf(navArgument("doctorId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val doctorId = backStackEntry.arguments?.getLong("doctorId")
                    DoctorProfileScreenVm(
                        vm = doctorProfileViewModel,
                        doctorId = doctorId,
                        onGoBack = { navController.popBackStack() }
                    )
                }

                // 10. AÑADIR LA NUEVA RUTA DE GESTIÓN
                composable(Route.AdminManageSpecialties.path) {
                    AdminManageSpecialtiesScreenVm(
                        vm = adminManageSpecialtiesViewModel,
                        onGoBack = { navController.popBackStack() },
                        showSnackbar = showSnackbar
                    )
                }
            }
        }
    }
}