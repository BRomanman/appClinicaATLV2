package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uinavegacion.R // Importante para R.drawable

/**
 * Pantalla de Bienvenida.
 * Reconstruida para coincidir con el diseño de 'app_clinica_atl' original,
 * pero usando la arquitectura de navegación de 'UINavegacion'.
 */
@Composable
fun HomeScreen(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onGoBookAppointment: () -> Unit // Esta lambda se usará para "Ver Especialidades"
) {
    // Definimos los datos estáticos que tenía la HomeScreen original
    val especialidadesList = listOf(
        Especialidad("Cardiología", R.drawable.doctor_cardio_1),
        Especialidad("Dermatología", R.drawable.doctor_derma_1),
        Especialidad("Medicina General", R.drawable.doctor_medgen_1),
        Especialidad("Nutrición", R.drawable.doctor_nutri_1),
        Especialidad("Pediatría", R.drawable.doctor_pedi_1),
        Especialidad("Psicología", R.drawable.doctor_psico_1)
    )

    val clinicasList = listOf(
        Clinica("Clínica ATL Viña del Mar", "Av. Libertad 123, Viña del Mar", R.drawable.clinica_1),
        Clinica("Clínica ATL Santiago", "Av. Providencia 456, Santiago", R.drawable.clinica_2),
        Clinica("Clínica ATL Rancagua", "Av. O'Higgins 789, Rancagua", R.drawable.clinica_3)
    )

    val segurosList = listOf(
        Seguro("Seguro de Salud", R.drawable.seguro_salud_1),
        Seguro("Seguro de Vida", R.drawable.seguro_vida_1),
        Seguro("Seguro Empresarial", R.drawable.seguro_empresarial1)
    )

    // Layout principal con scroll
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // --- Sección 1: Pager de Especialidades ---
        item {
            EspecialidadesSection(
                especialidades = especialidadesList,
                onVerTodasClick = onGoBookAppointment // Conectamos el botón a la navegación
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Sección 2: Clínicas Cercanas ---
        item {
            ClinicasSection(clinicas = clinicasList)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Sección 3: Seguros ---
        item {
            SegurosSection(seguros = segurosList)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Sección 4: Botones de Login/Registro ---
        // Estos botones son de UINavegacion y los mantenemos
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Ir a Login")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Ir a Registro")
                }
            }
        }
    }
}

// --- Componentes de la UI (Copiados de app_clinica_atl) ---

// Data classes para los ítems estáticos
private data class Especialidad(val nombre: String, val imagenRes: Int)
private data class Clinica(val nombre: String, val direccion: String, val imagenRes: Int)
private data class Seguro(val nombre: String, val imagenRes: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EspecialidadesSection(especialidades: List<Especialidad>, onVerTodasClick: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { especialidades.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Especialidades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onVerTodasClick) { // <--- NAVEGACIÓN
                Text("Ver todas")
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            EspecialidadCard(especialidad = especialidades[page], onClick = onVerTodasClick)
        }
    }
}

@Composable
private fun ClinicasSection(clinicas: List<Clinica>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Clínicas Cercanas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(clinicas) { clinica ->
                ClinicaCard(clinica = clinica)
            }
        }
    }
}

@Composable
private fun SegurosSection(seguros: List<Seguro>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Seguros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(seguros) { seguro ->
                SeguroCard(seguro = seguro)
            }
        }
    }
}

// --- Tarjetas de UI (Copiadas de app_clinica_atl) ---

@Composable
private fun EspecialidadCard(especialidad: Especialidad, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(160.dp, 180.dp)
            .clickable { onClick() }, // <--- NAVEGACIÓN
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = especialidad.imagenRes),
                contentDescription = especialidad.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            Text(
                text = especialidad.nombre,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun ClinicaCard(clinica: Clinica) {
    Card(
        modifier = Modifier.size(280.dp, 180.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = clinica.imagenRes),
                contentDescription = clinica.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = clinica.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = clinica.direccion, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun SeguroCard(seguro: Seguro) {
    Card(
        modifier = Modifier.size(160.dp, 160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = seguro.imagenRes),
                contentDescription = seguro.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Text(
                text = seguro.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}