package com.timer.app.presentation.screen.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val dailyLimit by vm.dailyLimitMinutes.collectAsState()
    val couponDuration by vm.couponDurationMinutes.collectAsState()
    val resetHour by vm.resetHour.collectAsState()
    val resetMinute by vm.resetMinute.collectAsState()

    val couponSecret by vm.couponSecret.collectAsState()
    var limitText by remember(dailyLimit) { mutableStateOf(dailyLimit.toString()) }
    var couponText by remember(couponDuration) { mutableStateOf(couponDuration.toString()) }
    var resetHourText by remember(resetHour) { mutableStateOf(resetHour.toString()) }
    var resetMinuteText by remember(resetMinute) { mutableStateOf(resetMinute.toString()) }
    var secretText by remember(couponSecret) { mutableStateOf(couponSecret) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 24.sp)

        SettingsSection("Tiempo diario") {
            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it },
                label = { Text("Límite diario (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        limitText.toIntOrNull()?.let { vm.setDailyLimit(it) }
                    }) { Icon(Icons.Filled.Check, "Guardar") }
                }
            )
            Text(
                "Actual: ${dailyLimit / 60}h ${dailyLimit % 60}m",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsSection("Cupones") {
            OutlinedTextField(
                value = couponText,
                onValueChange = { couponText = it },
                label = { Text("Duración de cupón (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        couponText.toIntOrNull()?.let { vm.setCouponDuration(it) }
                    }) { Icon(Icons.Filled.Check, "Guardar") }
                }
            )
        }

        SettingsSection("Hora de reseteo") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = resetHourText,
                    onValueChange = { resetHourText = it },
                    label = { Text("Hora (0-23)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = resetMinuteText,
                    onValueChange = { resetMinuteText = it },
                    label = { Text("Minuto (0-59)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Button(onClick = {
                val h = resetHourText.toIntOrNull()?.coerceIn(0, 23) ?: 0
                val m = resetMinuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                vm.setResetTime(h, m)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar hora de reset")
            }
        }

        SettingsSection("Cupones externos (deep link)") {
            Text(
                "Secreto compartido con la app generadora de cupones. Ambas apps deben tener el mismo valor.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = secretText,
                onValueChange = { secretText = it },
                label = { Text("Secreto compartido") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { vm.setCouponSecret(secretText) }) {
                        Icon(Icons.Filled.Check, "Guardar")
                    }
                }
            )
            Text(
                "Deep link que recibirás:\ntimer://coupon?duration=30&nonce=XXX&sig=YYY",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsSection("Permisos del sistema") {
            PermissionButton(
                title = "Accesibilidad (bloqueo de apps)",
                description = "Necesario para bloquear apps al pasar el límite",
                icon = Icons.Filled.Accessibility,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )
            PermissionButton(
                title = "Uso de apps (estadísticas)",
                description = "Necesario para rastrear el tiempo de uso",
                icon = Icons.Filled.BarChart,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )
            PermissionButton(
                title = "Mostrar sobre otras apps",
                description = "Necesario para la pantalla de bloqueo",
                icon = Icons.Filled.Layers,
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"))
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            content()
        }
    }
}

@Composable
private fun PermissionButton(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.OpenInNew, null, modifier = Modifier.size(16.dp))
        }
    }
}
