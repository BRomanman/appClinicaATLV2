package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// 1. Importar los DTOs, Entidades y VM/State
import com.example.uinavegacion.data.local.appointment.FullAppointmentDetails
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.ui.viewmodel.AdminMenuUiState
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla (conectada al VM) para "Menú de Admin".
 * Sigue el patrón de LoginScreenVm.
 */
@Composable
fun AdminMenuScreenVm(
    vm: AdminMenuViewModel, // <-- 2. Recibe el VM
    onLogout: () -> Unit // Lambda para cerrar sesión
) {
    // 3. Observamos el ESTADO ÚNICO
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 4. Delegamos la UI a la pantalla presentacional
    AdminMenuScreen(
        state = state,
        onRefresh = vm::loadAdminDashboard, // Exponemos la acción de refrescar
        onLogout = onLogout
    )
}

/**
 * Pantalla presentacional para "Menú de Admin".
 * Muestra el saludo y pestañas con listas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AdminMenuScreen(
    state: AdminMenuUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 }) // 0 = Usuarios, 1 = Citas
    val tabTitles = listOf("Usuarios", "Citas")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Admin") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                // 1. Estado de Carga
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // 2. Estado de Error
                state.errorMsg != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${state.errorMsg}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // 3. Estado con Datos (Éxito)
                else -> {
                    // Saludo
                    Text(
                        text = "Bienvenido, ${state.adminName}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Pestañas (Tabs)
                    TabRow(selectedTabIndex = pagerState.currentPage) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(text = title) }
                            )
                        }
                    }

                    // Contenido de las Pestañas
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            // Página 0: Lista de Usuarios
                            0 -> AdminUserList(users = state.allUsers)
                            // Página 1: Lista de Citas
                            1 -> AdminAppointmentList(appointments = state.allAppointments)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Muestra la lista de TODOS los usuarios.
 */
@Composable
private fun AdminUserList(users: List<UserEntity>) {
    if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay usuarios registrados.", modifier = Modifier.padding(16.dp))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            AdminUserCard(user = user)
        }
    }
}

/**
 * Muestra la lista de TODAS las citas.
 */
@Composable
private fun AdminAppointmentList(appointments: List<FullAppointmentDetails>) {
    if (appointments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay citas en el sistema.", modifier = Modifier.padding(16.dp))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(appointments) { appt ->
            AdminAppointmentCard(item = appt)
        }
    }
}

// --- Tarjetas de UI ---

@Composable
private fun AdminUserCard(user: UserEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (user.role) {
                "admin" -> MaterialTheme.colorScheme.errorContainer
                "doctor" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rol: ${user.role.uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Email: ${user.email}", style = MaterialTheme.typography.bodySmall)
            Text("Tel: ${user.phone}", style = MaterialTheme.typography.bodySmall)
            if (user.role == "doctor") {
                Text(
                    text = "Especialidad: ${user.specialty ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AdminAppointmentCard(item: FullAppointmentDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Cita #${item.id} (${item.status})",
                style = MaterialTheme.typography.labelLarge
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "Paciente: ${item.patientName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Doctor: ${item.doctorName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.date} a las ${item.time}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}