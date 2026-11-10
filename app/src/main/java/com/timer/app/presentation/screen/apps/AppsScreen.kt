package com.timer.app.presentation.screen.apps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timer.app.domain.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(vm: AppsViewModel = hiltViewModel()) {
    val apps by vm.filteredApps.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val search by vm.search.collectAsState()
    val showSystemApps by vm.showSystemApps.collectAsState()
    var editingApp by remember { mutableStateOf<AppInfo?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Apps", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sistem.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Switch(
                    checked = showSystemApps,
                    onCheckedChange = { vm.toggleSystemApps() },
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = vm::setSearch,
            placeholder = { Text("Buscar app...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(apps, key = { it.packageName }) { app ->
                    AppListItem(app = app, onClick = { editingApp = app })
                }
            }
        }
    }

    editingApp?.let { app ->
        AppEditDialog(
            app = app,
            onDismiss = { editingApp = null },
            onSave = { updated ->
                vm.updateApp(updated)
                editingApp = null
            }
        )
    }
}

@Composable
private fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(app.appName, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (app.isLeech) AppChip(
                    if (app.customTimeLimitMinutes > 0) "Leech (${app.customTimeLimitMinutes}m)" else "Leech",
                    Color(0xFFE53935)
                )
                if (app.isJoker) AppChip("Joker", Color(0xFF43A047))
                if (app.isNoCount) AppChip("No Count", Color(0xFF1E88E5))
                if (!app.isLeech && !app.isJoker && !app.isNoCount)
                    AppChip("Cuenta", Color(0xFF9E9E9E))
            }
        },
        trailingContent = {
            IconButton(onClick = onClick) { Icon(Icons.Filled.Edit, "Editar") }
        }
    )
    HorizontalDivider()
}

@Composable
private fun AppChip(label: String, color: Color) {
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AppEditDialog(app: AppInfo, onDismiss: () -> Unit, onSave: (AppInfo) -> Unit) {
    var isLeech by remember { mutableStateOf(app.isLeech) }
    var isJoker by remember { mutableStateOf(app.isJoker) }
    var isNoCount by remember { mutableStateOf(app.isNoCount) }
    var limitText by remember { mutableStateOf(app.customTimeLimitMinutes.takeIf { it > 0 }?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(app.appName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledSwitch(
                    label = "Leech",
                    description = "Tiene límite individual; puede disparar sinergia",
                    checked = isLeech,
                    onCheckedChange = { isLeech = it }
                )
                if (isLeech) {
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Límite individual (minutos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        supportingText = { Text("Mínimo de minutos para disparar sinergia") }
                    )
                }
                HorizontalDivider()
                LabeledSwitch(
                    label = "Joker",
                    description = "Siempre accesible aunque el tiempo se agote",
                    checked = isJoker,
                    onCheckedChange = { isJoker = it }
                )
                HorizontalDivider()
                LabeledSwitch(
                    label = "No Count",
                    description = "No suma al contador (anula el conteo, incluso si es leech)",
                    checked = isNoCount,
                    onCheckedChange = { isNoCount = it }
                )
                if (isNoCount) {
                    Text(
                        "No Count tiene prioridad sobre Leech: esta app no sumará al límite diario.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val limit = if (isLeech) (limitText.toIntOrNull() ?: 0) else 0
                onSave(
                    app.copy(
                        isLeech = isLeech,
                        isJoker = isJoker,
                        isNoCount = isNoCount,
                        customTimeLimitMinutes = limit
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun LabeledSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
