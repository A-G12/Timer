package com.timer.app.presentation.screen.synergy

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timer.app.domain.model.AppInfo
import com.timer.app.domain.model.SynergyRule
import com.timer.app.presentation.theme.SynergyBlue

@Composable
fun SynergyScreen(vm: SynergyViewModel = hiltViewModel()) {
    val rules by vm.rules.collectAsState()
    val leechApps by vm.leechApps.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<SynergyRule?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editingRule = null; showDialog = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nueva regla") }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Text("Sinergia", fontWeight = FontWeight.Bold, fontSize = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SynergyBlue.copy(alpha = 0.1f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("¿Cómo funciona?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SynergyBlue)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Configura reglas usando tus apps leech. Si todas las apps de una regla superan su límite individual, se resta ese tiempo de penalización a tu límite diario.",
                            fontSize = 12.sp
                        )
                        Text(
                            "Solo apps leech con límite individual configurado pueden usarse en reglas.",
                            fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SynergyBlue
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (rules.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Bolt, null, Modifier.size(64.dp), tint = SynergyBlue.copy(alpha = 0.3f))
                            Spacer(Modifier.height(8.dp))
                            Text("Sin reglas todavía", fontWeight = FontWeight.Bold)
                            Text("Agrega una regla para empezar", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(rules, key = { it.id }) { rule ->
                    SynergyRuleCard(
                        rule = rule,
                        leechApps = leechApps,
                        onEdit = { editingRule = rule; showDialog = true },
                        onDelete = { vm.deleteRule(rule) }
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        SynergyRuleDialog(
            existing = editingRule,
            leechApps = leechApps,
            onDismiss = { showDialog = false },
            onSave = { vm.saveRule(it); showDialog = false }
        )
    }
}

@Composable
private fun SynergyRuleCard(
    rule: SynergyRule,
    leechApps: List<AppInfo>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val appMap = leechApps.associateBy { it.packageName }
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(rule.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, null) }
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null) }
                }
            }
            Text(
                "Penalización: -${rule.penaltyMinutes} min",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text("Apps (deben superar su límite individual):", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            rule.appPackageNames.forEach { pkg ->
                val app = appMap[pkg]
                val limitText = if ((app?.customTimeLimitMinutes ?: 0) > 0)
                    " ≥ ${app?.customTimeLimitMinutes}min" else ""
                Text("• ${app?.appName ?: pkg}$limitText", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SynergyRuleDialog(
    existing: SynergyRule?,
    leechApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onSave: (SynergyRule) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var penaltyMinutes by remember { mutableStateOf(existing?.penaltyMinutes?.toString() ?: "30") }
    var selectedPkgs by remember { mutableStateOf(existing?.appPackageNames?.toSet() ?: emptySet()) }

    if (leechApps.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Sin apps leech") },
            text = {
                Text("Necesitas configurar al menos una app como 'Leech' y asignarle un límite individual en la sección Apps.")
            },
            confirmButton = { Button(onClick = onDismiss) { Text("Entendido") } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nueva regla" else "Editar regla") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la regla") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = penaltyMinutes,
                    onValueChange = { penaltyMinutes = it },
                    label = { Text("Penalización (minutos a restar)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Selecciona las apps leech (mínimo 2):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("La regla se activa si TODAS exceden su límite individual.", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.heightIn(max = 250.dp)) {
                    items(leechApps) { app ->
                        val isSelected = app.packageName in selectedPkgs
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPkgs = if (isSelected)
                                        selectedPkgs - app.packageName
                                    else
                                        selectedPkgs + app.packageName
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.appName, fontWeight = FontWeight.Medium)
                                Text(
                                    "Límite: ${app.customTimeLimitMinutes} min",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val penalty = penaltyMinutes.toIntOrNull() ?: 30
                    onSave(SynergyRule(
                        id = existing?.id ?: 0,
                        name = name,
                        penaltyMinutes = penalty,
                        appPackageNames = selectedPkgs.toList()
                    ))
                },
                enabled = name.isNotBlank() && selectedPkgs.size >= 2
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
