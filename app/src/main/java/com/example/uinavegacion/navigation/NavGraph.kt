package com.example.uinavegacion.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.uinavegacion.ui.components.AppTopBar
import com.example.uinavegacion.ui.components.AppDrawer
import com.example.uinavegacion.ui.components.DrawerItem
// 1. Importar todas las pantallas Vm
import com.example.uinavegacion.ui.screen.AdminMenuScreenVm
import com.example.uinavegacion.ui.screen.DoctorMenuScreenVm
import com.example.uinavegacion.ui.screen.MyReservationsScreenVm
import com.example.uinavegacion.ui.screen.PatientMenuScreenVm
import com.example.uinavegacion.ui.screen.BookAppointmentScreenVm
import com.example.uinavegacion.ui.screen.HomeScreen
import com.example.uinavegacion.ui.screen.LoginScreenVm
import com.example.uinavegacion.ui.screen.RegisterScreenVm
// 2. Importar todos los ViewModels
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModel
import com.example.uinavegacion.ui.viewmodel.AppointmentViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.DoctorMenuViewModel
import com.example.uinavegacion.ui.viewmodel.MyReservationsViewModel
import com.example.uinavegacion.ui.viewmodel.PatientMenuViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    appointmentViewModel: AppointmentViewModel,
    patientMenuViewModel: PatientMenuViewModel,
    myReservationsViewModel: MyReservationsViewModel,
    doctorMenuViewModel: DoctorMenuViewModel,
    adminMenuViewModel: AdminMenuViewModel // <-- 3. Recibir el nuevo VM
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- Helpers de Navegación ---
    val goHome: () -> Unit = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goPatientMenu: () -> Unit = { navController.navigate(Route.PatientMenu.path) }
    val goDoctorMenu: () -> Unit = { navController.navigate(Route.DoctorMenu.path) }
    val goAdminMenu: () -> Unit = { navController.navigate(Route.AdminMenu.path) }
    val goBookAppointment: () -> Unit = { navController.navigate(Route.BookAppointment.path) }
    val goMyReservations: () -> Unit = { navController.navigate(Route.MyReservations.path) }

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

    // Lista de ítems del Drawer (sin cambios)
    val drawerItems = mutableListOf(
        DrawerItem("Home", Icons.Default.Home, { scope.launch { drawerState.close() }; goHome() }),
        DrawerItem("Agendar Cita", Icons.Default.Event, { scope.launch { drawerState.close() }; goBookAppointment() }),
        DrawerItem("Mis Citas", Icons.Default.CalendarMonth, { scope.launch { drawerState.close() }; goMyReservations() }),
        DrawerItem("Login", Icons.Default.Login, { scope.launch { drawerState.close() }; goLogin() }),
        DrawerItem("Registro", Icons.Default.PersonAdd, { scope.launch { drawerState.close() }; goRegister() }),
        DrawerItem("Cerrar Sesión", Icons.Default.Logout, doLogout)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer(null, drawerItems) }
    ) {
        Scaffold(
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
                composable(Route.Home.path) {
                    HomeScreen(goLogin, goRegister, goBookAppointment)
                }
                composable(Route.Login.path) {
                    LoginScreenVm(authViewModel, onLoginOkNavigate, goRegister)
                }
                composable(Route.Register.path) {
                    RegisterScreenVm(authViewModel, goLogin, goLogin)
                }
                composable(Route.BookAppointment.path) {
                    BookAppointmentScreenVm(appointmentViewModel, goMyReservations)
                }
                composable(Route.PatientMenu.path) {
                    PatientMenuScreenVm(
                        vm = patientMenuViewModel,
                        onGoBookAppointment = goBookAppointment,
                        onGoMyReservations = goMyReservations,
                        onLogout = doLogout
                    )
                }
                composable(Route.MyReservations.path) {
                    MyReservationsScreenVm(
                        vm = myReservationsViewModel,
                        onGoBack = goPatientMenu
                    )
                }
                composable(Route.DoctorMenu.path) {
                    DoctorMenuScreenVm(
                        vm = doctorMenuViewModel,
                        onLogout = doLogout
                    )
                }

                // 4. ¡REEMPLAZAMOS EL ÚLTIMO PLACEHOLDER!
                composable(Route.AdminMenu.path) {
                    AdminMenuScreenVm(
                        vm = adminMenuViewModel, // <-- Inyectamos el VM
                        onLogout = doLogout
                    )
                }
            }
        }
    }
}