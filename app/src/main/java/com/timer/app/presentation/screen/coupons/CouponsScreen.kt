package com.timer.app.presentation.screen.coupons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timer.app.domain.model.Coupon
import com.timer.app.presentation.theme.CouponGold
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CouponsScreen(vm: CouponsViewModel = hiltViewModel()) {
    val coupons by vm.coupons.collectAsState()
    val availableCount by vm.availableCount.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.useCouponResult) {
        vm.useCouponResult.collect { success ->
            snackbarHostState.showSnackbar(
                if (success) "Cupón activado! Tienes 30 min extra" else "No hay cupones disponibles"
            )
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                CouponSummaryCard(availableCount = availableCount, onUse = { showConfirm = true })
                Spacer(Modifier.height(24.dp))
                Text("Historial de cupones", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
            }
            if (coupons.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Sin cupones generados aún", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(coupons, key = { it.id }) { coupon -> CouponCard(coupon) }
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Sobre los cupones", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("• Se generan 2 cupones cada semana", fontSize = 13.sp)
                        Text("• Úsalos cuando quieras para desbloquear apps", fontSize = 13.sp)
                        Text("• Cada cupón da tiempo extra configurable en Ajustes", fontSize = 13.sp)
                        Text("• Acumulables: los no usados se conservan", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Usar cupón") },
            text = { Text("¿Deseas usar un cupón para desbloquear todas las apps por el tiempo configurado?") },
            confirmButton = {
                Button(onClick = { vm.useCoupon(); showConfirm = false }) { Text("Usar") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CouponSummaryCard(availableCount: Int, onUse: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CouponGold.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.ConfirmationNumber, null, modifier = Modifier.size(48.dp), tint = CouponGold)
            Spacer(Modifier.height(8.dp))
            Text("$availableCount", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = CouponGold)
            Text("cupones disponibles", fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onUse,
                enabled = availableCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = CouponGold)
            ) {
                Icon(Icons.Filled.LockOpen, null)
                Spacer(Modifier.width(8.dp))
                Text("Quemar cupón", color = Color.Black)
            }
        }
    }
}

@Composable
private fun CouponCard(coupon: Coupon) {
    val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val isUsed = coupon.usedAt != null
    ListItem(
        leadingContent = {
            Icon(
                if (isUsed) Icons.Filled.CheckCircle else Icons.Filled.ConfirmationNumber,
                null,
                tint = if (isUsed) MaterialTheme.colorScheme.outline else CouponGold
            )
        },
        headlineContent = {
            Text(
                if (isUsed) "Cupón usado" else "Cupón disponible",
                color = if (isUsed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Column {
                Text("Generado: ${fmt.format(Date(coupon.generatedAt))}", fontSize = 12.sp)
                if (isUsed) Text("Usado: ${fmt.format(Date(coupon.usedAt!!))}", fontSize = 12.sp)
            }
        },
        trailingContent = { Text("${coupon.durationMinutes}m", fontWeight = FontWeight.Bold) }
    )
    HorizontalDivider()
}
