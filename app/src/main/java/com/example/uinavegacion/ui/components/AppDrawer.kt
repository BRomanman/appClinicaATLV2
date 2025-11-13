package com.example.uinavegacion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// 1. Definimos una data class simple para el ítem del Drawer
data class DrawerItem(
    val label: String,
    val icon: ImageVector?,
    val onClick: () -> Unit
)

// (Eliminamos la función defaultDrawerItems porque la movimos al NavGraph)

@Composable
fun AppDrawer(
    currentRoute: String?, // (Opcional, para resaltar el ítem activo)
    items: List<DrawerItem>, // 2. Recibimos la lista de ítems
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier) {
        // Header (opcional)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("Clínica ATL", style = MaterialTheme.typography.titleMedium)
        }
        Divider()
        Spacer(Modifier.height(12.dp))

        // 3. Iteramos sobre la lista de ítems recibida
        items.forEach { item ->
            NavigationDrawerItem(
                icon = {
                    if (item.icon != null) {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                selected = item.label.equals(currentRoute, ignoreCase = true),
                onClick = item.onClick, // Usamos la lambda del ítem
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}