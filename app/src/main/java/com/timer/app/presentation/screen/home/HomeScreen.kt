package com.timer.app.presentation.screen.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timer.app.domain.model.AppInfo
import com.timer.app.domain.model.DailyUsage
import com.timer.app.presentation.theme.BlockedRed
import com.timer.app.presentation.theme.CouponGold
import com.timer.app.presentation.theme.SafeGreen
import com.timer.app.presentation.theme.WarningOrange

@Composable
fun HomeScreen(vm: HomeViewModel = hiltViewModel()) {
    val usage by vm.usage.collectAsState()
    val coupons by vm.availableCoupons.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Timer", fontSize = 32.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp))

        usage?.let { u ->
            UsageCircle(u)
            Spacer(Modifier.height(32.dp))
            UsageStats(u)
        } ?: CircularProgressIndicator()

        val blockedLeech by vm.blockedLeechApps.collectAsState()
        if (blockedLeech.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            BlockedLeechCard(blockedLeech)
        }

        Spacer(Modifier.height(24.dp))
        CouponsCard(coupons)

        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.refresh() }) { Text("Actualizar") }
    }
}

@Composable
private fun UsageCircle(usage: DailyUsage) {
    val progress by animateFloatAsState(
        targetValue = usage.progressFraction,
        animationSpec = tween(800), label = "progress"
    )
    val color = when {
        usage.isBlocked -> BlockedRed
        usage.progressFraction > 0.8f -> WarningOrange
        else -> SafeGreen
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val remaining = usage.remainingMinutes
            val hours = remaining / 60
            val mins = remaining % 60
            Text(
                text = if (usage.isBlocked) "BLOQUEADO" else "${hours}h ${mins}m",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text("restantes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UsageStats(usage: DailyUsage) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatRow("Tiempo usado hoy", formatMinutes(usage.totalUsageMinutes))
            StatRow("Límite diario base", formatMinutes(usage.dailyLimitMinutes.toLong()))
            if (usage.synergyPenaltyMinutes > 0)
                StatRow(
                    "Penalización sinergia",
                    "-${formatMinutes(usage.synergyPenaltyMinutes.toLong())}",
                    color = MaterialTheme.colorScheme.error
                )
            if (usage.couponBonusMinutes > 0)
                StatRow(
                    "Bonus cupón activo",
                    "+${formatMinutes(usage.couponBonusMinutes.toLong())}",
                    color = SafeGreen
                )
            HorizontalDivider()
            StatRow(
                "Límite efectivo",
                formatMinutes(usage.effectiveLimitMinutes.toLong()),
                bold = true
            )
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    bold: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}

@Composable
private fun BlockedLeechCard(apps: List<AppInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlockedRed.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Apps leech bloqueadas", fontWeight = FontWeight.Bold, color = BlockedRed)
            Spacer(Modifier.height(4.dp))
            apps.forEach { app ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(app.appName, fontSize = 14.sp)
                    Text(
                        "${app.todayUsageMinutes}/${app.customTimeLimitMinutes}min",
                        fontSize = 12.sp,
                        color = BlockedRed
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Un cupón las desbloquea temporalmente.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CouponsCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CouponGold.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Cupones disponibles", fontWeight = FontWeight.Bold)
                Text("Se generan 2 por semana", fontSize = 12.sp)
            }
            Text("$count", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CouponGold)
        }
    }
}

private fun formatMinutes(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
