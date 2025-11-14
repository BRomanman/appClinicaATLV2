package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.data.local.specialty.SpecialtyEntity
import com.example.uinavegacion.ui.viewmodel.AdminManageSpecialtiesViewModel
import com.example.uinavegacion.ui.viewmodel.AdminSpecialtiesUiState
import kotlinx.coroutines.flow.collectLatest
import com.example.uinavegacion.data.repository.SpecialtyRepository

/**
 * Pantalla (conectada al VM) para "Gestionar Especialidades".
 */
@Composable
fun AdminManageSpecialtiesScreenVm(
    vm: AdminManageSpecialtiesViewModel,
    onGoBack: () -> Unit,
    showSnackbar: (String) -> Unit // Para mostrar mensajes de éxito/error
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Observador de mensajes de error/éxito
    LaunchedEffect(vm.uiState) {
        vm.uiState.collectLatest {
            // Limpiamos mensajes para que no se repitan
            it.errorMsg?.let { msg ->
                showSnackbar(msg)
                vm.clearMessages()
            }
            it.successMsg?.let { msg ->
                showSnackbar(msg)
                vm.clearMessages()
            }
        }
    }

    AdminManageSpecialtiesScreen(
        state = state,
        onGoBack = onGoBack,
        onRefresh = vm::loadSpecialties,
        onAddSpecialty = vm::submitNewSpecialty,
        onDeleteSpecialty = vm::deleteSpecialty,
        onUpdateSpecialty = vm::updateSpecialty,
        onNameChange = vm::onNewSpecialtyNameChange,
        onPriceChange = vm::onNewSpecialtyPriceChange
    )
}

/**
 * Pantalla presentacional para "Gestionar Especialidades".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminManageSpecialtiesScreen(
    state: AdminSpecialtiesUiState,
    onGoBack: () -> Unit,
    onRefresh: () -> Unit,
    onAddSpecialty: () -> Unit,
    onDeleteSpecialty: (SpecialtyEntity) -> Unit,
    onUpdateSpecialty: (SpecialtyEntity) -> Unit,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit
) {
    // Estado local para manejar el diálogo de edición
    var specialtyToEdit by remember { mutableStateOf<SpecialtyEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Especialidades") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
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
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Formulario para Añadir ---
            Text(
                text = "Añadir Nueva Especialidad",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.newSpecialtyName,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.errorMsg != null
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = state.newSpecialtyPrice,
                onValueChange = onPriceChange,
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.errorMsg != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddSpecialty,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir")
            }
            Spacer(Modifier.height(24.dp))

            Divider()
            Spacer(Modifier.height(16.dp))

            // --- Lista de Especialidades ---
            Text(
                text = "Especialidades Existentes",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            if (!state.isLoading && state.specialties.isEmpty()) {
                Text(
                    text = "No hay especialidades en la base de datos.",
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.specialties) { specialty ->
                        SpecialtyItemCard(
                            specialty = specialty,
                            onDelete = { onDeleteSpecialty(specialty) },
                            onEdit = { specialtyToEdit = specialty }, // Abre el diálogo
                            enabled = !state.isLoading
                        )
                    }
                }
            }
        }
    }

    // --- Diálogo de Edición ---
    if (specialtyToEdit != null) {
        // Obtenemos el precio actual y lo convertimos a String para el TextField
        val initialPrice = specialtyToEdit!!.price.toString()
        var newPrice by remember { mutableStateOf(initialPrice) }

        Dialog(onDismissRequest = { specialtyToEdit = null }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Editar Precio de ${specialtyToEdit!!.name}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Nuevo Precio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { specialtyToEdit = null }) {
                            Text("Cancelar")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val priceDouble = newPrice.toDoubleOrNull()
                            if (priceDouble != null) {
                                onUpdateSpecialty(specialtyToEdit!!.copy(price = priceDouble))
                                specialtyToEdit = null // Cierra el diálogo
                            }
                        }) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card para mostrar una especialidad y sus botones.
 */
@Composable
private fun SpecialtyItemCard(
    specialty: SpecialtyEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = specialty.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$${specialty.price.toInt()}", // Mostramos el precio
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Botón de Editar
            TextButton(onClick = onEdit, enabled = enabled) {
                Text("Editar")
            }

            // Botón de Borrar
            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar ${specialty.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}