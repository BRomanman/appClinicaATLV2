package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onGoBookAppointment: () -> Unit // <-- AÑADIDO
) {
    val bg = MaterialTheme.colorScheme.primaryContainer // Fondo distinto para contraste

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Pantalla de Bienvenida. Usa la barra superior, el menú lateral o los botones.",
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            // Botón Login
            Button(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Ir a Login")
            }
            Spacer(Modifier.height(8.dp))

            // Botón Registro
            OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Ir a Registro")
            }
            Spacer(Modifier.height(16.dp))

            // <-- AÑADIDO: Botón para Agendar Cita -->
            Button(
                onClick = onGoBookAppointment,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Icon(Icons.Default.Event, contentDescription = "Agendar")
                Spacer(Modifier.width(8.dp))
                Text("Agendar Cita (Demo)")
            }
        }
    }
}