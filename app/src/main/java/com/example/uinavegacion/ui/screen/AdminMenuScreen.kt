package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
// 1. Importar íconos nuevos
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.data.local.appointment.FullAppointmentDetails
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.ui.viewmodel.AdminMenuUiState
import com.example.uinavegacion.ui.viewmodel.AdminMenuViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla (conectada al VM) para "Menú de Admin".
 */
@Composable
fun AdminMenuScreenVm(
    vm: AdminMenuViewModel,
    onGoToManageSpecialties: () -> Unit, // <-- 2. AÑADIDA NUEVA LAMBDA
    onLogout: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    AdminMenuScreen(
        state = state,
        onRefresh = vm::loadAdminDashboard,
        onGoToManageSpecialties = onGoToManageSpecialties, // <-- 3. PASAR LAMBDA
        onLogout = onLogout
    )
}

/**
 * Pantalla presentacional para "Menú de Admin".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AdminMenuScreen(
    state: AdminMenuUiState,
    onRefresh: () -> Unit,
    onGoToManageSpecialties: () -> Unit, // <-- 4. RECIBIR LAMBDA
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    // 5. Estado para las pestañas
    val pagerState = rememberPagerState(pageCount = { 3 }) // 0=Usuarios, 1=Citas, 2=Gestión
    val tabTitles = listOf(
        "Usuarios" to Icons.Default.Groups,
        "Citas" to Icons.Default.ListAlt,
        "Gestión" to Icons.Default.Assignment
    )

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
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
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
                else -> {
                    Text(
                        text = "Bienvenido, ${state.adminName}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )

                    TabRow(selectedTabIndex = pagerState.currentPage) {
                        tabTitles.forEachIndexed { index, (title, icon) ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(text = title) },
                                icon = { Icon(icon, contentDescription = title) }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> AdminUserList(users = state.allUsers)
                            1 -> AdminAppointmentList(appointments = state.allAppointments)
                            // 6. AÑADIDA NUEVA PÁGINA DE GESTIÓN
                            2 -> AdminManagementPage(onGoToManageSpecialties = onGoToManageSpecialties)
                        }
                    }
                }
            }
        }
    }
}

// 7. AÑADIDO NUEVO COMPOSABLE PARA LA PÁGINA 2
@Composable
private fun AdminManagementPage(
    onGoToManageSpecialties: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Herramientas de Gestión",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Botón para gestionar especialidades
        Button(
            onClick = onGoToManageSpecialties,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar Especialidades")
        }

        Spacer(Modifier.height(16.dp))

        // (Aquí añadiremos el botón "Gestionar Doctores" en el Paso 17)
    }
}


// (AdminUserList, AdminAppointmentList, AdminUserCard, AdminAppointmentCard sin cambios)
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
            // Mostramos el salario
            if (user.role == "doctor") {
                Text(
                    text = "Especialidad: ${user.specialty ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Salario: $${user.salary ?: 0.0}", // <-- Mostramos el salario
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